#!/bin/bash
# ============================================================
# Skywalking Agent 安装管理脚本
# 作者: CH
# 创建时间: 2024/12/17
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_AGENT_DIR="${SKYWALKING_DIR}/skywalking-agent"
SKYWALKING_AGENT_JAR="${SKYWALKING_AGENT_DIR}/skywalking-agent.jar"

# Skywalking 配置
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_DOWNLOAD_URL="${SKYWALKING_DOWNLOAD_URL:-https://archive.apache.org/dist/skywalking/${SKYWALKING_VERSION}/apache-skywalking-java-agent-${SKYWALKING_VERSION}.tgz}"

# Agent 配置
SW_AGENT_NAME="${SW_AGENT_NAME:-}"
SW_AGENT_COLLECTOR_BACKEND_SERVICES="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-127.0.0.1:11800}"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Skywalking Agent 管理脚本${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  install     下载并安装 Skywalking Agent"
    echo "  check       检查 Skywalking Agent 是否已安装"
    echo "  clean       清理/删除 Skywalking Agent"
    echo "  info        显示 Skywalking 配置信息"
    echo "  help        显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  SKYWALKING_VERSION                  版本号 (默认: 9.0.0)"
    echo "  SKYWALKING_DOWNLOAD_URL             自定义下载地址"
    echo "  SW_AGENT_NAME                       服务名"
    echo "  SW_AGENT_COLLECTOR_BACKEND_SERVICES OAP地址 (默认: 127.0.0.1:11800)"
    echo ""
}

install_skywalking() {
    if [ -f "$SKYWALKING_AGENT_JAR" ]; then
        echo -e "${YELLOW}[警告] Skywalking Agent 已存在: ${SKYWALKING_AGENT_JAR}${NC}"
        read -p "是否重新下载? [y/N]: " confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            echo -e "${BLUE}[跳过] 保留现有安装${NC}"
            return 0
        fi
        rm -rf "${SKYWALKING_DIR}"
    fi
    
    mkdir -p "${SKYWALKING_DIR}"
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  下载 Skywalking Agent${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo -e "  版本: ${SKYWALKING_VERSION}"
    echo -e "  下载地址: ${SKYWALKING_DOWNLOAD_URL}"
    echo -e "  安装目录: ${SKYWALKING_DIR}"
    echo ""
    
    local tmp_file="${SKYWALKING_DIR}/skywalking-agent.tgz"
    
    echo -e "${YELLOW}[下载中...]${NC}"
    if command -v curl &> /dev/null; then
        curl -L --progress-bar -o "${tmp_file}" "${SKYWALKING_DOWNLOAD_URL}"
    elif command -v wget &> /dev/null; then
        wget --show-progress -O "${tmp_file}" "${SKYWALKING_DOWNLOAD_URL}"
    else
        echo -e "${RED}[错误] 未找到 curl 或 wget，请先安装${NC}"
        return 1
    fi
    
    if [ ! -f "${tmp_file}" ] || [ ! -s "${tmp_file}" ]; then
        echo -e "${RED}[错误] 下载失败${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}[解压中...]${NC}"
    tar -xzf "${tmp_file}" -C "${SKYWALKING_DIR}"
    rm -f "${tmp_file}"
    
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${GREEN}[成功] Skywalking Agent 安装完成${NC}"
        echo -e "  Agent JAR: ${SKYWALKING_AGENT_JAR}"
        return 0
    else
        echo -e "${RED}[错误] 解压后未找到 Agent JAR${NC}"
        echo -e "请检查下载地址或手动下载"
        return 1
    fi
}

check_skywalking() {
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${GREEN}[已安装] Skywalking Agent${NC}"
        echo -e "  路径: ${SKYWALKING_AGENT_JAR}"
        return 0
    else
        echo -e "${YELLOW}[未安装] Skywalking Agent${NC}"
        echo -e "  运行 '$0 install' 进行安装"
        return 1
    fi
}

clean_skywalking() {
    if [ -d "${SKYWALKING_DIR}" ]; then
        echo -e "${YELLOW}[清理] 删除 Skywalking 目录...${NC}"
        rm -rf "${SKYWALKING_DIR}"
        echo -e "${GREEN}[完成] 清理完成${NC}"
    else
        echo -e "${BLUE}[信息] Skywalking 目录不存在${NC}"
    fi
}

show_info() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Skywalking 配置信息${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "  版本:        ${SKYWALKING_VERSION}"
    echo -e "  安装目录:    ${SKYWALKING_DIR}"
    echo -e "  Agent JAR:   ${SKYWALKING_AGENT_JAR}"
    echo -e "  下载地址:    ${SKYWALKING_DOWNLOAD_URL}"
    echo ""
    echo -e "  Agent 配置:"
    echo -e "    服务名:    ${SW_AGENT_NAME:-<使用应用名>}"
    echo -e "    OAP地址:   ${SW_AGENT_COLLECTOR_BACKEND_SERVICES}"
    echo ""
    if [ -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "  状态: ${GREEN}已安装${NC}"
    else
        echo -e "  状态: ${RED}未安装${NC}"
    fi
    echo ""
}

# 获取 Agent JAR 路径 (供外部脚本调用)
get_agent_jar() {
    echo "${SKYWALKING_AGENT_JAR}"
}

# 确保 Agent 已安装 (供外部脚本调用)
ensure_installed() {
    if [ ! -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[提示] Skywalking Agent 未安装，开始自动下载...${NC}"
        install_skywalking
    fi
    return $?
}

# 主入口
case "${1:-}" in
    install)
        install_skywalking
        ;;
    check)
        check_skywalking
        ;;
    clean)
        clean_skywalking
        ;;
    info)
        show_info
        ;;
    get-jar)
        get_agent_jar
        ;;
    ensure)
        ensure_installed
        ;;
    help|--help|-h)
        show_help
        ;;
    "")
        # 默认行为：检查，未安装则提示
        if ! check_skywalking; then
            echo ""
            read -p "是否下载安装 Skywalking Agent? [Y/n]: " confirm
            if [ "$confirm" != "n" ] && [ "$confirm" != "N" ]; then
                install_skywalking
            fi
        fi
        ;;
    *)
        echo -e "${RED}[错误] 未知命令: $1${NC}"
        show_help
        exit 1
        ;;
esac
