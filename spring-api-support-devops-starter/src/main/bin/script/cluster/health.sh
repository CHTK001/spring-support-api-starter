#!/bin/bash
# ============================================================
# 集群健康检查脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群健康检查${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh health [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --local            仅检查本地实例"
    echo "  --remote           仅检查远程节点"
    echo "  --node <name>      检查指定节点"
    echo "  --verbose          显示详细信息"
    echo "  -h, --help         显示帮助"
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
# 健康检查
# ===========================================
check_health() {
    local host="$1"
    local port="$2"
    local name="$3"
    local verbose="$4"
    
    local path="${HEALTH_CHECK_PATH:-/actuator/health}"
    local protocol="${HEALTH_CHECK_PROTOCOL:-http}"
    local url="${protocol}://${host}:${port}${path}"
    
    local result
    local http_code
    local response
    
    # 发送请求
    response=$(curl -sf --connect-timeout 5 -w "\n%{http_code}" "$url" 2>/dev/null)
    local curl_exit=$?
    
    if [ $curl_exit -eq 0 ]; then
        http_code=$(echo "$response" | tail -1)
        local body=$(echo "$response" | head -n -1)
        
        if [ "$http_code" = "200" ]; then
            # 检查响应内容
            if echo "$body" | grep -qi '"status".*"UP"'; then
                echo -e "  ${GREEN}●${NC} ${name}  ${WHITE}状态:${NC} ${GREEN}健康${NC}  ${WHITE}端口:${NC} ${port}"
                if [ "$verbose" = "true" ]; then
                    echo -e "    ${WHITE}响应:${NC} ${body}"
                fi
                return 0
            else
                echo -e "  ${YELLOW}●${NC} ${name}  ${WHITE}状态:${NC} ${YELLOW}降级${NC}  ${WHITE}端口:${NC} ${port}"
                if [ "$verbose" = "true" ]; then
                    echo -e "    ${WHITE}响应:${NC} ${body}"
                fi
                return 1
            fi
        else
            echo -e "  ${RED}●${NC} ${name}  ${WHITE}状态:${NC} ${RED}异常${NC}  ${WHITE}端口:${NC} ${port}  ${WHITE}HTTP:${NC} ${http_code}"
            return 1
        fi
    else
        echo -e "  ${RED}○${NC} ${name}  ${WHITE}状态:${NC} ${RED}不可达${NC}  ${WHITE}端口:${NC} ${port}"
        return 1
    fi
}

# ===========================================
# 检查本地实例
# ===========================================
check_local_instance() {
    local port="$1"
    local verbose="$2"
    local instance_name="${APP_NAME}-${port}"
    
    check_health "localhost" "$port" "$instance_name" "$verbose"
}

# ===========================================
# 检查远程节点
# ===========================================
check_remote_node() {
    local node_str="$1"
    local verbose="$2"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    
    check_health "$host" "$port" "$alias" "$verbose"
}

# ===========================================
# 参数解析
# ===========================================
TARGET="all"
TARGET_NODE=""
VERBOSE="false"

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
        --verbose|-v)
            VERBOSE="true"
            shift
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
# 执行健康检查
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}                     健康检查                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}检查端点: ${HEALTH_CHECK_PROTOCOL:-http}://host:port${HEALTH_CHECK_PATH:-/actuator/health}${NC}"
echo ""

healthy_count=0
unhealthy_count=0

# 检查本地实例
if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
    if [ -n "$LOCAL_PORTS" ]; then
        echo -e "${CYAN}本地实例:${NC}"
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            if [[ "$port" =~ ^[0-9]+$ ]]; then
                if check_local_instance "$port" "$VERBOSE"; then
                    healthy_count=$((healthy_count + 1))
                else
                    unhealthy_count=$((unhealthy_count + 1))
                fi
            fi
        done
        echo ""
    fi
fi

# 检查远程节点
if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        echo -e "${CYAN}远程节点:${NC}"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            if check_remote_node "$node" "$VERBOSE"; then
                healthy_count=$((healthy_count + 1))
            else
                unhealthy_count=$((unhealthy_count + 1))
            fi
        done
        echo ""
    fi
fi

# 检查指定节点
if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
    if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
        if check_local_instance "$TARGET_NODE" "$VERBOSE"; then
            healthy_count=1
        else
            unhealthy_count=1
        fi
    else
        found="false"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            alias=$(parse_remote_node "$node" "alias")
            if [ "$alias" = "$TARGET_NODE" ]; then
                if check_remote_node "$node" "$VERBOSE"; then
                    healthy_count=1
                else
                    unhealthy_count=1
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
    echo ""
fi

# 结果
echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}●${NC} 健康: ${healthy_count}  ${RED}●${NC} 异常: ${unhealthy_count}"
echo ""

[ $unhealthy_count -gt 0 ] && exit 1
exit 0
