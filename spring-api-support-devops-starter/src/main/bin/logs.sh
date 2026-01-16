#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  logs.sh [-f] [-n 行数] [-g 关键词] [-e 关键词] [-i] [-c]

参数:
  -f          实时滚动日志
  -n 行数     显示最后 N 行，默认 200
  -g 关键词   过滤包含关键词的行（grep）
  -e 关键词   排除包含关键词的行（grep -v）
  -i          忽略大小写（配合 -g 或 -e 使用）
  -c          显示行号
  -h, --help  显示帮助信息

说明:
  - 可以组合使用多个参数，例如: logs.sh -n 500 -g ERROR -c
  - -g 和 -e 可以多次使用，实现多条件过滤
EOF
}

FOLLOW="false"
TAIL_LINES="200"
GREP_PATTERNS=""
EXCLUDE_PATTERNS=""
IGNORE_CASE="false"
SHOW_LINE_NUM="false"

while [ $# -gt 0 ]; do
  case "$1" in
    -f)
      FOLLOW="true"
      shift
      ;;
    -n)
      TAIL_LINES="${2:-200}"
      shift 2
      ;;
    -g)
      if [ -z "${GREP_PATTERNS}" ]; then
        GREP_PATTERNS="$2"
      else
        GREP_PATTERNS="${GREP_PATTERNS}|$2"
      fi
      shift 2
      ;;
    -e)
      if [ -z "${EXCLUDE_PATTERNS}" ]; then
        EXCLUDE_PATTERNS="$2"
      else
        EXCLUDE_PATTERNS="${EXCLUDE_PATTERNS}|$2"
      fi
      shift 2
      ;;
    -i)
      IGNORE_CASE="true"
      shift
      ;;
    -c)
      SHOW_LINE_NUM="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      warn "未知参数: $1"
      usage
      exit 1
      ;;
  esac
done

load_env
CONSOLE_LOG="$(get_console_log)"

if [ ! -f "${CONSOLE_LOG}" ]; then
  warn "日志文件不存在: ${CONSOLE_LOG}"
  exit 1
fi

if [ "${FOLLOW}" = "true" ]; then
  info "实时日志: ${CONSOLE_LOG}"
fi

# 使用临时文件或管道处理
if [ -n "${GREP_PATTERNS}" ] || [ -n "${EXCLUDE_PATTERNS}" ] || [ "${SHOW_LINE_NUM}" = "true" ]; then
  # 需要过滤或显示行号，使用管道
  if [ "${FOLLOW}" = "true" ]; then
    tail -f "${CONSOLE_LOG}" | (
      if [ -n "${GREP_PATTERNS}" ]; then
        if [ "${IGNORE_CASE}" = "true" ]; then
          grep -iE "${GREP_PATTERNS}" || true
        else
          grep -E "${GREP_PATTERNS}" || true
        fi
      else
        cat
      fi
    ) | (
      if [ -n "${EXCLUDE_PATTERNS}" ]; then
        if [ "${IGNORE_CASE}" = "true" ]; then
          grep -viE "${EXCLUDE_PATTERNS}" || true
        else
          grep -vE "${EXCLUDE_PATTERNS}" || true
        fi
      else
        cat
      fi
    ) | (
      if [ "${SHOW_LINE_NUM}" = "true" ]; then
        if command -v nl >/dev/null 2>&1; then
          nl
        elif command -v cat >/dev/null 2>&1; then
          cat -n
        else
          cat
        fi
      else
        cat
      fi
    )
  else
    tail -n "${TAIL_LINES}" "${CONSOLE_LOG}" | (
      if [ -n "${GREP_PATTERNS}" ]; then
        if [ "${IGNORE_CASE}" = "true" ]; then
          grep -iE "${GREP_PATTERNS}" || true
        else
          grep -E "${GREP_PATTERNS}" || true
        fi
      else
        cat
      fi
    ) | (
      if [ -n "${EXCLUDE_PATTERNS}" ]; then
        if [ "${IGNORE_CASE}" = "true" ]; then
          grep -viE "${EXCLUDE_PATTERNS}" || true
        else
          grep -vE "${EXCLUDE_PATTERNS}" || true
        fi
      else
        cat
      fi
    ) | (
      if [ "${SHOW_LINE_NUM}" = "true" ]; then
        if command -v nl >/dev/null 2>&1; then
          nl
        elif command -v cat >/dev/null 2>&1; then
          cat -n
        else
          cat
        fi
      else
        cat
      fi
    )
  fi
else
  # 简单情况，直接执行
  if [ "${FOLLOW}" = "true" ]; then
    tail -f "${CONSOLE_LOG}"
  else
    tail -n "${TAIL_LINES}" "${CONSOLE_LOG}"
  fi
fi


