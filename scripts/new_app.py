#!/usr/bin/env python3
"""从 scripts/templates/app 生成一个新 app 到 apps/<name>/。

用法：
    python scripts/new_app.py myapp
    python scripts/new_app.py myapp --package com.example.myapp --app-name "我的应用"

生成的是**最小可编译壳**：AndroidManifest + MainActivity + 主题 + strings +
build.gradle.kts（用 build-logic 的 androidkit.android.app 约定）。

它不碰底座文件，也不会改 settings.gradle.kts —— apps/ 下的模块由
settings.gradle.kts 自动 include，CI 自动构建。
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TEMPLATE_DIR = Path(__file__).resolve().parent / "templates" / "app"
APPS_DIR = ROOT / "apps"

NAME_RE = re.compile(r"^[a-z][a-z0-9_]*$")
PACKAGE_RE = re.compile(r"^[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)+$")
LEFTOVER_RE = re.compile(r"__[A-Z][A-Z0-9_]*__")


def escape_xml(value: str) -> str:
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def escape_kotlin(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")


def fail(message: str) -> None:
    print(f"✗ {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    parser = argparse.ArgumentParser(description="生成一个新的 Android app 壳")
    parser.add_argument("name", help="模块名（apps/ 下的目录名，小写字母/数字/下划线）")
    parser.add_argument("--package", help="包名，默认 com.androidkit.<name>")
    parser.add_argument("--app-name", help="桌面显示名，默认跟模块名一样")
    args = parser.parse_args()

    name = args.name
    if not NAME_RE.match(name):
        fail(f"模块名不合法：{name}（要 ^[a-z][a-z0-9_]*$）")
    if name == "demo":
        fail("demo 是示例模块，换个名字")
    if not TEMPLATE_DIR.is_dir():
        fail(f"模板目录不存在：{TEMPLATE_DIR}")
    if not APPS_DIR.is_dir():
        fail(f"apps/ 目录不存在：{APPS_DIR}（请在仓库根目录执行）")

    target = APPS_DIR / name
    if target.exists():
        fail(f"{target.relative_to(ROOT)} 已存在，换个名字或先删掉")

    package = args.package or f"com.androidkit.{name}"
    if not PACKAGE_RE.match(package):
        fail(f"包名不合法：{package}")

    app_name = (args.app_name or name).strip()
    if not app_name:
        fail("--app-name 不能为空")

    mapping = {
        "__APP_NAME_XML__": escape_xml(app_name),
        "__APP_NAME__": escape_kotlin(app_name),
        "__APP_DIR__": name,
        "__PACKAGE_PATH__": package.replace(".", "/"),
        "__PACKAGE__": package,
    }

    written: list[Path] = []
    for template in sorted(TEMPLATE_DIR.rglob("*.tmpl")):
        relative = str(template.relative_to(TEMPLATE_DIR))[: -len(".tmpl")]
        for key, value in mapping.items():
            relative = relative.replace(key, value)

        output = target / relative
        output.parent.mkdir(parents=True, exist_ok=True)

        text = template.read_text(encoding="utf-8")
        for key, value in mapping.items():
            text = text.replace(key, value)

        leftovers = LEFTOVER_RE.findall(text)
        if leftovers:
            fail(f"{template.name} 里有没替换的占位符：{sorted(set(leftovers))}")

        output.write_text(text, encoding="utf-8")
        written.append(output.relative_to(ROOT))

    print(f"✓ 已生成 apps/{name}/（package={package}，app_name={app_name}）")
    for path in written:
        print(f"  - {path}")
    print()
    print("下一步：")
    print(f"  ./gradlew :apps:{name}:testDebugUnitTest   # 先看模板自带的壳自检过不过")
    print(f"  ./gradlew :apps:{name}:assembleDebug")
    print("  用 Android Studio 打开仓库根目录，模块会自动出现在 apps/ 下。")
    print("  要接本地 agent：把 build.gradle.kts 里 kit-agent-koog 那两行注释解开。")
    print("  加自己的门禁：往 scripts/checks/ 放个 .sh，CI 自动跑（不用改 workflow）。")


if __name__ == "__main__":
    main()
