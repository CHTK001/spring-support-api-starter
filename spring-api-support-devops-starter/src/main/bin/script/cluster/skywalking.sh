#!/bin/bash
# ============================================================
# 集群 Skywalking Agent 管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# Skywalking 配置
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.3.0}"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SKYWALKING_CONFIG="${SKYWALKING_DIR}/skywalking-agent/config/agent.config"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群 Skywalking Agent 管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh skywalking [命令]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  (无参数)    显示 Agent 信息"
    echo "  install     安装 Skywalking Agent"
    echo "  uninstall   卸载 Agent"
    echo "  config      配置 Agent"
    echo "  status      查看各实例 Skywalking 状态"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --version <ver>    指定版本号 (默认: ${SKYWALKING_VERSION})"
    echo "  --backend <addr>   设置后端地址 (默认: 127.0.0.1:11800)"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh skywalking                     显示 Agent 信息"
    echo "  cluster.sh skywalking install             安装 Agent"
    echo "  cluster.sh skywalking install --version 9.2.0  安装指定版本"
    echo "  cluster.sh skywalking config --backend 10.0.0.1:11800"
    echo "  cluster.sh skywalking status              查看各实例状态"
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
        local version_file="${SKYWALKING_DIR}/skywalking-agent/VERSION"
        if [ -f "$version_file" ]; then
            cat "$version_file" 2>/dev/null
        else
            echo "未知"
        fi
    else
        echo "未安装"
    fi
}

# ===========================================
# 显示信息
# ===========================================
show_info() {
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}               Skywalking Agent 信息                          ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    echo -e "${CYAN}【安装信息】${NC}"
    echo -e "  目录:       ${WHITE}${SKYWALKING_DIR}${NC}"
    
    if is_installed; then
        local current_ver=$(get_current_version)
        echo -e "  状态:       ${GREEN}已安装${NC}"
        echo -e "  版本:       ${WHITE}${current_ver}${NC}"
        local size=$(du -h "${SKYWALKING_AGENT_JAR}" 2>/dev/null | cut -f1)
        echo -e "  Agent大小:  ${WHITE}${size}${NC}"
        
        # 显示配置
        if [ -f "${SKYWALKING_CONFIG}" ]; then
            echo ""
            echo -e "${CYAN}【当前配置】${NC}"
            local backend=$(grep -E "^collector.backend_service=" "${SKYWALKING_CONFIG}" 2>/dev/null | cut -d= -f2)
            [ -n "$backend" ] && echo -e "  后端地址:   ${WHITE}${backend}${NC}"
        fi
    else
        echo -e "  状态:       ${YELLOW}未安装${NC}"
        echo ""
        echo -e "${YELLOW}提示: 使用 'cluster.sh skywalking install' 安装${NC}"
    fi
    echo ""
    
    # 显示各实例状态
    show_instance_status
}

