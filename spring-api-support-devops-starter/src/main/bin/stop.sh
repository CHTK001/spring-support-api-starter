#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  stop.sh [-t 秒] [--force] [--no-cleanup]

参数:
  -t 秒         优雅停止等待时间，默认 30 秒
  --force       立即强制停止（跳过优雅停止，直接 kill -9）
  --no-cleanup  停止后不清理子进程和 PID 文件
  -h, --help    显示帮助信息

说明:
  - 默认使用优雅停止（SIGTERM），等待进程自行退出
  - 如果超时仍未退出，会自动强制停止（SIGKILL）
  - 使用 --force 可跳过等待，直接强制停止
EOF
}

WAIT_SECONDS="30"
FORCE_STOP="false"
NO_CLEANUP="false"

while [ $# -gt 0 ]; do
  case "$1" in
    -t)
      WAIT_SECONDS="${2:-30}"
      shift 2
      ;;
    --force)
      FORCE_STOP="true"
      shift
      ;;
    --no-cleanup)
      NO_CLEANUP="true"
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
PID_FILE="$(get_pid_file)"

if [ ! -f "${PID_FILE}" ]; then
  info "未发现 PID 文件，应用可能未启动"
  exit 0
fi

PID="$(cat "${PID_FILE}" || true)"
if ! is_running "${PID}"; then
  warn "进程不存在，PID=${PID}，将清理 PID 文件"
  rm -f "${PID_FILE}"
  exit 0
fi

if [ "${FORCE_STOP}" = "true" ]; then
  info "强制停止应用，PID=${PID}"
  kill -9 "${PID}" >/dev/null 2>&1 || true
else
  info "优雅停止应用，PID=${PID}（等待最多 ${WAIT_SECONDS} 秒）"
  # 先发送 SIGTERM 信号，允许应用优雅关闭
  kill -TERM "${PID}" >/dev/null 2>&1 || true
  
  COUNT=0
  while is_running "${PID}"; do
    if [ "${COUNT}" -ge "${WAIT_SECONDS}" ]; then
      warn "优雅停止超时（${WAIT_SECONDS}秒），强制停止，PID=${PID}"
      kill -9 "${PID}" >/dev/null 2>&1 || true
      break
    fi
    sleep 1
    COUNT=$((COUNT + 1))
    # 每 5 秒显示一次等待提示
    if [ $((COUNT % 5)) -eq 0 ]; then
      info "等待进程退出... (${COUNT}/${WAIT_SECONDS}秒)"
    fi
  done
  
  if ! is_running "${PID}"; then
    info "应用已优雅停止"
  fi
fi

# 清理子进程
if [ "${NO_CLEANUP}" = "false" ]; then
  # 兼容处理：如果 PID 进程退出，但残留子进程（例如历史版本 start.sh 通过 sh -c 启动 java）
  # 这里做一次 best-effort 的子进程清理，避免 java 进程孤儿化。
  if command -v ps >/dev/null 2>&1; then
    CHILD_PIDS="$(ps -o pid= --ppid "${PID}" 2>/dev/null || true)"
    if [ -n "${CHILD_PIDS}" ]; then
      warn "发现子进程残留，将尝试停止: ${CHILD_PIDS}"
      for CHILD_PID in ${CHILD_PIDS}; do
        kill "${CHILD_PID}" >/dev/null 2>&1 || true
      done
    fi
  fi
  
  rm -f "${PID_FILE}"
fi

info "停止完成"


