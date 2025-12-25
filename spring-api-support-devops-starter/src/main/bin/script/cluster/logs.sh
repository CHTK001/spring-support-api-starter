#!/bin/bash
# ============================================================
# 集群日志管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群日志管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh logs [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -p, --port <port>  查看指定本地端口实例的日志"
    echo "  --node <name>      查看指定远程节点的日志"
    echo "  --all              聚合显示所有本地实例日志"
    echo "  --split            分屏显示所有实例日志 (需要 tmux)"
    echo "  --list             列出所有可用日志"
    echo "  -f                 实时跟踪日志"
    echo "  -n <lines>         显示最近 N 行 (默认: 100)"
    echo "  --filter <pattern> 按模式过滤"
    echo "  --startup          查看启动日志 (默认)"
    echo "  --app              查看应用日志"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh logs                       列出可用日志"
    echo "  cluster.sh logs -p 8080               查看端口 8080 实例日志"
    echo "  cluster.sh logs -p 8080 -f            实时跟踪"
    echo "  cluster.sh logs --all                 聚合所有本地日志"
    echo "  cluster.sh logs --all -f              实时聚合日志"
    echo "  cluster.sh logs --split               分屏实时查看所有实例"
    echo "  cluster.sh logs --node node1 -f       实时跟踪远程日志"
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
# 列出可用日志
# ===========================================
list_logs() {
    echo -e "${CYAN}可用日志:${NC}"
    echo ""
    
    # 本地实例
    if [ -n "$LOCAL_PORTS" ]; then
        echo -e "${WHITE}本地实例:${NC}"
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            local startup_log="${LOG_DIR}/startup-${port}.log"
            local app_log="${LOG_DIR}/${APP_NAME}-${port}.log"
            
            echo -e "  ${YELLOW}端口 ${port}:${NC}"
            if [ -f "$startup_log" ]; then
                local size=$(du -h "$startup_log" 2>/dev/null | cut -f1)
                echo -e "    启动日志: ${startup_log} (${size})"
            fi
            if [ -f "$app_log" ]; then
                local size=$(du -h "$app_log" 2>/dev/null | cut -f1)
                echo -e "    应用日志: ${app_log} (${size})"
            fi
        done
        echo ""
    fi
    
    # 远程节点
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        echo -e "${WHITE}远程节点:${NC}"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            local alias=$(parse_remote_node "$node" "alias")
            local host=$(parse_remote_node "$node" "host")
            echo -e "  ${YELLOW}${alias}${NC} (${host})"
            echo "    使用: cluster.sh logs --node ${alias}"
        done
        echo ""
    fi
}

# ===========================================
# 查看本地实例日志
# ===========================================
view_local_log() {
    local port="$1"
    local follow="$2"
    local lines="$3"
    local filter="$4"
    local log_type="$5"
    
    local log_file
    if [ "$log_type" = "app" ]; then
        log_file="${LOG_DIR}/${APP_NAME}-${port}.log"
    else
        log_file="${LOG_DIR}/startup-${port}.log"
    fi
    
    if [ ! -f "$log_file" ]; then
        log_warn "日志文件不存在: ${log_file}"
        return 1
    fi
    
    print_title "日志 - ${APP_NAME}-${port}"
    echo -e "${WHITE}文件:${NC} ${log_file}"
    echo ""
    
    if [ "$follow" = "true" ]; then
        echo -e "${YELLOW}实时跟踪日志... 按 Ctrl+C 退出${NC}"
        echo ""
        if [ -n "$filter" ]; then
            tail -f "$log_file" | grep --color=auto --line-buffered -E "$filter"
        else
            tail -f "$log_file"
        fi
    else
        if [ -n "$filter" ]; then
            tail -n "$lines" "$log_file" | grep --color=auto -E "$filter"
        else
            tail -n "$lines" "$log_file"
        fi
    fi
}

