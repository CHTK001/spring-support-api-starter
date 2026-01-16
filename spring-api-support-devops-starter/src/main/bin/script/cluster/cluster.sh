#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# cluster.sh 位于 bin/script/cluster，bin 在其上两级
BIN_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

BIN_DIR_OVERRIDE="${BIN_DIR}"
export BIN_DIR_OVERRIDE

load_env

usage() {
  cat <<EOF
用法:
  cluster.sh -f hosts.txt -c start|stop|restart|logs|upgrade|rollback|status|test|health [-p 包路径] [--args "透传参数"]
  cluster.sh --node add <节点地址> [--file hosts.txt]     # 添加节点
  cluster.sh --node list [--file hosts.txt]                # 列出所有节点
  cluster.sh --node del <序号> [--file hosts.txt]          # 删除指定序号节点

说明:
  - 节点地址格式支持:
    * host                    # 远程主机（默认端口 22）
    * host:port               # 远程主机（指定端口）
    * user@host               # 远程主机（指定用户，默认端口 22）
    * user@host:port          # 远程主机（指定用户和端口）
    * localhost 或 127.0.0.1  # 本地节点（直接执行，不使用 ssh）
  - hosts 文件每行一个主机，空行/以 # 开头会忽略
  - 远程默认执行 <remoteBase>/bin 下脚本
  - SSH 认证方式（按优先级）:
    1. SSH 密钥免密登录（推荐）
    2. 密码登录（通过 -P 参数或 CLUSTER_SSH_PASSWORD 环境变量，需要安装 sshpass）

参数:
  -f 文件       hosts 文件路径（默认: <base>/bin/cluster_hosts.txt）
  -c 命令       start/stop/restart/logs/upgrade/rollback/status/test/health
  -p 路径       upgrade/rollback 使用的包路径（远程机器可访问的路径）
  -P 密码       SSH 密码（用于密码登录，需要安装 sshpass）
  --args 参数   透传给远程脚本的参数（例如 start 的 -f/-jvm 等）
  --no-restart  upgrade/rollback 完成后不自动重启（仅用于 upgrade/rollback 命令）
  --node 操作  节点管理操作: add/list/del
  --file 文件   节点管理时指定 hosts 文件（默认: <base>/bin/cluster_hosts.txt）
  --parallel   并发执行（upgrade/rollback/start/restart 默认串行以减少风险）
  --timeout 秒  SSH 连接和执行超时（默认 30 秒）
  --yes         跳过确认提示（危险操作时）
EOF
}

HOSTS_FILE=""
COMMAND=""
PACKAGE=""
PASS_ARGS=""
NODE_OP=""
NODE_ARG=""
SSH_PASSWORD="${CLUSTER_SSH_PASSWORD:-}"
PARALLEL="${CLUSTER_PARALLEL:-false}"
TIMEOUT="${CLUSTER_TIMEOUT:-30}"
SKIP_CONFIRM="${CLUSTER_SKIP_CONFIRM:-false}"
REMOTE_DIR="${REMOTE_DIR:-$(get_app_name)-$(get_app_version)}"
SUCCESS_COUNT=0
FAIL_COUNT=0

get_default_hosts_file() {
  var_base_dir="$(get_base_dir)"
  echo "${var_base_dir}/bin/cluster_hosts.txt"
}

load_env
var_base_dir="$(get_base_dir)"
DEFAULT_HOSTS_FILE="$(get_default_hosts_file)"

while [ $# -gt 0 ]; do
  case "$1" in
    -f)
      HOSTS_FILE="${2:-}"
      shift 2
      ;;
    -c)
      COMMAND="${2:-}"
      shift 2
      ;;
    -p)
      PACKAGE="${2:-}"
      shift 2
      ;;
    -P)
      SSH_PASSWORD="${2:-}"
      shift 2
      ;;
    --args)
      PASS_ARGS="${2:-}"
      shift 2
      ;;
    --parallel)
      PARALLEL="true"
      shift
      ;;
    --timeout)
      TIMEOUT="${2:-30}"
      shift 2
      ;;
    --yes)
      SKIP_CONFIRM="true"
      shift
      ;;
    --no-restart)
      # upgrade/rollback 命令支持 --no-restart 参数
      if [ -z "${PASS_ARGS}" ]; then
        PASS_ARGS="--no-restart"
      else
        PASS_ARGS="${PASS_ARGS} --no-restart"
      fi
      shift
      ;;
    --node)
      NODE_OP="${2:-}"
      shift 2
      ;;
    --file)
      HOSTS_FILE="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [ -n "${NODE_OP}" ] && [ -z "${NODE_ARG}" ]; then
        NODE_ARG="$1"
        shift
      else
        warn "[cluster] 未知参数: $1"
        usage
        exit 1
      fi
      ;;
  esac
done

