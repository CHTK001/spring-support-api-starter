#!/bin/bash
# ============================================================
# 还原管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}还原管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -l             显示备份列表"
    echo "  -i <index>     根据序号还原"
    echo "  --latest       还原最新备份"
    echo "  --clean        清除所有备份"
    echo "  -h, --help     显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 -l          查看备份列表"
    echo "  $0 -i 1        还原第 1 个备份"
    echo "  $0 --latest    还原最新备份"
    echo "  $0 --clean     清除所有备份"
    echo ""
}

# ===========================================
# 列出备份
# ===========================================
list_backups() {
    # 复用 backup.sh 的列表功能
    bash "${SCRIPT_DIR}/backup.sh" -l
}

# ===========================================
# 根据序号获取备份路径
# ===========================================
get_backup_by_index() {
    local index="$1"
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null))
    
    if [ ${#backups[@]} -eq 0 ]; then
        return 1
    fi
    
    if [ "$index" -lt 1 ] || [ "$index" -gt ${#backups[@]} ]; then
        return 1
    fi
    
    echo "${backups[$((index - 1))]}"
}

# ===========================================
# 执行还原
# ===========================================
do_restore() {
    local restore_path="$1"
    
    if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
        log_error "无效的备份路径: ${restore_path}"
        return 1
    fi
    
    print_title "还原操作"
    echo ""
    echo -e "  还原来源: ${WHITE}${restore_path}${NC}"
    echo ""
    
    # 还原配置文件
    if [ -d "${restore_path}/config" ]; then
        mkdir -p "${CONFIG_DIR}"
        cp -r "${restore_path}/config/"* "${CONFIG_DIR}/" 2>/dev/null
        log_info "配置文件已还原"
    else
        log_warn "备份中无配置文件"
    fi
    
    # 还原 JAR 文件
    local backup_jar="${restore_path}/${JAR_NAME}"
    if [ -f "${backup_jar}" ]; then
        cp "${backup_jar}" "${APP_HOME}/"
        log_info "JAR 文件已还原"
    else
        # 尝试查找任意 JAR 文件
        local any_jar=$(ls "${restore_path}"/*.jar 2>/dev/null | head -1)
        if [ -n "$any_jar" ]; then
            cp "$any_jar" "${APP_HOME}/"
            log_info "JAR 文件已还原: $(basename "$any_jar")"
        else
            log_warn "备份中无 JAR 文件"
        fi
    fi
    
    # 还原 lib 目录
    if [ -d "${restore_path}/lib" ]; then
        mkdir -p "${LIB_DIR}"
        cp -r "${restore_path}/lib/"* "${LIB_DIR}/" 2>/dev/null
        log_info "lib 目录已还原"
    fi
    
    echo ""
    log_info "还原完成"
}

# ===========================================
# 还原最新备份
# ===========================================
restore_latest() {
    local latest=""
    
    if [ -f "${BACKUP_DIR}/latest" ]; then
        latest=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
    fi
    
    if [ -z "${latest}" ] || [ ! -d "${latest}" ]; then
        # 如果 latest 文件不存在，使用最新的备份目录
        latest=$(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | head -1)
    fi
    
    if [ -z "${latest}" ] || [ ! -d "${latest}" ]; then
        log_error "未找到可还原的备份"
        echo -e "${CYAN}使用 '$0 -l' 查看可用备份${NC}"
        return 1
    fi
    
    do_restore "$latest"
}

# ===========================================
# 清除所有备份
# ===========================================
clean_backups() {
    # 复用 backup.sh 的清理功能
    bash "${SCRIPT_DIR}/backup.sh" --clean
}

# ===========================================
# 参数解析
# ===========================================
ACTION="help"
RESTORE_INDEX=""

while [ $# -gt 0 ]; do
    case "$1" in
        -l)
            ACTION="list"
            shift
            ;;
        -i)
            ACTION="restore"
            RESTORE_INDEX="$2"
            shift 2
            ;;
        --latest)
            ACTION="latest"
            shift
            ;;
        --clean)
            ACTION="clean"
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
    list)
        list_backups
        ;;
    restore)
        restore_path=$(get_backup_by_index "$RESTORE_INDEX")
        if [ -z "$restore_path" ]; then
            log_error "备份 #${RESTORE_INDEX} 不存在"
            echo -e "${CYAN}使用 '$0 -l' 查看可用备份${NC}"
            exit 1
        fi
        do_restore "$restore_path"
        ;;
    latest)
        restore_latest
        ;;
    clean)
        clean_backups
        ;;
    *)
        show_help
        ;;
esac
