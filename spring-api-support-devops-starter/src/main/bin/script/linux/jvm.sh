#!/bin/bash
# ============================================================
# JVM 诊断与调优脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}JVM 诊断与调优${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  (无参数)    显示 JVM 信息概览"
    echo "  info        显示 JVM 详细信息"
    echo "  flags       显示 JVM 参数"
    echo "  tuning      JVM 调优建议"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -h, --help  显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                         显示 JVM 概览"
    echo "  $0 info                    详细 JVM 信息"
    echo "  $0 flags                   显示所有 JVM 参数"
    echo "  $0 tuning                  获取调优建议"
    echo ""
}

# ===========================================
# JVM 信息概览
# ===========================================
show_jvm_info() {
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    print_title "JVM 信息"
    echo ""
    
    # 基本信息
    echo -e "${YELLOW}基本信息:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "  PID:           ${WHITE}${pid}${NC}"
    
    if command -v "${jcmd_cmd}" &> /dev/null; then
        local vm_version=$("${jcmd_cmd}" "$pid" VM.version 2>/dev/null | grep -v "^$pid:")
        echo -e "  ${CYAN}VM 版本:${NC}"
        echo "$vm_version" | sed 's/^/    /'
    fi
    echo ""
    
    # 堆内存
    echo -e "${YELLOW}堆内存:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    if command -v "${jcmd_cmd}" &> /dev/null; then
        "${jcmd_cmd}" "$pid" GC.heap_info 2>/dev/null | grep -v "^$pid:" || true
    fi
    echo ""
    
    # GC 信息
    echo -e "${YELLOW}GC 信息:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    if command -v "${jcmd_cmd}" &> /dev/null; then
        local gc_info=$("${jcmd_cmd}" "$pid" VM.flags 2>/dev/null | \
            grep -E "UseG1GC|UseConcMarkSweepGC|UseParallelGC|UseSerialGC|UseZGC|UseShenandoahGC" | \
            head -1)
        
        if echo "$gc_info" | grep -q "UseG1GC"; then
            echo -e "  GC 类型: ${WHITE}G1GC${NC}"
        elif echo "$gc_info" | grep -q "UseZGC"; then
            echo -e "  GC 类型: ${WHITE}ZGC${NC}"
        elif echo "$gc_info" | grep -q "UseShenandoahGC"; then
            echo -e "  GC 类型: ${WHITE}Shenandoah${NC}"
        elif echo "$gc_info" | grep -q "UseConcMarkSweepGC"; then
            echo -e "  GC 类型: ${WHITE}CMS${NC}"
        elif echo "$gc_info" | grep -q "UseParallelGC"; then
            echo -e "  GC 类型: ${WHITE}Parallel GC${NC}"
        else
            echo -e "  GC 类型: ${WHITE}未知${NC}"
        fi
    fi
    echo ""
}

# ===========================================
# JVM 详细信息
# ===========================================
show_jvm_detail() {
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
    
    print_title "JVM 详细信息"
    echo ""
    
    # VM 信息
    echo -e "${YELLOW}VM 信息:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    "${jcmd_cmd}" "$pid" VM.info 2>/dev/null | head -50 | grep -v "^$pid:"
    echo ""
    
    # 系统属性
    echo -e "${YELLOW}系统属性:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    "${jcmd_cmd}" "$pid" VM.system_properties 2>/dev/null | \
        grep -E "java.version|java.home|java.vm.name|os.name|os.arch|user.dir|file.encoding" | \
        grep -v "^$pid:" | sort
    echo ""
    
    # 类加载
    echo -e "${YELLOW}类加载统计:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    "${jcmd_cmd}" "$pid" VM.classloader_stats 2>/dev/null | head -20 | grep -v "^$pid:" || \
        "${jcmd_cmd}" "$pid" GC.class_stats 2>/dev/null | head -20 | grep -v "^$pid:" || \
        echo "  (无法获取类加载统计)"
    echo ""
    
    # 线程统计
    echo -e "${YELLOW}线程统计:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    local thread_count=$("${jcmd_cmd}" "$pid" Thread.print 2>/dev/null | grep -c "^\"")
    echo -e "  总线程数: ${WHITE}${thread_count}${NC}"
    echo ""
}

# ===========================================
# JVM 参数
# ===========================================
show_jvm_flags() {
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
    
    print_title "JVM 参数"
    echo ""
    
    # 显示所有 JVM 参数
    echo -e "${YELLOW}VM Flags:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    "${jcmd_cmd}" "$pid" VM.flags 2>/dev/null | grep -v "^$pid:"
    echo ""
    
    # 显示命令行参数
    echo -e "${YELLOW}命令行参数:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    "${jcmd_cmd}" "$pid" VM.command_line 2>/dev/null | grep -v "^$pid:"
    echo ""
}

