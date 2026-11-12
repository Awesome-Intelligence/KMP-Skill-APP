#!/usr/bin/env bash
# 底座保护门禁 —— 改动碰到「底座路径」就失败。
#
# 底座 = 上游仓库里所有 app 共用的东西。fork 只在 apps/<name>/ 和
# scripts/checks/<自己的名字>.sh 里写代码；底座路径改了，pull 上游必冲突。
# 上游靠 .github/workflows/base-guard.yml 在 PR 上强制这道门禁，
# fork 本地跑 `bash scripts/ci.sh base-guard` 提前自查。
#
# 用法：
#   bash scripts/check-base-unchanged.sh [--base <git-ref>] [--head <git-ref>]
#
#   --base  底座那一侧的 ref（默认：upstream 远端的默认分支）
#   --head  改动那一侧的 ref（默认：HEAD）
#
#   改动的文件 = diff(base...head) ∪ 工作区未提交的改动 ∪ 未跟踪的新文件。
#   diff(A...B) = 从 A、B 的共同祖先到 B 的差异 —— 即「B 这一侧引入的改动」，
#   不会把「上游有、我还没 pull 的改动」算成我改的。
#
# 退出码：0 = 没碰底座（或本次不检查）；1 = 碰了底座（打印清单）；2 = 用法错。
#
# 放行：BASE_GUARD_BYPASS=1（上游自己改底座时用）；fork 不该用这个。

set -o pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 2

# ── 底座路径清单 ─────────────────────────────────────────────
# 目录（以 / 结尾 = 整个子树）、单文件。想加一条就往这儿加一行。
# 没锁 scripts/checks/ 和 scripts/diff-ownership.sh —— 那块是 fork 自己的地界。
PROTECTED_PATHS=(
  "kit/"
  "kit-agent-koog/"
  "kit-membership/"
  "build-logic/"
  "backend/"
  "rules/"
  "config/"
  "gradle/"
  "skills/"
  "apps/demo/"
  ".github/actions/"
  ".github/workflows/ci.yml"
  ".github/workflows/base-guard.yml"
  ".github/workflows/release.yml"
  ".github/CODEOWNERS"
  ".github/dependabot.yml"
  ".github/pull_request_template.md"
  "settings.gradle.kts"
  "build.gradle.kts"
  "gradle.properties"
  "scripts/ci.sh"
  "scripts/check-base-unchanged.sh"
  "scripts/new_app.py"
  "scripts/install-kit.sh"
  "scripts/templates/"
  "scripts/verify-kit.py"
  "scripts/verify-i18n.py"
  "scripts/verify-skills.py"
  "scripts/merge_strings.py"
  "LICENSE"
  "SECURITY.md"
)

if [ "${BASE_GUARD_BYPASS:-}" = "1" ]; then
  echo "base-guard: BASE_GUARD_BYPASS=1，本次跳过"
  exit 0
fi

BASE=""
HEAD_REF="HEAD"
while [ $# -gt 0 ]; do
  case "$1" in
    --base) [ $# -ge 2 ] || { echo "--base 缺参数" >&2; exit 2; }; BASE="$2"; shift 2 ;;
    --head) [ $# -ge 2 ] || { echo "--head 缺参数" >&2; exit 2; }; HEAD_REF="$2"; shift 2 ;;
    -h|--help) sed -n '2,25p' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) echo "用法: $0 [--base <git-ref>] [--head <git-ref>]" >&2; exit 2 ;;
  esac
done

# base ref 解析顺序：--base > $BASE_REF > upstream 远端的默认分支
resolve_base() {
  if [ -n "$BASE" ]; then echo "$BASE"; return 0; fi
  if [ -n "${BASE_REF:-}" ]; then echo "$BASE_REF"; return 0; fi
  git remote get-url upstream >/dev/null 2>&1 || return 1
  local branch cand
  # upstream/HEAD 多数 clone 里没设，退回问远端（问不到就用 main/master）
  branch="$(git rev-parse --abbrev-ref upstream/HEAD 2>/dev/null | sed 's|^upstream/||')"
  if [ -z "$branch" ] || [ "$branch" = "HEAD" ]; then
    branch="$(GIT_TERMINAL_PROMPT=0 git ls-remote --symref upstream HEAD 2>/dev/null | sed -n 's|ref: refs/heads/\([^	]*\).*|\1|p')"
  fi
  for cand in "$branch" main master; do
    [ -n "$cand" ] || continue
    git rev-parse --verify "upstream/$cand" >/dev/null 2>&1 && { echo "upstream/$cand"; return 0; }
  done
  return 1
}

BASE="$(resolve_base)" || {
  echo "base-guard: 找不到底座 ref（--base / \$BASE_REF / upstream 远端），本次跳过"
  exit 0
}

git rev-parse --verify "$BASE" >/dev/null 2>&1 || {
  echo "base-guard: ref '$BASE' 不存在，本次跳过" >&2
  exit 0
}
git rev-parse --verify "$HEAD_REF" >/dev/null 2>&1 || {
  echo "base-guard: ref '$HEAD_REF' 不存在" >&2
  exit 2
}

changed_files() {
  # 已提交：B 这一侧相对共同祖先引入的改动
  git diff --name-only "$BASE...$HEAD_REF" 2>/dev/null
  # 工作区：暂存 + 未暂存 + 未跟踪
  git diff --name-only --cached
  git diff --name-only
  git ls-files --others --exclude-standard
}

is_protected() {
  local f="$1" p
  for p in "${PROTECTED_PATHS[@]}"; do
    case "$f" in "$p"*) return 0 ;; esac
  done
  return 1
}

violations="$(changed_files | sort -u | while IFS= read -r f; do
  [ -n "$f" ] || continue
  is_protected "$f" && echo "$f"
done)"

if [ -z "$violations" ]; then
  echo "base-guard: ✓ 没碰底座路径（base=$BASE head=$HEAD_REF）"
  exit 0
fi

echo "base-guard: ✗ 改了底座路径，fork 不许动这些文件："
echo "$violations" | sed 's/^/    /'
echo
echo "  规矩：只在 apps/<你的app>/ 和 scripts/checks/<你的名字>.sh 里写代码。"
echo "  底座能力不够 → 回上游提 PR（https://github.com/Awesome-Intelligence/KMP-Skill-APP)。"
exit 1