# ===========================================
# 聚合本地日志
# ===========================================
view_aggregate_logs() {
    local lines="$1"
    local filter="$2"
    local follow="$3"
    
    print_title "聚合日志"
    echo ""
    
    if [ -z "$LOCAL_PORTS" ]; then
        log_warn "未配置本地实例"
        return 1
    fi
    
    # 收集所有日志文件
    local log_files=()
    IFS=',' read -ra ports <<< "$LOCAL_PORTS"
    for port in "${ports[@]}"; do
        port=$(echo "$port" | tr -d ' ')
        local log_file="${LOG_DIR}/startup-${port}.log"
        [ -f "$log_file" ] && log_files+=("$log_file")
    done
    
    if [ ${#log_files[@]} -eq 0 ]; then
        log_warn "没有可用的日志文件"
        return 1
    fi
    
    # 实时跟踪模式
    if [ "$follow" = "true" ]; then
        echo -e "${YELLOW}实时聚合跟踪... 按 Ctrl+C 退出${NC}"
        echo ""
        if [ -n "$filter" ]; then
            tail -f "${log_files[@]}" 2>/dev/null | grep --color=auto --line-buffered -E "$filter"
        else
            tail -f "${log_files[@]}" 2>/dev/null
        fi
    else
        # 合并并排序日志（按时间戳）
        if [ -n "$filter" ]; then
            cat "${log_files[@]}" | sort | tail -n "$lines" | grep --color=auto -E "$filter"
        else
            cat "${log_files[@]}" | sort | tail -n "$lines"
        fi
    fi
}

# ===========================================
# 分屏显示日志 (tmux)
# ===========================================
view_split_logs() {
    local log_type="$1"
    
    # 检查 tmux
    if ! command -v tmux &> /dev/null; then
        log_error "tmux 未安装，无法使用分屏功能"
        echo "安装: sudo apt install tmux  或  sudo yum install tmux"
        return 1
    fi
    
    if [ -z "$LOCAL_PORTS" ]; then
        log_warn "未配置本地实例"
        return 1
    fi
    
    # 获取端口列表
    local ports_arr=()
    IFS=',' read -ra ports_arr <<< "$LOCAL_PORTS"
    local port_count=${#ports_arr[@]}
    
    if [ $port_count -eq 0 ]; then
        log_warn "没有可用的实例"
        return 1
    fi
    
    # 创建 tmux 会话
    local session_name="${APP_NAME}-logs"
    
    # 如果已在tmux中，使用新窗口
    if [ -n "$TMUX" ]; then
        log_info "在当前tmux中创建分屏..."
        
        # 第一个端口在当前pane
        local first_port=$(echo "${ports_arr[0]}" | tr -d ' ')
        local first_log="${LOG_DIR}/startup-${first_port}.log"
        
        # 为其他端口创建pane
        for ((i=1; i<port_count; i++)); do
            local port=$(echo "${ports_arr[$i]}" | tr -d ' ')
            local log_file="${LOG_DIR}/startup-${port}.log"
            if [ -f "$log_file" ]; then
                tmux split-window -v "tail -f '$log_file'"
                tmux select-layout tiled
            fi
        done
        
        # 最后在第一个pane运行
        if [ -f "$first_log" ]; then
            exec tail -f "$first_log"
        fi
    else
        # 创建新的tmux会话
        tmux kill-session -t "$session_name" 2>/dev/null
        
        # 第一个窗口
        local first_port=$(echo "${ports_arr[0]}" | tr -d ' ')
        local first_log="${LOG_DIR}/startup-${first_port}.log"
        
        if [ ! -f "$first_log" ]; then
            log_warn "日志文件不存在: $first_log"
            return 1
        fi
        
        tmux new-session -d -s "$session_name" "tail -f '$first_log'"
        
        # 添加其他pane
        for ((i=1; i<port_count; i++)); do
            local port=$(echo "${ports_arr[$i]}" | tr -d ' ')
            local log_file="${LOG_DIR}/startup-${port}.log"
            if [ -f "$log_file" ]; then
                tmux split-window -t "$session_name" -v "tail -f '$log_file'"
                tmux select-layout -t "$session_name" tiled
            fi
        done
        
        echo -e "${GREEN}分屏日志已启动${NC}"
        echo -e "  会话名: ${CYAN}${session_name}${NC}"
        echo -e "  连接: ${WHITE}tmux attach -t ${session_name}${NC}"
        echo -e "  退出: ${YELLOW}Ctrl+B, D${NC}"
        echo -e "  关闭: ${RED}tmux kill-session -t ${session_name}${NC}"
        echo ""
        
        # 直接连接
        tmux attach -t "$session_name"
    fi
}

# ===========================================
# 查看远程节点日志
# ===========================================
view_remote_log() {
    local node_str="$1"
    local follow="$2"
    local lines="$3"
    local filter="$4"
    local log_type="$5"
    
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
    
    print_title "远程日志 - ${alias} (${host})"
    echo ""
    
    # 构建远程命令参数
    local log_args=""
    [ "$log_type" = "app" ] && log_args="--app"
    [ -n "$filter" ] && log_args="$log_args --filter '$filter'"
    
    if [ "$follow" = "true" ]; then
        echo -e "${YELLOW}实时跟踪远程日志... 按 Ctrl+C 退出${NC}"
        echo ""
        ssh $ssh_opts "${user}@${host}" "${remote_script} logs -f $log_args"
    else
        ssh $ssh_opts "${user}@${host}" "${remote_script} logs $lines $log_args"
    fi
}

# ===========================================
# 参数解析
# ===========================================
TARGET_PORT=""
TARGET_NODE=""
AGGREGATE="false"
SPLIT_MODE="false"
LIST_ONLY="false"
FOLLOW="false"
LINES="100"
FILTER=""
LOG_TYPE="startup"

while [ $# -gt 0 ]; do
    case "$1" in
        -p|--port)
            TARGET_PORT="$2"
            shift 2
            ;;
        --node)
            TARGET_NODE="$2"
            shift 2
            ;;
        --all)
            AGGREGATE="true"
            shift
            ;;
        --split)
            SPLIT_MODE="true"
            shift
            ;;
        --list)
            LIST_ONLY="true"
            shift
            ;;
        -f)
            FOLLOW="true"
            shift
            ;;
        -n)
            LINES="$2"
            shift 2
            ;;
        --filter)
            FILTER="$2"
            shift 2
            ;;
        --startup)
            LOG_TYPE="startup"
            shift
            ;;
        --app)
            LOG_TYPE="app"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            # 检查是否是数字（行数）
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                LINES="$1"
                shift
            else
                log_error "未知参数: $1"
                show_help
                exit 1
            fi
            ;;
    esac
