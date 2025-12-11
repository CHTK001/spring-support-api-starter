#!/bin/bash
# ============================================================
# 备份还原脚本
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"
BACKUP_DIR="${APP_HOME}/backup"

ACTION="$1"; TARGET="$2"

init_backup_dir() { mkdir -p "${BACKUP_DIR}/config" "${BACKUP_DIR}/jar"; }

backup_config() {
    local ts=$(date '+%Y%m%d_%H%M%S')
    [ -d "${APP_HOME}/config" ] && tar -czf "${BACKUP_DIR}/config/config_${ts}.tar.gz" -C "${APP_HOME}" config 2>/dev/null && \
        echo -e "${GREEN}[成功] 配置已备份${NC}" || echo -e "${RED}[失败] 配置备份失败${NC}"
}

backup_jar() {
    local ts=$(date '+%Y%m%d_%H%M%S')
    local jar=$(find "${APP_HOME}" -maxdepth 1 -name "*.jar" -type f 2>/dev/null | head -1)
    [ -f "$jar" ] && cp "$jar" "${BACKUP_DIR}/jar/$(basename ${jar%.jar})_${ts}.jar" && \
        echo -e "${GREEN}[成功] JAR已备份${NC}" || echo -e "${RED}[失败] JAR备份失败${NC}"
}

restore_config() {
    local f=$(ls -t "${BACKUP_DIR}/config/"*.tar.gz 2>/dev/null | head -1)
    [ -f "$f" ] && [ -d "${APP_HOME}/config" ] && mv "${APP_HOME}/config" "${APP_HOME}/config.old.$(date '+%Y%m%d_%H%M%S')"
    [ -f "$f" ] && tar -xzf "$f" -C "${APP_HOME}" && echo -e "${GREEN}[成功] 配置已还原${NC}" || echo -e "${RED}[失败] 还原失败${NC}"
}

restore_jar() {
    local f=$(ls -t "${BACKUP_DIR}/jar/"*.jar 2>/dev/null | head -1)
    if [ -f "$f" ]; then
        local name=$(basename "$f" | sed 's/_[0-9]\{8\}_[0-9]\{6\}\.jar$/.jar/')
        [ -f "${APP_HOME}/$name" ] && mv "${APP_HOME}/$name" "${APP_HOME}/${name}.old.$(date '+%Y%m%d_%H%M%S')"
        cp "$f" "${APP_HOME}/$name" && echo -e "${GREEN}[成功] JAR已还原${NC}" || echo -e "${RED}[失败] 还原失败${NC}"
    fi
}

list_backups() {
    echo -e "${YELLOW}[配置备份]${NC}"; ls -lh "${BACKUP_DIR}/config/"*.tar.gz 2>/dev/null | tail -5
    echo -e "${YELLOW}[JAR备份]${NC}"; ls -lh "${BACKUP_DIR}/jar/"*.jar 2>/dev/null | tail -5
}

init_backup_dir
case "$ACTION" in
    backup) case "$TARGET" in config) backup_config;; jar) backup_jar;; *) backup_config; backup_jar;; esac ;;
    restore) case "$TARGET" in config) restore_config;; jar) restore_jar;; *) restore_config; restore_jar;; esac ;;
    list) list_backups ;;
    clean) find "${BACKUP_DIR}" -type f -mtime +"${TARGET:-7}" -delete; echo -e "${GREEN}[完成] 已清理${NC}" ;;
    *) echo "用法: $0 [backup|restore|list|clean] [config|jar|all]" ;;
esac
