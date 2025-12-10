#!/bin/bash
# ============================================================
# CPU/内存分析脚本
# 分析高CPU或内存占用的代码位置
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

PID="$1"
MODE="${2:-cpu}"  # cpu 或 mem
APP_NAME="${3:-Java应用}"

[ -z "$PID" ] && echo -e "${RED}[错误] 未提供进程ID${NC}" && exit 1
! ps -p "$PID" > /dev/null 2>&1 && echo -e "${RED}[错误] 进程 $PID 不存在${NC}" && exit 1

# 获取 Java 工具路径
JAVA_BIN=""
if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME/bin" ]; then
    JAVA_BIN="$JAVA_HOME/bin"
else
    jp=$(which java 2>/dev/null)
    [ -n "$jp" ] && JAVA_BIN=$(dirname $(readlink -f "$jp" 2>/dev/null || echo "$jp"))
fi

echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}  ${APP_NAME} CPU/内存分析${NC}"
echo -e "${BLUE}============================================================${NC}"
echo -e "  进程ID: ${PID}"
echo -e "  分析模式: ${MODE}"
echo -e "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo -e "${BLUE}============================================================${NC}"

# CPU 分析
analyze_cpu() {
    echo -e "\n${YELLOW}[1. 获取高CPU线程...]${NC}"
    
    # 获取进程中 CPU 占用最高的线程
    TOP_THREADS=$(ps -Leo pid,lwp,pcpu -p "$PID" 2>/dev/null | sort -k3 -rn | head -11 | tail -10)
    
    if [ -z "$TOP_THREADS" ]; then
        echo -e "${RED}无法获取线程信息${NC}"
        return 1
    fi
    
    echo -e "${CYAN}PID      LWP      %CPU${NC}"
    echo "$TOP_THREADS"
    
    echo -e "\n${YELLOW}[2. 分析高CPU线程堆栈...]${NC}"
    
    # 获取 jstack
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstack" ]; then
        JSTACK_OUTPUT=$("$JAVA_BIN/jstack" "$PID" 2>/dev/null)
        
        if [ -n "$JSTACK_OUTPUT" ]; then
            # 分析每个高 CPU 线程
            echo "$TOP_THREADS" | while read line; do
                LWP=$(echo "$line" | awk '{print $2}')
                CPU=$(echo "$line" | awk '{print $3}')
                
                [ -z "$LWP" ] && continue
                [ "$CPU" = "0.0" ] && continue
                
                # 转换为 16 进制
                HEX_LWP=$(printf "%x" "$LWP" 2>/dev/null)
                
                echo -e "\n${GREEN}>>> 线程 $LWP (0x$HEX_LWP) CPU: ${CPU}%${NC}"
                
                # 从 jstack 中提取该线程的堆栈
                echo "$JSTACK_OUTPUT" | awk -v nid="0x$HEX_LWP" '
                    $0 ~ "nid="nid {found=1}
                    found {print}
                    found && /^$/ {found=0}
                ' | head -20
            done
        else
            echo -e "${RED}无法获取线程堆栈${NC}"
        fi
    else
        echo -e "${RED}jstack 不可用${NC}"
    fi
    
    echo -e "\n${YELLOW}[3. CPU 热点分析摘要...]${NC}"
    
    if [ -n "$JSTACK_OUTPUT" ]; then
        echo -e "${CYAN}最常见的方法调用:${NC}"
        echo "$JSTACK_OUTPUT" | grep "at " | sed 's/^[[:space:]]*//' | sort | uniq -c | sort -rn | head -15
    fi
}

# 内存分析
analyze_memory() {
    echo -e "\n${YELLOW}[1. 堆内存概览...]${NC}"
    
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
        "$JAVA_BIN/jcmd" "$PID" GC.heap_info 2>/dev/null | tail -n +2
    elif [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstat" ]; then
        "$JAVA_BIN/jstat" -gc "$PID" 2>/dev/null
    fi
    
    echo -e "\n${YELLOW}[2. 堆内存直方图 (Top 20)...]${NC}"
    
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
        "$JAVA_BIN/jcmd" "$PID" GC.class_histogram 2>/dev/null | head -25
    elif [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jmap" ]; then
        "$JAVA_BIN/jmap" -histo:live "$PID" 2>/dev/null | head -25
    else
        echo -e "${RED}jmap/jcmd 不可用${NC}"
    fi
    
    echo -e "\n${YELLOW}[3. 内存占用最高的类...]${NC}"
    
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
        HISTO=$("$JAVA_BIN/jcmd" "$PID" GC.class_histogram 2>/dev/null)
        
        if [ -n "$HISTO" ]; then
            echo -e "${CYAN}内存占用分析:${NC}"
            echo "$HISTO" | grep -v "^\s*num\|^---" | head -20 | while read line; do
                CLASS=$(echo "$line" | awk '{print $4}')
                BYTES=$(echo "$line" | awk '{print $3}')
                
                [ -z "$CLASS" ] && continue
                
                # 格式化大小
                if [ "$BYTES" -gt 1048576 ] 2>/dev/null; then
                    SIZE="$((BYTES / 1048576))MB"
                elif [ "$BYTES" -gt 1024 ] 2>/dev/null; then
                    SIZE="$((BYTES / 1024))KB"
                else
                    SIZE="${BYTES}B"
                fi
                
                echo "  $SIZE - $CLASS"
            done
        fi
    fi
    
    echo -e "\n${YELLOW}[4. 线程内存使用...]${NC}"
    
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstack" ]; then
        THREAD_COUNT=$("$JAVA_BIN/jstack" "$PID" 2>/dev/null | grep -c "^\"")
        echo "  活跃线程数: $THREAD_COUNT"
        echo "  每线程栈内存: 约 1MB (默认 -Xss1m)"
        echo "  线程栈总占用: 约 ${THREAD_COUNT}MB"
    fi
}

# 主逻辑
case "$MODE" in
    cpu|CPU)
        analyze_cpu
        ;;
    mem|memory|MEM|MEMORY)
        analyze_memory
        ;;
    all|ALL)
        analyze_cpu
        echo -e "\n"
        analyze_memory
        ;;
    *)
        echo -e "${RED}未知模式: $MODE${NC}"
        echo "用法: $0 <PID> [cpu|mem|all]"
        exit 1
        ;;
esac

echo -e "\n${BLUE}============================================================${NC}"
echo -e "${CYAN}提示: 使用 'arthas' 可进行更深入的在线诊断${NC}"
echo -e "${BLUE}============================================================${NC}"