# 节点管理功能
if [ -n "${NODE_OP}" ]; then
  if [ -z "${HOSTS_FILE}" ]; then
    HOSTS_FILE="${DEFAULT_HOSTS_FILE}"
  fi
  
  case "${NODE_OP}" in
    add)
      if [ -z "${NODE_ARG}" ]; then
        warn "[cluster] 添加节点需要提供节点地址"
        warn "[cluster] 格式: host, host:port, user@host, user@host:port, localhost, 127.0.0.1"
        exit 1
      fi
      
      # 验证格式：支持 host, host:port, user@host, user@host:port, localhost, 127.0.0.1
      if ! echo "${NODE_ARG}" | grep -qE '^([^@:]+@)?[^@:]+(:[0-9]+)?$|^localhost$|^127\.0\.0\.1$'; then
        warn "[cluster] 节点格式错误"
        warn "[cluster] 支持格式: host, host:port, user@host, user@host:port, localhost, 127.0.0.1"
        exit 1
      fi
      
      # 检查是否已存在
      if [ -f "${HOSTS_FILE}" ]; then
        if grep -q "^[[:space:]]*${NODE_ARG}[[:space:]]*$" "${HOSTS_FILE}" 2>/dev/null; then
          warn "[cluster] 节点已存在: ${NODE_ARG}"
          exit 1
        fi
      fi
      
      # 添加节点
      mkdir -p "$(dirname "${HOSTS_FILE}")"
      echo "${NODE_ARG}" >> "${HOSTS_FILE}"
      info "[cluster] 已添加节点: ${NODE_ARG}"
      exit 0
      ;;
    
    list)
      if [ ! -f "${HOSTS_FILE}" ]; then
        info "[cluster] hosts 文件不存在: ${HOSTS_FILE}"
        info "[cluster] 使用 --node add 添加节点"
        exit 0
      fi
      
      echo "=========================================="
      echo "节点列表: ${HOSTS_FILE}"
      echo "=========================================="
      COUNT=0
      while IFS= read -r host || [ -n "${host}" ]; do
        host="$(echo "${host}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
        if [ -z "${host}" ]; then
          continue
        fi
        case "${host}" in
          \#*)
            continue
            ;;
        esac
        COUNT=$((COUNT + 1))
        echo "${COUNT}. ${host}"
      done < "${HOSTS_FILE}"
      
      if [ "${COUNT}" -eq 0 ]; then
        echo "（无节点）"
      else
        echo "=========================================="
        echo "共 ${COUNT} 个节点"
      fi
      exit 0
      ;;
    
    del)
      if [ -z "${NODE_ARG}" ]; then
        warn "[cluster] 删除节点需要提供序号"
        exit 1
      fi
      
      if [ ! -f "${HOSTS_FILE}" ]; then
        warn "[cluster] hosts 文件不存在: ${HOSTS_FILE}"
        exit 1
      fi
      
      # 读取所有节点
      TMP_FILE="${HOSTS_FILE}.tmp.$$"
      rm -f "${TMP_FILE}"
      COUNT=0
      TARGET_HOST=""
      
      while IFS= read -r host || [ -n "${host}" ]; do
        host_trimmed="$(echo "${host}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
        if [ -z "${host_trimmed}" ]; then
          echo "${host}" >> "${TMP_FILE}"
          continue
        fi
        case "${host_trimmed}" in
          \#*)
            echo "${host}" >> "${TMP_FILE}"
            continue
            ;;
        esac
        
        COUNT=$((COUNT + 1))
        if [ "${COUNT}" -eq "${NODE_ARG}" ]; then
          TARGET_HOST="${host_trimmed}"
          # 跳过这一行（删除）
        else
          echo "${host}" >> "${TMP_FILE}"
        fi
      done < "${HOSTS_FILE}"
      
      if [ -z "${TARGET_HOST}" ]; then
        rm -f "${TMP_FILE}"
        warn "[cluster] 未找到序号为 ${NODE_ARG} 的节点"
        exit 1
      fi
      
      mv "${TMP_FILE}" "${HOSTS_FILE}"
      info "[cluster] 已删除节点: ${TARGET_HOST}"
      exit 0
      ;;
    
    *)
      warn "[cluster] 不支持的节点操作: ${NODE_OP}"
      warn "[cluster] 支持的操作: add/list/del"
      exit 1
      ;;
  esac
fi

# 传统命令模式
if [ -z "${HOSTS_FILE}" ]; then
  HOSTS_FILE="${DEFAULT_HOSTS_FILE}"
fi

if [ -z "${COMMAND}" ]; then
  usage
  exit 1
fi

if [ ! -f "${HOSTS_FILE}" ]; then
  warn "[cluster] hosts 文件不存在: ${HOSTS_FILE}"
  warn "[cluster] 使用 --node add 添加节点，或使用 -f 指定 hosts 文件"
  exit 1
fi

info "[cluster] 远程目录: ${REMOTE_DIR}"

