#!/bin/bash
# ============================================================
# Skywalking Agent 管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# Skywalking 配置
SKYWALKING_DIR="${BASE_DIR}/skywalking"
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.3.0}"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SKYWALKING_CONFIG="${SKYWALKING_DIR}/skywalking-agent/config/agent.config"

# 下载地址
SKYWALKING_DOWNLOAD_URL="https://archive.apache.org/dist/skywalking/java-agent/${SKYWALKING_VERSION}/apache-skywalking-java-agent-${SKYWALKING_VERSION}.tgz"
SKYWALKING_MIRROR_URL="https://mirrors.tuna.tsinghua.edu.cn/apache/skywalking/java-agent/${SKYWALKING_VERSION}/apache-skywalking-java-agent-${SKYWALKING_VERSION}.tgz"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}Skywalking Agent 管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  (无参数)    显示 Agent 信息"
    echo "  -i          安装 Skywalking Agent"
    echo "  upgrade     升级到最新版本"
    echo "  -u          卸载 Agent"
    echo "  config      配置 Agent"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --version <ver>  指定版本号 (默认 ${SKYWALKING_VERSION})"
    echo "  -h, --help       显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                           显示 Agent 信息"
    echo "  $0 -i                        安装 Agent"
    echo "  $0 -i --version 9.2.0        安装指定版本"
    echo "  $0 upgrade                   升级 Agent"
    echo "  $0 -u                        卸载 Agent"
    echo "  $0 config                    配置 Agent"
    echo ""
}

# ===========================================
# 检查是否已安装
# ===========================================
is_installed() {
    [ -f "${SKYWALKING_AGENT_JAR}" ]
}

# ===========================================
# 获取当前版本
# ===========================================
get_current_version() {
    if is_installed; then
        # 尝试从目录名获取版本
        local version_file="${SKYWALKING_DIR}/skywalking-agent/VERSION"
        if [ -f "$version_file" ]; then
            cat "$version_file" 2>/dev/null
        else
            # 从配置文件获取
            grep -E "^agent.version=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2 || echo "未知"
        fi
    else
        echo "未安装"
    fi
}

# ===========================================
# 显示信息
# ===========================================
show_info() {
    print_title "Skywalking Agent 信息"
    echo ""
    
    echo -e "${YELLOW}基本信息:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "  目录:     ${WHITE}${SKYWALKING_DIR}${NC}"
    echo -e "  Agent:    ${WHITE}${SKYWALKING_AGENT_JAR}${NC}"
    
    if is_installed; then
        local current_ver=$(get_current_version)
        echo -e "  状态:     ${GREEN}已安装${NC}"
        echo -e "  版本:     ${WHITE}${current_ver}${NC}"
        local size=$(du -h "${SKYWALKING_AGENT_JAR}" 2>/dev/null | cut -f1)
        echo -e "  大小:     ${WHITE}${size}${NC}"
        
        # 显示配置信息
        if [ -f "${SKYWALKING_CONFIG}" ]; then
            echo ""
            echo -e "${YELLOW}当前配置:${NC}"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            local service_name=$(grep -E "^agent.service_name=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2)
            local backend=$(grep -E "^collector.backend_service=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2)
            [ -n "$service_name" ] && echo -e "  服务名:   ${WHITE}${service_name}${NC}"
            [ -n "$backend" ] && echo -e "  后端地址: ${WHITE}${backend}${NC}"
        fi
    else
        echo -e "  状态:     ${YELLOW}未安装${NC}"
    fi
    echo ""
}

# ===========================================
# 安装
# ===========================================
do_install() {
    local version="${1:-$SKYWALKING_VERSION}"
    local force="${2:-false}"
    
    print_title "安装 Skywalking Agent"
    echo ""
    
    # 检查是否已安装
    if is_installed && [ "$force" = "false" ]; then
        log_warning "Skywalking Agent 已安装"
        local current_ver=$(get_current_version)
        echo -e "  当前版本: ${WHITE}${current_ver}${NC}"
        echo ""
        read -p "是否重新安装? [y/N] " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            return 0
        fi
        # 备份配置
        if [ -f "${SKYWALKING_CONFIG}" ]; then
            cp "${SKYWALKING_CONFIG}" "${SKYWALKING_CONFIG}.bak"
            log_info "已备份配置文件"
        fi
    fi
    
    # 创建目录
    mkdir -p "${SKYWALKING_DIR}"
    
    local download_url="https://archive.apache.org/dist/skywalking/java-agent/${version}/apache-skywalking-java-agent-${version}.tgz"
    local tmp_file="${SKYWALKING_DIR}/skywalking-agent.tgz"
    
    log_step "下载 Skywalking Agent v${version}..."
    
    local download_success=false
    
    # 尝试主下载地址
    if command -v curl &> /dev/null; then
        if curl -L --progress-bar -o "${tmp_file}" "${download_url}" 2>/dev/null; then
            download_success=true
        fi
    elif command -v wget &> /dev/null; then
        if wget -O "${tmp_file}" "${download_url}" 2>/dev/null; then
            download_success=true
        fi
    else
        log_error "未找到 curl 或 wget"
        return 1
    fi
    
    # 尝试镜像地址
    if [ "$download_success" = false ]; then
        log_warning "主下载地址失败，尝试镜像..."
        local mirror_url="https://mirrors.tuna.tsinghua.edu.cn/apache/skywalking/java-agent/${version}/apache-skywalking-java-agent-${version}.tgz"
        if command -v curl &> /dev/null; then
            curl -L --progress-bar -o "${tmp_file}" "${mirror_url}" 2>/dev/null && download_success=true
        elif command -v wget &> /dev/null; then
            wget -O "${tmp_file}" "${mirror_url}" 2>/dev/null && download_success=true
        fi
    fi
    
    if [ "$download_success" = false ] || [ ! -f "${tmp_file}" ] || [ ! -s "${tmp_file}" ]; then
        log_error "下载失败"
        rm -f "${tmp_file}"
        return 1
    fi
    
    # 清理旧版本
    if [ -d "${SKYWALKING_DIR}/skywalking-agent" ]; then
        rm -rf "${SKYWALKING_DIR}/skywalking-agent"
    fi
    
    log_step "解压 Agent..."
    tar -xzf "${tmp_file}" -C "${SKYWALKING_DIR}"
    rm -f "${tmp_file}"
    
    if is_installed; then
        # 恢复配置
        if [ -f "${SKYWALKING_CONFIG}.bak" ]; then
            cp "${SKYWALKING_CONFIG}.bak" "${SKYWALKING_CONFIG}"
            rm -f "${SKYWALKING_CONFIG}.bak"
            log_info "已恢复配置文件"
        fi
        
        # 保存版本信息
        echo "$version" > "${SKYWALKING_DIR}/skywalking-agent/VERSION"
        
        log_info "Skywalking Agent v${version} 安装成功"
        echo ""
        echo -e "  Agent: ${WHITE}${SKYWALKING_AGENT_JAR}${NC}"
        echo -e "  配置:  ${WHITE}${SKYWALKING_CONFIG}${NC}"
        echo ""
    else
        log_error "安装失败"
        return 1
    fi
}

# ===========================================
# 升级
# ===========================================
do_upgrade() {
    local version="${1:-$SKYWALKING_VERSION}"
    
    print_title "升级 Skywalking Agent"
    echo ""
    
    if ! is_installed; then
        log_warning "Skywalking Agent 未安装，将执行全新安装"
        do_install "$version" "true"
        return $?
    fi
    
    local current_ver=$(get_current_version)
    echo -e "  当前版本: ${WHITE}${current_ver}${NC}"
    echo -e "  目标版本: ${WHITE}${version}${NC}"
    echo ""
    
    if [ "$current_ver" = "$version" ]; then
        log_info "已是最新版本"
        return 0
    fi
    
    read -p "确认升级? [Y/n] " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Nn]$ ]]; then
        log_info "取消升级"
        return 0
    fi
    
    do_install "$version" "true"
}

