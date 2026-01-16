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
PASS_ARGS=""

while [ $# -gt 0 ]; do
  case "$1" in
    -t)
      WAIT_SECONDS="${2:-30}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [ -z "${PASS_ARGS}" ]; then
        PASS_ARGS="$1"
      else
        PASS_ARGS="${PASS_ARGS} $1"
      fi
      shift
      ;;
  esac
done

info "重启应用"
"${BIN_DIR}/stop.sh" -t "${WAIT_SECONDS}"
if [ -n "${PASS_ARGS}" ]; then
  "${BIN_DIR}/start.sh" ${PASS_ARGS}
else
  "${BIN_DIR}/start.sh"
fi


