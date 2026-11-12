#!/usr/bin/env bash
# 示例门禁：仓库里不许出现硬编码的 API key。
#
# 这道门禁存在的理由：key 进了版本库 = 泄漏了（git 历史里还在，删了文件也没用）。
# 底座多处强调「key 不进仓库」，但靠 review 抓不住 —— 复制粘贴一次就漏。
#
# 判据：源码/配置里出现形如 sk-ant-… / sk-… 的长串。
# 明显是示例的短串（"sk-xxx"）长度不够，不匹配。
#
# 用 grep -r 而不是 git grep：git grep 只看已跟踪的文件，
# 新写的文件在提交前是未跟踪的 —— 那正是最容易漏的时候。
#
# 退出码：0 = 干净，1 = 找到疑似 key。
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT" || exit 1

HITS="$(grep -rInE '(sk-ant-[A-Za-z0-9_-]{16,}|sk-[A-Za-z0-9]{32,})' \
  --include='*.kt' --include='*.kts' --include='*.java' --include='*.xml' \
  --include='*.json' --include='*.ts' --include='*.js' --include='*.yml' \
  --include='*.yaml' --include='*.properties' --include='*.md' \
  --exclude-dir=build --exclude-dir=.git --exclude-dir=.gradle --exclude-dir=node_modules \
  --exclude-dir=.kotlin --exclude='*.api' --exclude='verification-metadata.xml' \
  . 2>/dev/null || true)"

if [ -z "$HITS" ]; then
  echo "✓ 没找到硬编码的 API key"
  exit 0
fi

echo "✗ 找到疑似硬编码的 API key："
echo "$HITS" | sed 's/^/  - /'
echo
echo "  key 进了版本库就是泄漏了（git 历史里还在，删文件也没用）。"
echo "  要撤销：去 Anthropic 控制台吊销这个 key，重新发一个。"
exit 1