# ===========================================
# JVM 调优建议
# ===========================================
show_tuning() {
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    print_title "JVM 调优建议"
    echo ""
    
    # 获取当前配置
    local heap_info=""
    local vm_flags=""
    
    if command -v "${jcmd_cmd}" &> /dev/null; then
        heap_info=$("${jcmd_cmd}" "$pid" GC.heap_info 2>/dev/null)
        vm_flags=$("${jcmd_cmd}" "$pid" VM.flags 2>/dev/null)
    fi
    
    echo -e "${YELLOW}当前配置分析:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # 分析堆大小
    local max_heap=$(echo "$vm_flags" | grep -oE "MaxHeapSize=[0-9]+" | cut -d= -f2)
    local init_heap=$(echo "$vm_flags" | grep -oE "InitialHeapSize=[0-9]+" | cut -d= -f2)
    
    if [ -n "$max_heap" ]; then
        local max_heap_mb=$((max_heap / 1024 / 1024))
        echo -e "  最大堆: ${WHITE}${max_heap_mb}MB${NC}"
    fi
    
    if [ -n "$init_heap" ]; then
        local init_heap_mb=$((init_heap / 1024 / 1024))
        echo -e "  初始堆: ${WHITE}${init_heap_mb}MB${NC}"
    fi
    
    # 获取系统内存
    local total_mem=$(grep MemTotal /proc/meminfo 2>/dev/null | awk '{print $2}')
    if [ -n "$total_mem" ]; then
        local total_mem_mb=$((total_mem / 1024))
        echo -e "  系统内存: ${WHITE}${total_mem_mb}MB${NC}"
    fi
    
    echo ""
    
    # 调优建议
    echo -e "${YELLOW}调优建议:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # 堆大小建议
    echo -e "${CYAN}1. 堆内存配置${NC}"
    if [ -n "$max_heap" ] && [ -n "$init_heap" ]; then
        if [ "$max_heap" != "$init_heap" ]; then
            echo -e "   ${YELLOW}建议:${NC} 将 -Xms 和 -Xmx 设置为相同值，避免动态调整开销"
            echo "         示例: -Xms${max_heap_mb}m -Xmx${max_heap_mb}m"
        else
            echo -e "   ${GREEN}✓${NC} Xms 和 Xmx 已设置为相同值"
        fi
    fi
    echo ""
    
    # GC 建议
    echo -e "${CYAN}2. GC 配置${NC}"
    if echo "$vm_flags" | grep -q "UseG1GC"; then
        echo -e "   ${GREEN}✓${NC} 使用 G1GC (推荐)"
        echo "   建议参数:"
        echo "     -XX:+UseG1GC"
        echo "     -XX:MaxGCPauseMillis=200"
        echo "     -XX:G1HeapRegionSize=8m"
    elif echo "$vm_flags" | grep -q "UseZGC"; then
        echo -e "   ${GREEN}✓${NC} 使用 ZGC (低延迟)"
    elif echo "$vm_flags" | grep -q "UseShenandoahGC"; then
        echo -e "   ${GREEN}✓${NC} 使用 Shenandoah (低延迟)"
    else
        echo -e "   ${YELLOW}建议:${NC} 考虑使用 G1GC 或 ZGC"
        echo "     G1GC: -XX:+UseG1GC"
        echo "     ZGC:  -XX:+UseZGC (JDK 11+)"
    fi
    echo ""
    
    # 元空间建议
    echo -e "${CYAN}3. 元空间配置${NC}"
    local metaspace=$(echo "$vm_flags" | grep -oE "MaxMetaspaceSize=[0-9]+" | cut -d= -f2)
    if [ -z "$metaspace" ] || [ "$metaspace" = "0" ]; then
        echo -e "   ${YELLOW}建议:${NC} 设置元空间上限防止内存泄漏"
        echo "     -XX:MetaspaceSize=256m"
        echo "     -XX:MaxMetaspaceSize=512m"
    else
        local metaspace_mb=$((metaspace / 1024 / 1024))
        echo -e "   ${GREEN}✓${NC} 元空间上限: ${metaspace_mb}MB"
    fi
    echo ""
    
    # GC 日志建议
    echo -e "${CYAN}4. GC 日志配置${NC}"
    if echo "$vm_flags" | grep -qE "PrintGC|Xlog:gc"; then
        echo -e "   ${GREEN}✓${NC} GC 日志已启用"
    else
        echo -e "   ${YELLOW}建议:${NC} 启用 GC 日志用于问题排查"
        echo "     JDK 8:  -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log"
        echo "     JDK 9+: -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m"
    fi
    echo ""
    
    # OOM 配置建议
    echo -e "${CYAN}5. OOM 配置${NC}"
    if echo "$vm_flags" | grep -q "HeapDumpOnOutOfMemoryError"; then
        echo -e "   ${GREEN}✓${NC} OOM 时自动 dump 已启用"
    else
        echo -e "   ${YELLOW}建议:${NC} 启用 OOM 时自动 dump"
        echo "     -XX:+HeapDumpOnOutOfMemoryError"
        echo "     -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof"
    fi
    echo ""
    
    # 推荐配置
    echo -e "${YELLOW}推荐 JVM 参数模板:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "  # 堆内存 (根据实际需求调整)"
    echo "  -Xms2g -Xmx2g"
    echo ""
    echo "  # G1GC"
    echo "  -XX:+UseG1GC"
    echo "  -XX:MaxGCPauseMillis=200"
    echo ""
    echo "  # 元空间"
    echo "  -XX:MetaspaceSize=256m"
    echo "  -XX:MaxMetaspaceSize=512m"
    echo ""
    echo "  # OOM 处理"
    echo "  -XX:+HeapDumpOnOutOfMemoryError"
    echo "  -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof"
    echo ""
    echo "  # GC 日志 (JDK 9+)"
    echo "  -Xlog:gc*:file=${LOG_DIR}/gc.log:time,uptime:filecount=5,filesize=10m"
    echo ""
}

# ===========================================
# 参数解析
# ===========================================
case "${1:-}" in
    ""|info)
        if [ "$1" = "info" ]; then
            show_jvm_detail
        else
            show_jvm_info
        fi
        ;;
    flags)
        show_jvm_flags
        ;;
    tuning)
        show_tuning
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        log_error "未知命令: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
