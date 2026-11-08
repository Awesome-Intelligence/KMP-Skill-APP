"""Skill 工具静态自检 —— 只报警告，不挡构建。

检查 app 层（apps/、app/、任何业务模块）里的 Koog skill 工具是否符合「逻辑 api 化」规范
（规范原文见 rules/CLAUDE.template.md 的「逻辑 api 化」一节）：

1. tool 类里不许有 Compose 代码            —— tool 跑在 agent loop 里，没有 UI 上下文
2. doExecute 里不许出现 if/when/for/while/try —— 分支属于业务，该下沉到 data 层
3. tool 名必须英文 snake_case             —— 模型靠 name 挑工具

用法：python scripts/verify-skills.py
退出码恒为 0（只警告）—— 硬失败留给编译期和 review。
"""
import os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# 这些目录不扫：agent 运行时自己（它的 SkillTool 基类必然带分支）、
# 构建产物、以及 kit 本身（kit 是通用层，不写业务 tool）
SKIP_DIRS = {".git", ".gradle", "build", "node_modules", ".idea", "kit", "kit-agent-koog"}

KEYWORDS = re.compile(r"\b(if|when|for|while|try)\b")
# 类声明里构造函数参数可能带类型注解（`class X(private val s: S) : SkillTool<A>`），
# 所以中间那段不能排除 ':' —— 用懒惰匹配找到 `): SkillTool<` 那一处
TOOL_CLASS = re.compile(r"class\s+[A-Za-z_]\w*.*?:\s*SkillTool\s*<")
NAME_ARG = re.compile(r"typeToken\s*<\s*[^<>]*\s*>\s*\(\s*\)\s*,\s*\"([^\"]+)\"")
DO_EXEC = re.compile(r"override\s+suspend\s+fun\s+doExecute\s*\(")
COMPOSE_IN_CLASS = re.compile(r"@Composable|androidx\.compose")
# 表达式体的续行字符：这些开头说明还没结束
CONT_CHARS = set(".?:,+&|>*")
CONT_WORDS = {"else", "catch", "finally"}


