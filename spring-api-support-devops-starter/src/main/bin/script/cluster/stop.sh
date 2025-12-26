#!/bin/bash
# ============================================================
# 集群停止脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群停止${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh stop [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --local            仅停止本地实例"
    echo "  --remote           仅停止远程节点"
    echo "  --node <name>      停止指定节点"
    echo "  --exclude <node>   排除指定节点 (可多次使用)"
    echo "  --force            强制停止 (kill -9)"
    echo "  --graceful-timeout <s>  优雅停机超时时间 (秒，默认: 30)"
    echo "  --parallel         并行停止 (默认顺序)"
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
# 停止本地实例
# ===========================================
stop_local_instance() {
    local port="$1"
    local force="$2"
    local instance_name="${APP_NAME}-${port}"
    local pid_file="${APP_HOME}/${instance_name}.pid"
    
    if [ ! -f "$pid_file" ]; then
        echo -e "  ${YELLOW}⊙${NC} ${instance_name} 未运行 (无 PID 文件)"
        return 0
    fi
    
    local pid=$(cat "$pid_file" 2>/dev/null)
    if [ -z "$pid" ] || ! ps -p "$pid" > /dev/null 2>&1; then
        rm -f "$pid_file"
        echo -e "  ${YELLOW}⊙${NC} ${instance_name} 未运行"
        return 0
    fi
    
    echo -e "  ${CYAN}→${NC} 停止 ${instance_name} (PID: ${pid})..."
    
    # 检查是否有权限停止该进程
    local proc_user=$(ps -o user= -p "$pid" 2>/dev/null | tr -d ' ')
    local current_user=$(whoami)
    
    if [ "$proc_user" != "$current_user" ] && [ "$current_user" != "root" ]; then
        echo -e "  ${RED}✗${NC} ${instance_name} 无法停止: 进程属于用户 '${proc_user}'，当前用户 '${current_user}' 无权限"
        echo -e "  ${YELLOW}提示:${NC} 请使用 sudo 或以 root 用户执行"
        return 1
    fi
    
    if [ "$force" = "true" ]; then
        if ! kill -9 "$pid" 2>&1; then
            echo -e "  ${RED}✗${NC} ${instance_name} 强制停止失败"
            return 1
        fi
    else
        if ! kill "$pid" 2>&1; then
            echo -e "  ${RED}✗${NC} ${instance_name} 发送停止信号失败"
            return 1
        fi
        
        # 等待进程退出
        local count=0
        while [ $count -lt $GRACEFUL_TIMEOUT ] && ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            count=$((count + 1))
        done
        
        # 如果还在运行，强制停止
        if ps -p "$pid" > /dev/null 2>&1; then
            if ! kill -9 "$pid" 2>&1; then
                echo -e "  ${RED}✗${NC} ${instance_name} 强制停止失败"
                return 1
            fi
        fi
    fi
    
    # 最终验证进程是否真的停止
    sleep 0.5
    if ps -p "$pid" > /dev/null 2>&1; then
        echo -e "  ${RED}✗${NC} ${instance_name} 停止失败: 进程仍在运行"
        return 1
    fi
    
    rm -f "$pid_file"
    echo -e "  ${GREEN}✓${NC} ${instance_name} 已停止"
    return 0
}

# ===========================================
# 停止远程节点
# ===========================================
stop_remote_node() {
    local node_str="$1"
    local force="$2"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    # 构建 SSH 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    local remote_script=$(echo "$REMOTE_STARTUP_SCRIPT" | sed "s/\${APP_NAME}/${APP_NAME}/g" | sed "s|\${REMOTE_APP_HOME}|${remote_home}|g")
    
    echo -e "  ${CYAN}→${NC} 停止 ${alias} (${host}:${port})..."
    
    # 执行远程停止
    if ssh $ssh_opts "${user}@${host}" "${remote_script} stop" 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} ${alias} 已停止"
        return 0
    else
        echo -e "  ${RED}✗${NC} ${alias} 停止失败"
        return 1
    fi
}

# ===========================================
# 检查是否在排除列表中
# ===========================================
is_excluded() {
    local node="$1"
    for excluded in "${EXCLUDE_NODES[@]}"; do
        [ "$node" = "$excluded" ] && return 0
    done
    return 1
}

# ===========================================
# 参数解析
# ===========================================
TARGET="all"  # all, local, remote, node
TARGET_NODE=""
FORCE="false"
GRACEFUL_TIMEOUT="30"
EXCLUDE_NODES=()
PARALLEL="false"

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
        --exclude)
            EXCLUDE_NODES+=("$2")
            shift 2
            ;;
        --force|-f)
            FORCE="true"
            shift
            ;;
        --graceful-timeout)
            GRACEFUL_TIMEOUT="$2"
            shift 2
            ;;
        --parallel)
            PARALLEL="true"
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
# 执行停止
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${YELLOW}                     集群停止                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

success_count=0
fail_count=0

# 停止本地实例
if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
    if [ -n "$LOCAL_PORTS" ]; then
        echo -e "${CYAN}本地实例:${NC}"
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        
        if [ "$PARALLEL" = "true" ]; then
            # 并行停止
            pids=()
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [[ "$port" =~ ^[0-9]+$ ]] && ! is_excluded "$port"; then
                    stop_local_instance "$port" "$FORCE" &
                    pids+=($!)
                fi
            done
            for pid in "${pids[@]}"; do
                if wait $pid; then
                    success_count=$((success_count + 1))
                else
                    fail_count=$((fail_count + 1))
                fi
            done
        else
            # 顺序停止
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [[ "$port" =~ ^[0-9]+$ ]]; then
                    if is_excluded "$port"; then
                        echo -e "  ${YELLOW}⊚${NC} ${APP_NAME}-${port} 已排除"
                        continue
                    fi
                    if stop_local_instance "$port" "$FORCE"; then
                        success_count=$((success_count + 1))
                    else
                        fail_count=$((fail_count + 1))
                    fi
                fi
            done
        fi
        echo ""
    fi
fi

# 停止远程节点
if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        echo -e "${CYAN}远程节点:${NC}"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            if stop_remote_node "$node" "$FORCE"; then
                success_count=$((success_count + 1))
            else
                fail_count=$((fail_count + 1))
            fi
        done
        echo ""
    fi
fi

# 停止指定节点
if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
    # 检查是否是本地端口
    if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
        if stop_local_instance "$TARGET_NODE" "$FORCE"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    else
        # 查找远程节点
        found="false"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            alias=$(parse_remote_node "$node" "alias")
            if [ "$alias" = "$TARGET_NODE" ]; then
                if stop_remote_node "$node" "$FORCE"; then
                    success_count=$((success_count + 1))
                else
                    fail_count=$((fail_count + 1))
                fi
                found="true"
                break
            fi
        done
        if [ "$found" = "false" ]; then
            log_error "未找到节点: $TARGET_NODE"
            fail_count=$((fail_count + 1))
        fi
    fi
fi

# 结果统计
echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}成功:${NC} ${success_count}  ${RED}失败:${NC} ${fail_count}"
echo ""

[ $fail_count -gt 0 ] && exit 1
exit 0