SCRIPT_NAME=""
case "${COMMAND}" in
  start)
    SCRIPT_NAME="start.sh"
    # 启动默认使用串行执行，减少风险
    if [ "${PARALLEL}" != "true" ]; then
      PARALLEL="false"
    fi
    ;;
  stop)
    SCRIPT_NAME="stop.sh"
    ;;
  restart)
    SCRIPT_NAME="restart.sh"
    # 重启默认使用串行执行，减少风险
    if [ "${PARALLEL}" != "true" ]; then
      PARALLEL="false"
    fi
    ;;
  logs)
    SCRIPT_NAME="logs.sh"
    ;;
  upgrade)
    SCRIPT_NAME="upgrade.sh"
    # 升级默认使用滚动升级（串行执行）
    if [ "${PARALLEL}" != "true" ]; then
      PARALLEL="false"
    fi
    ;;
  rollback)
    SCRIPT_NAME="rollback.sh"
    # 回滚默认使用滚动回滚（串行执行）
    if [ "${PARALLEL}" != "true" ]; then
      PARALLEL="false"
    fi
    ;;
  status)
    SCRIPT_NAME="status.sh"
    ;;
  test)
    # test 命令不需要脚本，直接测试 SSH 连通性和节点健康状态
    ;;
  health)
    # health 命令：全面健康检查（SSH、应用状态、资源等）
    SCRIPT_NAME="status.sh"
    ;;
  *)
    warn "[cluster] 不支持命令: ${COMMAND}"
    warn "[cluster] 支持的命令: start/stop/restart/logs/upgrade/rollback/status/test"
    exit 1
    ;;
esac

# 危险操作确认
if [ "${COMMAND}" = "stop" ] || [ "${COMMAND}" = "upgrade" ] || [ "${COMMAND}" = "rollback" ]; then
  if [ "${SKIP_CONFIRM}" != "true" ]; then
    if [ "${COMMAND}" = "upgrade" ]; then
      warn "[cluster] 警告: 即将执行滚动升级操作"
      warn "[cluster] 升级将逐个节点进行，每个节点升级完成后才会继续下一个节点"
    elif [ "${COMMAND}" = "rollback" ]; then
      warn "[cluster] 警告: 即将执行滚动回滚操作"
      warn "[cluster] 回滚将逐个节点进行，每个节点回滚完成后才会继续下一个节点"
    else
      warn "[cluster] 警告: 即将执行 ${COMMAND} 操作"
    fi
    warn "[cluster] 这将影响所有节点，请确认是否继续？(yes/no)"
    read -r CONFIRM
    if [ "${CONFIRM}" != "yes" ]; then
      info "[cluster] 操作已取消"
      exit 0
    fi
  fi
fi

parse_node() {
  # 解析节点地址，返回 user, host, port
  # 格式: host, host:port, user@host, user@host:port, localhost, 127.0.0.1
  var_node="${1:-}"
  
  # 本地节点
  if [ "${var_node}" = "localhost" ] || [ "${var_node}" = "127.0.0.1" ]; then
    echo "local"
    echo ""
    echo "22"
    return 0
  fi
  
  # 提取端口
  var_port="22"
  if echo "${var_node}" | grep -q ":"; then
    var_port="$(echo "${var_node}" | sed 's/.*:\([0-9]*\)$/\1/')"
    var_node="$(echo "${var_node}" | sed 's/:.*$//')"
  fi
  
  # 提取用户和主机
  var_user=""
  var_host="${var_node}"
  if echo "${var_node}" | grep -q "@"; then
    var_user="$(echo "${var_node}" | sed 's/@.*$//')"
    var_host="$(echo "${var_node}" | sed 's/^.*@//')"
  fi
  
  echo "${var_user}"
  echo "${var_host}"
  echo "${var_port}"
}

