#!/bin/bash
# ============================================================
# JVM 信息查看脚本
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
APP_NAME="${2:-Java应用}"

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
echo -e "${GREEN}  ${APP_NAME} JVM 信息${NC}"
echo -e "${BLUE}============================================================${NC}"
echo -e "  进程ID: ${PID}"
echo -e "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo -e "${BLUE}============================================================${NC}"

# 进程信息
echo -e "\n${YELLOW}[进程信息]${NC}"
ps -p "$PID" -o pid,%cpu,%mem,rss,start,time --no-headers 2>/dev/null

# JVM 版本
echo -e "\n${YELLOW}[JVM版本]${NC}"
[ -n "$JAVA_BIN" ] && "$JAVA_BIN/java" -version 2>&1 | head -1

# JVM 参数
echo -e "\n${YELLOW}[JVM参数]${NC}"
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
    "$JAVA_BIN/jcmd" "$PID" VM.flags 2>/dev/null | tail -n +2
elif [ -f /proc/"$PID"/cmdline ]; then
    cat /proc/"$PID"/cmdline 2>/dev/null | tr '\0' '\n' | grep -E "^-X|^-D" | head -10
fi

# 堆内存
echo -e "\n${YELLOW}[堆内存]${NC}"
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
    "$JAVA_BIN/jcmd" "$PID" GC.heap_info 2>/dev/null | tail -n +2
elif [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstat" ]; then
    "$JAVA_BIN/jstat" -gc "$PID" 2>/dev/null
fi

# GC 统计
echo -e "\n${YELLOW}[GC统计]${NC}"
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstat" ]; then
    "$JAVA_BIN/jstat" -gcutil "$PID" 2>/dev/null
    echo -e "${CYAN}S0/S1=Survivor E=Eden O=Old M=Meta YGC/FGC=GC次数 GCT=总耗时(秒)${NC}"
fi

# 线程统计
echo -e "\n${YELLOW}[线程统计]${NC}"
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
    thread_count=$("$JAVA_BIN/jcmd" "$PID" Thread.print 2>/dev/null | grep -c "^\"")
    echo "活跃线程数: $thread_count"
elif [ -d /proc/"$PID"/task ]; then
    echo "线程数: $(ls /proc/$PID/task 2>/dev/null | wc -l)"
fi

echo -e "\n${BLUE}============================================================${NC}"