def r(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def strip_noise(src):
    """去掉注释和字符串字面量 —— 'if you want' 这种文本不该被当成关键字。"""
    src = re.sub(r"/\*(?:.|\n)*?\*/", "", src)
    src = re.sub(r"\"(?:\\.|[^\"\\\n])*\"", '""', src)
    src = re.sub(r"'(?:\\.|[^'\\\n])*'", "''", src)
    src = re.sub(r"//[^\n]*", "", src)
    return src


def line_of(src, idx):
    return src.count("\n", 0, idx) + 1


def expr_body(src, i):
    """从 i（`=` 之后）开始取表达式体的文本，直到语句真的结束。"""
    depth = 0
    j = i
    n = len(src)
    prev = ""
    while j < n:
        c = src[j]
        if c in "([{":
            depth += 1
        elif c in ")]}":
            if depth == 0:
                break
            depth -= 1
        elif c == "\n" and depth == 0:
            k = j + 1
            while k < n and src[k] in " \t":
                k += 1
            word = ""
            while k < n and (src[k].isalnum() or src[k] == "_"):
                word += src[k]
                k += 1
            if prev in CONT_CHARS or word in CONT_WORDS or (k < n and src[k] in CONT_CHARS):
                j = k
                prev = ""
                continue
            break
        if not c.isspace():
            prev = c
        j += 1
    return src[i:j]


def block_body(src, i):
    """从 i（`{`）开始取配对大括号里的文本。"""
    depth = 0
    j = i
    n = len(src)
    while j < n:
        if src[j] == "{":
            depth += 1
        elif src[j] == "}":
            depth -= 1
            if depth == 0:
                return src[i + 1:j]
        j += 1
    return src[i + 1:]


def do_execute_bodies(src):
    """返回 [(body, line)] —— 文件里每个 doExecute 的实现体。拿不准就跳过，宁漏不误报。"""
    out = []
    for m in DO_EXEC.finditer(src):
        i = m.end() - 1  # 指向 '('
        depth = 0
        while i < len(src):
            if src[i] == "(":
                depth += 1
            elif src[i] == ")":
                depth -= 1
                if depth == 0:
                    i += 1
                    break
            i += 1
        # 跳过返回类型（`: String`）
        m2 = re.match(r"\s*:\s*[\w.<>?\[\]\s]+", src[i:])
        if m2:
            i += m2.end()
        rest = src[i:]
        m3 = re.match(r"\s*=", rest)
        if m3:
            start = i + m3.end()
            out.append((expr_body(src, start), line_of(src, start)))
            continue
        j = rest.find("{")
        if j < 0:
            continue
        start = i + j
        out.append((block_body(src, start), line_of(src, start)))
    return out


def tool_class_bodies(src):
    """返回 [(class_body, line)] —— 文件里每个 tool 类的类体。"""
    out = []
    for m in TOOL_CLASS.finditer(src):
        j = src.find("{", m.end())
        if j < 0:
            continue
        out.append((block_body(src, j), line_of(src, j)))
    return out


def check_text(raw):
    """检查一份源码（self-test 用）。返回 warning 列表。"""
    src = strip_noise(raw)
    out = []
    for body, _ in tool_class_bodies(src):
        if COMPOSE_IN_CLASS.search(body):
            out.append("compose")
    for body, _ in do_execute_bodies(src):
        hit = KEYWORDS.search(body)
        if hit:
            out.append("branch:" + hit.group(1))
    for nm in NAME_ARG.findall(raw):
        if not re.match(r"^[a-z][a-z0-9_]*$", nm):
            out.append("name:" + nm)
    return out


def check_file(path, rel):
    """返回 (warnings, tool_count)"""
    raw = r(path)
    if not TOOL_CLASS.search(raw):
        return [], 0
    warnings = []
    src = strip_noise(raw)

    # C1：tool 类里不许有 Compose 代码
    for body, ln in tool_class_bodies(src):
        hit = COMPOSE_IN_CLASS.search(body)
        if hit:
            warnings.append("%s:%d: tool 类里有 Compose 代码（%s）—— tool 跑在 agent loop 里，没有 UI 上下文"
                            % (rel, ln, hit.group(0)))

    # C2：doExecute 里不许有业务分支
    for body, ln in do_execute_bodies(src):
        hit = KEYWORDS.search(body)
        if hit:
            warnings.append("%s:%d: doExecute 里有 `%s` —— 分支属于业务，该下沉到 data 层"
                            % (rel, ln, hit.group(1)))

    # C3：tool 名必须 snake_case（名字在字符串里，用原文找）
    for nm in NAME_ARG.findall(raw):
        if not re.match(r"^[a-z][a-z0-9_]*$", nm):
            warnings.append("%s: 工具名 %r 不是英文 snake_case —— 模型靠 name 挑工具" % (rel, nm))

    return warnings, len(tool_class_bodies(src)) or 1


def self_test():
    """解析器自己也会错 —— 拿几段样例先自检一遍。"""
    good = '''
class A(private val s: Store) : SkillTool<Args>(
    typeToken<Args>(), "add_todo", "新增",
) {
    override suspend fun doExecute(args: Args): String = s.add(args.title)
}
'''
    bad_branch = '''
class B(private val s: Store) : SkillTool<Args>(
    typeToken<Args>(), "add_todo", "新增",
) {
    override suspend fun doExecute(args: Args): String {
        if (args.title.isEmpty()) return "空的"
        return s.add(args.title)
    }
}
'''
    bad_name = '''
class C : SkillTool<Args>(typeToken<Args>(), "AddTodo", "新增") {
    override suspend fun doExecute(args: Args): String = "x"
}
'''
    bad_compose = '''
class D : SkillTool<Args>(typeToken<Args>(), "d", "x") {
    @Composable
    override suspend fun doExecute(args: Args): String = "x"
}
'''
    two_tools_one_bad = good + '''
class E : SkillTool<Args>(typeToken<Args>(), "e", "x") {
    override suspend fun doExecute(args: Args): String {
        when (args) { else -> "y" }
    }
}
'''
    cases = [
        (good, []),
        (bad_branch, ["branch:if"]),
        (bad_name, ["name:AddTodo"]),
        (bad_compose, ["compose"]),
        (two_tools_one_bad, ["branch:when"]),
    ]
    for src, want in cases:
        got = check_text(src)
        assert got == want, "自检失败：期望 %s，实际 %s\n%s" % (want, got, src)
    return True


def main():
    self_test()
    warnings = []
    tools = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for fn in filenames:
            if not fn.endswith(".kt"):
                continue
            p = os.path.join(dirpath, fn)
            w, n = check_file(p, os.path.relpath(p, ROOT).replace(os.sep, "/"))
            warnings += w
            tools += n
    if warnings:
        print("⚠ %d 处提醒（不挡构建）：" % len(warnings))
        for w in warnings:
            print("  -", w)
        print("\n规范见 rules/CLAUDE.template.md「逻辑 api 化」一节。")
    else:
        print("✓ skill 工具自检通过：%d 个 tool，分支都在 data 层" % tools)


if __name__ == "__main__":
    main()
    sys.exit(0)
