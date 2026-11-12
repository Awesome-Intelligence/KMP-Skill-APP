#!/usr/bin/env bash
# 门禁执行层 —— CI 的每个 job 和本地都调这一份脚本。
#
# 为什么门禁逻辑不写在 .github/workflows/ 里：
#   workflows/ 是「底座不可改路径」——fork 想加一道自己的门禁，改它就等于跟上游冲突。
#   逻辑放 scripts/（fork 可以自由改），YAML 只负责「在什么环境跑哪个子命令」。
#
# 用法：
#   bash scripts/ci.sh detekt        # 静态规则（命名 / 复杂度 / 吞异常…）
#   bash scripts/ci.sh api           # 公开 API 二进制兼容
#   bash scripts/ci.sh verify        # 自带脚本：import / Res.string / i18n / skill 规范
#   bash scripts/ci.sh test          # 单元测试
#   bash scripts/ci.sh lint          # Android lint
#   bash scripts/ci.sh build-android # 打包所有 Android app
#   bash scripts/ci.sh build-desktop # 编桌面目标
#   bash scripts/ci.sh build-ios     # 编 iOS framework（只能在 macOS host 上跑）
#   bash scripts/ci.sh package-deb   # 打 .deb（只能在 linux 上跑）
#   bash scripts/ci.sh package-dmg   # 打 .dmg（只能在 macOS 上跑）
#   bash scripts/ci.sh backend       # agent 网关：类型检查 + 契约冒烟（不花钱、不连网）
#   bash scripts/ci.sh base-guard   # 底座保护：改动碰到底座路径就失败（fork 必跑）
#   bash scripts/ci.sh all           # 除 build/* / lint / backend 外全跑（本地提交前跑这个）
#                                    # backend 没放进来：它要先 npm ci 装依赖，慢且可能需要网
#
# 加一道新门禁的落点：
#   1. 逻辑写进本文件的一个新子命令
#   2. .github/workflows/ci.yml 里加一个 job 调它（改底座文件前先想清楚，见 CONTRIBUTING）
#   3. 在 CONTRIBUTING.md「质量门禁」一节登记
set -o pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 1

# CI 上禁用 daemon（每个 job 独立进程，快一点）；本地留 daemon（复用编译缓存）
GRADLE_FLAGS=""
[ "${CI:-}" = "true" ] && GRADLE_FLAGS="--no-daemon"

FAILED=()
ok()   { printf '  \033[32m✓\033[0m %s\n' "$1"; }
bad()  { printf '  \033[31m✗\033[0m %s\n' "$1"; FAILED+=("$1"); }
step() { printf '\n\033[1m==> %s\033[0m\n' "$1"; }

