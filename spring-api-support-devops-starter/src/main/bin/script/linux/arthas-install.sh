#!/bin/bash
# ============================================================
# Arthas 在线安装脚本
# 版本: 2.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../../.." && pwd)"
ARTHAS_DIR="${APP_HOME}/arthas"
ARTHAS_BOOT="${ARTHAS_DIR}/arthas-boot.jar"
PID="$1"

install_arthas() {
    mkdir -p "$ARTHAS_DIR"
    echo -e "${YELLOW}[下载 Arthas...]${NC}"
    
    if command -v curl &> /dev/null; then
        curl -L --progress-bar -o "$ARTHAS_BOOT" "https://arthas.aliyun.com/arthas-boot.jar"
    elif command -v wget &> /dev/null; then
        wget -O "$ARTHAS_BOOT" "https://arthas.aliyun.com/arthas-boot.jar"
    else
        echo -e "${RED}[错误] 未找到 curl 或 wget${NC}"
        return 1
    fi
    
    if [ -f "$ARTHAS_BOOT" ] && [ -s "$ARTHAS_BOOT" ]; then
        echo -e "${GREEN}[成功] Arthas 已安装${NC}"
        return 0
    fi
    echo -e "${RED}[失败] 下载失败${NC}"
    return 1
}

start_arthas() {
    [ ! -f "$ARTHAS_BOOT" ] && install_arthas
    
    JAVA_CMD="java"
    [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] && JAVA_CMD="$JAVA_HOME/bin/java"
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                    启动 Arthas                               ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    
    if [ -n "$1" ]; then
        "$JAVA_CMD" -jar "$ARTHAS_BOOT" "$1"
    else
        "$JAVA_CMD" -jar "$ARTHAS_BOOT"
    fi
}

case "$1" in
    install) install_arthas ;;
    help|--help|-h) echo "用法: $0 [PID|install|help]" ;;
    *) start_arthas "$1" ;;
esac
