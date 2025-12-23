#!/bin/bash
# ============================================================
# 备份还原脚本
# 版本: 2.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../../.." && pwd)"
BACKUP_DIR="${APP_HOME}/backup"
CONFIG_DIR="${APP_HOME}/config"

COMMAND="${1:-}"
MODE="${2:-all}"
APP_NAME="${3:-app}"
JAR_NAME="${4:-${APP_NAME}.jar}"

show_help() {
    echo -e "${BLUE}备份还原脚本${NC}"
    echo ""
    echo "用法: $0 <backup|restore> [mode] [app_name] [jar_name]"
    echo ""
    echo "命令:"
    echo "  backup     备份配置/JAR"
    echo "  restore    还原配置/JAR"
    echo ""
    echo "模式:"
    echo "  all        备份/还原全部 (默认)"
    echo "  config     仅配置文件"
    echo "  jar        仅 JAR 文件"
    echo ""
}

do_backup() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="${BACKUP_DIR}/${timestamp}"
    
    mkdir -p "${backup_path}"
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                      备份操作                                ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  备份目录:   ${backup_path}"
    echo -e "  备份模式:   ${MODE}"
    echo ""
    
    case "${MODE}" in
        all|config)
            if [ -d "${CONFIG_DIR}" ]; then
                cp -r "${CONFIG_DIR}" "${backup_path}/"
                echo -e "${GREEN}  ✓ 配置文件已备份${NC}"
            else
                echo -e "${YELLOW}  - 配置目录不存在${NC}"
            fi
            ;&
        jar)
            if [ "${MODE}" = "config" ]; then break; fi
            local jar_file="${APP_HOME}/${JAR_NAME}"
            if [ -f "${jar_file}" ]; then
                cp "${jar_file}" "${backup_path}/"
                echo -e "${GREEN}  ✓ JAR 文件已备份${NC}"
            else
                echo -e "${YELLOW}  - JAR 文件不存在: ${jar_file}${NC}"
            fi
            ;;
    esac
    
    # 记录最新备份
    echo "${backup_path}" > "${BACKUP_DIR}/latest"
    
    echo ""
    echo -e "${GREEN}[成功] 备份完成: ${backup_path}${NC}"
}

do_restore() {
    local restore_path=""
    
    if [ -f "${BACKUP_DIR}/latest" ]; then
        restore_path=$(cat "${BACKUP_DIR}/latest")
    fi
    
    if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
        echo -e "${RED}[错误] 未找到可还原的备份${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                      还原操作                                ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  还原来源:   ${restore_path}"
    echo -e "  还原模式:   ${MODE}"
    echo ""
    
    case "${MODE}" in
        all|config)
            if [ -d "${restore_path}/config" ]; then
                cp -r "${restore_path}/config/"* "${CONFIG_DIR}/" 2>/dev/null
                echo -e "${GREEN}  ✓ 配置文件已还原${NC}"
            else
                echo -e "${YELLOW}  - 备份中无配置文件${NC}"
            fi
            ;&
        jar)
            if [ "${MODE}" = "config" ]; then break; fi
            local backup_jar="${restore_path}/${JAR_NAME}"
            if [ -f "${backup_jar}" ]; then
                cp "${backup_jar}" "${APP_HOME}/"
                echo -e "${GREEN}  ✓ JAR 文件已还原${NC}"
            else
                echo -e "${YELLOW}  - 备份中无 JAR 文件${NC}"
            fi
            ;;
    esac
    
    echo ""
    echo -e "${GREEN}[成功] 还原完成${NC}"
}

case "${COMMAND}" in
    backup) do_backup ;;
    restore) do_restore ;;
    --help|-h|help) show_help ;;
    *) show_help; exit 1 ;;
esac
