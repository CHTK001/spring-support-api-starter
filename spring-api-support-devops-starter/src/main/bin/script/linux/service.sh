#!/bin/bash
# ============================================================
# Systemd 服务管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# 服务配置
SERVICE_NAME="${APP_NAME}"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}Systemd 服务管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项/命令]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -i               安装服务"
    echo "  --name <name>    指定服务名称 (默认: ${APP_NAME})"
    echo "  -u               卸载服务"
    echo "  -h, --help       显示帮助信息"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  enable           启用开机自启"
    echo "  disable          禁用开机自启"
    echo "  status           查看服务状态"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 -i                    安装服务"
    echo "  $0 -i --name myapp       安装服务并指定名称"
    echo "  $0 enable                启用开机自启"
    echo "  $0 disable               禁用开机自启"
    echo "  $0 status                查看服务状态"
    echo "  $0 -u                    卸载服务"
    echo ""
}

# ===========================================
# 检查 root 权限
# ===========================================
check_root() {
    if [ "$(id -u)" -ne 0 ]; then
        log_error "需要 root 权限执行此命令"
        echo "请使用: sudo $0 $*"
        exit 1
    fi
}

# ===========================================
# 安装服务
# ===========================================
install_service() {
    check_root
    
    if [ -f "${SERVICE_FILE}" ]; then
        log_warn "服务已存在: ${SERVICE_NAME}"
        echo -e "是否覆盖? [y/N] "
        read -r answer
        if [[ ! "$answer" =~ ^[Yy]$ ]]; then
            return 0
        fi
    fi
    
    print_title "安装 Systemd 服务"
    echo ""
    echo -e "  服务名称:   ${WHITE}${SERVICE_NAME}${NC}"
    echo -e "  服务文件:   ${WHITE}${SERVICE_FILE}${NC}"
    echo -e "  应用目录:   ${WHITE}${APP_HOME}${NC}"
    echo ""
    
    # 获取当前用户
    local run_user="${SUDO_USER:-$(whoami)}"
    local run_group=$(id -gn "$run_user" 2>/dev/null || echo "$run_user")
    
    # 创建服务文件
    cat > "${SERVICE_FILE}" << EOF
[Unit]
Description=${APP_NAME} Service
After=network.target

[Service]
Type=forking
User=${run_user}
Group=${run_group}
WorkingDirectory=${APP_HOME}
ExecStart=${BIN_DIR}/startup.sh start
ExecStop=${BIN_DIR}/startup.sh stop
ExecReload=${BIN_DIR}/startup.sh restart
PIDFile=${PID_FILE}
Restart=on-failure
RestartSec=10
SuccessExitStatus=143
TimeoutStopSec=30
KillMode=process

[Install]
WantedBy=multi-user.target
EOF
    
    # 重新加载 systemd
    systemctl daemon-reload
    
    log_info "服务已安装: ${SERVICE_NAME}"
    echo ""
    echo -e "${CYAN}可用命令:${NC}"
    echo "  systemctl start ${SERVICE_NAME}     # 启动服务"
    echo "  systemctl stop ${SERVICE_NAME}      # 停止服务"
    echo "  systemctl restart ${SERVICE_NAME}   # 重启服务"
    echo "  systemctl status ${SERVICE_NAME}    # 查看状态"
    echo "  systemctl enable ${SERVICE_NAME}    # 开机自启"
    echo "  journalctl -u ${SERVICE_NAME} -f    # 查看日志"
    echo ""
}

# ===========================================
# 卸载服务
# ===========================================
uninstall_service() {
    check_root
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        log_warn "服务不存在: ${SERVICE_NAME}"
        return 0
    fi
    
    print_title "卸载 Systemd 服务"
    echo ""
    
    # 停止服务
    systemctl stop "${SERVICE_NAME}" 2>/dev/null
    systemctl disable "${SERVICE_NAME}" 2>/dev/null
    
    # 删除服务文件
    rm -f "${SERVICE_FILE}"
    systemctl daemon-reload
    
    log_info "服务已卸载: ${SERVICE_NAME}"
}

# ===========================================
# 启用开机自启
# ===========================================
enable_service() {
    check_root
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        log_error "服务未安装，请先执行: $0 -i"
        return 1
    fi
    
    systemctl enable "${SERVICE_NAME}"
    log_info "已启用开机自启"
}

# ===========================================
# 禁用开机自启
# ===========================================
disable_service() {
    check_root
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        log_warn "服务未安装"
        return 0
    fi
    
    systemctl disable "${SERVICE_NAME}"
    log_info "已禁用开机自启"
}

# ===========================================
# 查看服务状态
# ===========================================
show_status() {
    if [ ! -f "${SERVICE_FILE}" ]; then
        log_warn "服务未安装: ${SERVICE_NAME}"
        echo -e "使用 '$0 -i' 安装服务"
        return 0
    fi
    
    print_title "Systemd 服务状态"
    echo ""
    systemctl status "${SERVICE_NAME}" --no-pager
}

# ===========================================
# 参数解析
# ===========================================
ACTION="help"

while [ $# -gt 0 ]; do
    case "$1" in
        -i)
            ACTION="install"
            shift
            ;;
        --name)
            SERVICE_NAME="$2"
            SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"
            shift 2
            ;;
        -u)
            ACTION="uninstall"
            shift
            ;;
        enable)
            ACTION="enable"
            shift
            ;;
        disable)
            ACTION="disable"
            shift
            ;;
        status)
            ACTION="status"
            shift
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
case "$ACTION" in
    install)   install_service ;;
    uninstall) uninstall_service ;;
    enable)    enable_service ;;
    disable)   disable_service ;;
    status)    show_status ;;
    *)         show_help ;;
esac
