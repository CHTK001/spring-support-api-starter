#!/bin/bash
# ============================================================
# 集群管理脚本
# 版本: 1.0.0
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

# 集群配置文件
CLUSTER_CONF="${APP_HOME}/cluster.conf"
CLUSTER_CONF_EXAMPLE="${SCRIPT_DIR}/cluster.conf.example"
CLUSTER_SCRIPT_DIR="${SCRIPT_DIR}/script/cluster"

# ===========================================
# 默认配置
# ===========================================
CLUSTER_NAME=""
LOCAL_PORTS=""
REMOTE_NODES=()
DEFAULT_SSH_USER="root"
DEFAULT_SSH_PORT="22"
DEFAULT_SSH_KEY=""
SSH_TIMEOUT="10"
REMOTE_APP_HOME="/opt/apps/\${APP_NAME}"
REMOTE_STARTUP_SCRIPT="\${REMOTE_APP_HOME}/bin/startup.sh"
ROLLING_UPGRADE_INTERVAL="10"
AUTO_BACKUP_BEFORE_UPGRADE="true"
HEALTH_CHECK_RETRIES="30"
HEALTH_CHECK_INTERVAL="2"
HEALTH_CHECK_PATH="/actuator/health"
HEALTH_CHECK_PROTOCOL="http"
AGGREGATE_LOG_LINES="50"

# ===========================================
# 加载集群配置
# ===========================================
load_cluster_conf() {
    if [ -f "${CLUSTER_CONF}" ]; then
        source "${CLUSTER_CONF}"
        return 0
    fi
    return 1
}

# ===========================================
# 保存集群配置
# ===========================================
save_cluster_conf() {
    cat > "${CLUSTER_CONF}" << EOF
#!/bin/bash
# 集群配置文件 (自动生成)
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

# 集群名称
CLUSTER_NAME="${CLUSTER_NAME:-${APP_NAME}-cluster}"

# 本地实例端口
LOCAL_PORTS="${LOCAL_PORTS}"

# 远程节点
REMOTE_NODES=(
EOF
    
    for node in "${REMOTE_NODES[@]}"; do
        echo "    \"${node}\"" >> "${CLUSTER_CONF}"
    done
    
    cat >> "${CLUSTER_CONF}" << EOF
)

# SSH 配置
DEFAULT_SSH_USER="${DEFAULT_SSH_USER}"
DEFAULT_SSH_PORT="${DEFAULT_SSH_PORT}"
DEFAULT_SSH_KEY="${DEFAULT_SSH_KEY}"
SSH_TIMEOUT="${SSH_TIMEOUT}"

# 远程路径
REMOTE_APP_HOME="${REMOTE_APP_HOME}"
REMOTE_STARTUP_SCRIPT="${REMOTE_STARTUP_SCRIPT}"

# 升级配置
ROLLING_UPGRADE_INTERVAL="${ROLLING_UPGRADE_INTERVAL}"
AUTO_BACKUP_BEFORE_UPGRADE="${AUTO_BACKUP_BEFORE_UPGRADE}"
HEALTH_CHECK_RETRIES="${HEALTH_CHECK_RETRIES}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL}"

# 健康检查
HEALTH_CHECK_PATH="${HEALTH_CHECK_PATH}"
HEALTH_CHECK_PROTOCOL="${HEALTH_CHECK_PROTOCOL}"

# 日志
AGGREGATE_LOG_LINES="${AGGREGATE_LOG_LINES}"
EOF
    
    log_info "配置已保存: ${CLUSTER_CONF}"
}

# ===========================================
# 解析远程节点信息
# ===========================================
# 格式: "别名|主机|端口|SSH用户|SSH端口|SSH密钥"
parse_remote_node() {
    local node_str="$1"
    local field="$2"
    
    IFS='|' read -ra parts <<< "$node_str"
    
    case "$field" in
        alias)   echo "${parts[0]}" ;;
        host)    echo "${parts[1]}" ;;
        port)    echo "${parts[2]:-8080}" ;;
        user)    echo "${parts[3]:-${DEFAULT_SSH_USER}}" ;;
        ssh_port) echo "${parts[4]:-${DEFAULT_SSH_PORT}}" ;;
        ssh_key) echo "${parts[5]:-${DEFAULT_SSH_KEY}}" ;;
    esac
}

# ===========================================
# 获取所有节点列表
# ===========================================
get_all_nodes() {
    local nodes=()
    
    # 本地节点
    if [ -n "${LOCAL_PORTS}" ]; then
        IFS=',' read -ra ports <<< "${LOCAL_PORTS}"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            nodes+=("local:${port}")
        done
    fi
    
    # 远程节点
    for node in "${REMOTE_NODES[@]}"; do
        [ -n "$node" ] && nodes+=("remote:${node}")
    done
    
    printf '%s\n' "${nodes[@]}"
}

# ===========================================
# SSH 执行命令
# ===========================================
ssh_exec() {
    local host="$1"
    local user="$2"
    local port="$3"
    local key="$4"
    shift 4
    local cmd="$*"
    
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT}"
    [ -n "$key" ] && [ -f "$key" ] && ssh_opts="$ssh_opts -i $key"
    [ -n "$port" ] && ssh_opts="$ssh_opts -p $port"
    
    ssh $ssh_opts "${user}@${host}" "$cmd"
}