# 执行远程命令的函数
execute_remote_cmd() {
  var_host="$1"
  var_command="$2"
  var_script_name="$3"
  var_package="$4"
  var_pass_args="$5"
  var_ssh_password="$6"
  var_timeout="$7"
  
  # 解析节点信息
  NODE_INFO="$(parse_node "${var_host}")"
  NODE_USER="$(echo "${NODE_INFO}" | sed -n '1p')"
  NODE_HOST="$(echo "${NODE_INFO}" | sed -n '2p')"
  NODE_PORT="$(echo "${NODE_INFO}" | sed -n '3p')"
  
  # test 命令：测试 SSH 连通性
  if [ "${var_command}" = "test" ]; then
    if [ "${NODE_USER}" = "local" ]; then
      info "[cluster] ${var_host} -> 本地节点，跳过测试"
      return 0
    fi
    
    SSH_CMD_BASE="ssh -o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3 -o BatchMode=yes"
    if [ "${NODE_PORT}" != "22" ]; then
      SSH_CMD_BASE="${SSH_CMD_BASE} -p ${NODE_PORT}"
    fi
    if [ -n "${NODE_USER}" ]; then
      SSH_TARGET="${NODE_USER}@${NODE_HOST}"
    else
      SSH_TARGET="${NODE_HOST}"
    fi
    
    # 带重试的SSH连接测试
    MAX_RETRIES=3
    RETRY_COUNT=0
    
    while [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; do
      if [ -n "${var_ssh_password}" ]; then
        if ! command -v sshpass >/dev/null 2>&1; then
          warn "[cluster] ${var_host} -> 需要 sshpass 但未安装"
          return 1
        fi
        export SSHPASS="${var_ssh_password}"
        if sshpass -e ${SSH_CMD_BASE} "${SSH_TARGET}" "echo 'OK'" >/dev/null 2>&1; then
          info "[cluster] ${var_host} -> 连接成功"
          unset SSHPASS
          return 0
        fi
        unset SSHPASS
      else
        if ${SSH_CMD_BASE} "${SSH_TARGET}" "echo 'OK'" >/dev/null 2>&1; then
          info "[cluster] ${var_host} -> 连接成功"
          return 0
        fi
      fi
      
      RETRY_COUNT=$((RETRY_COUNT + 1))
      if [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; then
        warn "[cluster] ${var_host} -> 连接失败，重试 ${RETRY_COUNT}/${MAX_RETRIES}..."
        sleep 2
      fi
    done
    
    warn "[cluster] ${var_host} -> 连接失败（已重试 ${MAX_RETRIES} 次）"
    return 1
  fi
  
  # health 命令：全面健康检查
  if [ "${var_command}" = "health" ]; then
    info "[cluster] ${var_host} -> 执行健康检查..."
    
    # 路径验证
    REMOTE_DIR_ABS=""
    
    # 本地节点验证：使用 get_base_dir 而不是 REMOTE_DIR
    if [ "${NODE_USER}" = "local" ]; then
      # 本地节点直接使用当前基础目录
      REMOTE_DIR_ABS="$(get_base_dir)"
      if [ ! -d "${REMOTE_DIR_ABS}" ]; then
        warn "[cluster] ${var_host} -> 本地目录不存在: ${REMOTE_DIR_ABS}"
        return 1
      fi
    else
      # 远程节点验证
      VALIDATE_CMD="if [ ! -d \"${REMOTE_DIR}\" ]; then echo \"ERROR: 远程目录不存在: ${REMOTE_DIR}\" >&2; exit 1; fi; "
      VALIDATE_CMD="${VALIDATE_CMD}REMOTE_DIR_ABS=\"\$(cd \"${REMOTE_DIR}\" && pwd)\"; "
      VALIDATE_CMD="${VALIDATE_CMD}echo \"\${REMOTE_DIR_ABS}\""
      
      SSH_CMD_BASE="ssh -o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3"
      if [ "${NODE_PORT}" != "22" ]; then
        SSH_CMD_BASE="${SSH_CMD_BASE} -p ${NODE_PORT}"
      fi
      if [ -n "${NODE_USER}" ]; then
        SSH_TARGET="${NODE_USER}@${NODE_HOST}"
      else
        SSH_TARGET="${NODE_HOST}"
      fi
      
      if [ -n "${var_ssh_password}" ]; then
        if ! command -v sshpass >/dev/null 2>&1; then
          warn "[cluster] ${var_host} -> 需要 sshpass 但未安装"
          return 1
        fi
        export SSHPASS="${var_ssh_password}"
        VALIDATE_RESULT="$(sshpass -e ${SSH_CMD_BASE} "${SSH_TARGET}" "${VALIDATE_CMD}" 2>&1)"
        VALIDATE_EXIT=$?
        unset SSHPASS
      else
        VALIDATE_RESULT="$(${SSH_CMD_BASE} "${SSH_TARGET}" "${VALIDATE_CMD}" 2>&1)"
        VALIDATE_EXIT=$?
      fi
      
      if [ "${VALIDATE_EXIT}" -ne 0 ]; then
        warn "[cluster] ${var_host} -> 路径验证失败: ${VALIDATE_RESULT}"
        return 1
      fi
      REMOTE_DIR_ABS="${VALIDATE_RESULT}"
    fi
    
    # 构建健康检查命令（使用验证后的绝对路径）
    HEALTH_CMD="cd \"${REMOTE_DIR_ABS}\" || { echo 'ERROR: 无法切换到目录: ${REMOTE_DIR_ABS}' >&2; exit 1; }; "
    HEALTH_CMD="${HEALTH_CMD}echo '=== SSH 连接检查 ===' && "
    HEALTH_CMD="${HEALTH_CMD}echo 'OK' && "
    HEALTH_CMD="${HEALTH_CMD}echo '' && "
    HEALTH_CMD="${HEALTH_CMD}echo '=== 应用状态检查 ===' && "
    HEALTH_CMD="${HEALTH_CMD}if [ -f ./bin/status.sh ]; then ./bin/status.sh; else echo 'status.sh 不存在'; fi && "
    HEALTH_CMD="${HEALTH_CMD}echo '' && "
    HEALTH_CMD="${HEALTH_CMD}echo '=== 系统资源检查 ===' && "
    HEALTH_CMD="${HEALTH_CMD}if command -v free >/dev/null 2>&1; then free -h; else echo 'free 命令不可用'; fi && "
    HEALTH_CMD="${HEALTH_CMD}if command -v df >/dev/null 2>&1; then df -h / | tail -n 1; else echo 'df 命令不可用'; fi"
    
    # 本地节点直接执行
    if [ "${NODE_USER}" = "local" ]; then
      EXEC_OUTPUT="$(eval "${HEALTH_CMD}" 2>&1)"
      EXEC_EXIT=$?
      echo "${EXEC_OUTPUT}" | sed "s/^/[cluster][${var_host}] /"
      return ${EXEC_EXIT}
    else
      # 远程节点使用 ssh
      SSH_CMD="${SSH_CMD_BASE}"
      
      if [ -n "${var_ssh_password}" ]; then
        if ! command -v sshpass >/dev/null 2>&1; then
          warn "[cluster] ${var_host} -> 需要 sshpass 但未安装"
          return 1
        fi
        export SSHPASS="${var_ssh_password}"
        EXEC_OUTPUT="$(sshpass -e ${SSH_CMD} "${SSH_TARGET}" "${HEALTH_CMD}" 2>&1)"
        EXEC_EXIT=$?
        unset SSHPASS
      else
        EXEC_OUTPUT="$(${SSH_CMD} "${SSH_TARGET}" "${HEALTH_CMD}" 2>&1)"
        EXEC_EXIT=$?
      fi
      
      echo "${EXEC_OUTPUT}" | sed "s/^/[cluster][${var_host}] /"
      return ${EXEC_EXIT}
    fi
  fi
  
  # 其他命令：执行远程脚本
  # 1. 路径验证：确保远程目录和脚本存在
  REMOTE_DIR_ABS=""
  REMOTE_SCRIPT_PATH=""
  
  # 本地节点验证：使用 get_base_dir 而不是 REMOTE_DIR
  if [ "${NODE_USER}" = "local" ]; then
    # 本地节点直接使用当前基础目录
    REMOTE_DIR_ABS="$(get_base_dir)"
    if [ ! -d "${REMOTE_DIR_ABS}" ]; then
      warn "[cluster] ${var_host} -> 本地目录不存在: ${REMOTE_DIR_ABS}"
      return 1
    fi
    if [ ! -f "${REMOTE_DIR_ABS}/bin/${var_script_name}" ]; then
      warn "[cluster] ${var_host} -> 脚本不存在: ${REMOTE_DIR_ABS}/bin/${var_script_name}"
      return 1
    fi
    if [ ! -x "${REMOTE_DIR_ABS}/bin/${var_script_name}" ]; then
      warn "[cluster] ${var_host} -> 脚本不可执行: ${REMOTE_DIR_ABS}/bin/${var_script_name}"
      return 1
    fi
  else
    # 远程节点验证
    VALIDATE_CMD="if [ ! -d \"${REMOTE_DIR}\" ]; then echo \"ERROR: 远程目录不存在: ${REMOTE_DIR}\" >&2; exit 1; fi; "
    VALIDATE_CMD="${VALIDATE_CMD}REMOTE_DIR_ABS=\"\$(cd \"${REMOTE_DIR}\" && pwd)\"; "
    VALIDATE_CMD="${VALIDATE_CMD}if [ ! -f \"\${REMOTE_DIR_ABS}/bin/${var_script_name}\" ]; then echo \"ERROR: 脚本不存在: \${REMOTE_DIR_ABS}/bin/${var_script_name}\" >&2; exit 1; fi; "
    VALIDATE_CMD="${VALIDATE_CMD}if [ ! -x \"\${REMOTE_DIR_ABS}/bin/${var_script_name}\" ]; then echo \"ERROR: 脚本不可执行: \${REMOTE_DIR_ABS}/bin/${var_script_name}\" >&2; exit 1; fi; "
    VALIDATE_CMD="${VALIDATE_CMD}echo \"\${REMOTE_DIR_ABS}\""
    
    SSH_CMD_BASE="ssh -o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3"
    if [ "${NODE_PORT}" != "22" ]; then
      SSH_CMD_BASE="${SSH_CMD_BASE} -p ${NODE_PORT}"
    fi
    if [ -n "${NODE_USER}" ]; then
      SSH_TARGET="${NODE_USER}@${NODE_HOST}"
    else
      SSH_TARGET="${NODE_HOST}"
    fi
    
    # 执行验证（带重试）
    MAX_RETRIES=3
    RETRY_COUNT=0
    VALIDATE_RESULT=""
    VALIDATE_EXIT=1
    
    while [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; do
      if [ -n "${var_ssh_password}" ]; then
        if ! command -v sshpass >/dev/null 2>&1; then
          warn "[cluster] ${var_host} -> 需要 sshpass 但未安装"
          return 1
        fi
        export SSHPASS="${var_ssh_password}"
        VALIDATE_RESULT="$(sshpass -e ${SSH_CMD_BASE} "${SSH_TARGET}" "${VALIDATE_CMD}" 2>&1)"
        VALIDATE_EXIT=$?
        unset SSHPASS
      else
        VALIDATE_RESULT="$(${SSH_CMD_BASE} "${SSH_TARGET}" "${VALIDATE_CMD}" 2>&1)"
        VALIDATE_EXIT=$?
      fi
      
      if [ "${VALIDATE_EXIT}" -eq 0 ]; then
        REMOTE_DIR_ABS="${VALIDATE_RESULT}"
        break
      fi
      
      RETRY_COUNT=$((RETRY_COUNT + 1))
      if [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; then
        warn "[cluster] ${var_host} -> 路径验证失败，重试 ${RETRY_COUNT}/${MAX_RETRIES}..."
        sleep 2
      fi
    done
    
    if [ "${VALIDATE_EXIT}" -ne 0 ]; then
      warn "[cluster] ${var_host} -> 路径验证失败（已重试 ${MAX_RETRIES} 次）: ${VALIDATE_RESULT}"
      return 1
    fi
  fi
  
  # 2. 构建远程命令（使用验证后的绝对路径）
  REMOTE_CMD=""
  # 检查是否有 timeout 命令
  TIMEOUT_CMD=""
  if command -v timeout >/dev/null 2>&1; then
    TIMEOUT_CMD="timeout ${var_timeout}"
  fi
  
  # 设置环境变量（确保远程脚本能找到必需的环境）
  ENV_SETUP="export REMOTE_DIR=\"${REMOTE_DIR_ABS}\"; "
  ENV_SETUP="${ENV_SETUP}cd \"${REMOTE_DIR_ABS}\" || { echo \"ERROR: 无法切换到目录: ${REMOTE_DIR_ABS}\" >&2; exit 1; }; "
  
  # 参数转义：正确处理特殊字符
  ESCAPED_PACKAGE=""
  ESCAPED_ARGS=""
  if [ -n "${var_package}" ]; then
    # 转义包路径中的特殊字符
    ESCAPED_PACKAGE="$(printf '%s\n' "${var_package}" | sed "s/'/'\\\\''/g")"
    ESCAPED_PACKAGE="'${ESCAPED_PACKAGE}'"
  fi
  if [ -n "${var_pass_args}" ]; then
    # 转义参数中的特殊字符（保持参数结构）
    ESCAPED_ARGS="$(printf '%s\n' "${var_pass_args}" | sed "s/'/'\\\\''/g")"
    ESCAPED_ARGS="'${ESCAPED_ARGS}'"
  fi
  
  if [ "${var_command}" = "upgrade" ] || [ "${var_command}" = "rollback" ]; then
    if [ -z "${var_package}" ]; then
      warn "[cluster] ${var_host} -> ${var_command} 需要 -p 指定包路径"
      return 1
    fi
    if [ -n "${TIMEOUT_CMD}" ]; then
      REMOTE_CMD="${ENV_SETUP}${TIMEOUT_CMD} ./bin/${var_script_name} -p ${ESCAPED_PACKAGE}"
    else
      REMOTE_CMD="${ENV_SETUP}./bin/${var_script_name} -p ${ESCAPED_PACKAGE}"
    fi
    if [ -n "${ESCAPED_ARGS}" ]; then
      REMOTE_CMD="${REMOTE_CMD} ${ESCAPED_ARGS}"
    fi
  else
    if [ -n "${TIMEOUT_CMD}" ]; then
      REMOTE_CMD="${ENV_SETUP}${TIMEOUT_CMD} ./bin/${var_script_name}"
    else
      REMOTE_CMD="${ENV_SETUP}./bin/${var_script_name}"
    fi
    if [ -n "${ESCAPED_ARGS}" ]; then
      REMOTE_CMD="${REMOTE_CMD} ${ESCAPED_ARGS}"
    fi
  fi

  info "[cluster] ${var_host} -> ${var_command}"
  
  # 本地节点直接执行，远程节点使用 ssh
  if [ "${NODE_USER}" = "local" ]; then
    EXEC_OUTPUT="$(eval "${REMOTE_CMD}" 2>&1)"
    EXEC_EXIT=$?
    # 添加节点前缀到输出
    echo "${EXEC_OUTPUT}" | sed "s/^/[cluster][${var_host}] /"
    return ${EXEC_EXIT}
  else
    # 构建 ssh 命令（添加保活和重试）
    SSH_CMD="${SSH_CMD_BASE}"
    
    # 执行命令（带重试）
    MAX_RETRIES=3
    RETRY_COUNT=0
    EXEC_EXIT=1
    EXEC_OUTPUT=""
    
    while [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; do
      if [ -n "${var_ssh_password}" ]; then
        if ! command -v sshpass >/dev/null 2>&1; then
          warn "[cluster] ${var_host} -> 需要 sshpass 但未安装"
          return 1
        fi
        export SSHPASS="${var_ssh_password}"
        EXEC_OUTPUT="$(sshpass -e ${SSH_CMD} "${SSH_TARGET}" "${REMOTE_CMD}" 2>&1)"
        EXEC_EXIT=$?
        unset SSHPASS
      else
        EXEC_OUTPUT="$(${SSH_CMD} "${SSH_TARGET}" "${REMOTE_CMD}" 2>&1)"
        EXEC_EXIT=$?
      fi
      
      # 如果成功或非网络错误，立即返回
      if [ ${EXEC_EXIT} -eq 0 ] || [ ${EXEC_EXIT} -eq 255 ]; then
        break
      fi
      
      RETRY_COUNT=$((RETRY_COUNT + 1))
      if [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; then
        warn "[cluster] ${var_host} -> 执行失败（退出码: ${EXEC_EXIT}），重试 ${RETRY_COUNT}/${MAX_RETRIES}..."
        sleep 2
      fi
    done
    
    # 添加节点前缀到输出
    echo "${EXEC_OUTPUT}" | sed "s/^/[cluster][${var_host}] /"
    
    if [ ${EXEC_EXIT} -ne 0 ]; then
      warn "[cluster] ${var_host} -> 执行失败（退出码: ${EXEC_EXIT}，已重试 ${MAX_RETRIES} 次）"
      return ${EXEC_EXIT}
    fi
    
    return 0
  fi
}

# 收集所有节点
NODES=""
while IFS= read -r host || [ -n "${host}" ]; do
  # trim
  host="$(echo "${host}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  if [ -z "${host}" ]; then
    continue
  fi
  case "${host}" in
    \#*)
      continue
      ;;
  esac
  NODES="${NODES}${NODES:+ }${host}"
done < "${HOSTS_FILE}"

# 升级、回滚、启动、重启默认使用串行执行（逐个节点处理，减少风险）
if [ "${COMMAND}" = "upgrade" ] || [ "${COMMAND}" = "rollback" ] || [ "${COMMAND}" = "start" ] || [ "${COMMAND}" = "restart" ]; then
  if [ "${PARALLEL}" = "true" ]; then
    if [ "${COMMAND}" = "upgrade" ] || [ "${COMMAND}" = "rollback" ]; then
      warn "[cluster] 警告: ${COMMAND} 操作建议使用滚动模式（串行执行）"
      warn "[cluster] 使用 --parallel 可能导致服务中断，是否继续？(yes/no)"
      read -r CONFIRM_PARALLEL
      if [ "${CONFIRM_PARALLEL}" != "yes" ]; then
        info "[cluster] 已切换为滚动模式（串行执行）"
        PARALLEL="false"
      fi
    else
      warn "[cluster] 警告: ${COMMAND} 操作建议使用串行执行以减少风险"
      warn "[cluster] 使用 --parallel 可能导致服务不稳定，是否继续？(yes/no)"
      read -r CONFIRM_PARALLEL
      if [ "${CONFIRM_PARALLEL}" != "yes" ]; then
        info "[cluster] 已切换为串行执行模式"
        PARALLEL="false"
      fi
    fi
  fi
  
  if [ "${PARALLEL}" != "true" ]; then
    if [ "${COMMAND}" = "upgrade" ] || [ "${COMMAND}" = "rollback" ]; then
      info "[cluster] 滚动${COMMAND}模式: 逐个节点处理"
    else
      info "[cluster] 串行${COMMAND}模式: 逐个节点处理"
    fi
    NODE_INDEX=0
    TOTAL_NODES="$(echo "${NODES}" | wc -w)"
    
    for host in ${NODES}; do
      NODE_INDEX=$((NODE_INDEX + 1))
      info "[cluster] ========================================"
      info "[cluster] 处理节点 ${NODE_INDEX}/${TOTAL_NODES}: ${host}"
      info "[cluster] ========================================"
      
      if execute_remote_cmd "${host}" "${COMMAND}" "${SCRIPT_NAME}" "${PACKAGE}" "${PASS_ARGS}" "${SSH_PASSWORD}" "${TIMEOUT}"; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        info "[cluster] ${host} -> ${COMMAND} 成功"
        
        # 等待一段时间确保服务稳定（升级/回滚/启动/重启时）
        if [ "${COMMAND}" = "upgrade" ] || [ "${COMMAND}" = "rollback" ] || [ "${COMMAND}" = "start" ] || [ "${COMMAND}" = "restart" ]; then
          WAIT_TIME="${ROLLING_WAIT_TIME:-10}"
          if [ "${WAIT_TIME}" -gt 0 ] 2>/dev/null; then
            info "[cluster] 等待 ${WAIT_TIME} 秒确保服务稳定..."
            sleep "${WAIT_TIME}"
          fi
        fi
      else
        FAIL_COUNT=$((FAIL_COUNT + 1))
        warn "[cluster] ${host} -> ${COMMAND} 失败"
        
        # 失败时询问是否继续
        if [ "${SKIP_CONFIRM}" != "true" ]; then
          warn "[cluster] 节点 ${host} ${COMMAND} 失败，是否继续处理下一个节点？(yes/no)"
          read -r CONTINUE_ON_FAIL
          if [ "${CONTINUE_ON_FAIL}" != "yes" ]; then
            warn "[cluster] 已停止${COMMAND}操作"
            break
          fi
        fi
      fi
    done
  else
    # 并发执行（不推荐用于升级/回滚/启动/重启）
    info "[cluster] 并发执行模式（不推荐用于${COMMAND}）"
    PIDS=""
    RESULTS_DIR="/tmp/cluster_results_$$"
    mkdir -p "${RESULTS_DIR}"
    
    for host in ${NODES}; do
      (
        SAFE_HOST="$(echo "${host}" | sed 's/[^a-zA-Z0-9]/_/g')"
        RESULT_FILE="${RESULTS_DIR}/${SAFE_HOST}.result"
        STATUS_FILE="${RESULTS_DIR}/${SAFE_HOST}.status"
        if execute_remote_cmd "${host}" "${COMMAND}" "${SCRIPT_NAME}" "${PACKAGE}" "${PASS_ARGS}" "${SSH_PASSWORD}" "${TIMEOUT}" > "${RESULT_FILE}" 2>&1; then
          echo "0" > "${STATUS_FILE}"
        else
          echo "1" > "${STATUS_FILE}"
        fi
      ) &
      PIDS="${PIDS} ${!}"
    done
    
    for pid in ${PIDS}; do
      wait "${pid}" || true
    done
    
    for host in ${NODES}; do
      SAFE_HOST="$(echo "${host}" | sed 's/[^a-zA-Z0-9]/_/g')"
      RESULT_FILE="${RESULTS_DIR}/${SAFE_HOST}.result"
      STATUS_FILE="${RESULTS_DIR}/${SAFE_HOST}.status"
      if [ -f "${STATUS_FILE}" ]; then
        STATUS="$(cat "${STATUS_FILE}")"
        if [ "${STATUS}" = "0" ]; then
          SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
          info "[cluster] ${host} -> 成功"
        else
          FAIL_COUNT=$((FAIL_COUNT + 1))
          warn "[cluster] ${host} -> 失败"
          if [ -f "${RESULT_FILE}" ]; then
            cat "${RESULT_FILE}" | sed 's/^/[cluster]   /'
          fi
        fi
      else
        FAIL_COUNT=$((FAIL_COUNT + 1))
        warn "[cluster] ${host} -> 执行异常"
      fi
    done
    
    rm -rf "${RESULTS_DIR}"
  fi
else
  # 其他命令：并发或串行执行
  if [ "${PARALLEL}" = "true" ]; then
    info "[cluster] 并发执行模式"
    PIDS=""
    RESULTS_DIR="/tmp/cluster_results_$$"
    mkdir -p "${RESULTS_DIR}"
    
    for host in ${NODES}; do
      (
        SAFE_HOST="$(echo "${host}" | sed 's/[^a-zA-Z0-9]/_/g')"
        RESULT_FILE="${RESULTS_DIR}/${SAFE_HOST}.result"
        STATUS_FILE="${RESULTS_DIR}/${SAFE_HOST}.status"
        if execute_remote_cmd "${host}" "${COMMAND}" "${SCRIPT_NAME}" "${PACKAGE}" "${PASS_ARGS}" "${SSH_PASSWORD}" "${TIMEOUT}" > "${RESULT_FILE}" 2>&1; then
          echo "0" > "${STATUS_FILE}"
        else
          echo "1" > "${STATUS_FILE}"
        fi
      ) &
      PIDS="${PIDS} ${!}"
    done
    
    for pid in ${PIDS}; do
      wait "${pid}" || true
    done
    
    for host in ${NODES}; do
      SAFE_HOST="$(echo "${host}" | sed 's/[^a-zA-Z0-9]/_/g')"
      RESULT_FILE="${RESULTS_DIR}/${SAFE_HOST}.result"
      STATUS_FILE="${RESULTS_DIR}/${SAFE_HOST}.status"
      if [ -f "${STATUS_FILE}" ]; then
        STATUS="$(cat "${STATUS_FILE}")"
        if [ "${STATUS}" = "0" ]; then
          SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
          info "[cluster] ${host} -> 成功"
        else
          FAIL_COUNT=$((FAIL_COUNT + 1))
          warn "[cluster] ${host} -> 失败"
          if [ -f "${RESULT_FILE}" ]; then
            cat "${RESULT_FILE}" | sed 's/^/[cluster]   /'
          fi
        fi
      else
        FAIL_COUNT=$((FAIL_COUNT + 1))
        warn "[cluster] ${host} -> 执行异常"
      fi
    done
    
    rm -rf "${RESULTS_DIR}"
  else
    # 串行执行
    for host in ${NODES}; do
      if execute_remote_cmd "${host}" "${COMMAND}" "${SCRIPT_NAME}" "${PACKAGE}" "${PASS_ARGS}" "${SSH_PASSWORD}" "${TIMEOUT}"; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
      else
        FAIL_COUNT=$((FAIL_COUNT + 1))
      fi
    done
  fi
fi

# 显示汇总结果
echo ""
echo "=========================================="
echo "执行汇总"
echo "=========================================="
echo "成功: ${SUCCESS_COUNT}"
echo "失败: ${FAIL_COUNT}"
echo "总计: $((SUCCESS_COUNT + FAIL_COUNT))"
echo "=========================================="

if [ "${FAIL_COUNT}" -gt 0 ]; then
  exit 1
fi


