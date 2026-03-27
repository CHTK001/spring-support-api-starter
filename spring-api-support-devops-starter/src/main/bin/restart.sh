#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  restart.sh [-t 秒] [start.sh 的其他参数]

参数:
  -t 秒   停止等待时间，默认 30 秒（传递给 stop.sh）
  其他参数会透传给 start.sh，例如:
    -f              启动后实时查看日志
    -j JAVA_HOME    指定 JAVA_HOME
    -jvm "JVM参数"  追加 JVM 参数
    -p 端口         设置应用端口
    --skywalking    启用 SkyWalking
    --arthas        启动后自动附加 Arthas

说明:
  - 先执行 stop.sh 停止应用，再执行 start.sh 启动应用
  - 所有 start.sh 的参数都可以透传
EOF
}

WAIT_SECONDS="30"

while [ $# -gt 0 ]; do
  case "$1" in
    -t)
      if [ $# -lt 2 ]; then
        die "-t 需要指定等待秒数，例如: -t 30"
      fi
      WAIT_SECONDS="${2:-30}"
      if ! echo "${WAIT_SECONDS}" | grep -qE '^[0-9]+$'; then
        die "无效的等待秒数: ${WAIT_SECONDS}"
      fi
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    --)
      shift
      break
      ;;
    *)
      # 其余参数原样透传给 start.sh，避免字符串拼接导致参数被错误拆分
      break
      ;;
  esac
done

info "重启应用"
"${BIN_DIR}/stop.sh" -t "${WAIT_SECONDS}"
if [ $# -gt 0 ]; then
  "${BIN_DIR}/start.sh" "$@"
else
  "${BIN_DIR}/start.sh"
fi