# ===========================================
# SCP 传输文件
# ===========================================
scp_upload() {
    local local_file="$1"
    local host="$2"
    local user="$3"
    local port="$4"
    local key="$5"
    local remote_path="$6"
    
    local scp_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT}"
    [ -n "$key" ] && [ -f "$key" ] && scp_opts="$scp_opts -i $key"
    [ -n "$port" ] && scp_opts="$scp_opts -P $port"
    
    scp $scp_opts "$local_file" "${user}@${host}:${remote_path}"
}

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                    集群管理脚本 v1.0                         ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}生命周期命令:${NC}"
    echo "  start              启动集群"
    echo "  stop               停止集群"
    echo "  restart            重启集群"
    echo "  status             查看集群状态"
    echo ""
    echo -e "${YELLOW}运维命令:${NC}"
    echo "  logs               日志管理"
    echo "  upgrade            滚动升级"
    echo "  deploy             远程部署"
    echo "  backup             集群备份"
    echo "  health             健康检查"
    echo "  config             配置管理"
    echo ""
    echo -e "${YELLOW}通用选项:${NC}"
    echo "  --local            仅操作本地实例"
    echo "  --remote           仅操作远程节点"
    echo "  --node <name>      操作指定节点"
    echo "  -h, --help         显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 start --ports 8080,8081,8082    启动本地集群"
    echo "  $0 stop                            停止所有实例"
    echo "  $0 restart --local                 仅重启本地实例"
    echo "  $0 status                          查看集群状态"
    echo "  $0 logs -p 8080                    查看端口 8080 实例日志"
    echo "  $0 logs --node node1 -f            实时跟踪远程节点日志"
    echo "  $0 upgrade app-2.0.jar             滚动升级"
    echo "  $0 config --show                   显示当前配置"
    echo ""
}

# ===========================================
# 检查配置
# ===========================================
check_cluster_conf() {
    if ! load_cluster_conf; then
        log_warn "集群配置文件不存在: ${CLUSTER_CONF}"
        echo ""
        echo -e "${YELLOW}请先配置集群:${NC}"
        echo "  1. 复制示例配置: cp ${CLUSTER_CONF_EXAMPLE} ${CLUSTER_CONF}"
        echo "  2. 编辑配置文件: vim ${CLUSTER_CONF}"
        echo "  3. 或使用命令行: $0 start --ports 8080,8081"
        echo ""
        return 1
    fi
    
    # 检查是否有任何节点配置
    if [ -z "${LOCAL_PORTS}" ] && [ ${#REMOTE_NODES[@]} -eq 0 ]; then
        log_error "未配置任何集群节点"
        echo "请在 ${CLUSTER_CONF} 中配置 LOCAL_PORTS 或 REMOTE_NODES"
        return 1
    fi
    
    return 0
}

# ===========================================
# 子命令路由
# ===========================================
route_subcommand() {
    local cmd="$1"
    shift
    
    local script="${CLUSTER_SCRIPT_DIR}/${cmd}.sh"
    if [ -f "$script" ]; then
        # 导出配置变量供子脚本使用
        export CLUSTER_CONF LOCAL_PORTS REMOTE_NODES
        export DEFAULT_SSH_USER DEFAULT_SSH_PORT DEFAULT_SSH_KEY SSH_TIMEOUT
        export REMOTE_APP_HOME REMOTE_STARTUP_SCRIPT
        export ROLLING_UPGRADE_INTERVAL AUTO_BACKUP_BEFORE_UPGRADE
        export HEALTH_CHECK_RETRIES HEALTH_CHECK_INTERVAL
        export HEALTH_CHECK_PATH HEALTH_CHECK_PROTOCOL
        export AGGREGATE_LOG_LINES CLUSTER_NAME
        
        bash "$script" "$@"
    else
        log_error "子命令 '${cmd}' 的脚本不存在: ${script}"
        return 1
    fi
}

# ===========================================
# 主入口
# ===========================================
COMMAND=""
SUBCOMMAND_ARGS=()

# 子命令列表
SUBCOMMANDS="start stop restart status logs upgrade deploy backup health config"

# 解析第一个参数作为命令
if [ $# -gt 0 ]; then
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            # 检查是否是有效的子命令
            for sub in $SUBCOMMANDS; do
                if [ "$1" = "$sub" ]; then
                    COMMAND="$1"
                    shift
                    SUBCOMMAND_ARGS=("$@")
                    break
                fi
            done
            
            if [ -z "$COMMAND" ]; then
                log_error "未知命令: $1"
                show_help
                exit 1
            fi
            ;;
    esac
else
    show_help
    exit 0
fi

# 加载配置（start 命令可以在无配置时运行）
if [ "$COMMAND" != "start" ] && [ "$COMMAND" != "config" ]; then
    check_cluster_conf || exit 1
else
    load_cluster_conf 2>/dev/null
fi

# 执行子命令
route_subcommand "$COMMAND" "${SUBCOMMAND_ARGS[@]}"
