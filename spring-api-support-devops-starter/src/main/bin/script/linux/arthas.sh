#!/bin/bash
# ============================================================
# Arthas 诊断工具脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# Arthas 配置
ARTHAS_HOME="${BASE_DIR}/arthas"
ARTHAS_BOOT_JAR="${ARTHAS_HOME}/arthas-boot.jar"
ARTHAS_VERSION="3.7.2"
ARTHAS_DOWNLOAD_URL="https://arthas.aliyun.com/arthas-boot.jar"
ARTHAS_MIRROR_URL="https://maven.aliyun.com/repository/public/com/taobao/arthas/arthas-boot/${ARTHAS_VERSION}/arthas-boot-${ARTHAS_VERSION}.jar"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}Arthas 诊断工具${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  (无参数)    启动 Arthas 并附加到应用"
    echo "  -i          安装/更新 Arthas"
    echo "  -u          卸载 Arthas"
    echo "  -v          显示 Arthas 版本"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -h, --help  显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                         启动 Arthas"
    echo "  $0 -i                      安装 Arthas"
    echo "  $0 -u                      卸载 Arthas"
    echo ""
    echo -e "${YELLOW}常用 Arthas 命令:${NC}"
    echo "  dashboard                  实时数据面板"
    echo "  thread                     线程信息"
    echo "  jvm                        JVM 信息"
    echo "  sc                         搜索类"
    echo "  sm                         搜索方法"
    echo "  watch                      观察方法执行"
    echo "  trace                      方法调用追踪"
    echo "  stack                      方法调用堆栈"
    echo "  tt                         时间隧道"
    echo "  profiler                   性能分析"
    echo ""
}

# ===========================================
# 检查 Arthas 是否已安装
# ===========================================
is_arthas_installed() {
    [ -f "${ARTHAS_BOOT_JAR}" ]
}

# ===========================================
# 安装 Arthas
# ===========================================
install_arthas() {
    print_title "安装 Arthas"
    echo ""
    
    # 检查是否已安装
    if is_arthas_installed; then
        log_warning "Arthas 已安装: ${ARTHAS_BOOT_JAR}"
        echo ""
        read -p "是否重新下载? [y/N] " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            return 0
        fi
    fi
    
    # 创建目录
    mkdir -p "${ARTHAS_HOME}"
    
    # 下载 Arthas
    log_step "下载 Arthas..."
    
    local download_success=false
    
    # 尝试主下载地址
    if command -v curl &> /dev/null; then
        if curl -L -o "${ARTHAS_BOOT_JAR}" "${ARTHAS_DOWNLOAD_URL}" 2>/dev/null; then
            download_success=true
        fi
    elif command -v wget &> /dev/null; then
        if wget -O "${ARTHAS_BOOT_JAR}" "${ARTHAS_DOWNLOAD_URL}" 2>/dev/null; then
            download_success=true
        fi
    fi
    
    # 如果主地址失败，尝试镜像地址
    if [ "$download_success" = false ]; then
        log_warning "主下载地址失败，尝试镜像..."
        if command -v curl &> /dev/null; then
            if curl -L -o "${ARTHAS_BOOT_JAR}" "${ARTHAS_MIRROR_URL}" 2>/dev/null; then
                download_success=true
            fi
        elif command -v wget &> /dev/null; then
            if wget -O "${ARTHAS_BOOT_JAR}" "${ARTHAS_MIRROR_URL}" 2>/dev/null; then
                download_success=true
            fi
        fi
    fi
    
    if [ "$download_success" = false ]; then
        log_error "下载 Arthas 失败"
        rm -f "${ARTHAS_BOOT_JAR}"
        return 1
    fi
    
    # 验证下载
    if [ ! -f "${ARTHAS_BOOT_JAR}" ] || [ ! -s "${ARTHAS_BOOT_JAR}" ]; then
        log_error "下载的文件无效"
        rm -f "${ARTHAS_BOOT_JAR}"
        return 1
    fi
    
    log_info "Arthas 安装成功: ${ARTHAS_BOOT_JAR}"
    echo ""
}

# ===========================================
# 卸载 Arthas
# ===========================================
uninstall_arthas() {
    print_title "卸载 Arthas"
    echo ""
    
    if ! is_arthas_installed; then
        log_warning "Arthas 未安装"
        return 0
    fi
    
    read -p "确认卸载 Arthas? [y/N] " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "取消卸载"
        return 0
    fi
    
    rm -rf "${ARTHAS_HOME}"
    
    # 清理用户目录下的 arthas 缓存
    local user_arthas_home="${HOME}/.arthas"
    if [ -d "$user_arthas_home" ]; then
        log_step "清理 Arthas 缓存..."
        rm -rf "$user_arthas_home"
    fi
    
    log_info "Arthas 已卸载"
}

# ===========================================
# 显示版本
# ===========================================
show_version() {
    if ! is_arthas_installed; then
        log_error "Arthas 未安装"
        return 1
    fi
    
    check_java
    local java_cmd="java"
    [ -n "$JAVA_HOME" ] && java_cmd="$JAVA_HOME/bin/java"
    
    echo -e "${BLUE}Arthas 版本信息${NC}"
    echo ""
    "${java_cmd}" -jar "${ARTHAS_BOOT_JAR}" --versions 2>/dev/null || \
        "${java_cmd}" -jar "${ARTHAS_BOOT_JAR}" -h 2>/dev/null | head -5
}

# ===========================================
# 启动 Arthas
# ===========================================
start_arthas() {
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        log_error "${APP_NAME} 未在运行"
        return 1
    fi
    
    if ! is_arthas_installed; then
        log_warning "Arthas 未安装"
        echo ""
        read -p "是否立即安装? [Y/n] " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Nn]$ ]]; then
            install_arthas
            if ! is_arthas_installed; then
                return 1
            fi
        else
            return 1
        fi
    fi
    
    check_java
    local java_cmd="java"
    [ -n "$JAVA_HOME" ] && java_cmd="$JAVA_HOME/bin/java"
    
    print_title "启动 Arthas"
    echo ""
    echo -e "  应用: ${WHITE}${APP_NAME}${NC}"
    echo -e "  PID:  ${WHITE}${pid}${NC}"
    echo ""
    
    log_step "附加到进程..."
    echo ""
    
    # 启动 Arthas
    "${java_cmd}" -jar "${ARTHAS_BOOT_JAR}" --target-ip 127.0.0.1 "$pid"
}

# ===========================================
# 参数解析
# ===========================================
case "${1:-}" in
    "")
        start_arthas
        ;;
    -i|--install)
        install_arthas
        ;;
    -u|--uninstall)
        uninstall_arthas
        ;;
    -v|--version)
        show_version
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        log_error "未知命令: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
