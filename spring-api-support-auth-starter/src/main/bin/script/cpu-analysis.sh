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
MODE="${2:-cpu}"
APP_NAME="${3:-Java应用}"

[ -z "$PID" ] && echo -e "${RED}[错误] 未提供进程ID${NC}" && exit 1
! ps -p "$PID" > /dev/null 2>&1 && echo -e "${RED}[错误] 进程 $PID 不存在${NC}" && exit 1

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
echo -e "  进程ID: ${PID} | 模式: ${MODE}"
echo -e "${BLUE}============================================================${NC}"

analyze_cpu() {
    echo -e "\n${YELLOW}[1. 高CPU线程...]${NC}"
    TOP_THREADS=$(ps -Leo pid,lwp,pcpu -p "$PID" 2>/dev/null | sort -k3 -rn | head -11 | tail -10)
    echo "$TOP_THREADS"
    
    echo -e "\n${YELLOW}[2. 线程堆栈分析...]${NC}"
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstack" ]; then
        JSTACK=$("$JAVA_BIN/jstack" "$PID" 2>/dev/null)
        echo "$TOP_THREADS" | while read line; do
            LWP=$(echo "$line" | awk '{print $2}')
            CPU=$(echo "$line" | awk '{print $3}')
            [ -z "$LWP" ] || [ "$CPU" = "0.0" ] && continue
            HEX=$(printf "%x" "$LWP" 2>/dev/null)
            echo -e "\n${GREEN}>>> 线程 $LWP (0x$HEX) CPU: ${CPU}%${NC}"
            echo "$JSTACK" | awk -v nid="0x$HEX" '$0~"nid="nid{f=1} f; f&&/^$/{f=0}' | head -15
        done
        
        echo -e "\n${YELLOW}[3. 热点方法...]${NC}"
        echo "$JSTACK" | grep "at " | sed 's/^[[:space:]]*//' | sort | uniq -c | sort -rn | head -10
    fi
}

analyze_memory() {
    echo -e "\n${YELLOW}[1. 堆内存...]${NC}"
    [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ] && "$JAVA_BIN/jcmd" "$PID" GC.heap_info 2>/dev/null | tail -n +2
    
    echo -e "\n${YELLOW}[2. 内存直方图 (Top 15)...]${NC}"
    if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
        "$JAVA_BIN/jcmd" "$PID" GC.class_histogram 2>/dev/null | head -20
    elif [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jmap" ]; then
        "$JAVA_BIN/jmap" -histo:live "$PID" 2>/dev/null | head -20
    fi
    
    echo -e "\n${YELLOW}[3. 线程统计...]${NC}"
    [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstack" ] && echo "线程数: $("$JAVA_BIN/jstack" "$PID" 2>/dev/null | grep -c '^\"')"
}

case "$MODE" in
    cpu|CPU) analyze_cpu ;;
    mem|memory) analyze_memory ;;
    all|ALL) analyze_cpu; echo; analyze_memory ;;
    *) echo "用法: $0 <PID> [cpu|mem|all]" ;;
esac

echo -e "\n${BLUE}============================================================${NC}"
