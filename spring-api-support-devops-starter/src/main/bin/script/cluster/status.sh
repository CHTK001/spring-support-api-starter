#!/bin/bash
# ============================================================
# 集群状态脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
# 默认刷新间隔
DEFAULT_REFRESH_INTERVAL=3

show_help() {
    echo -e "${BLUE}集群状态${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh status [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -p, --port <port>  查看指定实例的详细信息"
    echo "  --local            仅显示本地实例"
    echo "  --remote           仅显示远程节点"
    echo "  --node <name>      显示指定节点"
    echo "  -f                 实时监控模式"
    echo "  --interval <sec>   刷新间隔 (默认: ${DEFAULT_REFRESH_INTERVAL}秒)"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh status              查看集群状态"
    echo "  cluster.sh status -p 8080      查看端口 8080 详细信息"
    echo "  cluster.sh status -f           实时监控"
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
# 显示实例详细信息
# ===========================================
show_instance_detail() {
    local port="$1"
    local instance_name="${APP_NAME}-${port}"
    local pid_file="${APP_HOME}/${instance_name}.pid"
    local startup_log="${LOG_DIR}/startup-${port}.log"
    
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                实例详情 - ${instance_name}                    ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    if [ ! -f "$pid_file" ]; then
        echo -e "${CYAN}【基础信息】${NC}"
        echo -e "  实例名称:     ${WHITE}${instance_name}${NC}"
        echo -e "  端口:         ${WHITE}${port}${NC}"
        echo -e "  状态:         ${YELLOW}已停止${NC}"
        echo ""
        return
    fi
    
    local pid=$(cat "$pid_file" 2>/dev/null)
    if [ -z "$pid" ] || ! ps -p "$pid" > /dev/null 2>&1; then
        echo -e "${CYAN}【基础信息】${NC}"
        echo -e "  实例名称:     ${WHITE}${instance_name}${NC}"
        echo -e "  端口:         ${WHITE}${port}${NC}"
        echo -e "  状态:         ${YELLOW}已停止${NC}"
        echo ""
        return
    fi
    
    # 基础信息
    echo -e "${CYAN}【基础信息】${NC}"
    echo -e "  实例名称:     ${WHITE}${instance_name}${NC}"
    echo -e "  端口:         ${WHITE}${port}${NC}"
    echo -e "  状态:         ${GREEN}运行中${NC}"
    echo -e "  进程 ID:      ${WHITE}${pid}${NC}"
    
    # 运行时间
    local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
    [ -n "$start_time" ] && echo -e "  启动时间:     ${WHITE}${start_time}${NC}"
    
    local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
    [ -n "$etime" ] && echo -e "  运行时长:     ${WHITE}${etime}${NC}"
    
    # Spring环境
    local profile=$(get_spring_profile "$pid" "$port")
    echo -e "  Spring环境:   ${CYAN}${profile}${NC}"
    echo ""
    
    # 资源使用
    echo -e "${CYAN}【资源使用】${NC}"
    local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
    if [ -n "$mem" ]; then
        local mem_mb=$((mem / 1024))
        echo -e "  内存使用:     ${WHITE}${mem_mb} MB${NC}"
    fi
    
    local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
    [ -n "$cpu" ] && echo -e "  CPU 使用:     ${WHITE}${cpu}%${NC}"
    
    local threads=$(ps -o nlwp= -p "$pid" 2>/dev/null | xargs)
    [ -n "$threads" ] && echo -e "  线程数:       ${WHITE}${threads}${NC}"
    echo ""
    
    # JVM参数
    echo -e "${CYAN}【JVM 配置】${NC}"
    local cmdline=$(ps -p "$pid" -o args= 2>/dev/null)
    local heap_opts=$(echo "$cmdline" | grep -oE '(-Xms[0-9]+[mMgG]|-Xmx[0-9]+[mMgG])' | tr '\n' ' ')
    [ -n "$heap_opts" ] && echo -e "  堆内存:       ${WHITE}${heap_opts}${NC}"
    
    # 检查Skywalking
    if echo "$cmdline" | grep -q "skywalking-agent"; then
        echo -e "  Skywalking:   ${GREEN}已启用${NC}"
    fi
    
    # 检查调试模式
    local debug_port=$(echo "$cmdline" | grep -oP 'address=\*?:?\K[0-9]+' || echo "")
    if [ -n "$debug_port" ]; then
        echo -e "  调试端口:     ${YELLOW}${debug_port}${NC}"
    fi
    echo ""
    
    # 日志文件
    echo -e "${CYAN}【日志文件】${NC}"
    if [ -f "$startup_log" ]; then
        local log_size=$(du -h "$startup_log" 2>/dev/null | cut -f1)
        echo -e "  启动日志:     ${WHITE}${startup_log} (${log_size})${NC}"
    fi
    echo ""
    
    # 最近日志
    echo -e "${CYAN}【最近日志 (最后10行)】${NC}"
    if [ -f "$startup_log" ]; then
        tail -10 "$startup_log" | sed 's/^/  /'
    fi
    echo ""
}

# ===========================================
# 获取Spring环境
# ===========================================
get_spring_profile() {
    local pid="$1"
    local port="$2"
    local profile="-"
    
    # 方法1: 从进程命令行获取
    if [ -n "$pid" ]; then
        local cmdline=$(ps -p "$pid" -o args= 2>/dev/null)
        profile=$(echo "$cmdline" | grep -oP 'spring\.profiles\.active=\K[^\s]+' || echo "")
    fi
    
    # 方法2: 从actuator获取
    if [ -z "$profile" ] || [ "$profile" = "-" ]; then
        local health_url="http://127.0.0.1:${port}/actuator/info"
        local resp=$(curl -s --connect-timeout 1 --max-time 2 "$health_url" 2>/dev/null)
        if [ -n "$resp" ]; then
            profile=$(echo "$resp" | grep -oP '"active":\s*\[?"?\K[^"\]]+' || echo "")
        fi
    fi
    
    [ -z "$profile" ] && profile="-"
    echo "$profile"
}

# ===========================================
# 检查本地实例状态
# ===========================================
check_local_instance() {
    local port="$1"
    local instance_name="${APP_NAME}-${port}"
    local pid_file="${APP_HOME}/${instance_name}.pid"
    
    local status="stopped"
    local pid="-"
    local uptime="-"
    local memory="-"
    local profile="-"
    
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file" 2>/dev/null)
        if [ -n "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
            status="running"
            
            # 获取进程运行时间
            if command -v ps &> /dev/null; then
                uptime=$(ps -o etime= -p "$pid" 2>/dev/null | tr -d ' ')
            fi
            
            # 获取内存使用
            if command -v ps &> /dev/null; then
                memory=$(ps -o rss= -p "$pid" 2>/dev/null | tr -d ' ')
                if [ -n "$memory" ]; then
                    memory="$((memory / 1024))MB"
                fi
            fi
            
            # 获取Spring环境
            profile=$(get_spring_profile "$pid" "$port")
        fi
    fi
    
    if [ "$status" = "running" ]; then
        echo -e "  ${GREEN}●${NC} ${instance_name}  ${WHITE}PID:${NC} ${pid}  ${WHITE}端口:${NC} ${port}  ${WHITE}环境:${NC} ${CYAN}${profile}${NC}  ${WHITE}运行:${NC} ${uptime}  ${WHITE}内存:${NC} ${memory}"
    else
        echo -e "  ${RED}○${NC} ${instance_name}  ${YELLOW}已停止${NC}"
    fi
}

# ===========================================
# 检查远程节点状态
# ===========================================
check_remote_node() {
    local node_str="$1"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    # 构建 SSH 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10} -o BatchMode=yes"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    local remote_script=$(echo "$REMOTE_STARTUP_SCRIPT" | sed "s/\${APP_NAME}/${APP_NAME}/g" | sed "s|\${REMOTE_APP_HOME}|${remote_home}|g")
    
    # 尝试连接并获取状态
    local result=$(ssh $ssh_opts "${user}@${host}" "${remote_script} status" 2>/dev/null)
    local ssh_exit=$?
    
    if [ $ssh_exit -eq 0 ]; then
        if echo "$result" | grep -q "运行中\|running"; then
            local pid=$(echo "$result" | grep -oP 'PID: \K[0-9]+' || echo "-")
            echo -e "  ${GREEN}●${NC} ${alias}  ${WHITE}主机:${NC} ${host}  ${WHITE}端口:${NC} ${port}  ${WHITE}PID:${NC} ${pid}"
        else
            echo -e "  ${RED}○${NC} ${alias}  ${WHITE}主机:${NC} ${host}  ${WHITE}端口:${NC} ${port}  ${YELLOW}已停止${NC}"
        fi
    else
        echo -e "  ${YELLOW}?${NC} ${alias}  ${WHITE}主机:${NC} ${host}  ${WHITE}端口:${NC} ${port}  ${RED}连接失败${NC}"
    fi
}

# 渲染辅助函数（用于减少闪烁，先构建缓冲区）
get_local_instance_line() {
    # 捕获并返回本地实例的一行状态输出
    check_local_instance "$1"
}

get_remote_node_line() {
    # 捕获并返回远程节点的一行状态输出
    check_remote_node "$1"
}

# ===========================================
# 参数解析
# ===========================================
TARGET="all"
TARGET_NODE=""
TARGET_PORT=""
FOLLOW_MODE="false"
REFRESH_INTERVAL="${DEFAULT_REFRESH_INTERVAL}"

while [ $# -gt 0 ]; do
    case "$1" in
        -p|--port)
            TARGET_PORT="$2"
            shift 2
            ;;
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
        -f)
            FOLLOW_MODE="true"
            shift
            ;;
        --interval)
            REFRESH_INTERVAL="$2"
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

