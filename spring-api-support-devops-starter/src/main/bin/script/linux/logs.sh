#!/bin/bash
# ============================================================
# 日志管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}日志管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项] [行数]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  (无参数)         显示最近 100 条日志"
    echo "  [N]              显示最近 N 条日志"
    echo "  -f               实时跟踪日志"
    echo "  -t <time>        按时间过滤 (格式: HH:MM 或 YYYY-MM-DD)"
    echo "  --filter <pattern> 按模式过滤"
    echo "  --startup        查看启动日志"
    echo "  --app            查看应用日志"
    echo "  -h, --help       显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                      查看最近 100 条日志"
    echo "  $0 200                  查看最近 200 条日志"
    echo "  $0 -f                   实时跟踪日志"
    echo "  $0 -t 10:30             过滤 10:30 之后的日志"
    echo "  $0 --filter ERROR       过滤包含 ERROR 的日志"
    echo "  $0 --filter 'Exception' 过滤包含 Exception 的日志"
    echo ""
}

# ===========================================
# 获取日志文件
# ===========================================
get_log_file() {
    local log_type="${1:-auto}"
    
    case "$log_type" in
        startup)
            echo "${STARTUP_LOG}"
            ;;
        app)
            echo "${APP_LOG}"
            ;;
        *)
            # 自动选择：优先使用应用日志
            if [ -f "${APP_LOG}" ]; then
                echo "${APP_LOG}"
            else
                echo "${STARTUP_LOG}"
            fi
            ;;
    esac
}

# ===========================================
# 显示日志
# ===========================================
show_logs() {
    local lines="${1:-100}"
    local log_file=$(get_log_file "$LOG_TYPE")
    
    if [ ! -f "${log_file}" ]; then
        log_warn "日志文件不存在: ${log_file}"
        return 1
    fi
    
    print_title "日志 - ${log_file}"
    echo ""
    
    if [ -n "$FILTER_PATTERN" ]; then
        tail -n "${lines}" "${log_file}" | grep --color=auto -E "$FILTER_PATTERN"
    elif [ -n "$TIME_FILTER" ]; then
        tail -n "${lines}" "${log_file}" | grep --color=auto -E "^.*${TIME_FILTER}"
    else
        tail -n "${lines}" "${log_file}"
    fi
}

# ===========================================
# 实时跟踪日志
# ===========================================
tail_logs() {
    local log_file=$(get_log_file "$LOG_TYPE")
    
    if [ ! -f "${log_file}" ]; then
        log_warn "日志文件不存在: ${log_file}"
        echo -e "${YELLOW}等待日志文件生成...${NC}"
        
        local count=0
        while [ ! -f "${log_file}" ] && [ $count -lt 30 ]; do
            sleep 1
            count=$((count + 1))
            # 重新检查应用日志
            [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
        done
        
        if [ ! -f "${log_file}" ]; then
            log_error "等待超时，日志文件仍不存在"
            return 1
        fi
    fi
    
    print_title "实时日志 - ${log_file}"
    echo -e "${YELLOW}按 Ctrl+C 退出${NC}"
    echo ""
    
    if [ -n "$FILTER_PATTERN" ]; then
        tail -f "${log_file}" | grep --color=auto --line-buffered -E "$FILTER_PATTERN"
    elif [ -n "$TIME_FILTER" ]; then
        tail -f "${log_file}" | grep --color=auto --line-buffered -E "^.*${TIME_FILTER}"
    else
        tail -f "${log_file}"
    fi
}

# ===========================================
# 参数解析
# ===========================================
LINES="100"
FOLLOW="false"
TIME_FILTER=""
FILTER_PATTERN=""
LOG_TYPE="auto"

while [ $# -gt 0 ]; do
    case "$1" in
        -f)
            FOLLOW="true"
            shift
            ;;
        -t)
            TIME_FILTER="$2"
            shift 2
            ;;
        --filter)
            FILTER_PATTERN="$2"
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
            # 检查是否是数字
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
if [ "$FOLLOW" = "true" ]; then
    tail_logs
else
    show_logs "$LINES"
fi