# ── 环境探测 ────────────────────────────────────────────────────
# 模块清单从 settings.gradle.kts 里读 —— fork 加了 apps/<name>/ 会自动被认出来，
# 拷贝模式装出来的项目没有 apps/ 目录也不会炸。
have_module() {
  grep -qE "include\(\":$1\"\)" settings.gradle.kts 2>/dev/null
}
app_modules() {
  for d in apps/*/; do
    [ -f "$d/build.gradle.kts" ] || continue
    echo ":apps:$(basename "$d")"
  done
  # 拷贝模式（install-kit.sh）装出来的项目 app 模块就叫 :app，不在 apps/ 下
  have_module "app" && [ -f app/build.gradle.kts ] && echo ":app"
  return 0
}
# 模块的测试任务名：
#   Android app → testDebugUnitTest
#   KMP 且声明了 jvm("desktop") → desktopTest（commonTest 只在 JVM 目标跑得起来）
#   纯 JVM 模块 → test
# 认不出来就返回空 —— 调用方跳过这个模块，别硬跑出一个 "task not found"。
test_task_of() {
  local dir f
  dir="$(module_dir "$1")"
  f="$dir/build.gradle.kts"
  [ -f "$f" ] || return 0
  case "$1" in
    *:apps:*) echo testDebugUnitTest; return ;;
  esac
  # Android app 模块（拷贝模式的 :app）—— 也走 testDebugUnitTest
  if grep -q 'com.android.application' "$f"; then echo testDebugUnitTest; return; fi
  if grep -q 'org.jetbrains.kotlin.multiplatform' "$f"; then
    grep -q 'jvm("desktop")' "$f" && echo desktopTest
    return
  fi
  if grep -q 'org.jetbrains.kotlin.jvm' "$f"; then echo test; fi
}
module_dir() {
  case "$1" in
    *:apps:*) echo "apps/${1#:apps:}" ;;
    *) echo "${1#:}" ;;
  esac
}
# 该模块声明了 detekt 插件吗？（没声明就没有 detekt 任务，硬跑会 "task not found"）
has_detekt() {
  grep -q 'detekt' "$(module_dir "$1")/build.gradle.kts" 2>/dev/null
}
# 该模块声明了 binary-compatibility-validator 吗？
has_api_plugin() {
  grep -q 'binary-compatibility-validator' "$(module_dir "$1")/build.gradle.kts" 2>/dev/null
}
python_bin() {
  for p in python3 python; do
    command -v "$p" >/dev/null 2>&1 && { echo "$p"; return 0; }
  done
  echo ""
}

# ── 各道门禁 ────────────────────────────────────────────────────
cmd_detekt() {
  step "detekt（静态规则）"
  local modules=()
  for m in :kit :kit-agent-koog :kit-membership :chat-demo; do
    have_module "${m#:}" && modules+=("$m")
  done
  while read -r m; do [ -n "$m" ] && modules+=("$m"); done < <(app_modules)

  local targets=()
  for m in "${modules[@]:-}"; do
    [ -n "$m" ] || continue
    if has_detekt "$m"; then targets+=("${m}:detekt"); else ok "${m}（没声明 detekt 插件，跳过）"; fi
  done
  [ ${#targets[@]} -eq 0 ] && { bad "没有可跑的 detekt 目标"; return; }

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "detekt"; else bad "detekt"; fi
}

cmd_api() {
  step "apiCheck（公开 API 二进制兼容）"
  local targets=()
  for m in :kit :kit-agent-koog :kit-membership; do
    have_module "${m#:}" || continue
    has_api_plugin "$m" || { ok "${m}（没声明 api 插件，跳过）"; continue; }
    # 基线不存在 = 还没 dump 过 —— 明确报错让人去跑 apiDump，
    # 比让它红着 "no dump file" 强
    local baseline
    baseline="$(ls "$(module_dir "$m")"/api/*/*.api 2>/dev/null | head -1)"
    if [ -z "$baseline" ]; then
      bad "${m}: 没有 api 基线 —— 先跑 ./gradlew ${m}:apiDump 生成并提交"
      continue
    fi
    targets+=("${m}:apiCheck")
  done
  [ ${#targets[@]} -eq 0 ] && { ok "没有需要校验的模块"; return; }

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "apiCheck"; else bad "apiCheck"; fi
}

cmd_verify() {
  step "verify（自带静态脚本）"
  local py
  py="$(python_bin)"
  if [ -z "$py" ]; then
    bad "找不到 python —— verify 脚本跑不了"
    return
  fi
  local ran=0
  for s in verify-kit.py verify-skills.py verify-i18n.py; do
    [ -f "scripts/$s" ] || continue
    ran=1
    if "$py" "scripts/$s"; then ok "$s"; else bad "$s"; fi
  done
  [ $ran -eq 0 ] && ok "没有 verify 脚本（拷贝模式的项目可能没装全）"

  # fork 自定义检查：scripts/checks/*.sh 全部自动跑。
  # 这是「加门禁」的标准落点 —— 放个脚本进去就行，不用改 ci.sh，也不用改 workflow。
  if [ -d scripts/checks ]; then
    for s in scripts/checks/*.sh; do
      [ -f "$s" ] || continue
      ran=1
      if bash "$s"; then ok "checks/$(basename "$s")"; else bad "checks/$(basename "$s")"; fi
    done
  fi
}

cmd_test() {
  step "单元测试"
  local modules=()
  for m in :kit :kit-agent-koog :kit-membership :chat-demo; do
    have_module "${m#:}" && modules+=("$m")
  done
  while read -r m; do [ -n "$m" ] && modules+=("$m"); done < <(app_modules)

  local targets=()
  for m in "${modules[@]:-}"; do
    [ -n "$m" ] || continue
    local t
    t="$(test_task_of "$m")"
    if [ -z "$t" ]; then
      ok "${m}（没有 JVM 测试目标，跳过）"
    else
      targets+=("${m}:${t}")
    fi
  done
  [ ${#targets[@]} -eq 0 ] && { ok "没有测试模块"; return; }

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "单元测试"; else bad "单元测试"; fi
}

cmd_lint() {
  step "Android lint"
  local targets=()
  while read -r m; do [ -n "$m" ] && targets+=("${m}:lintDebug"); done < <(app_modules)
  if [ ${#targets[@]} -eq 0 ]; then ok "没有 Android app（拷贝模式的项目 lint 在别的任务里）"; return; fi

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "lint"; else bad "lint"; fi
}

cmd_build_android() {
  step "构建 Android app"
  local modules=()
  while read -r m; do [ -n "$m" ] && modules+=("$m"); done < <(app_modules)
  if [ ${#modules[@]} -eq 0 ]; then ok "没有 Android app"; return; fi

  local targets=()
  for m in "${modules[@]}"; do targets+=("${m}:assembleDebug"); done
  # Koog 引擎是独立模块，app 默认不引它，所以这里单独编一遍
  have_module "kit-agent-koog" && targets+=(":kit-agent-koog:compileAndroidMain")
  have_module "kit-membership" && targets+=(":kit-membership:compileAndroidMain")

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "Android 构建"; else bad "Android 构建"; fi
}

cmd_build_desktop() {
  step "构建桌面目标"
  local targets=()
  for m in :kit :kit-agent-koog :kit-membership; do
    have_module "${m#:}" && targets+=("${m}:compileKotlinDesktop")
  done
  [ ${#targets[@]} -eq 0 ] && { ok "没有桌面模块"; return; }

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "桌面构建"; else bad "桌面构建"; fi
}

cmd_build_ios() {
  step "构建 iOS framework"
  have_module "kit" || { ok "没有 :kit，跳过"; return; }

  local targets=(":kit:linkDebugFrameworkIosArm64" ":kit:linkDebugFrameworkIosSimulatorArm64")
  have_module "kit-agent-koog" && targets+=(":kit-agent-koog:compileKotlinIosArm64")
  have_module "kit-membership" && targets+=(":kit-membership:compileKotlinIosArm64")

  if ./gradlew "${targets[@]}" $GRADLE_FLAGS; then ok "iOS 构建"; else bad "iOS 构建"; fi
}

cmd_package() {
  local fmt="$1"
  step "打包桌面 .$fmt"
  have_module "kit" || { ok "没有 :kit，跳过"; return; }

  if ./gradlew ":kit:package${fmt^}" $GRADLE_FLAGS; then ok ".$fmt 打包"; else bad ".$fmt 打包"; fi
}

# 底座保护：改动碰到底座路径（kit/、rules/、.github/…）就失败。
# 上游靠 .github/workflows/base-guard.yml 在 PR 上强制它；这里是给 fork 本地自查的。
cmd_base_guard() {
  step "base-guard（底座路径未被改动）"
  if [ ! -f scripts/check-base-unchanged.sh ]; then
    ok "没有 check-base-unchanged.sh，跳过"
    return
  fi
  if bash scripts/check-base-unchanged.sh "$@"; then
    ok "base-guard"
  else
    bad "base-guard：改了底座路径"
  fi
}

cmd_backend() {
  step "agent 网关自检"
  if [ ! -f backend/package.json ]; then
    ok "没有 backend/ 目录，跳过"
    return
  fi
  if [ ! -d backend/node_modules ]; then
    echo "  (装依赖…)"
    (cd backend && npm ci) || { bad "backend: npm ci 失败"; return; }
  fi
  # typecheck + 契约冒烟：不花钱、不连网
  if (cd backend && npm run check && npm run smoke); then
    ok "backend 网关自检"
  else
    bad "backend 网关自检"
  fi
}

# ── 分发 ────────────────────────────────────────────────────────
if [ $# -eq 0 ]; then
  sed -n '2,25p' "$0" | sed 's/^# \{0,1\}//'
  exit 0
fi

while [ $# -gt 0 ]; do
  case "$1" in
    detekt)        cmd_detekt; shift ;;
    api)           cmd_api; shift ;;
    verify)        cmd_verify; shift ;;
    test)          cmd_test; shift ;;
    lint)          cmd_lint; shift ;;
    build-android) cmd_build_android; shift ;;
    build-desktop) cmd_build_desktop; shift ;;
    build-ios)     cmd_build_ios; shift ;;
    package-deb)   cmd_package deb; shift ;;
    package-dmg)   cmd_package dmg; shift ;;
    backend)       cmd_backend; shift ;;
    base-guard)    shift; cmd_base_guard "$@"; break ;;
    all)           cmd_detekt; cmd_api; cmd_verify; cmd_test; cmd_base_guard; shift ;;
    *)             echo "未知子命令：$1" >&2; exit 2 ;;
  esac
done

if [ ${#FAILED[@]} -gt 0 ]; then
  printf '\n\033[31m%d 项门禁没过：\033[0m\n' "${#FAILED[@]}"
  for f in "${FAILED[@]}"; do printf '  - %s\n' "$f"; done
  exit 1
fi
printf '\n\033[32m全部门禁通过\033[0m\n'