# 如果指定了端口，显示详细信息
if [ -n "$TARGET_PORT" ]; then
    show_instance_detail "$TARGET_PORT"
    exit 0
fi

# ===========================================
# 显示状态函数
# ===========================================
show_status() {
    local output=""
    
    output+="\n"
    output+="${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}\n"
    output+="${BLUE}║${NC}${GREEN}                     集群状态                                 ${NC}${BLUE}║${NC}\n"
    output+="${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}\n"
    output+="\n"
    output+="${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}\n"
    output+="${CYAN}│${NC} ${WHITE}集群名称${NC}    │ ${GREEN}${CLUSTER_NAME:-${APP_NAME}-cluster}${NC}\n"
    output+="${CYAN}│${NC} ${WHITE}应用名称${NC}    │ ${GREEN}${APP_NAME}${NC}\n"
    output+="${CYAN}│${NC} ${WHITE}本地端口${NC}    │ ${YELLOW}${LOCAL_PORTS:-<无>}${NC}\n"
    output+="${CYAN}│${NC} ${WHITE}远程节点${NC}    │ ${YELLOW}${#REMOTE_NODES[@]} 个${NC}\n"
    [ "${FOLLOW_MODE}" = "true" ] && output+="${CYAN}│${NC} ${WHITE}刷新时间${NC}    │ ${MAGENTA}$(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    output+="${CYAN}└─────────────────────────────────────────────────────────────┘${NC}\n"
    output+="\n"

    running_count=0
    stopped_count=0
    
    # 本地实例状态
    if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
        if [ -n "$LOCAL_PORTS" ]; then
            output+="${CYAN}本地实例:${NC}\n"
            IFS=',' read -ra ports <<< "$LOCAL_PORTS"
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [[ "$port" =~ ^[0-9]+$ ]]; then
                    output+="$(get_local_instance_line "$port")\n"
                fi
            done
            output+="\n"
        fi
    fi
    
    # 远程节点状态
    if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
        if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
            output+="${CYAN}远程节点:${NC}\n"
            for node in "${REMOTE_NODES[@]}"; do
                [ -z "$node" ] && continue
                output+="$(get_remote_node_line "$node")\n"
            done
            output+="\n"
        fi
    fi
    
    # 指定节点状态
    if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
        if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
            output+="${CYAN}本地实例:${NC}\n"
            output+="$(get_local_instance_line "$TARGET_NODE")\n"
        else
            output+="${CYAN}远程节点:${NC}\n"
            for node in "${REMOTE_NODES[@]}"; do
                [ -z "$node" ] && continue
                IFS='|' read -ra parts <<< "$node"
                if [ "${parts[0]}" = "$TARGET_NODE" ]; then
                    output+="$(get_remote_node_line "$node")\n"
                    break
                fi
            done
        fi
        output+="\n"
    fi
    
    output+="${CYAN}─────────────────────────────────────────────────────────────${NC}\n"
    output+="  ${GREEN}●${NC} 运行中  ${RED}○${NC} 已停止  ${YELLOW}?${NC} 未知\n"
    [ "${FOLLOW_MODE}" = "true" ] && output+="  ${WHITE}Ctrl+C 退出实时监控${NC}\n"
    output+="\n"
    
    # 输出内容
    if [ "${FOLLOW_MODE}" = "true" ]; then
        # 使用 tput 进行无闪烁更新
        tput home 2>/dev/null || printf "\033[H"
        echo -e "$output"
        tput ed 2>/dev/null || printf "\033[J"  # 清除剩余内容
    else
        echo -e "$output"
    fi
}

# ===========================================
# 执行状态显示
# ===========================================
if [ "${FOLLOW_MODE}" = "true" ]; then
    # 实时监控模式（使用备用屏幕并隐藏光标，减少闪烁）
    trap 'tput cnorm 2>/dev/null; tput rmcup 2>/dev/null; echo ""; echo -e "${YELLOW}退出实时监控${NC}"; exit 0' INT TERM
    tput smcup 2>/dev/null
    tput civis 2>/dev/null
    while true; do
        show_status
        sleep "${REFRESH_INTERVAL}"
    done
else
    # 单次显示
    show_status
fi
