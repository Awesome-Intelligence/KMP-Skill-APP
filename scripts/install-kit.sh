#!/usr/bin/env bash
# 把 kit 装进一个新项目。
#
#   bash scripts/install-kit.sh <目标项目目录> <新包名> [应用名]
#
# 例：bash scripts/install-kit.sh ~/code/MyApp com.myname.myapp MyApp
#
# 做的事：
#   1. 复制 KMP 模块（:kit 组件库 + :kit-agent-koog agent 运行时）到目标项目
#   2. 把代码里的 com.androidkit.app 全量替换成新包名
#   3. strings.xml 合并（只补缺失的 key，不覆盖你已有的）
#   4. rules/CLAUDE.template.md → 目标 CLAUDE.md
#   5. ci/*.yml → .github/workflows/
#   6. scripts/diff-ownership.sh → .claude/scripts/
#   7. skills/ → .claude/skills/ 与 .agents/skills/
#
# 不做的事：不动你的 build.gradle.kts（只提示改哪几行）、不编译。

set -euo pipefail

KIT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="${1:-}"
NEW_PKG="${2:-}"
APP_NAME="${3:-App}"

if [[ -z "$TARGET" || -z "$NEW_PKG" ]]; then
  echo "用法: bash scripts/install-kit.sh <目标项目目录> <新包名> [应用名]" >&2
  exit 1
fi
if [[ ! -d "$TARGET" ]]; then
  echo "目标目录不存在: $TARGET" >&2
  exit 1
fi

OLD_PKG="com.androidkit.app"
PKG_PATH="${NEW_PKG//.//}"
OLD_PKG_PATH="${OLD_PKG//.//}"

echo "==> kit:    $KIT"
echo "==> target: $TARGET"
echo "==> package: $OLD_PKG  ->  $NEW_PKG"
echo

mkdir -p "$TARGET/kit/src"
mkdir -p "$TARGET/.github/workflows"
mkdir -p "$TARGET/scripts"
mkdir -p "$TARGET/.claude/scripts"
mkdir -p "$TARGET/.claude/skills"
mkdir -p "$TARGET/.agents/skills"

# ── 1. KMP 源集 ───────────────────────────────────────────────
echo "==> 复制 KMP 源集"
# 模块 → 要拷的源集（:kit 没有 commonTest；:kit-agent-koog 有）
copy_module() {
  local mod="$1"; shift
  mkdir -p "$TARGET/$mod/src"
  cp "$KIT/$mod/build.gradle.kts" "$TARGET/$mod/build.gradle.kts"
  for ss in "$@"; do
    [[ -d "$KIT/$mod/src/$ss" ]] || continue
    mkdir -p "$TARGET/$mod/src/$ss"
    cp -r "$KIT/$mod/src/$ss/." "$TARGET/$mod/src/$ss/"
  done
}
copy_module kit commonMain androidMain desktopMain iosMain
copy_module kit-agent-koog commonMain commonTest androidMain desktopMain iosMain
# 根构建配置参考（插件版本声明 —— 合并进你自己的 build.gradle.kts）
cp "$KIT/build.gradle.kts" "$TARGET/kit-root-plugins.gradle.kts"

# ── 2. 包名替换 ────────────────────────────────────────────────
echo "==> 替换包名"
find "$TARGET/kit/src" "$TARGET/kit-agent-koog/src" -name '*.kt' -print0 | \
  xargs -0 sed -i "s/${OLD_PKG//./\\.}/${NEW_PKG//./\\.}/g"
# compose-resources 的包名在 build.gradle.kts 里
sed -i "s/com\.androidkit\.app\.res/${NEW_PKG}.res/g" "$TARGET/kit/build.gradle.kts"
sed -i "s/${OLD_PKG//./\\.}/${NEW_PKG//./\\.}/g" "$TARGET/kit/build.gradle.kts"
sed -i "s/${OLD_PKG//./\\.}/${NEW_PKG//./\\.}/g" "$TARGET/kit-agent-koog/build.gradle.kts"
# 目录也要重排（com/androidkit/app → com/test/app），否则包名和路径对不上
for mod in kit kit-agent-koog; do
  for ss in commonMain commonTest androidMain desktopMain iosMain; do
    src="$TARGET/$mod/src/$ss/kotlin/${OLD_PKG_PATH//.//}"
    [[ -d "$src" ]] || continue
    dst="$TARGET/$mod/src/$ss/kotlin/${PKG_PATH//.//}"
    mkdir -p "$dst"
    cp -r "$src/." "$dst/"
    rm -rf "$src"
    # 清掉旧包名留下的空目录（com/androidkit/… → com/test/…）
    find "$TARGET/$mod/src/$ss/kotlin" -depth -type d -empty -delete
  done
done

# ── 3. strings.xml 合并 ────────────────────────────────────────
if command -v python >/dev/null 2>&1; then
  echo "==> 合并 strings.xml"
  for loc in "" "-en"; do
    python "$KIT/scripts/merge_strings.py" \
      "$KIT/kit/src/commonMain/composeResources/values$loc/strings.xml" \
      "$TARGET/kit/src/commonMain/composeResources/values$loc/strings.xml" || true
  done
fi

# ── 4. 规则 / CI / 脚本 / skills ───────────────────────────────
echo "==> 复制规则、CI、脚本、skills"
[[ -f "$TARGET/CLAUDE.md" ]] || cp "$KIT/rules/CLAUDE.template.md" "$TARGET/CLAUDE.md"
cp "$KIT/ci/ci.yml" "$TARGET/.github/workflows/ci.yml"
cp "$KIT/ci/release.yml" "$TARGET/.github/workflows/release.yml"
cp "$KIT/scripts/diff-ownership.sh" "$TARGET/.claude/scripts/diff-ownership.sh"
cp "$KIT/scripts/verify-kit.py" "$KIT/scripts/merge_strings.py" "$TARGET/scripts/"
chmod +x "$TARGET/.claude/scripts/diff-ownership.sh"
cp -r "$KIT/skills/." "$TARGET/.claude/skills/" 2>/dev/null || true
cp -r "$KIT/skills/android-skills/." "$TARGET/.agents/skills/" 2>/dev/null || true

echo
echo "✅ 装好了。还差这几步（脚本不替你做决定）："
echo "  1. settings.gradle.kts: include(\":kit\")，要 agent 再 include(\":kit-agent-koog\")，并把 kit-root-plugins.gradle.kts 的插件块合进根 build.gradle.kts"
echo "  2. kit/build.gradle.kts: compose.resources 的 packageOfResClass 改成 $NEW_PKG.res（脚本已代改）"
echo "  3. 你的 :app 模块: implementation(project(\":kit\"))；要 agent 再加 implementation(project(\":kit-agent-koog\"))"
echo "  4. 你的首页: kit 里的 App() 是**演示页**（显示「Android-Kit」），换成你自己的根 Composable"
echo "  5. app/build.gradle.kts: namespace / applicationId 改成 $NEW_PKG"
echo "  6. app/build.gradle.kts: release 签名换成真 keystore（见 kit 里 app/build.gradle.kts 的注释）"
echo "  7. settings.gradle.kts: rootProject.name = \"$APP_NAME\""
echo "  8. res/values/strings.xml: app_name 改成你的应用名"
echo "  9. 跑一次 python scripts/verify-kit.py 确认没漏"
echo " 10. 编译：./gradlew :app:assembleDebug（Android）/ :kit:compileKotlinDesktop（桌面）"
