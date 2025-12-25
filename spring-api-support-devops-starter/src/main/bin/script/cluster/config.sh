#!/bin/bash
# ============================================================
# 集群配置管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

CLUSTER_CONF="${APP_HOME}/cluster.conf"
CLUSTER_CONF_EXAMPLE="${SCRIPT_DIR}/../../cluster.conf.example"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群配置管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh config <命令> [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  --show             显示当前配置"
    echo "  --init             初始化配置文件"
    echo "  --add-local <port> 添加本地端口"
    echo "  --add-remote <node> 添加远程节点 (格式: 别名|主机|端口|用户)"
    echo "  --remove <name>    移除节点 (端口号或节点别名)"
    echo "  --edit             编辑配置文件"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh config --show                      显示配置"
    echo "  cluster.sh config --init                      初始化配置"
    echo "  cluster.sh config --add-local 8082            添加本地端口"
    echo "  cluster.sh config --add-remote 'node1|192.168.1.10|8080|root'"
    echo "  cluster.sh config --remove 8082               移除本地端口"
    echo "  cluster.sh config --remove node1              移除远程节点"
    echo ""
}

# ===========================================
# 显示配置
# ===========================================
show_config() {
    if [ ! -f "$CLUSTER_CONF" ]; then
        log_warn "配置文件不存在: ${CLUSTER_CONF}"
        echo ""
        echo -e "${YELLOW}使用以下命令初始化:${NC}"
        echo "  cluster.sh config --init"
        return 1
    fi
    
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                     集群配置                                 ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # 加载配置
    source "$CLUSTER_CONF"
    
    echo -e "${CYAN}基本信息:${NC}"
    echo -e "  ${WHITE}集群名称:${NC} ${CLUSTER_NAME:-<未设置>}"
    echo -e "  ${WHITE}配置文件:${NC} ${CLUSTER_CONF}"
    echo ""
    
    echo -e "${CYAN}本地实例:${NC}"
    if [ -n "$LOCAL_PORTS" ]; then
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            echo -e "  ${GREEN}●${NC} 端口 ${port}"
        done
    else
        echo -e "  ${YELLOW}未配置${NC}"
    fi
    echo ""
    
    echo -e "${CYAN}远程节点:${NC}"
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            IFS='|' read -ra parts <<< "$node"
            local alias="${parts[0]}"
            local host="${parts[1]}"
            local port="${parts[2]:-8080}"
            local user="${parts[3]:-${DEFAULT_SSH_USER}}"
            echo -e "  ${GREEN}●${NC} ${alias}  ${WHITE}主机:${NC} ${host}  ${WHITE}端口:${NC} ${port}  ${WHITE}用户:${NC} ${user}"
        done
    else
        echo -e "  ${YELLOW}未配置${NC}"
    fi
    echo ""
    
    echo -e "${CYAN}SSH 配置:${NC}"
    echo -e "  ${WHITE}默认用户:${NC} ${DEFAULT_SSH_USER:-root}"
    echo -e "  ${WHITE}默认端口:${NC} ${DEFAULT_SSH_PORT:-22}"
    echo -e "  ${WHITE}连接超时:${NC} ${SSH_TIMEOUT:-10}s"
    echo ""
    
    echo -e "${CYAN}升级配置:${NC}"
    echo -e "  ${WHITE}滚动间隔:${NC} ${ROLLING_UPGRADE_INTERVAL:-10}s"
    echo -e "  ${WHITE}自动备份:${NC} ${AUTO_BACKUP_BEFORE_UPGRADE:-true}"
    echo -e "  ${WHITE}健康检查:${NC} ${HEALTH_CHECK_PATH:-/actuator/health}"
    echo ""
}

# ===========================================
# 初始化配置
# ===========================================
init_config() {
    if [ -f "$CLUSTER_CONF" ]; then
        echo -e "${YELLOW}配置文件已存在: ${CLUSTER_CONF}${NC}"
        read -p "是否覆盖? [y/N] " confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            log_info "已取消"
            return 0
        fi
    fi
    
    if [ -f "$CLUSTER_CONF_EXAMPLE" ]; then
        cp "$CLUSTER_CONF_EXAMPLE" "$CLUSTER_CONF"
        log_info "配置文件已创建: ${CLUSTER_CONF}"
        echo ""
        echo -e "${YELLOW}请编辑配置文件:${NC}"
        echo "  vim ${CLUSTER_CONF}"
    else
        # 创建基本配置
        cat > "$CLUSTER_CONF" << EOF
#!/bin/bash
# 集群配置文件
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

CLUSTER_NAME="${APP_NAME}-cluster"
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
EOF
        log_info "配置文件已创建: ${CLUSTER_CONF}"
    fi
}

# ===========================================
# 添加本地端口
# ===========================================
add_local_port() {
    local new_port="$1"
    
    if ! [[ "$new_port" =~ ^[0-9]+$ ]]; then
        log_error "无效端口: $new_port"
        return 1
    fi
    
    # 加载现有配置
    [ -f "$CLUSTER_CONF" ] && source "$CLUSTER_CONF"
    
    # 检查是否已存在
    if [ -n "$LOCAL_PORTS" ]; then
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            if [ "$port" = "$new_port" ]; then
                log_warn "端口已存在: $new_port"
                return 0
            fi
        done
        LOCAL_PORTS="${LOCAL_PORTS},${new_port}"
    else
        LOCAL_PORTS="$new_port"
    fi
    
    # 保存配置
    save_config
    log_info "已添加本地端口: $new_port"
}

