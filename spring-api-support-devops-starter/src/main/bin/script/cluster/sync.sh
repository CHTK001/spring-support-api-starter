#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# sync.sh 位于 bin/script/cluster，bin 在其上两级
BIN_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

BIN_DIR_OVERRIDE="${BIN_DIR}"
export BIN_DIR_OVERRIDE

load_env

usage() {
  cat <<EOF
用法:
  sync.sh -f hosts.txt [选项]
  sync.sh --node <节点地址> [选项]

说明:
  - 同步 master 目录结构和代码到 cluster node
  - 默认排除 backup 目录，可通过 --exclude 添加更多排除目录
  - 远程节点项目地址默认与 master 路径一致（保持相同目录结构）
  - 本地节点可通过 --path 指定项目路径（用于本地存在多个 node 的场景）
  - 节点地址格式支持:
    * host                    # 远程主机（默认端口 22）
    * host:port               # 远程主机（指定端口）
    * user@host               # 远程主机（指定用户，默认端口 22）
    * user@host:port          # 远程主机（指定用户和端口）
    * localhost 或 127.0.0.1  # 本地节点（直接执行，不使用 ssh）
  - hosts 文件格式:
    * 每行一个主机，空行/以 # 开头会忽略
    * 支持设置 master: # master: host 或 # master: user@host:port
    * 如果不设置 master，默认使用当前执行目录作为 master
  - 无参数时默认从 hosts 文件读取 master 和所有 node，将 master 同步到所有 node
  - SSH 认证方式（按优先级）:
    1. SSH 密钥免密登录（推荐）
    2. 密码登录（通过 -P 参数或 SYNC_SSH_PASSWORD 环境变量，需要安装 sshpass）

参数:
  -f 文件        hosts 文件路径（默认: <base>/bin/cluster_hosts.txt）
  --node 地址    单个节点地址（不使用 hosts 文件）
  --path 路径    本地节点项目路径（仅用于本地节点，远程节点自动使用当前 master 路径）
  --exclude 目录 排除目录（可多次指定，默认排除 backup）
  -P 密码        SSH 密码（用于密码登录，需要安装 sshpass）
  --timeout 秒   SSH 连接和执行超时（默认 30 秒）
  --dry-run      仅显示将要同步的内容，不实际执行
  -h, --help     显示帮助信息

示例:
  # 无参数：默认从 hosts 文件读取 master 和所有 node，将 master 同步到所有 node
  sync.sh

  # 同步到 hosts 文件中的所有节点
  sync.sh -f hosts.txt

  # 同步到单个节点
  sync.sh --node user@192.168.1.100:22

  # 同步到本地节点，指定项目路径
  sync.sh --node localhost --path /opt/app/node1

  # 排除多个目录
  sync.sh -f hosts.txt --exclude backup --exclude logs --exclude target

  # 使用密码登录
  sync.sh -f hosts.txt -P mypassword

  # 预览同步内容
  sync.sh -f hosts.txt --dry-run
EOF
}

HOSTS_FILE=""
NODE_ADDRESS=""
REMOTE_PATH=""
EXCLUDE_DIRS="backup"
SSH_PASSWORD="${SYNC_SSH_PASSWORD:-}"
TIMEOUT="${SYNC_TIMEOUT:-30}"
DRY_RUN="false"

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
    --node)
      NODE_ADDRESS="${2:-}"
      shift 2
      ;;
    --path)
      REMOTE_PATH="${2:-}"
      shift 2
      ;;
    --exclude)
      if [ -z "${EXCLUDE_DIRS}" ]; then
        EXCLUDE_DIRS="${2:-}"
      else
        EXCLUDE_DIRS="${EXCLUDE_DIRS} ${2:-}"
      fi
      shift 2
      ;;
    -P)
      SSH_PASSWORD="${2:-}"
      shift 2
      ;;
    --timeout)
      TIMEOUT="${2:-30}"
      shift 2
      ;;
    --dry-run)
      DRY_RUN="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      warn "[sync] 未知参数: $1"
      usage
      exit 1
      ;;
  esac
