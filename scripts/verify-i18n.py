"""多语言文案对齐自检 —— values 和 values-en 的 key 集合必须一致。

为什么要有这道门禁：
    compose-resources 的 values-en 缺一个 key，编译不报错、运行不崩，
    英文界面那一格直接显示 key 名（"shortcut_new_todo_short"）。
    这种洞靠肉眼 review 抓不住 —— 加了 key 忘了补英文、或者删了中文忘了删英文，都是常事。

检查几类资源：
    1. kit/src/commonMain/composeResources/values{,-en}/strings.xml
    2. kit-membership/src/commonMain/composeResources/values{,-en}/strings.xml
    3. apps/*/src/main/res/values{,-en}/strings.xml（Android 壳的文案）

用法：python scripts/verify-i18n.py
退出码 0 = 对齐；1 = 有缺失（缺 key 的一侧报出来）。
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 一个文件里可能有多个 <resources> 块，也可能有 comments
STRING_RE = re.compile(r'<string\s+name="([^"]+)"')


def strings_of(path):
    """返回 {key: 值}；文件不存在返回 None（表示这一侧没有这个语言）。"""
    if not os.path.isfile(path):
        return None
    with open(path, encoding="utf-8") as f:
        text = f.read()
    out = {}
    for m in STRING_RE.finditer(text):
        out.setdefault(m.group(1), "")  # 重名保留第一次，重复名另说
    return out


def pairs():
    """列出要对照的 (默认语言文件, 英文文件) 对。"""
    result = []
    kit = os.path.join(ROOT, "kit/src/commonMain/composeResources")
    result.append((os.path.join(kit, "values/strings.xml"),
                   os.path.join(kit, "values-en/strings.xml")))

    # 各可选模块（kit-agent-koog 没文案，kit-membership 有）
    for mod in ("kit-membership",):
        res = os.path.join(ROOT, mod, "src/commonMain/composeResources")
        result.append((os.path.join(res, "values/strings.xml"),
                       os.path.join(res, "values-en/strings.xml")))

    apps_dir = os.path.join(ROOT, "apps")
    if os.path.isdir(apps_dir):
        for name in sorted(os.listdir(apps_dir)):
            res = os.path.join(apps_dir, name, "src/main/res")
            if not os.path.isdir(res):
                continue
            result.append((os.path.join(res, "values/strings.xml"),
                           os.path.join(res, "values-en/strings.xml")))
    return result


def main():
    errors = []
    checked = 0

    for default_path, en_path in pairs():
        default = strings_of(default_path)
        if default is None:
            continue  # 这一对里连默认语言都没有 —— 不是这套资源的范围

        rel = os.path.relpath(default_path, ROOT).replace(os.sep, "/")
        en = strings_of(en_path)

        if en is None:
            errors.append(f"{rel}: 有 {len(default)} 个 key，但没有对应的 values-en/strings.xml")
            continue

        missing_en = sorted(set(default) - set(en))
        extra_en = sorted(set(en) - set(default))

        for k in missing_en:
            errors.append(f"{rel}: key \"{k}\" 缺英文翻译（values-en/strings.xml）")
        for k in extra_en:
            errors.append(f"{rel}: values-en 里的 key \"{k}\" 在 values 里不存在")

        checked += len(default)

    if errors:
        print(f"✗ {len(errors)} 个问题：")
        for e in errors:
            print(f"  - {e}")
        sys.exit(1)

    print(f"✓ 多语言文案对齐：{checked} 个 key，values 和 values-en 一致")


if __name__ == "__main__":
    main()
