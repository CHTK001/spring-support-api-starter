#!/bin/bash
# ============================================================
# GC 触发脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}GC 触发${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [类型]"
    echo ""
    echo -e "${YELLOW}类型:${NC}"
    echo "  (无参数)    触发 Full GC (默认)"
    echo "  full        触发 Full GC"
    echo "  young       触发 Young GC"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0              触发 Full GC"
    echo "  $0 full         触发 Full GC"
    echo "  $0 young        触发 Young GC"
    echo ""
}

# ===========================================
# 触发 GC
# ===========================================
do_gc() {
    local gc_type="${1:-full}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    if ! command -v "${jcmd_cmd}" &> /dev/null; then
        log_error "未找到 jcmd 命令"
        return 1
    fi
    
    print_title "触发 GC"
    echo ""
    echo -e "  PID:      ${WHITE}${pid}${NC}"
    echo -e "  GC 类型:  ${WHITE}${gc_type}${NC}"
    echo ""
    
    # GC 前状态
    echo -e "${CYAN}【GC 前状态】${NC}"
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    # 触发 GC
    case "${gc_type}" in
        young|minor)
            log_step "触发 Young GC..."
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        full|major|*)
            log_step "触发 Full GC..."
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
    esac
    
    sleep 2
    
    # GC 后状态
    echo ""
    echo -e "${CYAN}【GC 后状态】${NC}"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    log_info "GC 完成"
}

# ===========================================
# 参数解析
# ===========================================
GC_TYPE="full"

while [ $# -gt 0 ]; do
    case "$1" in
        full|major)
            GC_TYPE="full"
            shift
            ;;
        young|minor)
            GC_TYPE="young"
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
do_gc "$GC_TYPE"