# ===========================================
# 显示实例Skywalking状态
# ===========================================
show_instance_status() {
    if [ -z "$LOCAL_PORTS" ]; then
        return
    fi
    
    echo -e "${CYAN}【实例状态】${NC}"
    IFS=',' read -ra ports <<< "$LOCAL_PORTS"
    for port in "${ports[@]}"; do
        port=$(echo "$port" | tr -d ' ')
        local instance_name="${APP_NAME}-${port}"
        local pid_file="${APP_HOME}/${instance_name}.pid"
        
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file" 2>/dev/null)
            if [ -n "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
                local cmdline=$(ps -p "$pid" -o args= 2>/dev/null)
                if echo "$cmdline" | grep -q "skywalking-agent"; then
                    echo -e "  ${instance_name}: ${GREEN}已启用${NC}"
                else
                    echo -e "  ${instance_name}: ${YELLOW}未启用${NC}"
                fi
            else
                echo -e "  ${instance_name}: ${RED}未运行${NC}"
            fi
        else
            echo -e "  ${instance_name}: ${RED}未运行${NC}"
        fi
    done
    echo ""
}

# ===========================================
# 安装
# ===========================================
do_install() {
    local sw_ver="$1"
    
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}             安装 Skywalking Agent                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # 检查是否已安装
    if is_installed; then
        local current_ver=$(get_current_version)
        log_warn "Skywalking Agent 已安装 (版本: ${current_ver})"
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
    
    local download_url="https://archive.apache.org/dist/skywalking/java-agent/${sw_ver}/apache-skywalking-java-agent-${sw_ver}.tgz"
    local tmp_file="${SKYWALKING_DIR}/skywalking-agent.tgz"
    
    log_step "下载 Skywalking Agent v${sw_ver}..."
    echo "  下载地址: ${download_url}"
    
    local download_success=false
    
    # 下载
    if command -v curl &> /dev/null; then
        if curl -fL# -o "${tmp_file}" "${download_url}"; then
            download_success=true
        fi
    elif command -v wget &> /dev/null; then
        if wget -q --show-progress -O "${tmp_file}" "${download_url}"; then
            download_success=true
        fi
    else
        log_error "未找到 curl 或 wget"
        return 1
    fi
    
    # 尝试镜像
    if [ "$download_success" = false ]; then
        log_warn "主下载地址失败，尝试镜像..."
        local mirror_url="https://mirrors.tuna.tsinghua.edu.cn/apache/skywalking/java-agent/${sw_ver}/apache-skywalking-java-agent-${sw_ver}.tgz"
        if command -v curl &> /dev/null; then
            curl -fL# -o "${tmp_file}" "${mirror_url}" && download_success=true
        elif command -v wget &> /dev/null; then
            wget -q --show-progress -O "${tmp_file}" "${mirror_url}" && download_success=true
        fi
    fi
    
    if [ "$download_success" = false ] || [ ! -f "${tmp_file}" ] || [ ! -s "${tmp_file}" ]; then
        log_error "下载失败，请检查版本号: ${sw_ver}"
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
        
        # 保存版本
        echo "${sw_ver}" > "${SKYWALKING_DIR}/skywalking-agent/VERSION"
        
        log_info "Skywalking Agent v${sw_ver} 安装成功!"
        echo ""
        echo -e "${YELLOW}提示:${NC}"
        echo "  1. 配置后端: cluster.sh skywalking config --backend <地址>"
        echo "  2. 启动时启用: cluster.sh start --skywalking --ports 8080,8081"
        echo ""
    else
        log_error "安装失败"
        return 1
    fi
}

# ===========================================
# 卸载
# ===========================================
do_uninstall() {
    echo ""
    if ! is_installed; then
        log_warn "Skywalking Agent 未安装"
        return 0
    fi
    
    read -p "确定要卸载 Skywalking Agent? [y/N] " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        return 0
    fi
    
    rm -rf "${SKYWALKING_DIR}/skywalking-agent"
    log_info "Skywalking Agent 已卸载"
    echo ""
}

# ===========================================
# 配置
# ===========================================
do_config() {
    local backend="$1"
    
    if ! is_installed; then
        log_error "Skywalking Agent 未安装，请先安装"
        return 1
    fi
    
    if [ -z "$backend" ]; then
        # 显示当前配置
        echo ""
        echo -e "${CYAN}【当前配置】${NC}"
        if [ -f "${SKYWALKING_CONFIG}" ]; then
            grep -E "^(agent\.|collector\.)" "${SKYWALKING_CONFIG}" | head -20
        fi
        echo ""
        echo -e "${YELLOW}修改配置:${NC}"
        echo "  cluster.sh skywalking config --backend <后端地址>"
        echo ""
        return 0
    fi
    
    # 更新后端地址
    if [ -f "${SKYWALKING_CONFIG}" ]; then
        sed -i "s|^collector.backend_service=.*|collector.backend_service=${backend}|" "${SKYWALKING_CONFIG}"
        log_info "后端地址已更新: ${backend}"
        echo ""
        echo -e "${YELLOW}提示: 需要重启实例才能生效${NC}"
        echo "  cluster.sh restart --skywalking"
        echo ""
    else
        log_error "配置文件不存在: ${SKYWALKING_CONFIG}"
        return 1
    fi
}

# ===========================================
# 参数解析
# ===========================================
COMMAND=""
SW_VERSION="${SKYWALKING_VERSION}"
BACKEND_ADDR=""

while [ $# -gt 0 ]; do
    case "$1" in
        install)
            COMMAND="install"
            shift
            ;;
        uninstall)
            COMMAND="uninstall"
            shift
            ;;
        config)
            COMMAND="config"
            shift
            ;;
        status)
            COMMAND="status"
            shift
            ;;
        --version)
            SW_VERSION="$2"
            shift 2
            ;;
        --backend)
            BACKEND_ADDR="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# ===========================================
# 执行
# ===========================================
case "$COMMAND" in
    install)
        do_install "$SW_VERSION"
        ;;
    uninstall)
        do_uninstall
        ;;
    config)
        do_config "$BACKEND_ADDR"
        ;;
    status)
        show_instance_status
        ;;
    *)
        show_info
        ;;
esac
