#!/bin/bash
# ============================================================
# 应用信息显示脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}应用信息显示${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  (无参数)    显示基本信息"
    echo "  -a          显示详细信息"
    echo "  -c          显示 CPU 信息"
    echo "  -m          显示内存信息"
    echo "  -f          实时刷新 (每 2 秒)"
    echo "  -h, --help  显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0              查看基本信息"
    echo "  $0 -a           查看详细信息"
    echo "  $0 -c           查看 CPU 信息"
    echo "  $0 -m           查看内存信息"
    echo "  $0 -f           实时刷新信息"
    echo ""
}

# ===========================================
# 基本信息
# ===========================================
show_basic_info() {
    local pid=$(get_pid)
    
    print_title "应用信息"
    echo ""
    echo -e "${CYAN}【基础信息】${NC}"
    echo -e "  应用名称:     ${WHITE}${APP_NAME}${NC}"
    echo -e "  应用版本:     ${WHITE}${APP_VERSION}${NC}"
    echo -e "  JAR 文件:     ${WHITE}${JAR_NAME}${NC}"
    echo -e "  应用目录:     ${WHITE}${APP_HOME}${NC}"
    echo -e "  配置目录:     ${WHITE}${CONFIG_DIR}${NC}"
    echo -e "  日志目录:     ${WHITE}${LOG_DIR}${NC}"
    echo ""
    
    if [ -n "$pid" ]; then
        echo -e "${CYAN}【运行状态】${NC}"
        echo -e "  状态:         ${GREEN}运行中${NC}"
        echo -e "  进程 ID:      ${WHITE}${pid}${NC}"
        
        # 获取启动时间
        local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
        [ -n "$start_time" ] && echo -e "  启动时间:     ${WHITE}${start_time}${NC}"
        
        # 获取运行时长
        local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
        [ -n "$etime" ] && echo -e "  运行时长:     ${WHITE}${etime}${NC}"
        
        # 获取内存使用
        local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
        if [ -n "$mem" ]; then
            local mem_mb=$((mem / 1024))
            echo -e "  内存使用:     ${WHITE}${mem_mb} MB${NC}"
        fi
        
        # 获取 CPU 使用率
        local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
        [ -n "$cpu" ] && echo -e "  CPU 使用:     ${WHITE}${cpu}%${NC}"
    else
        echo -e "${CYAN}【运行状态】${NC}"
        echo -e "  状态:         ${YELLOW}已停止${NC}"
    fi
    
    # Java 信息
    echo ""
    echo -e "${CYAN}【Java 环境】${NC}"
    check_java 2>/dev/null
    if [ -n "$JAVA_CMD" ]; then
        echo -e "  Java 路径:    ${WHITE}${JAVA_CMD}${NC}"
        echo -e "  Java 版本:    ${WHITE}$(get_java_version)${NC}"
    else
        echo -e "  Java:         ${YELLOW}未配置${NC}"
    fi
    
    # Spring 配置
    echo ""
    echo -e "${CYAN}【Spring 配置】${NC}"
    build_loader_path
    echo -e "  Loader 路径:              ${WHITE}${LOADER_PATH:-<空>}${NC}"
    echo -e "  config.additional-location: ${WHITE}file:${CONFIG_DIR}/,file:${CONFIG_DIR}/config/${NC}"
    
    # 日志文件
    echo ""
    echo -e "${CYAN}【日志文件】${NC}"
    if [ -f "${STARTUP_LOG}" ]; then
        local log_size=$(du -h "${STARTUP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  启动日志:     ${WHITE}${STARTUP_LOG} (${log_size})${NC}"
    fi
    if [ -f "${APP_LOG}" ]; then
        local log_size=$(du -h "${APP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  应用日志:     ${WHITE}${APP_LOG} (${log_size})${NC}"
    fi
    echo ""
}

# ===========================================
# 详细信息
# ===========================================
show_detailed_info() {
    show_basic_info
    
    local pid=$(get_pid)
    [ -z "$pid" ] && return 0
    
    echo -e "${CYAN}【JVM 详细信息】${NC}"
    
    # JVM 参数
    local jvm_opts=$(ps -o args= -p "$pid" 2>/dev/null | grep -oE -- '-X[^[:space:]]+' | head -10 | tr '\n' ' ')
    [ -n "$jvm_opts" ] && echo -e "  JVM 参数:     ${WHITE}${jvm_opts}${NC}"
    
    # GC 信息
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        echo ""
        echo -e "${CYAN}【GC 统计】${NC}"
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    
    # 线程信息
    local threads=$(ps -o nlwp= -p "$pid" 2>/dev/null | xargs)
    [ -n "$threads" ] && echo -e "  线程数:       ${WHITE}${threads}${NC}"
    
    echo ""
}

# ===========================================
# CPU 信息
# ===========================================
show_cpu_info() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        log_warn "${APP_NAME} 未在运行"
        return 1
    fi
    
    print_title "CPU 信息"
    echo ""
    
    # 进程 CPU 使用
    local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
    echo -e "  进程 CPU:     ${WHITE}${cpu}%${NC}"
    
    # 系统 CPU 负载
    if [ -f /proc/loadavg ]; then
        local load=$(cat /proc/loadavg | awk '{print $1, $2, $3}')
        echo -e "  系统负载:     ${WHITE}${load}${NC}"
    fi
    
    # CPU 核心数
    local cores=$(nproc 2>/dev/null || grep -c processor /proc/cpuinfo 2>/dev/null)
    [ -n "$cores" ] && echo -e "  CPU 核心:     ${WHITE}${cores}${NC}"
    
    # 高 CPU 线程 (Top 5)
    echo ""
    echo -e "${CYAN}【高 CPU 线程 (Top 5)】${NC}"
    ps -T -p "$pid" -o spid,%cpu,comm --sort=-%cpu 2>/dev/null | head -6
    
    echo ""
}

# ===========================================
# 内存信息
# ===========================================
show_mem_info() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        log_warn "${APP_NAME} 未在运行"
        return 1
    fi
    
    print_title "内存信息"
    echo ""
    
    # 进程内存使用
    local rss=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
    local vsz=$(ps -o vsz= -p "$pid" 2>/dev/null | xargs)
    
    if [ -n "$rss" ]; then
        local rss_mb=$((rss / 1024))
        echo -e "  物理内存 (RSS): ${WHITE}${rss_mb} MB${NC}"
    fi
    if [ -n "$vsz" ]; then
        local vsz_mb=$((vsz / 1024))
        echo -e "  虚拟内存 (VSZ): ${WHITE}${vsz_mb} MB${NC}"
    fi
    
    # JVM 堆内存 (使用 jstat)
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    
    if command -v "${jstat_cmd}" &> /dev/null; then
        echo ""
        echo -e "${CYAN}【JVM 堆内存】${NC}"
        "${jstat_cmd}" -gc "$pid" 2>/dev/null | head -2
    fi
    
    # 系统内存
    echo ""
    echo -e "${CYAN}【系统内存】${NC}"
    free -h 2>/dev/null | head -2
    
    echo ""
}

# ===========================================
# 实时刷新
# ===========================================
show_realtime() {
    local mode="${1:-basic}"
    
    echo -e "${YELLOW}实时刷新模式 (按 Ctrl+C 退出)${NC}"
    echo ""
    
    while true; do
        clear
        case "$mode" in
            cpu)  show_cpu_info ;;
            mem)  show_mem_info ;;
            *)    show_basic_info ;;
        esac
        sleep 2
    done
}

# ===========================================
# 参数解析
# ===========================================
MODE="basic"
REALTIME="false"

while [ $# -gt 0 ]; do
    case "$1" in
        -a)
            MODE="detailed"
            shift
            ;;
        -c)
            MODE="cpu"
            shift
            ;;
        -m)
            MODE="mem"
            shift
            ;;
        -f)
            REALTIME="true"
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
# 执行
# ===========================================
if [ "$REALTIME" = "true" ]; then
    show_realtime "$MODE"
else
    case "$MODE" in
        detailed) show_detailed_info ;;
        cpu)      show_cpu_info ;;
        mem)      show_mem_info ;;
        *)        show_basic_info ;;
    esac
fi
