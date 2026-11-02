#!/usr/bin/env bash
# 列出工作区里「不是我改的」文件 —— 多 Claude 并行改同一个项目时，用来判断编译报错该谁背锅。
#
# 用法： .claude/scripts/diff-ownership.sh <我这次改的文件...>
#   不传参数 → 打印所有改动文件（自检用）
#   传了参数 → 打印除这些之外的改动文件
#
# 退出码恒为 0；有输出就说明存在别人改的文件。

cd "$(dirname "$0")/../.." || exit 1

MINE=("$@")

is_mine() {
  local target="$1" m
  for m in ${MINE[@]+"${MINE[@]}"}; do
    [ "$m" = "$target" ] && return 0
  done
  return 1
}

git -c core.quotepath=false status --porcelain -uall | while IFS= read -r line; do
  f="${line:3}"
  # 重命名/复制会显示成 "old -> new"，取新名字
  case "$f" in
    *" -> "*) f="${f##* -> }" ;;
  esac
  [ -z "$f" ] && continue
  [ -d "$f" ] && continue   # 只可能是 submodule
  is_mine "$f" || echo "$f"
done
