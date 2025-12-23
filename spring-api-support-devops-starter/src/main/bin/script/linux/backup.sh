#!/bin/bash
# ============================================================
# 备份管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}备份管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  (无参数)       创建新备份 (配置、JAR、lib)"
    echo "  -l             显示备份列表"
    echo "  --rm <index>   删除指定序号的备份"
    echo "  --clean        清除所有备份"
    echo "  -h, --help     显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0             创建新备份"
    echo "  $0 -l          查看备份列表"
    echo "  $0 --rm 1      删除第 1 个备份"
    echo "  $0 --clean     清除所有备份"
    echo ""
}

# ===========================================
# 列出备份
# ===========================================
list_backups() {
    print_title "备份列表"
    echo ""
    
    if [ ! -d "${BACKUP_DIR}" ]; then
        echo -e "  ${YELLOW}无备份${NC}"
        echo ""
        return 0
    fi
    
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null))
    
    if [ ${#backups[@]} -eq 0 ]; then
        echo -e "  ${YELLOW}无备份${NC}"
        echo ""
        return 0
    fi
    
    # 获取最新备份标记
    local latest=""
    [ -f "${BACKUP_DIR}/latest" ] && latest=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
    
    echo -e "  ${WHITE}备份目录: ${BACKUP_DIR}${NC}"
    echo ""
    printf "  ${CYAN}%-6s %-24s %-10s %s${NC}\n" "序号" "时间" "大小" "路径"
    echo "  ────────────────────────────────────────────────────────────"
    
    local index=1
    for backup in "${backups[@]}"; do
        local backup_name=$(basename "$backup")
        local backup_time=$(echo "$backup_name" | sed 's/_/ /' | sed 's/\([0-9]\{4\}\)\([0-9]\{2\}\)\([0-9]\{2\}\)/\1-\2-\3/')
        local backup_size=$(du -sh "$backup" 2>/dev/null | cut -f1)
        
        local latest_marker=""
        if [ "$backup" = "${latest}/" ] || [ "$backup" = "${latest}" ]; then
            latest_marker=" ${GREEN}[最新]${NC}"
        fi
        
        printf "  %-6s %-24s %-10s %s%b\n" "$index" "$backup_time" "$backup_size" "$backup" "$latest_marker"
        index=$((index + 1))
    done
    
    echo ""
}

# ===========================================
# 创建备份
# ===========================================
do_backup() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="${BACKUP_DIR}/${timestamp}"
    
    mkdir -p "${backup_path}"
    
    print_title "创建备份"
    echo ""
    echo -e "  备份目录: ${WHITE}${backup_path}${NC}"
    echo ""
    
    # 备份配置目录
    if [ -d "${CONFIG_DIR}" ]; then
        cp -r "${CONFIG_DIR}" "${backup_path}/"
        log_info "配置文件已备份"
    else
        log_warn "配置目录不存在: ${CONFIG_DIR}"
    fi
    
    # 备份 JAR 文件
    local jar_file="${APP_HOME}/${JAR_NAME}"
    if [ -f "${jar_file}" ]; then
        cp "${jar_file}" "${backup_path}/"
        log_info "JAR 文件已备份: ${JAR_NAME}"
    else
        log_warn "JAR 文件不存在: ${jar_file}"
    fi
    
    # 备份 lib 目录
    if [ -d "${LIB_DIR}" ]; then
        cp -r "${LIB_DIR}" "${backup_path}/"
        log_info "lib 目录已备份"
    fi
    
    # 更新最新备份标记
    echo "${backup_path}" > "${BACKUP_DIR}/latest"
    
    echo ""
    log_info "备份完成: ${backup_path}"
    
    # 显示备份大小
    local total_size=$(du -sh "${backup_path}" 2>/dev/null | cut -f1)
    echo -e "  备份大小: ${WHITE}${total_size}${NC}"
    echo ""
}

# ===========================================
# 删除备份
# ===========================================
remove_backup() {
    local index="$1"
    
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null))
    
    if [ ${#backups[@]} -eq 0 ]; then
        log_warn "无可删除的备份"
        return 0
    fi
    
    if [ "$index" -lt 1 ] || [ "$index" -gt ${#backups[@]} ]; then
        log_error "无效的序号: ${index} (有效范围: 1-${#backups[@]})"
        return 1
    fi
    
    local target="${backups[$((index - 1))]}"
    
    print_title "删除备份"
    echo ""
    echo -e "  目标: ${WHITE}${target}${NC}"
    echo ""
    
    rm -rf "$target"
    log_info "备份已删除"
    
    # 如果删除的是最新备份，更新标记
    local latest=""
    [ -f "${BACKUP_DIR}/latest" ] && latest=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
    if [ "${target%/}" = "${latest}" ]; then
        local new_latest=$(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | head -1)
        if [ -n "$new_latest" ]; then
            echo "${new_latest%/}" > "${BACKUP_DIR}/latest"
        else
            rm -f "${BACKUP_DIR}/latest"
        fi
    fi
}

# ===========================================
# 清除所有备份
# ===========================================
clean_backups() {
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null))
    
    if [ ${#backups[@]} -eq 0 ]; then
        log_warn "无可清除的备份"
        return 0
    fi
    
    print_title "清除所有备份"
    echo ""
    echo -e "  备份目录: ${WHITE}${BACKUP_DIR}${NC}"
    echo -e "  备份数量: ${WHITE}${#backups[@]}${NC}"
    
    local total_size=$(du -sh "${BACKUP_DIR}" 2>/dev/null | cut -f1)
    echo -e "  总大小:   ${WHITE}${total_size}${NC}"
    echo ""
    
    # 删除所有备份
    for backup in "${backups[@]}"; do
        rm -rf "$backup"
    done
    rm -f "${BACKUP_DIR}/latest"
    
    log_info "已清除 ${#backups[@]} 个备份，释放 ${total_size} 空间"
}

# ===========================================
# 参数解析
# ===========================================
ACTION="backup"
REMOVE_INDEX=""

while [ $# -gt 0 ]; do
    case "$1" in
        -l)
            ACTION="list"
            shift
            ;;
        --rm)
            ACTION="remove"
            REMOVE_INDEX="$2"
            shift 2
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
    backup) do_backup ;;
    list)   list_backups ;;
    remove) remove_backup "$REMOVE_INDEX" ;;
    clean)  clean_backups ;;
esac
