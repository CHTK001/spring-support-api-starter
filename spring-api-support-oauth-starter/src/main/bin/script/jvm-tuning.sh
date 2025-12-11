#!/bin/bash
# ============================================================
# JVM 调优建议脚本 (AI 辅助)
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

JAVA_BIN=""
if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME/bin" ]; then
    JAVA_BIN="$JAVA_HOME/bin"
else
    jp=$(which java 2>/dev/null)
    [ -n "$jp" ] && JAVA_BIN=$(dirname $(readlink -f "$jp" 2>/dev/null || echo "$jp"))
fi

echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}  ${APP_NAME} JVM 调优分析${NC}"
echo -e "${BLUE}============================================================${NC}"

echo -e "\n${YELLOW}[收集系统信息...]${NC}"
TOTAL_MEM=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || echo "unknown")
CPU_CORES=$(nproc 2>/dev/null || echo "unknown")
echo "  总内存: ${TOTAL_MEM}MB | CPU核心: ${CPU_CORES}"

echo -e "\n${YELLOW}[收集JVM信息...]${NC}"
CURRENT_FLAGS=""
[ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ] && CURRENT_FLAGS=$("$JAVA_BIN/jcmd" "$PID" VM.flags 2>/dev/null | tail -n +2)

GC_STATS=""
[ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstat" ] && GC_STATS=$("$JAVA_BIN/jstat" -gcutil "$PID" 2>/dev/null | tail -1)

echo -e "\n${YELLOW}[当前JVM参数]${NC}"
echo "$CURRENT_FLAGS"

echo -e "\n${YELLOW}[GC统计]${NC}"
echo "$GC_STATS"

echo -e "\n${BLUE}============================================================${NC}"
echo -e "${GREEN}  调优建议${NC}"
echo -e "${BLUE}============================================================${NC}"

if [ -n "$DEEPSEEK_API_KEY" ] || [ -n "$OPENAI_API_KEY" ]; then
    echo -e "${CYAN}[调用 AI API...]${NC}"
else
    echo -e "${YELLOW}[本地规则分析...]${NC}"
    echo -e "${CYAN}(设置 DEEPSEEK_API_KEY 或 OPENAI_API_KEY 可获取 AI 建议)${NC}\n"
    
    if [ "$TOTAL_MEM" != "unknown" ] && [ "$TOTAL_MEM" -gt 0 ]; then
        HEAP_MAX=$((TOTAL_MEM * 60 / 100)); [ "$HEAP_MAX" -gt 8192 ] && HEAP_MAX=8192
        HEAP_MIN=$((HEAP_MAX / 2)); [ "$HEAP_MIN" -lt 512 ] && HEAP_MIN=512
        
        echo -e "${GREEN}推荐 JVM 参数:${NC}\n"
        echo "-Xms${HEAP_MIN}m -Xmx${HEAP_MAX}m"
        echo "-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m"
        echo "-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        echo "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/"
    fi
fi

echo -e "\n${BLUE}============================================================${NC}"