# ===========================================
# 卸载
# ===========================================
do_uninstall() {
    print_title "卸载 Skywalking Agent"
    echo ""
    
    if ! is_installed; then
        log_warning "Skywalking Agent 未安装"
        return 0
    fi
    
    read -p "确认卸载 Skywalking Agent? [y/N] " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "取消卸载"
        return 0
    fi
    
    rm -rf "${SKYWALKING_DIR}"
    log_info "Skywalking Agent 已卸载"
}

# ===========================================
# 配置
# ===========================================
do_config() {
    print_title "配置 Skywalking Agent"
    echo ""
    
    if ! is_installed; then
        log_error "Skywalking Agent 未安装"
        return 1
    fi
    
    if [ ! -f "${SKYWALKING_CONFIG}" ]; then
        log_error "配置文件不存在: ${SKYWALKING_CONFIG}"
        return 1
    fi
    
    # 获取当前配置
    local current_service=$(grep -E "^agent.service_name=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2)
    local current_backend=$(grep -E "^collector.backend_service=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2)
    
    echo -e "${YELLOW}当前配置:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "  服务名:   ${WHITE}${current_service:-Your_ApplicationName}${NC}"
    echo -e "  后端地址: ${WHITE}${current_backend:-127.0.0.1:11800}${NC}"
    echo ""
    
    # 输入新配置
    read -p "服务名 [${current_service:-${APP_NAME}}]: " new_service
    new_service="${new_service:-${current_service:-${APP_NAME}}}"
    
    read -p "后端地址 [${current_backend:-127.0.0.1:11800}]: " new_backend
    new_backend="${new_backend:-${current_backend:-127.0.0.1:11800}}"
    
    echo ""
    
    # 更新配置
    log_step "更新配置..."
    
    # 更新服务名
    if grep -q "^agent.service_name=" "${SKYWALKING_CONFIG}"; then
        sed -i "s/^agent.service_name=.*/agent.service_name=${new_service}/" "${SKYWALKING_CONFIG}"
    else
        echo "agent.service_name=${new_service}" >> "${SKYWALKING_CONFIG}"
    fi
    
    # 更新后端地址
    if grep -q "^collector.backend_service=" "${SKYWALKING_CONFIG}"; then
        sed -i "s/^collector.backend_service=.*/collector.backend_service=${new_backend}/" "${SKYWALKING_CONFIG}"
    else
        echo "collector.backend_service=${new_backend}" >> "${SKYWALKING_CONFIG}"
    fi
    
    log_info "配置已更新"
    echo ""
    echo -e "  服务名:   ${WHITE}${new_service}${NC}"
    echo -e "  后端地址: ${WHITE}${new_backend}${NC}"
    echo ""
}

# ===========================================
# 参数解析
# ===========================================
VERSION="$SKYWALKING_VERSION"

while [ $# -gt 0 ]; do
    case "$1" in
        --version)
            VERSION="$2"
            shift 2
            ;;
        -i|--install)
            do_install "$VERSION"
            exit $?
            ;;
        upgrade)
            do_upgrade "$VERSION"
            exit $?
            ;;
        -u|--uninstall)
            do_uninstall
            exit $?
            ;;
        config)
            do_config
            exit $?
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        "")
            show_info
            exit 0
            ;;
        *)
            log_error "未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
done

# 默认显示信息
show_info
