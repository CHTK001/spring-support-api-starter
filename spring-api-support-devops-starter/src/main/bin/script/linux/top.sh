#!/bin/bash
# ============================================================
# CPU/内存分析脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}CPU/内存分析${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <类型> [数量]"
    echo ""
    echo -e "${YELLOW}类型:${NC}"
    echo "  cpu [N]     高 CPU 线程分析 (默认 Top 10)"
    echo "  mem [N]     高内存对象分析 (默认 Top 10)"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -h, --help  显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 cpu              高 CPU 线程分析 (Top 10)"
    echo "  $0 cpu 20           高 CPU 线程分析 (Top 20)"
    echo "  $0 mem              高内存对象分析 (Top 10)"
    echo "  $0 mem 20           高内存对象分析 (Top 20)"
    echo ""
}

# ===========================================
# CPU 分析
# ===========================================
analyze_cpu() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        log_error "未找到 jstack 命令"
        return 1
    fi
    
    print_title "高 CPU 线程分析 (Top ${top_n})"
    echo ""
    
    # 获取进程的线程 CPU 使用情况
    log_step "收集线程 CPU 信息..."
    
    # 使用 top 获取线程级 CPU
    local top_threads=$(top -H -b -n 1 -p "$pid" 2>/dev/null | \
        grep -E "^\s*[0-9]+" | \
        sort -k9 -rn | \
        head -n "$top_n")
    
    if [ -z "$top_threads" ]; then
        log_warning "未能获取线程 CPU 信息"
        return 1
    fi
    
    # 获取线程栈
    log_step "获取线程栈..."
    local thread_dump=$("${jstack_cmd}" "$pid" 2>/dev/null)
    
    if [ -z "$thread_dump" ]; then
        log_error "获取线程栈失败"
        return 1
    fi
    
    echo ""
    echo -e "${YELLOW}高 CPU 线程:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    printf "${WHITE}%-8s %-8s %-50s${NC}\n" "TID" "CPU%" "线程名称"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    echo "$top_threads" | while read -r line; do
        local tid=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        
        # 转换 TID 为十六进制
        local tid_hex=$(printf "%x" "$tid")
        
        # 查找线程名
        local thread_name=$(echo "$thread_dump" | \
            grep -E "nid=0x${tid_hex}\s" | \
            sed 's/.*"\(.*\)".*/\1/' | \
            head -1)
        
        [ -z "$thread_name" ] && thread_name="(未知)"
        
        printf "%-8s %-8s %-50s\n" "$tid" "${cpu}%" "$thread_name"
    done
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    # 显示详细堆栈
    echo -e "${YELLOW}详细堆栈信息:${NC}"
    echo ""
    
    echo "$top_threads" | head -3 | while read -r line; do
        local tid=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        local tid_hex=$(printf "%x" "$tid")
        
        echo -e "${CYAN}TID: ${tid} (0x${tid_hex}), CPU: ${cpu}%${NC}"
        echo "$thread_dump" | \
            grep -A 30 "nid=0x${tid_hex}\s" | \
            grep -E "^\s+at|^\"" | \
            head -15
        echo ""
    done
}

# ===========================================
# 内存分析
# ===========================================
analyze_mem() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    check_java
    local jmap_cmd="jmap"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap"
    
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    print_title "高内存对象分析 (Top ${top_n})"
    echo ""
    
    log_step "分析堆内存..."
    
    local histo=""
    
    # 优先使用 jcmd
    if command -v "${jcmd_cmd}" &> /dev/null; then
        histo=$("${jcmd_cmd}" "$pid" GC.class_histogram 2>/dev/null | head -n $((top_n + 4)))
    elif command -v "${jmap_cmd}" &> /dev/null; then
        histo=$("${jmap_cmd}" -histo:live "$pid" 2>/dev/null | head -n $((top_n + 4)))
    else
        log_error "未找到 jmap 或 jcmd 命令"
        return 1
    fi
    
    if [ -z "$histo" ]; then
        log_error "获取内存信息失败"
        return 1
    fi
    
    echo ""
    echo -e "${YELLOW}高内存对象:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "$histo"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    # 显示堆内存概览
    log_step "堆内存概览..."
    echo ""
    
    if command -v "${jcmd_cmd}" &> /dev/null; then
        "${jcmd_cmd}" "$pid" GC.heap_info 2>/dev/null || true
    fi
    
    echo ""
}

# ===========================================
# 参数解析
# ===========================================
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

case "$1" in
    cpu)
        analyze_cpu "${2:-10}"
        ;;
    mem)
        analyze_mem "${2:-10}"
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        log_error "未知类型: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
