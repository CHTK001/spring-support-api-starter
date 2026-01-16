#!/usr/bin/env sh

set -eu

BIN_DIR=$(cd "$(dirname "$0")" && pwd)
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  start.sh [-f] [-j JAVA_HOME] [-jvm "JVM参数"] [-t 时区] [--charset 编码] [-p 端口] [--skywalking] [--arthas] [--dump [路径]]

参数:
  -f              启动后实时查看日志（等同执行 logs.sh -f）
  -j JAVA_HOME    指定 JAVA_HOME（也可直接传 java 可执行文件路径）
  -jvm 参数       追加任意 JVM 参数（会拼到 java 命令后）
  -t 时区         设置时区，例如 Asia/Shanghai
  --charset 编码  设置文件编码，例如 UTF-8
  -p 端口         设置应用端口（通过 SERVER_PORT 环境变量传递）
  --skywalking    启用 SkyWalking 探针（等同于 SKYWALKING_ENABLED=true）
  --arthas        启动后自动附加 Arthas（等同于 ARTHAS_ENABLED=true）
  --dump [路径]   启用 OOM 时自动生成堆转储（等同于 DUMP_ON_OOM=true）
                  如果指定路径，则使用该路径；否则使用默认路径 <base>/logs/heapdump.hprof

说明:
  - 所有参数设置后会自动保存到 app.env，下次启动时自动读取
  - 如果命令行未提供参数，将从 app.env 读取上次保存的配置
  - --dump 会在 OOM 时自动生成堆转储文件，默认路径为 <base>/logs/heapdump.hprof
EOF
}

FOLLOW_LOG="false"
JAVA_HOME_ARG=""
JVM_EXTRA=""
TIMEZONE_ARG=""
CHARSET_ARG=""
SERVER_PORT_ARG=""
SKYWALKING_FLAG=""
ARTHAS_FLAG=""
DUMP_FLAG=""
DUMP_PATH_ARG=""

