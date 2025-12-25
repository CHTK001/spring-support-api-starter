#!/bin/bash
# ============================================================
# 集群备份脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群备份${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh backup [选项]"
    echo ""
    echo -e "${YELLOW}说明:${NC}"
    echo "  备份集群中所有节点的应用文件"
    echo ""
    echo -e "${YELLOW}备份内容:${NC}"
    echo "  - JAR 文件 (app.jar)"
    echo "  - 配置目录 (config/)"
    echo "  - 依赖库 (lib/)"
    echo ""
    echo -e "${YELLOW}备份位置:${NC}"
    echo "  \${APP_HOME}/backup/YYYYmmdd_HHMMSS/"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --local            仅备份本地实例"
    echo "  --remote           仅备份远程节点"
    echo "  --node <name>      备份指定节点"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh backup                备份所有节点"
    echo "  cluster.sh backup --local        仅备份本地"
    echo "  cluster.sh backup --node node1   备份指定节点"
    echo ""
}

# ===========================================
# 解析远程节点
# ===========================================
parse_remote_node() {
    local node_str="$1"
    local field="$2"
    
    IFS='|' read -ra parts <<< "$node_str"
    
    case "$field" in
        alias)    echo "${parts[0]}" ;;
        host)     echo "${parts[1]}" ;;
        port)     echo "${parts[2]:-8080}" ;;
        user)     echo "${parts[3]:-${DEFAULT_SSH_USER}}" ;;
        ssh_port) echo "${parts[4]:-${DEFAULT_SSH_PORT}}" ;;
        ssh_key)  echo "${parts[5]:-${DEFAULT_SSH_KEY}}" ;;
    esac
}

# ===========================================
# 备份本地
# ===========================================
backup_local() {
    echo -e "${CYAN}备份本地应用...${NC}"
    
    local backup_dir="${APP_HOME}/backup"
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_name="${APP_NAME}_${timestamp}"
    
    mkdir -p "${backup_dir}"
    
    # 备份 JAR
    if [ -f "${APP_HOME}/${JAR_NAME}" ]; then
        cp "${APP_HOME}/${JAR_NAME}" "${backup_dir}/${backup_name}.jar"
        echo -e "  ${GREEN}✓${NC} JAR 已备份: ${backup_dir}/${backup_name}.jar"
    fi
    
    # 备份配置
    if [ -d "${CONFIG_DIR}" ] && [ "$(ls -A ${CONFIG_DIR})" ]; then
        tar -czf "${backup_dir}/${backup_name}_config.tar.gz" -C "${APP_HOME}" config 2>/dev/null
        echo -e "  ${GREEN}✓${NC} 配置已备份: ${backup_dir}/${backup_name}_config.tar.gz"
    fi
    
    echo ""
}

# ===========================================
# 备份远程节点
# ===========================================
backup_remote_node() {
    local node_str="$1"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    echo -e "${CYAN}备份远程节点: ${alias} (${host})${NC}"
    
    # 构建 SSH 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    local remote_script=$(echo "$REMOTE_STARTUP_SCRIPT" | sed "s/\${APP_NAME}/${APP_NAME}/g" | sed "s|\${REMOTE_APP_HOME}|${remote_home}|g")
    
    # 执行远程备份
    if ssh $ssh_opts "${user}@${host}" "${remote_script} backup" 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} ${alias} 备份完成"
        return 0
    else
        echo -e "  ${RED}✗${NC} ${alias} 备份失败"
        return 1
    fi
}

# ===========================================
# 参数解析
# ===========================================
TARGET="all"
TARGET_NODE=""

while [ $# -gt 0 ]; do
    case "$1" in
        --local)
            TARGET="local"
            shift
            ;;
        --remote)
            TARGET="remote"
            shift
            ;;
        --node)
            TARGET="node"
            TARGET_NODE="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# ===========================================
# 执行备份
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}                     集群备份                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

success_count=0
fail_count=0

# 备份本地
if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
    backup_local
    success_count=$((success_count + 1))
fi

# 备份远程节点
if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            if backup_remote_node "$node"; then
                success_count=$((success_count + 1))
            else
                fail_count=$((fail_count + 1))
            fi
        done
        echo ""
    fi
fi

# 备份指定节点
if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
    if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]] || [ "$TARGET_NODE" = "local" ]; then
        backup_local
        success_count=1
    else
        found="false"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            alias=$(parse_remote_node "$node" "alias")
            if [ "$alias" = "$TARGET_NODE" ]; then
                if backup_remote_node "$node"; then
                    success_count=1
                else
                    fail_count=1
                fi
                found="true"
                break
            fi
        done
        if [ "$found" = "false" ]; then
            log_error "未找到节点: $TARGET_NODE"
            exit 1
        fi
    fi
fi

# 结果
echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}成功:${NC} ${success_count}  ${RED}失败:${NC} ${fail_count}"
echo ""

[ $fail_count -gt 0 ] && exit 1
exit 0
