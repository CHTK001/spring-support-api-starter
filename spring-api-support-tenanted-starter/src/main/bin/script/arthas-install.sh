#!/bin/bash
# ============================================================
# Arthas 在线安装脚本
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

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ARTHAS_DIR="${SCRIPT_DIR}/../arthas"
ARTHAS_BOOT="${ARTHAS_DIR}/arthas-boot.jar"
ARTHAS_VERSION="${ARTHAS_VERSION:-latest}"

PID="$1"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Arthas 诊断工具${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 [PID]           附加到指定进程"
    echo "  $0 install         仅安装/更新 Arthas"
    echo "  $0 help            显示帮助"
    echo ""
    echo -e "${YELLOW}环境变量:${NC}"
    echo "  ARTHAS_VERSION     指定版本 (默认: latest)"
    echo ""
}

# 下载 Arthas
install_arthas() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Arthas 安装${NC}"
    echo -e "${BLUE}============================================================${NC}"
    
    mkdir -p "$ARTHAS_DIR"
    
    echo -e "\n${YELLOW}[下载 Arthas...]${NC}"
    
    # 优先使用阿里云镜像
    DOWNLOAD_URL="https://arthas.aliyun.com/arthas-boot.jar"
    
    if command -v curl &> /dev/null; then
        curl -L --progress-bar -o "$ARTHAS_BOOT" "$DOWNLOAD_URL"
    elif command -v wget &> /dev/null; then
        wget -O "$ARTHAS_BOOT" "$DOWNLOAD_URL"
    else
        echo -e "${RED}[错误] 未找到 curl 或 wget${NC}"
        return 1
    fi
    
    if [ -f "$ARTHAS_BOOT" ] && [ -s "$ARTHAS_BOOT" ]; then
        echo -e "${GREEN}[成功] Arthas 已安装到: ${ARTHAS_DIR}${NC}"
        return 0
    else
        echo -e "${RED}[失败] Arthas 下载失败${NC}"
        return 1
    fi
}

# 启动 Arthas
start_arthas() {
    local pid="$1"
    
    # 检查是否已安装
    if [ ! -f "$ARTHAS_BOOT" ]; then
        echo -e "${YELLOW}[提示] Arthas 未安装，正在下载...${NC}"
        install_arthas || exit 1
    fi
    
    # 获取 Java
    JAVA_CMD="java"
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  启动 Arthas${NC}"
    echo -e "${BLUE}============================================================${NC}"
    
    if [ -n "$pid" ]; then
        echo -e "  目标进程: ${pid}"
        "$JAVA_CMD" -jar "$ARTHAS_BOOT" "$pid"
    else
        echo -e "  选择进程模式"
        "$JAVA_CMD" -jar "$ARTHAS_BOOT"
    fi
}

# 主逻辑
case "$1" in
    install)
        install_arthas
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        start_arthas "$1"
        ;;
esac
