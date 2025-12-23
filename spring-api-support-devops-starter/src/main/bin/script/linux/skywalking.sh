#!/bin/bash
# ============================================================
# Skywalking Agent 管理脚本
# 版本: 2.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../../.." && pwd)"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"

show_help() {
    echo -e "${BLUE}Skywalking Agent 管理脚本${NC}"
    echo ""
    echo "用法: $0 <命令>"
    echo ""
    echo "命令:"
    echo "  install    下载并安装 Skywalking Agent"
    echo "  check      检查 Agent 是否已安装"
    echo "  clean      清理 Agent 文件"
    echo "  info       显示 Agent 信息"
    echo ""
}

do_install() {
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[提示] Skywalking Agent 已存在${NC}"
        return 0
    fi
    
    mkdir -p "${SKYWALKING_DIR}"
    
    local download_url="https://archive.apache.org/dist/skywalking/java-agent/${SKYWALKING_VERSION}/apache-skywalking-java-agent-${SKYWALKING_VERSION}.tgz"
    local tmp_file="${SKYWALKING_DIR}/skywalking-agent.tgz"
    
    echo -e "${BLUE}下载 Skywalking Agent v${SKYWALKING_VERSION}...${NC}"
    
    if command -v curl &> /dev/null; then
        curl -L --progress-bar -o "${tmp_file}" "${download_url}"
    elif command -v wget &> /dev/null; then
        wget -O "${tmp_file}" "${download_url}"
    else
        echo -e "${RED}[错误] 未找到 curl 或 wget${NC}"
        return 1
    fi
    
    if [ ! -f "${tmp_file}" ] || [ ! -s "${tmp_file}" ]; then
        echo -e "${RED}[错误] 下载失败${NC}"
        return 1
    fi
    
    echo -e "${BLUE}解压 Agent...${NC}"
    tar -xzf "${tmp_file}" -C "${SKYWALKING_DIR}"
    rm -f "${tmp_file}"
    
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${GREEN}[成功] Skywalking Agent 已安装${NC}"
        return 0
    else
        echo -e "${RED}[错误] 安装失败${NC}"
        return 1
    fi
}

do_check() {
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${GREEN}[已安装]${NC} Skywalking Agent"
        echo "  路径: ${SKYWALKING_AGENT_JAR}"
        return 0
    else
        echo -e "${YELLOW}[未安装]${NC} Skywalking Agent"
        return 1
    fi
}

do_clean() {
    if [ -d "${SKYWALKING_DIR}" ]; then
        rm -rf "${SKYWALKING_DIR}"
        echo -e "${GREEN}[成功] Skywalking Agent 已清理${NC}"
    else
        echo -e "${YELLOW}[提示] Skywalking 目录不存在${NC}"
    fi
}

do_info() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}              Skywalking Agent 信息                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  版本:     ${SKYWALKING_VERSION}"
    echo -e "  目录:     ${SKYWALKING_DIR}"
    echo -e "  Agent:    ${SKYWALKING_AGENT_JAR}"
    
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "  状态:     ${GREEN}已安装${NC}"
        local size=$(du -h "${SKYWALKING_AGENT_JAR}" 2>/dev/null | cut -f1)
        echo -e "  大小:     ${size}"
    else
        echo -e "  状态:     ${YELLOW}未安装${NC}"
    fi
    echo ""
}

case "${1:-}" in
    install) do_install ;;
    check) do_check ;;
    clean) do_clean ;;
    info) do_info ;;
    --help|-h|help) show_help ;;
    *) show_help ;;
esac
