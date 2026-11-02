"""Kit 静态自检 —— 不编译也能发现三类断裂：

1. `import com.androidkit.app.*` 指向的符号在 kit 里不存在（复制漏了 / 引用了领域代码）
2. `Res.string.x` 在 kit 的 strings.xml 里没有对应 key
3. `DemoData.A.B` 在 kit 的 DemoData 里没有对应成员

用法：python scripts/verify-kit.py
"""
import os, re, sys

KIT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# KMP 结构：源码分布在 commonMain / androidMain / desktopMain / iosMain
SRC_SETS = ["commonMain", "androidMain", "desktopMain", "iosMain"]
RES = os.path.join(KIT, "kit/src/commonMain/composeResources/values")

def detect_base_pkg():
    """从目录结构反推根包名（装进新项目后包名被换过，不能写死 com.androidkit.app）。"""
    for ss in SRC_SETS:
        kotlin_root = os.path.join(KIT, "kit/src", ss, "kotlin")
        if not os.path.isdir(kotlin_root):
            continue
        for dirpath, dirnames, filenames in os.walk(kotlin_root):
            for fn in filenames:
                if fn.endswith(".kt"):
                    rel = os.path.relpath(dirpath, kotlin_root).replace(os.sep, ".")
                    return rel
    return "com.androidkit.app"

BASE_PKG = detect_base_pkg()
CODE = os.path.join(KIT, "kit/src/commonMain/kotlin", BASE_PKG.replace(".", os.sep))

errors = []

def r(p):
    with open(p, encoding="utf-8") as f: return f.read()

kt = []
for ss in SRC_SETS:
    base = os.path.join(KIT, "kit/src", ss, "kotlin")
    for root, _, files in os.walk(base):
        for fn in files:
            if fn.endswith(".kt"):
                kt.append(os.path.join(root, fn))

# ── 1. package 内符号：把每个 kt 文件里声明的顶层符号收集起来
decl = re.compile(r"^(?:@\w+(?:\([^)]*\))?\s*)*"
                  r"(?:public |internal |private |abstract |open |sealed |data |value |enum |annotation )*"
                  r"(?:class|interface|object|fun|val|var|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)", re.M)
ext = re.compile(r"^(?:public |internal |private )?(?:val|var|fun)\s+[A-Za-z_][\w.<>]*\.([A-Za-z_]\w*)", re.M)
by_pkg = {}
for p in kt:
    src = r(p)
    m = re.search(r"^package\s+([\w.]+)", src, re.M)
    pkg = m.group(1) if m else ""
    names = set(decl.findall(src))
    names |= set(ext.findall(src))
    by_pkg.setdefault(pkg, set()).update(names)

# 允许由新项目自己提供的包 / 由构建系统生成的类
ALLOWED_MISSING = {
    BASE_PKG + ".data.repository",   # Repository 层：kit 不提供，各项目自己写
}
# compose-resources 生成的访问器包（commonMain 里用 `import <pkg>.res.*`）
ALLOWED_EXACT = {BASE_PKG + ".R", BASE_PKG + ".BuildConfig", BASE_PKG + ".R.string",
                 BASE_PKG + ".res"}

for p in kt:
    src = r(p)
    for imp in re.findall(r"^import\s+(%s\.[\w.]+)" % re.escape(BASE_PKG), src, re.M):
        if imp.endswith(".") or imp in ALLOWED_EXACT or imp.startswith(BASE_PKG + ".res."):
            continue
        imp_pkg = imp.rsplit(".", 1)[0]
        imp_name = imp.rsplit(".", 1)[1]
        if imp_pkg in ALLOWED_MISSING:
            continue
        # 顶层符号？
        if imp_name in by_pkg.get(imp_pkg, set()):
            continue
        # 嵌套成员？父类在 kit 里就行
        parent = imp_pkg.rsplit(".", 1)[0]
        if imp_name in by_pkg.get(parent, set()):
            continue
        errors.append("%s: import %s 在 kit 里找不到" % (os.path.relpath(p, KIT), imp))

# ── 2. Res.string（compose-resources）
zh = r(os.path.join(RES, "strings.xml"))
have = set(re.findall(r"<string\s+name=\"([^\"]+)\"", zh))
for p in kt:
    src = r(p)
    # `Res.string.xxx`（`import com.androidkit.app.res.*`）
    used = set(re.findall(r"\bRes\.string\.([A-Za-z0-9_]+)", src))
    # `import ...Res.string as RString` —— 别名引用
    for a in re.findall(r"^import\s+[\w.]*\.Res\.string\s+as\s+([A-Za-z0-9_]+)", src, re.M):
        used |= set(re.findall(r"\b%s\.([A-Za-z0-9_]+)" % re.escape(a), src))
    for k in used:
        if k not in have:
            errors.append("%s: Res.string.%s 不在 strings.xml" % (os.path.relpath(p, KIT), k))

# ── 3. DemoData 成员
demo = r(os.path.join(CODE, "ui/preview/DemoData.kt"))
demo_members = set(re.findall(r"^\s*(?:const )?val\s+([A-Za-z0-9_]+)", demo, re.M))
demo_objs = set(re.findall(r"^\s*object\s+([A-Za-z0-9_]+)", demo, re.M))
for p in kt:
    if p.endswith("DemoData.kt"): continue
    for m in re.finditer(r"DemoData\.([A-Za-z0-9_]+)\.([A-Za-z0-9_]+)", r(p)):
        o, mem = m.group(1), m.group(2)
        if o not in demo_objs:
            errors.append("%s: DemoData.%s 不存在" % (os.path.relpath(p, KIT), o))
        elif mem not in demo_members and mem not in demo_objs:
            errors.append("%s: DemoData.%s.%s 不存在" % (os.path.relpath(p, KIT), o, mem))

if errors:
    print("✗ %d 个问题：" % len(errors))
    for e in sorted(set(errors)): print("  -", e)
    sys.exit(1)
print("✓ kit 静态自检通过：%d 个 kt 文件，%d 个 string key"
      % (len(kt), len(have)))

