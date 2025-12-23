#!/bin/bash
# ============================================================
# 转储管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}转储管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [类型] [选项]"
    echo ""
    echo -e "${YELLOW}类型:${NC}"
    echo "  (无参数)    生成堆 + 线程转储 (默认)"
    echo "  heap        仅生成堆转储"
    echo "  thread      仅生成线程转储"
    echo "  all         生成堆 + 线程转储"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -o <path>   指定输出目录"
    echo "  -h, --help  显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                         生成堆 + 线程转储"
    echo "  $0 heap                    仅堆转储"
    echo "  $0 thread                  仅线程转储"
    echo "  $0 -o /tmp/dump            指定输出目录"
    echo ""
}

# ===========================================
# 堆转储
# ===========================================
dump_heap() {
    local pid="$1"
    local output_dir="$2"
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local dump_file="${output_dir}/heap_${timestamp}.hprof"
    
    check_java
    local jmap_cmd="jmap"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        # 尝试使用 jcmd
        local jcmd_cmd="jcmd"
        [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
        
        if command -v "${jcmd_cmd}" &> /dev/null; then
            log_step "生成堆转储 (使用 jcmd)..."
            "${jcmd_cmd}" "$pid" GC.heap_dump "${dump_file}" 2>/dev/null
        else
            log_error "未找到 jmap 或 jcmd 命令"
            return 1
        fi
    else
        log_step "生成堆转储..."
        "${jmap_cmd}" -dump:format=b,file="${dump_file}" "$pid" 2>/dev/null
    fi
    
    if [ -f "${dump_file}" ]; then
        local size=$(du -h "${dump_file}" | cut -f1)
        log_info "堆转储已生成: ${dump_file} (${size})"
    else
        log_error "堆转储生成失败"
        return 1
    fi
}

# ===========================================
# 线程转储
# ===========================================
dump_thread() {
    local pid="$1"
    local output_dir="$2"
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local dump_file="${output_dir}/thread_${timestamp}.txt"
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        # 尝试使用 jcmd
        local jcmd_cmd="jcmd"
        [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
        
        if command -v "${jcmd_cmd}" &> /dev/null; then
            log_step "生成线程转储 (使用 jcmd)..."
            "${jcmd_cmd}" "$pid" Thread.print > "${dump_file}" 2>/dev/null
        else
            log_error "未找到 jstack 或 jcmd 命令"
            return 1
        fi
    else
        log_step "生成线程转储..."
        "${jstack_cmd}" -l "$pid" > "${dump_file}" 2>/dev/null
    fi
    
    if [ -f "${dump_file}" ] && [ -s "${dump_file}" ]; then
        local size=$(du -h "${dump_file}" | cut -f1)
        log_info "线程转储已生成: ${dump_file} (${size})"
    else
        log_error "线程转储生成失败"
        return 1
    fi
}

# ===========================================
# 执行转储
# ===========================================
do_dump() {
    local dump_type="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    mkdir -p "$output_dir"
    
    print_title "生成转储"
    echo ""
    echo -e "  PID:      ${WHITE}${pid}${NC}"
    echo -e "  类型:     ${WHITE}${dump_type}${NC}"
    echo -e "  输出目录: ${WHITE}${output_dir}${NC}"
    echo ""
    
    case "$dump_type" in
        heap)
            dump_heap "$pid" "$output_dir"
            ;;
        thread)
            dump_thread "$pid" "$output_dir"
            ;;
        all|*)
            dump_heap "$pid" "$output_dir"
            echo ""
            dump_thread "$pid" "$output_dir"
            ;;
    esac
    
    echo ""
    log_info "转储完成"
}

# ===========================================
# 参数解析
# ===========================================
DUMP_TYPE="all"
OUTPUT_DIR="${LOG_DIR}/dump"

while [ $# -gt 0 ]; do
    case "$1" in
        heap|thread|all)
            DUMP_TYPE="$1"
            shift
            ;;
        -o|--output)
            OUTPUT_DIR="$2"
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
# 执行
# ===========================================
do_dump "$DUMP_TYPE" "$OUTPUT_DIR"