done

# 解析节点信息（复用 cluster.sh 的逻辑）
parse_node() {
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

# 从 master 同步到节点
sync_from_master_to_node() {
  var_master="${1:-}"
  var_node="${2:-}"
  var_excludes="${3:-}"
  var_ssh_password="${4:-}"
  var_timeout="${5:-30}"
  var_dry_run="${6:-false}"
  
  # 解析 master 信息
  MASTER_INFO="$(parse_node "${var_master}")"
  MASTER_USER="$(echo "${MASTER_INFO}" | sed -n '1p')"
  MASTER_HOST="$(echo "${MASTER_INFO}" | sed -n '2p')"
  MASTER_PORT="$(echo "${MASTER_INFO}" | sed -n '3p')"
  
  # 解析 node 信息
  NODE_INFO="$(parse_node "${var_node}")"
  NODE_USER="$(echo "${NODE_INFO}" | sed -n '1p')"
  NODE_HOST="$(echo "${NODE_INFO}" | sed -n '2p')"
  NODE_PORT="$(echo "${NODE_INFO}" | sed -n '3p')"
  
  # 获取 master 的基础目录
  if [ "${MASTER_USER}" = "local" ]; then
    # master 是本地
    MASTER_BASE_DIR="$(get_base_dir)"
  else
    # master 是远程，假设远程路径与本地路径结构一致（默认 /opt/app）
    # 可以通过环境变量 SYNC_MASTER_BASE_DIR 指定
    MASTER_BASE_DIR="${SYNC_MASTER_BASE_DIR:-/opt/app}"
    info "[sync] ${var_master} -> 使用远程 master 路径: ${MASTER_BASE_DIR}"
  fi
  
  # 确定 node 的目标路径（默认与 master 路径一致）
  NODE_TARGET_PATH="${MASTER_BASE_DIR}"
  
  # 同步逻辑
  if [ "${NODE_USER}" = "local" ]; then
    # node 是本地
    if [ ! -d "${NODE_TARGET_PATH}" ]; then
      warn "[sync] ${var_node} -> 本地目录不存在: ${NODE_TARGET_PATH}"
      return 1
    fi
    
    info "[sync] ${var_master} -> ${var_node} -> 同步到本地目录: ${NODE_TARGET_PATH}"
    
    # 从 master 同步到本地 node
    if [ "${MASTER_USER}" = "local" ]; then
      # master 和 node 都是本地，直接 rsync，添加实时进度显示
      RSYNC_CMD="rsync -av --delete --progress --human-readable"
      for exclude_dir in ${var_excludes}; do
        RSYNC_CMD="${RSYNC_CMD} --exclude=${exclude_dir}"
      done
      if [ "${var_dry_run}" = "true" ]; then
        RSYNC_CMD="${RSYNC_CMD} --dry-run"
      fi
      RSYNC_CMD="${RSYNC_CMD} \"${MASTER_BASE_DIR}/\" \"${NODE_TARGET_PATH}/\""
      
      if command -v stdbuf >/dev/null 2>&1; then
        if eval "stdbuf -oL -eL ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_node}] /"; then
          info "[sync] ${var_node} -> 同步成功"
          return 0
        else
          warn "[sync] ${var_node} -> 同步失败"
          return 1
        fi
      else
        if eval "${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_node}] /"; then
          info "[sync] ${var_node} -> 同步成功"
          return 0
        else
          warn "[sync] ${var_node} -> 同步失败"
          return 1
        fi
      fi
    else
      # master 是远程，node 是本地，使用 rsync over SSH
      SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3"
      if [ "${MASTER_PORT}" != "22" ]; then
        SSH_OPTS="${SSH_OPTS} -p ${MASTER_PORT}"
      fi
      if [ -n "${MASTER_USER}" ]; then
        MASTER_SOURCE="${MASTER_USER}@${MASTER_HOST}:${MASTER_BASE_DIR}/"
      else
        MASTER_SOURCE="${MASTER_HOST}:${MASTER_BASE_DIR}/"
      fi
      
      RSYNC_CMD="rsync -av --delete --progress --human-readable"
      for exclude_dir in ${var_excludes}; do
        RSYNC_CMD="${RSYNC_CMD} --exclude=${exclude_dir}"
      done
      RSYNC_CMD="${RSYNC_CMD} -e \"ssh ${SSH_OPTS}\""
      if [ "${var_dry_run}" = "true" ]; then
        RSYNC_CMD="${RSYNC_CMD} --dry-run"
      fi
      RSYNC_CMD="${RSYNC_CMD} \"${MASTER_SOURCE}\" \"${NODE_TARGET_PATH}/\""
      
      SYNC_EXIT=0
      if [ -n "${var_ssh_password}" ]; then
        export SSHPASS="${var_ssh_password}"
        if command -v stdbuf >/dev/null 2>&1; then
          if eval "stdbuf -oL -eL sshpass -e ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        else
          if eval "sshpass -e ${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        fi
        unset SSHPASS
      else
        if command -v stdbuf >/dev/null 2>&1; then
          if eval "stdbuf -oL -eL ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        else
          if eval "${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        fi
      fi
      
      if [ "${SYNC_EXIT}" -eq 0 ]; then
        info "[sync] ${var_node} -> 同步成功"
        return 0
      else
        warn "[sync] ${var_node} -> 同步失败"
        return 1
      fi
    fi
  else
    # node 是远程
    info "[sync] ${var_master} -> ${var_node} -> 同步到远程目录: ${NODE_TARGET_PATH}"
    
    # 构建 node SSH 选项
    NODE_SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3"
    if [ "${NODE_PORT}" != "22" ]; then
      NODE_SSH_OPTS="${NODE_SSH_OPTS} -p ${NODE_PORT}"
    fi
    
    # 构建 node 目标地址
    if [ -n "${NODE_USER}" ]; then
      NODE_TARGET="${NODE_USER}@${NODE_HOST}:${NODE_TARGET_PATH}"
    else
      NODE_TARGET="${NODE_HOST}:${NODE_TARGET_PATH}"
    fi
    
    # 从 master 同步到远程 node
    if [ "${MASTER_USER}" = "local" ]; then
      # master 是本地，node 是远程，添加实时进度显示
      RSYNC_CMD="rsync -av --delete --progress --human-readable"
      for exclude_dir in ${var_excludes}; do
        RSYNC_CMD="${RSYNC_CMD} --exclude=${exclude_dir}"
      done
      RSYNC_CMD="${RSYNC_CMD} -e \"ssh ${NODE_SSH_OPTS}\""
      if [ "${var_dry_run}" = "true" ]; then
        RSYNC_CMD="${RSYNC_CMD} --dry-run"
      fi
      RSYNC_CMD="${RSYNC_CMD} \"${MASTER_BASE_DIR}/\" \"${NODE_TARGET}/\""
      
      SYNC_EXIT=0
      if [ -n "${var_ssh_password}" ]; then
        export SSHPASS="${var_ssh_password}"
        if command -v stdbuf >/dev/null 2>&1; then
          if eval "stdbuf -oL -eL sshpass -e ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        else
          if eval "sshpass -e ${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        fi
        unset SSHPASS
      else
        if command -v stdbuf >/dev/null 2>&1; then
          if eval "stdbuf -oL -eL ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        else
          if eval "${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_node}] /"; then
            SYNC_EXIT=0
          else
            SYNC_EXIT=1
          fi
        fi
      fi
    else
      # master 和 node 都是远程，需要先同步到本地临时目录，再同步到 node
      # 或者使用 rsync 的 --from0 和 --to0 选项，但更简单的是使用本地中转
      warn "[sync] 暂不支持两个远程节点之间的直接同步"
      warn "[sync] 建议：先将 master 同步到本地，再同步到 node"
      return 1
    fi
    
    if [ "${SYNC_EXIT}" -eq 0 ]; then
      info "[sync] ${var_node} -> 同步成功"
      return 0
    else
      warn "[sync] ${var_node} -> 同步失败"
      return 1
    fi
  fi
}


# 同步到单个节点
sync_to_node() {
  var_host="${1:-}"
  var_remote_path="${2:-}"
  var_excludes="${3:-}"
  var_ssh_password="${4:-}"
  var_timeout="${5:-30}"
  var_dry_run="${6:-false}"
  
  # 解析节点信息
  NODE_INFO="$(parse_node "${var_host}")"
  NODE_USER="$(echo "${NODE_INFO}" | sed -n '1p')"
  NODE_HOST="$(echo "${NODE_INFO}" | sed -n '2p')"
  NODE_PORT="$(echo "${NODE_INFO}" | sed -n '3p')"
  
  # 获取当前 master 的基础目录
  LOCAL_BASE_DIR="$(get_base_dir)"
  
  # 确定远程路径
  REMOTE_TARGET_PATH=""
  if [ "${NODE_USER}" = "local" ]; then
    # 本地节点
    if [ -n "${var_remote_path}" ]; then
      REMOTE_TARGET_PATH="${var_remote_path}"
    else
      REMOTE_TARGET_PATH="${LOCAL_BASE_DIR}"
    fi
    
    if [ ! -d "${REMOTE_TARGET_PATH}" ]; then
      warn "[sync] ${var_host} -> 本地目录不存在: ${REMOTE_TARGET_PATH}"
      return 1
    fi
    
    info "[sync] ${var_host} -> 同步到本地目录: ${REMOTE_TARGET_PATH}"
    
    # 本地同步使用 rsync，添加实时进度显示
    RSYNC_CMD="rsync -av --delete --progress --human-readable"
    
    # 添加排除参数
    for exclude_dir in ${var_excludes}; do
      RSYNC_CMD="${RSYNC_CMD} --exclude=${exclude_dir}"
    done
    
    if [ "${var_dry_run}" = "true" ]; then
      RSYNC_CMD="${RSYNC_CMD} --dry-run"
      info "[sync] ${var_host} -> 预览模式"
    fi
    
    RSYNC_CMD="${RSYNC_CMD} \"${LOCAL_BASE_DIR}/\" \"${REMOTE_TARGET_PATH}/\""
    
    # 使用 stdbuf 确保实时输出，类似 docker pull 的效果
    if command -v stdbuf >/dev/null 2>&1; then
      if eval "stdbuf -oL -eL ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_host}] /"; then
        info "[sync] ${var_host} -> 同步成功"
        return 0
      else
        warn "[sync] ${var_host} -> 同步失败"
        return 1
      fi
    else
      # 如果没有 stdbuf，使用 unbuffered 模式
      if eval "${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_host}] /"; then
        info "[sync] ${var_host} -> 同步成功"
        return 0
      else
        warn "[sync] ${var_host} -> 同步失败"
        return 1
      fi
    fi
  else
    # 远程节点
    if [ -n "${var_remote_path}" ]; then
      REMOTE_TARGET_PATH="${var_remote_path}"
    else
      # 远程默认使用当前 master 的绝对路径（保持路径结构一致）
      REMOTE_TARGET_PATH="${LOCAL_BASE_DIR}"
    fi
    
    info "[sync] ${var_host} -> 同步到远程目录: ${REMOTE_TARGET_PATH}"
    
    # 构建 SSH 选项
    SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=${var_timeout} -o ServerAliveInterval=10 -o ServerAliveCountMax=3"
    if [ "${NODE_PORT}" != "22" ]; then
      SSH_OPTS="${SSH_OPTS} -p ${NODE_PORT}"
    fi
    
    # 构建远程目标地址
    if [ -n "${NODE_USER}" ]; then
      REMOTE_TARGET="${NODE_USER}@${NODE_HOST}:${REMOTE_TARGET_PATH}"
    else
      REMOTE_TARGET="${NODE_HOST}:${REMOTE_TARGET_PATH}"
    fi
    
    # 使用 rsync over SSH，添加实时进度显示
    RSYNC_CMD="rsync -av --delete --progress --human-readable"
    
    # 添加排除参数
    for exclude_dir in ${var_excludes}; do
      RSYNC_CMD="${RSYNC_CMD} --exclude=${exclude_dir}"
    done
    
    RSYNC_CMD="${RSYNC_CMD} -e \"ssh ${SSH_OPTS}\""
    
    if [ "${var_dry_run}" = "true" ]; then
      RSYNC_CMD="${RSYNC_CMD} --dry-run"
      info "[sync] ${var_host} -> 预览模式"
    fi
    
    RSYNC_CMD="${RSYNC_CMD} \"${LOCAL_BASE_DIR}/\" \"${REMOTE_TARGET}/\""
    
    # 执行同步，使用 stdbuf 确保实时输出
    SYNC_EXIT=0
    if [ -n "${var_ssh_password}" ]; then
      if ! command -v sshpass >/dev/null 2>&1; then
        warn "[sync] ${var_host} -> 需要 sshpass 但未安装"
        return 1
      fi
      export SSHPASS="${var_ssh_password}"
      if command -v stdbuf >/dev/null 2>&1; then
        if eval "stdbuf -oL -eL sshpass -e ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_host}] /"; then
          SYNC_EXIT=0
        else
          SYNC_EXIT=1
        fi
      else
        if eval "sshpass -e ${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_host}] /"; then
          SYNC_EXIT=0
        else
          SYNC_EXIT=1
        fi
      fi
      unset SSHPASS
    else
      if command -v stdbuf >/dev/null 2>&1; then
        if eval "stdbuf -oL -eL ${RSYNC_CMD}" 2>&1 | stdbuf -oL -eL sed "s/^/[sync][${var_host}] /"; then
          SYNC_EXIT=0
        else
          SYNC_EXIT=1
        fi
      else
        if eval "${RSYNC_CMD}" 2>&1 | sed -u "s/^/[sync][${var_host}] /"; then
          SYNC_EXIT=0
        else
          SYNC_EXIT=1
        fi
      fi
    fi
    
    if [ "${SYNC_EXIT}" -eq 0 ]; then
      info "[sync] ${var_host} -> 同步成功"
      return 0
    else
      warn "[sync] ${var_host} -> 同步失败"
      return 1
    fi
  fi
}

# 从 hosts 文件读取 master 和节点列表
read_hosts_file() {
  var_hosts_file="${1:-}"
  var_master_var="${2:-}"  # 输出变量名：MASTER_ADDRESS
  var_nodes_var="${3:-}"    # 输出变量名：NODE_LIST
  
  MASTER_FOUND=""
  NODES_FOUND=""
  
  if [ ! -f "${var_hosts_file}" ]; then
    return 1
  fi
  
  while IFS= read -r line || [ -n "${line}" ]; do
    line_trimmed="$(echo "${line}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    if [ -z "${line_trimmed}" ]; then
      continue
    fi
    
    # 检查是否是 master 配置: # master: host 或 #master: host
    if echo "${line_trimmed}" | grep -qiE "^#\s*master\s*:"; then
      MASTER_FOUND="$(echo "${line_trimmed}" | sed -E 's/^#\s*master\s*:\s*//i' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
      continue
    fi
    
    # 跳过注释行
    case "${line_trimmed}" in
      \#*)
        continue
        ;;
    esac
    
    # 普通节点
    if [ -z "${NODES_FOUND}" ]; then
      NODES_FOUND="${line_trimmed}"
    else
      NODES_FOUND="${NODES_FOUND}
${line_trimmed}"
    fi
  done < "${var_hosts_file}"
  
  # 设置输出变量
  eval "${var_master_var}=\"${MASTER_FOUND}\""
  eval "${var_nodes_var}=\"${NODES_FOUND}\""
  return 0
}

# 检查是否指定了节点或 hosts 文件
if [ -z "${NODE_ADDRESS}" ] && [ -z "${HOSTS_FILE}" ]; then
  HOSTS_FILE="${DEFAULT_HOSTS_FILE}"
fi

# 如果指定了单个节点，使用当前目录作为 master 同步到该节点
if [ -n "${NODE_ADDRESS}" ]; then
  info "[sync] 从当前目录同步到节点: ${NODE_ADDRESS}"
  if sync_to_node "${NODE_ADDRESS}" "${REMOTE_PATH}" "${EXCLUDE_DIRS}" "${SSH_PASSWORD}" "${TIMEOUT}" "${DRY_RUN}"; then
    info "[sync] 同步完成"
    exit 0
  else
    warn "[sync] 同步失败"
    exit 1
  fi
fi

# 从 hosts 文件读取 master 和节点列表
if [ ! -f "${HOSTS_FILE}" ]; then
  warn "[sync] hosts 文件不存在: ${HOSTS_FILE}"
  warn "[sync] 使用 --node 指定单个节点，或使用 -f 指定 hosts 文件"
  exit 1
fi

info "[sync] 从 hosts 文件读取配置: ${HOSTS_FILE}"

# 读取 hosts 文件
read_hosts_file "${HOSTS_FILE}" "MASTER_ADDRESS" "NODE_LIST"

# 确定 master
if [ -z "${MASTER_ADDRESS}" ]; then
  # 没有配置 master，使用当前目录作为 master
  MASTER_ADDRESS="localhost"
  info "[sync] 未配置 master，使用当前目录作为 master"
else
  info "[sync] Master: ${MASTER_ADDRESS}"
fi

# 统计节点数量
TOTAL_NODES=0
if [ -n "${NODE_LIST}" ]; then
  while IFS= read -r node || [ -n "${node}" ]; do
    node_trimmed="$(echo "${node}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    if [ -n "${node_trimmed}" ]; then
      TOTAL_NODES=$((TOTAL_NODES + 1))
    fi
  done <<EOF
${NODE_LIST}
EOF
fi

if [ "${TOTAL_NODES}" -eq 0 ]; then
  warn "[sync] hosts 文件中没有有效节点"
  exit 1
fi

info "[sync] 排除目录: ${EXCLUDE_DIRS}"
if [ "${DRY_RUN}" = "true" ]; then
  info "[sync] 预览模式，不会实际同步"
fi

info "[sync] 共 ${TOTAL_NODES} 个节点，开始从 master 同步..."

SUCCESS_COUNT=0
FAIL_COUNT=0
NODE_INDEX=0

# 逐个同步节点
while IFS= read -r node || [ -n "${node}" ]; do
  node_trimmed="$(echo "${node}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  if [ -z "${node_trimmed}" ]; then
    continue
  fi
  
  # 如果 node 是 master，跳过
  if [ "${node_trimmed}" = "${MASTER_ADDRESS}" ]; then
    info "[sync] 跳过 master 节点: ${node_trimmed}"
    continue
  fi
  
  NODE_INDEX=$((NODE_INDEX + 1))
  info "[sync] ========================================"
  info "[sync] 处理节点 ${NODE_INDEX}/${TOTAL_NODES}: ${node_trimmed}"
  info "[sync] ========================================"
  
  if sync_from_master_to_node "${MASTER_ADDRESS}" "${node_trimmed}" "${EXCLUDE_DIRS}" "${SSH_PASSWORD}" "${TIMEOUT}" "${DRY_RUN}"; then
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    info "[sync] ${node_trimmed} -> 同步成功"
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    warn "[sync] ${node_trimmed} -> 同步失败"
  fi
done <<EOF
${NODE_LIST}
EOF

# 输出统计信息
info "[sync] ========================================"
info "[sync] 同步完成"
info "[sync] 成功: ${SUCCESS_COUNT}, 失败: ${FAIL_COUNT}, 总计: ${TOTAL_NODES}"
info "[sync] ========================================"

if [ "${FAIL_COUNT}" -gt 0 ]; then
  exit 1
fi

exit 0

