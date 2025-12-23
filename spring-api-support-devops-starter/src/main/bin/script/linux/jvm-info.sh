#!/bin/bash
# ============================================================
# JVM 信息查看脚本
# 版本: 2.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

PID="$1"
APP_NAME="${2:-Application}"

if [ -z "$PID" ]; then
    echo -e "${RED}[错误] 请提供进程 PID${NC}"
    echo "用法: $0 <PID> [APP_NAME]"
    exit 1
fi

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo -e "${RED}[错误] 进程 $PID 不存在${NC}"
    exit 1
fi

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}                    JVM 运行信息                              ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}【进程信息】${NC}"
echo -e "  应用名称:   ${APP_NAME}"
echo -e "  进程 ID:    ${PID}"

# 获取进程启动时间
start_time=$(ps -o lstart= -p "$PID" 2>/dev/null | xargs)
[ -n "$start_time" ] && echo -e "  启动时间:   ${start_time}"

# 获取运行时长
etime=$(ps -o etime= -p "$PID" 2>/dev/null | xargs)
[ -n "$etime" ] && echo -e "  运行时长:   ${etime}"

echo ""

# JVM 版本信息
if command -v jps &> /dev/null; then
    echo -e "${CYAN}【JVM 进程】${NC}"
    jps -lvm 2>/dev/null | grep "$PID" | head -1
    echo ""
fi

# 内存使用
echo -e "${CYAN}【内存使用】${NC}"
mem_rss=$(ps -o rss= -p "$PID" 2>/dev/null | xargs)
if [ -n "$mem_rss" ]; then
    mem_mb=$((mem_rss / 1024))
    echo -e "  RSS 内存:   ${mem_mb} MB"
fi

mem_vsz=$(ps -o vsz= -p "$PID" 2>/dev/null | xargs)
if [ -n "$mem_vsz" ]; then
    vsz_mb=$((mem_vsz / 1024))
    echo -e "  VSZ 内存:   ${vsz_mb} MB"
fi

# CPU 使用
cpu=$(ps -o %cpu= -p "$PID" 2>/dev/null | xargs)
[ -n "$cpu" ] && echo -e "  CPU 使用:   ${cpu}%"

echo ""

# GC 统计
if command -v jstat &> /dev/null; then
    echo -e "${CYAN}【GC 统计】${NC}"
    echo -e "  ${YELLOW}S0     S1     E      O      M      CCS    YGC    YGCT   FGC    FGCT   GCT${NC}"
    jstat -gcutil "$PID" 2>/dev/null | tail -1
    echo ""
    
    echo -e "${CYAN}【堆内存】${NC}"
    jstat -gc "$PID" 2>/dev/null | head -2
    echo ""
fi

# 线程信息
if command -v jstack &> /dev/null; then
    echo -e "${CYAN}【线程统计】${NC}"
    thread_count=$(jstack "$PID" 2>/dev/null | grep -c "java.lang.Thread.State")
    runnable=$(jstack "$PID" 2>/dev/null | grep -c "RUNNABLE")
    waiting=$(jstack "$PID" 2>/dev/null | grep -c "WAITING")
    timed_waiting=$(jstack "$PID" 2>/dev/null | grep -c "TIMED_WAITING")
    blocked=$(jstack "$PID" 2>/dev/null | grep -c "BLOCKED")
    
    echo -e "  总线程数:     ${thread_count}"
    echo -e "  RUNNABLE:     ${runnable}"
    echo -e "  WAITING:      ${waiting}"
    echo -e "  TIMED_WAITING: ${timed_waiting}"
    echo -e "  BLOCKED:      ${blocked}"
    
    if [ "$blocked" -gt 0 ]; then
        echo -e "  ${RED}[警告] 存在阻塞线程!${NC}"
    fi
fi

echo ""