# ===========================================
# 添加远程节点
# ===========================================
add_remote_node() {
    local node_str="$1"
    
    # 验证格式
    IFS='|' read -ra parts <<< "$node_str"
    if [ ${#parts[@]} -lt 2 ]; then
        log_error "无效格式。正确格式: 别名|主机|端口|用户"
        return 1
    fi
    
    local alias="${parts[0]}"
    
    # 加载现有配置
    [ -f "$CLUSTER_CONF" ] && source "$CLUSTER_CONF"
    
    # 检查是否已存在
    for node in "${REMOTE_NODES[@]}"; do
        [ -z "$node" ] && continue
        IFS='|' read -ra existing <<< "$node"
        if [ "${existing[0]}" = "$alias" ]; then
            log_warn "节点已存在: $alias"
            return 0
        fi
    done
    
    # 添加节点
    REMOTE_NODES+=("$node_str")
    
    # 保存配置
    save_config
    log_info "已添加远程节点: $alias"
}

# ===========================================
# 移除节点
# ===========================================
remove_node() {
    local target="$1"
    
    # 加载现有配置
    [ -f "$CLUSTER_CONF" ] && source "$CLUSTER_CONF"
    
    local found="false"
    
    # 检查是否是本地端口
    if [[ "$target" =~ ^[0-9]+$ ]]; then
        if [ -n "$LOCAL_PORTS" ]; then
            local new_ports=""
            IFS=',' read -ra ports <<< "$LOCAL_PORTS"
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [ "$port" != "$target" ]; then
                    [ -n "$new_ports" ] && new_ports="${new_ports},"
                    new_ports="${new_ports}${port}"
                else
                    found="true"
                fi
            done
            LOCAL_PORTS="$new_ports"
        fi
    fi
    
    # 检查是否是远程节点
    if [ "$found" = "false" ]; then
        local new_nodes=()
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            IFS='|' read -ra parts <<< "$node"
            if [ "${parts[0]}" != "$target" ]; then
                new_nodes+=("$node")
            else
                found="true"
            fi
        done
        REMOTE_NODES=("${new_nodes[@]}")
    fi
    
    if [ "$found" = "true" ]; then
        save_config
        log_info "已移除节点: $target"
    else
        log_error "未找到节点: $target"
        return 1
    fi
}

# ===========================================
# 保存配置
# ===========================================
save_config() {
    cat > "$CLUSTER_CONF" << EOF
#!/bin/bash
# 集群配置文件 (自动生成)
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

CLUSTER_NAME="${CLUSTER_NAME:-${APP_NAME}-cluster}"
LOCAL_PORTS="${LOCAL_PORTS}"
REMOTE_NODES=(
EOF
    
    for node in "${REMOTE_NODES[@]}"; do
        [ -n "$node" ] && echo "    \"${node}\"" >> "$CLUSTER_CONF"
    done
    
    cat >> "$CLUSTER_CONF" << EOF
)

DEFAULT_SSH_USER="${DEFAULT_SSH_USER:-root}"
DEFAULT_SSH_PORT="${DEFAULT_SSH_PORT:-22}"
DEFAULT_SSH_KEY="${DEFAULT_SSH_KEY}"
SSH_TIMEOUT="${SSH_TIMEOUT:-10}"

REMOTE_APP_HOME="${REMOTE_APP_HOME:-/opt/apps/\${APP_NAME}}"
REMOTE_STARTUP_SCRIPT="${REMOTE_STARTUP_SCRIPT:-\${REMOTE_APP_HOME}/bin/startup.sh}"

ROLLING_UPGRADE_INTERVAL="${ROLLING_UPGRADE_INTERVAL:-10}"
AUTO_BACKUP_BEFORE_UPGRADE="${AUTO_BACKUP_BEFORE_UPGRADE:-true}"
HEALTH_CHECK_RETRIES="${HEALTH_CHECK_RETRIES:-30}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-2}"
HEALTH_CHECK_PATH="${HEALTH_CHECK_PATH:-/actuator/health}"
HEALTH_CHECK_PROTOCOL="${HEALTH_CHECK_PROTOCOL:-http}"

AGGREGATE_LOG_LINES="${AGGREGATE_LOG_LINES:-50}"
EOF
}

# ===========================================
# 参数解析
# ===========================================
if [ $# -eq 0 ]; then
    show_config
    exit 0
fi

case "$1" in
    --show)
        show_config
        ;;
    --init)
        init_config
        ;;
    --add-local)
        [ -z "$2" ] && { log_error "请指定端口"; exit 1; }
        add_local_port "$2"
        ;;
    --add-remote)
        [ -z "$2" ] && { log_error "请指定节点信息"; exit 1; }
        add_remote_node "$2"
        ;;
    --remove)
        [ -z "$2" ] && { log_error "请指定要移除的节点"; exit 1; }
        remove_node "$2"
        ;;
    --edit)
        if [ -f "$CLUSTER_CONF" ]; then
            ${EDITOR:-vim} "$CLUSTER_CONF"
        else
            log_error "配置文件不存在，请先初始化: cluster.sh config --init"
            exit 1
        fi
        ;;
    -h|--help)
        show_help
        ;;
    *)
        log_error "未知命令: $1"
        show_help
        exit 1
        ;;
esac