done

# ===========================================
# 执行
# ===========================================
echo ""

# 列出日志
if [ "$LIST_ONLY" = "true" ]; then
    list_logs
    exit 0
fi

# 如果没有指定目标，列出可用日志
if [ -z "$TARGET_PORT" ] && [ -z "$TARGET_NODE" ] && [ "$AGGREGATE" = "false" ]; then
    list_logs
    exit 0
fi

# 查看指定端口日志
if [ -n "$TARGET_PORT" ]; then
    view_local_log "$TARGET_PORT" "$FOLLOW" "$LINES" "$FILTER" "$LOG_TYPE"
    exit $?
fi

# 分屏日志
if [ "$SPLIT_MODE" = "true" ]; then
    view_split_logs "$LOG_TYPE"
    exit $?
fi

# 聚合日志
if [ "$AGGREGATE" = "true" ]; then
    view_aggregate_logs "$LINES" "$FILTER" "$FOLLOW"
    exit $?
fi

# 查看远程节点日志
if [ -n "$TARGET_NODE" ]; then
    # 查找远程节点
    found="false"
    for node in "${REMOTE_NODES[@]}"; do
        [ -z "$node" ] && continue
        alias=$(parse_remote_node "$node" "alias")
        if [ "$alias" = "$TARGET_NODE" ]; then
            view_remote_log "$node" "$FOLLOW" "$LINES" "$FILTER" "$LOG_TYPE"
            found="true"
            break
        fi
    done
    
    if [ "$found" = "false" ]; then
        log_error "未找到远程节点: $TARGET_NODE"
        exit 1
    fi
fi