while [ $# -gt 0 ]; do
  case "$1" in
    -f)
      FOLLOW_LOG="true"
      shift
      ;;
    -j)
      JAVA_HOME_ARG="${2:-}"
      shift 2
      ;;
    -jvm)
      JVM_EXTRA="${2:-}"
      shift 2
      ;;
    -t)
      TIMEZONE_ARG="${2:-}"
      shift 2
      ;;
    --charset)
      CHARSET_ARG="${2:-}"
      shift 2
      ;;
    -p)
      SERVER_PORT_ARG="${2:-}"
      if ! validate_port "${SERVER_PORT_ARG}"; then
        die "无效的端口号: ${SERVER_PORT_ARG}（必须是 1-65535 之间的数字）"
      fi
      shift 2
      ;;
    --skywalking)
      SKYWALKING_FLAG="true"
      shift
      ;;
    --arthas)
      ARTHAS_FLAG="true"
      shift
      ;;
    --dump)
      DUMP_FLAG="true"
      # 检查下一个参数是否是路径（不以 - 开头）
      if [ $# -gt 1 ]; then
        NEXT_ARG="${2}"
        # 使用 expr 提取第一个字符（兼容性更好）
        FIRST_CHAR="$(expr "${NEXT_ARG}" : '\(.\).*' 2>/dev/null || echo "")"
        if [ -n "${FIRST_CHAR}" ] && [ "${FIRST_CHAR}" != "-" ]; then
          DUMP_PATH_ARG="${NEXT_ARG}"
          shift 2
        else
          shift
        fi
      else
        shift
      fi
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

ensure_env_file
load_env

# 如果命令行未提供参数，从 env 读取
if [ -z "${JAVA_HOME_ARG}" ] && [ -n "${JAVA_HOME_SAVED:-}" ]; then
  JAVA_HOME_ARG="${JAVA_HOME_SAVED}"
fi
if [ -z "${JVM_EXTRA}" ] && [ -n "${JVM_EXTRA_SAVED:-}" ]; then
  JVM_EXTRA="${JVM_EXTRA_SAVED}"
fi
if [ -z "${TIMEZONE_ARG}" ] && [ -n "${TIMEZONE:-}" ]; then
  TIMEZONE_ARG="${TIMEZONE}"
fi
if [ -z "${CHARSET_ARG}" ] && [ -n "${CHARSET:-}" ]; then
  CHARSET_ARG="${CHARSET}"
fi
if [ -z "${SERVER_PORT_ARG}" ] && [ -n "${SERVER_PORT:-}" ]; then
  SERVER_PORT_ARG="${SERVER_PORT}"
fi
if [ -z "${SKYWALKING_FLAG}" ] && [ "${SKYWALKING_ENABLED:-false}" = "true" ]; then
  SKYWALKING_FLAG="true"
fi
if [ -z "${ARTHAS_FLAG}" ] && [ "${ARTHAS_ENABLED:-false}" = "true" ]; then
  ARTHAS_FLAG="true"
fi
if [ -z "${DUMP_FLAG}" ] && [ "${DUMP_ON_OOM:-false}" = "true" ]; then
  DUMP_FLAG="true"
fi
if [ -z "${DUMP_PATH_ARG}" ] && [ -n "${DUMP_PATH:-}" ]; then
  DUMP_PATH_ARG="${DUMP_PATH}"
fi

# 如果命令行提供了参数，写入 env（保存配置）
if [ -n "${JAVA_HOME_ARG}" ]; then
  set_env_var "JAVA_HOME_SAVED" "${JAVA_HOME_ARG}"
fi
if [ -n "${JVM_EXTRA}" ]; then
  set_env_var "JVM_EXTRA_SAVED" "${JVM_EXTRA}"
fi
if [ -n "${TIMEZONE_ARG}" ]; then
  set_env_var "TIMEZONE" "${TIMEZONE_ARG}"
fi
if [ -n "${CHARSET_ARG}" ]; then
  set_env_var "CHARSET" "${CHARSET_ARG}"
fi
if [ -n "${SERVER_PORT_ARG}" ]; then
  set_env_var "SERVER_PORT" "${SERVER_PORT_ARG}"
fi
if [ -n "${SKYWALKING_FLAG}" ]; then
  set_env_var "SKYWALKING_ENABLED" "${SKYWALKING_FLAG}"
fi
if [ -n "${ARTHAS_FLAG}" ]; then
  set_env_var "ARTHAS_ENABLED" "${ARTHAS_FLAG}"
fi
if [ -n "${DUMP_FLAG}" ]; then
  set_env_var "DUMP_ON_OOM" "${DUMP_FLAG}"
fi
if [ -n "${DUMP_PATH_ARG}" ]; then
  set_env_var "DUMP_PATH" "${DUMP_PATH_ARG}"
fi

# 重新加载 env（读取刚写入的值）
load_env

JAVA_BIN="$(resolve_java "${JAVA_HOME_ARG}")"
JAR_FILE="$(get_jar_file)"
PID_FILE="$(get_pid_file)"
CONSOLE_LOG="$(get_console_log)"

if [ ! -f "${JAR_FILE}" ]; then
  BASE_DIR="$(get_base_dir)"
  info "当前工作目录: ${BASE_DIR}"
  info "目录内容如下:"
  ls -al "${BASE_DIR}" || true
  die "未找到 jar 文件: ${JAR_FILE}"
fi

if [ -f "${PID_FILE}" ]; then
  PID="$(cat "${PID_FILE}" || true)"
  if is_running "${PID}"; then
    info "应用已在运行，PID=${PID}"
    exit 0
  fi
  rm -f "${PID_FILE}"
fi

TIMEZONE_VAL="${TIMEZONE_ARG:-${TIMEZONE:-}}"
CHARSET_VAL="${CHARSET_ARG:-${CHARSET:-UTF-8}}"
JVM_ARGS_VAL="${JVM_ARGS:-}"

JAVA_OPTS=""
if [ -n "${TIMEZONE_VAL}" ]; then
  JAVA_OPTS="${JAVA_OPTS} -Duser.timezone=${TIMEZONE_VAL}"
fi
if [ -n "${CHARSET_VAL}" ]; then
  JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=${CHARSET_VAL}"
fi

SKYWALKING_EFFECTIVE="${SKYWALKING_FLAG:-${SKYWALKING_ENABLED:-false}}"
if [ "${SKYWALKING_EFFECTIVE}" = "true" ]; then
  SKY_OPTS="$(get_skywalking_javaagent_opts)"
  JAVA_OPTS="${JAVA_OPTS} ${SKY_OPTS}"
fi

DUMP_EFFECTIVE="${DUMP_FLAG:-${DUMP_ON_OOM:-false}}"
if [ "${DUMP_EFFECTIVE}" = "true" ]; then
  # 确定堆转储文件路径
  DUMP_PATH_VAL="${DUMP_PATH_ARG:-${DUMP_PATH:-}}"
  if [ -n "${DUMP_PATH_VAL}" ]; then
    # 使用自定义路径
    DUMP_FILE="${DUMP_PATH_VAL}"
    # 确保目录存在
    DUMP_DIR="$(dirname "${DUMP_FILE}")"
    if [ "${DUMP_DIR}" != "." ] && [ "${DUMP_DIR}" != "/" ]; then
      mkdir -p "${DUMP_DIR}"
    fi
  else
    # 使用默认路径
    BASE_DIR="$(get_base_dir)"
    DUMP_DIR="${BASE_DIR}/logs"
    mkdir -p "${DUMP_DIR}"
    DUMP_FILE="${DUMP_DIR}/heapdump.hprof"
  fi
  JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${DUMP_FILE}"
  info "已启用 OOM 堆转储: ${DUMP_FILE}"
fi

# 设置 SERVER_PORT 环境变量（如果指定了端口）
if [ -n "${SERVER_PORT_ARG}" ]; then
  export SERVER_PORT="${SERVER_PORT_ARG}"
fi

CMD="${JAVA_BIN} ${JAVA_OPTS} ${JVM_ARGS_VAL} ${JVM_EXTRA} -jar ${JAR_FILE}"

info "启动应用: $(get_app_name) $(get_app_version)"
if [ -n "${SERVER_PORT_ARG}" ]; then
  info "端口: ${SERVER_PORT_ARG}"
fi
info "命令: ${CMD}"
info "日志: ${CONSOLE_LOG}"

# 说明：
# - 这里不能用 `sh -c "${CMD}"`，否则 $! 拿到的是 sh 的 PID，stop.sh 会只杀掉 sh，导致 java 进程残留。
# - 直接后台启动 java，确保 PID 文件记录的是 java 进程 PID。
nohup "${JAVA_BIN}" ${JAVA_OPTS} ${JVM_ARGS_VAL} ${JVM_EXTRA} -jar "${JAR_FILE}" >> "${CONSOLE_LOG}" 2>&1 &
PID="$!"
echo "${PID}" > "${PID_FILE}"
info "启动成功，PID=${PID}"

# 等待进程真正启动
sleep 1
if ! is_running "${PID}"; then
  warn "进程启动后立即退出，请检查日志: ${CONSOLE_LOG}"
  tail -n 50 "${CONSOLE_LOG}" 2>/dev/null || true
  rm -f "${PID_FILE}"
  exit 1
fi

ARTHAS_EFFECTIVE="${ARTHAS_FLAG:-${ARTHAS_ENABLED:-false}}"
if [ "${ARTHAS_EFFECTIVE}" = "true" ]; then
  start_arthas_if_enabled "${PID}" "${JAVA_BIN}"
fi

# 如果指定了端口，尝试等待应用就绪（可选，通过环境变量控制）
if [ -n "${SERVER_PORT_ARG}" ] && [ "${WAIT_FOR_START:-false}" = "true" ]; then
  wait_for_start "${SERVER_PORT_ARG}" "/hello" "${START_TIMEOUT:-60}" || true
fi

if [ "${FOLLOW_LOG}" = "true" ]; then
  "${BIN_DIR}/logs.sh" -f
fi


