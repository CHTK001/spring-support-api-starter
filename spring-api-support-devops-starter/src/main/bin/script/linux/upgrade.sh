#!/bin/bash
# ============================================================
# 升级管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}升级管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -p <path>      指定升级包路径 (zip/tar.gz)"
    echo "  --ignore       不进行备份"
    echo "  --no-restart   升级后不重启"
    echo "  -h, --help     显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 -p /tmp/app.zip           从 zip 包升级"
    echo "  $0 -p /tmp/app.tar.gz        从 tar.gz 包升级"
    echo "  $0 -p /tmp/app.zip --ignore  升级但不备份"
    echo "  $0 -p /tmp/app.zip --no-restart  升级但不重启"
    echo ""
}

# ===========================================
# 升级
# ===========================================
do_upgrade() {
    local package_path="$1"
    local skip_backup="$2"
    local no_restart="$3"
    
    # 检查升级包
    if [ -z "$package_path" ]; then
        log_error "请指定升级包路径 (-p)"
        show_help
        return 1
    fi
    
    if [ ! -f "$package_path" ]; then
        log_error "升级包不存在: ${package_path}"
        return 1
    fi
    
    print_title "升级应用"
    echo ""
    echo -e "  升级包: ${WHITE}${package_path}${NC}"
    echo ""
    
    # 创建临时目录
    local tmp_dir="${APP_HOME}/.upgrade_tmp"
    rm -rf "$tmp_dir"
    mkdir -p "$tmp_dir"
    
    # 解压升级包
    log_step "解压升级包..."
    case "$package_path" in
        *.zip)
            if command -v unzip &> /dev/null; then
                unzip -q "$package_path" -d "$tmp_dir"
            else
                log_error "未找到 unzip 命令"
                rm -rf "$tmp_dir"
                return 1
            fi
            ;;
        *.tar.gz|*.tgz)
            tar -xzf "$package_path" -C "$tmp_dir"
            ;;
        *.tar)
            tar -xf "$package_path" -C "$tmp_dir"
            ;;
        *)
            log_error "不支持的包格式，支持: zip, tar.gz, tar"
            rm -rf "$tmp_dir"
            return 1
            ;;
    esac
    
    if [ $? -ne 0 ]; then
        log_error "解压失败"
        rm -rf "$tmp_dir"
        return 1
    fi
    
    # 查找解压后的内容
    local content_dir="$tmp_dir"
    # 如果解压后只有一个目录，进入该目录
    local subdirs=($(ls -d "$tmp_dir"/*/ 2>/dev/null))
    if [ ${#subdirs[@]} -eq 1 ]; then
        content_dir="${subdirs[0]}"
    fi
    
    # 备份当前版本
    if [ "$skip_backup" != "true" ]; then
        log_step "备份当前版本..."
        bash "${SCRIPT_DIR}/backup.sh"
        if [ $? -ne 0 ]; then
            log_warn "备份失败，继续升级..."
        fi
    else
        log_warn "跳过备份"
    fi
    
    # 停止应用
    local was_running="false"
    if is_running; then
        was_running="true"
        log_step "停止应用..."
        bash "${SCRIPT_DIR}/../../startup.sh" stop 2>/dev/null || \
        bash "${BIN_DIR}/startup.sh" stop 2>/dev/null
    fi
    
    # 复制新文件
    log_step "更新文件..."
    
    # 更新 JAR 文件
    local new_jar=$(ls "$content_dir"/*.jar 2>/dev/null | grep -v -E '(sources|javadoc)\.jar$' | head -1)
    if [ -n "$new_jar" ]; then
        cp "$new_jar" "${APP_HOME}/"
        log_info "JAR 文件已更新: $(basename "$new_jar")"
    fi
    
    # 更新配置文件 (如果存在)
    if [ -d "${content_dir}/config" ]; then
        mkdir -p "${CONFIG_DIR}"
        cp -r "${content_dir}/config/"* "${CONFIG_DIR}/" 2>/dev/null
        log_info "配置文件已更新"
    fi
    
    # 更新 lib 目录 (如果存在)
    if [ -d "${content_dir}/lib" ]; then
        mkdir -p "${LIB_DIR}"
        cp -r "${content_dir}/lib/"* "${LIB_DIR}/" 2>/dev/null
        log_info "lib 目录已更新"
    fi
    
    # 更新启动脚本 (如果存在)
    if [ -d "${content_dir}/bin" ]; then
        cp -r "${content_dir}/bin/"* "${BIN_DIR}/" 2>/dev/null
        chmod +x "${BIN_DIR}"/*.sh 2>/dev/null
        chmod +x "${BIN_DIR}"/script/linux/*.sh 2>/dev/null
        log_info "启动脚本已更新"
    fi
    
    # 清理临时目录
    rm -rf "$tmp_dir"
    
    echo ""
    log_info "升级完成"
    
    # 重启应用
    if [ "$no_restart" != "true" ] && [ "$was_running" = "true" ]; then
        echo ""
        log_step "重启应用..."
        bash "${SCRIPT_DIR}/../../startup.sh" start 2>/dev/null || \
        bash "${BIN_DIR}/startup.sh" start 2>/dev/null
    elif [ "$no_restart" = "true" ]; then
        log_info "已跳过重启，请手动启动应用"
    fi
}

# ===========================================
# 参数解析
# ===========================================
PACKAGE_PATH=""
SKIP_BACKUP="false"
NO_RESTART="false"

while [ $# -gt 0 ]; do
    case "$1" in
        -p)
            PACKAGE_PATH="$2"
            shift 2
            ;;
        --ignore)
            SKIP_BACKUP="true"
            shift
            ;;
        --no-restart)
            NO_RESTART="true"
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
if [ -z "$PACKAGE_PATH" ]; then
    show_help
    exit 0
fi

do_upgrade "$PACKAGE_PATH" "$SKIP_BACKUP" "$NO_RESTART"
