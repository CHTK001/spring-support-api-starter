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

# 获取 Java 工具路径
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

# 收集系统信息
echo -e "\n${YELLOW}[收集系统信息...]${NC}"
TOTAL_MEM=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || echo "unknown")
CPU_CORES=$(nproc 2>/dev/null || echo "unknown")
echo "  总内存: ${TOTAL_MEM}MB"
echo "  CPU核心: ${CPU_CORES}"

# 收集当前 JVM 信息
echo -e "\n${YELLOW}[收集JVM信息...]${NC}"
CURRENT_FLAGS=""
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
    CURRENT_FLAGS=$("$JAVA_BIN/jcmd" "$PID" VM.flags 2>/dev/null | tail -n +2)
fi

# GC 统计
GC_STATS=""
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jstat" ]; then
    GC_STATS=$("$JAVA_BIN/jstat" -gcutil "$PID" 2>/dev/null | tail -1)
fi

# 堆内存使用
HEAP_INFO=""
if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/jcmd" ]; then
    HEAP_INFO=$("$JAVA_BIN/jcmd" "$PID" GC.heap_info 2>/dev/null | tail -n +2)
fi

echo -e "\n${YELLOW}[当前JVM参数]${NC}"
echo "$CURRENT_FLAGS"

echo -e "\n${YELLOW}[GC统计]${NC}"
echo "$GC_STATS"

# 构建分析提示
PROMPT="根据以下JVM运行信息，给出优化建议和推荐的JVM参数配置：
系统信息：总内存${TOTAL_MEM}MB，CPU${CPU_CORES}核
当前JVM参数：${CURRENT_FLAGS}
GC统计(S0 S1 E O M CCS YGC YGCT FGC FGCT GCT)：${GC_STATS}
堆内存使用：${HEAP_INFO}
请输出推荐的JVM启动参数，包括堆大小、GC选择、GC调优参数等。"

echo -e "\n${BLUE}============================================================${NC}"
echo -e "${GREEN}  AI 调优建议${NC}"
echo -e "${BLUE}============================================================${NC}"

# 尝试调用 AI 接口获取建议
# 优先使用环境变量中的 API Key
if [ -n "$DEEPSEEK_API_KEY" ]; then
    echo -e "${CYAN}[使用 DeepSeek API...]${NC}"
    RESPONSE=$(curl -s --max-time 30 "https://api.deepseek.com/v1/chat/completions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $DEEPSEEK_API_KEY" \
        -d "{
            \"model\": \"deepseek-chat\",
            \"messages\": [{\"role\": \"user\", \"content\": \"$PROMPT\"}],
            \"max_tokens\": 1000
        }" 2>/dev/null)
    
    if [ -n "$RESPONSE" ]; then
        echo "$RESPONSE" | grep -o '"content":"[^"]*"' | sed 's/"content":"//;s/"$//' | sed 's/\\n/\n/g'
    fi
elif [ -n "$OPENAI_API_KEY" ]; then
    echo -e "${CYAN}[使用 OpenAI API...]${NC}"
    RESPONSE=$(curl -s --max-time 30 "https://api.openai.com/v1/chat/completions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $OPENAI_API_KEY" \
        -d "{
            \"model\": \"gpt-3.5-turbo\",
            \"messages\": [{\"role\": \"user\", \"content\": \"$PROMPT\"}],
            \"max_tokens\": 1000
        }" 2>/dev/null)
    
    if [ -n "$RESPONSE" ]; then
        echo "$RESPONSE" | grep -o '"content":"[^"]*"' | sed 's/"content":"//;s/"$//' | sed 's/\\n/\n/g'
    fi
else
    # 无 API Key，使用本地规则生成建议
    echo -e "${YELLOW}[本地规则分析...]${NC}"
    echo -e "${CYAN}(设置 DEEPSEEK_API_KEY 或 OPENAI_API_KEY 可获取 AI 调优建议)${NC}\n"
    
    # 基于系统内存的推荐
    if [ "$TOTAL_MEM" != "unknown" ] && [ "$TOTAL_MEM" -gt 0 ]; then
        HEAP_MAX=$((TOTAL_MEM * 60 / 100))
        HEAP_MIN=$((HEAP_MAX / 2))
        META_SIZE=256
        
        [ "$HEAP_MAX" -gt 8192 ] && HEAP_MAX=8192
        [ "$HEAP_MIN" -lt 512 ] && HEAP_MIN=512
        
        echo -e "${GREEN}推荐 JVM 参数:${NC}"
        echo ""
        echo "# 堆内存配置"
        echo "-Xms${HEAP_MIN}m"
        echo "-Xmx${HEAP_MAX}m"
        echo "-XX:MetaspaceSize=${META_SIZE}m"
        echo "-XX:MaxMetaspaceSize=${META_SIZE}m"
        echo ""
        echo "# GC 配置 (推荐 G1GC)"
        echo "-XX:+UseG1GC"
        echo "-XX:MaxGCPauseMillis=200"
        echo "-XX:G1HeapRegionSize=16m"
        echo "-XX:InitiatingHeapOccupancyPercent=45"
        echo ""
        echo "# GC 日志"
        echo "-Xlog:gc*:file=logs/gc.log:time,uptime:filecount=5,filesize=10m"
        echo ""
        echo "# 其他优化"
        echo "-XX:+HeapDumpOnOutOfMemoryError"
        echo "-XX:HeapDumpPath=logs/"
        echo "-XX:+DisableExplicitGC"
    fi
fi

echo -e "\n${BLUE}============================================================${NC}"
