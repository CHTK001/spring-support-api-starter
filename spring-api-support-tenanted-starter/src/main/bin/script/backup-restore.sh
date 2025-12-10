#!/bin/bash
# ============================================================
# 备份还原脚本
# 支持配置文件和JAR包的备份与还原
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"
BACKUP_DIR="${APP_HOME}/backup"
APP_NAME="${3:-app}"
JAR_NAME="${4:-}"

ACTION="$1"      # backup / restore / list / clean
TARGET="$2"      # config / jar / all

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  备份还原工具${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 backup [config|jar|all]    备份配置/JAR/全部"
    echo "  $0 restore [config|jar|all]   还原配置/JAR/全部"
    echo "  $0 list                        查看备份列表"
    echo "  $0 clean [days]                清理N天前的备份(默认7天)"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 backup config               备份配置文件"
    echo "  $0 backup jar                  备份JAR包"
    echo "  $0 backup all                  备份全部"
    echo "  $0 restore config              还原最新配置"
    echo "  $0 restore jar 2024-12-10      还原指定日期的JAR"
    echo "  $0 list                        列出所有备份"
    echo "  $0 clean 30                    清理30天前的备份"
    echo ""
}

# 创建备份目录
init_backup_dir() {
    mkdir -p "${BACKUP_DIR}/config"
    mkdir -p "${BACKUP_DIR}/jar"
}

# 备份配置
backup_config() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_file="${BACKUP_DIR}/config/config_${timestamp}.tar.gz"
    
    if [ -d "${APP_HOME}/config" ]; then
        echo -e "${YELLOW}[备份配置...]${NC}"
        tar -czf "$backup_file" -C "${APP_HOME}" config 2>/dev/null
        
        if [ -f "$backup_file" ]; then
            local size=$(du -h "$backup_file" | cut -f1)
            echo -e "${GREEN}[成功] 配置已备份: $backup_file ($size)${NC}"
            return 0
        fi
    fi
    
    echo -e "${RED}[失败] 配置目录不存在或备份失败${NC}"
    return 1
}

# 备份JAR
backup_jar() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local jar_file=$(find "${APP_HOME}" -maxdepth 1 -name "*.jar" -type f 2>/dev/null | head -1)
    
    if [ -n "$jar_file" ] && [ -f "$jar_file" ]; then
        local jar_basename=$(basename "$jar_file")
        local backup_file="${BACKUP_DIR}/jar/${jar_basename%.jar}_${timestamp}.jar"
        
        echo -e "${YELLOW}[备份JAR...]${NC}"
        cp "$jar_file" "$backup_file"
        
        if [ -f "$backup_file" ]; then
            local size=$(du -h "$backup_file" | cut -f1)
            echo -e "${GREEN}[成功] JAR已备份: $backup_file ($size)${NC}"
            return 0
        fi
    fi
    
    echo -e "${RED}[失败] JAR文件不存在或备份失败${NC}"
    return 1
}

# 还原配置
restore_config() {
    local date_filter="$1"
    local backup_file
    
    if [ -n "$date_filter" ]; then
        backup_file=$(ls -t "${BACKUP_DIR}/config/config_${date_filter}"*.tar.gz 2>/dev/null | head -1)
    else
        backup_file=$(ls -t "${BACKUP_DIR}/config/"*.tar.gz 2>/dev/null | head -1)
    fi
    
    if [ -z "$backup_file" ] || [ ! -f "$backup_file" ]; then
        echo -e "${RED}[失败] 未找到配置备份${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}[还原配置...] $backup_file${NC}"
    
    # 备份当前配置
    if [ -d "${APP_HOME}/config" ]; then
        mv "${APP_HOME}/config" "${APP_HOME}/config.old.$(date '+%Y%m%d_%H%M%S')"
    fi
    
    tar -xzf "$backup_file" -C "${APP_HOME}" 2>/dev/null
    
    if [ -d "${APP_HOME}/config" ]; then
        echo -e "${GREEN}[成功] 配置已还原${NC}"
        return 0
    fi
    
    echo -e "${RED}[失败] 配置还原失败${NC}"
    return 1
}

# 还原JAR
restore_jar() {
    local date_filter="$1"
    local backup_file
    
    if [ -n "$date_filter" ]; then
        backup_file=$(ls -t "${BACKUP_DIR}/jar/"*"_${date_filter}"*.jar 2>/dev/null | head -1)
    else
        backup_file=$(ls -t "${BACKUP_DIR}/jar/"*.jar 2>/dev/null | head -1)
    fi
    
    if [ -z "$backup_file" ] || [ ! -f "$backup_file" ]; then
        echo -e "${RED}[失败] 未找到JAR备份${NC}"
        return 1
    fi
    
    # 获取原始JAR名称
    local backup_name=$(basename "$backup_file")
    local original_name=$(echo "$backup_name" | sed 's/_[0-9]\{8\}_[0-9]\{6\}\.jar$/.jar/')
    local target_file="${APP_HOME}/${original_name}"
    
    echo -e "${YELLOW}[还原JAR...] $backup_file${NC}"
    
    # 备份当前JAR
    if [ -f "$target_file" ]; then
        mv "$target_file" "${target_file}.old.$(date '+%Y%m%d_%H%M%S')"
    fi
    
    cp "$backup_file" "$target_file"
    
    if [ -f "$target_file" ]; then
        echo -e "${GREEN}[成功] JAR已还原: $target_file${NC}"
        return 0
    fi
    
    echo -e "${RED}[失败] JAR还原失败${NC}"
    return 1
}

# 列出备份
list_backups() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  备份列表${NC}"
    echo -e "${BLUE}============================================================${NC}"
    
    echo -e "\n${YELLOW}[配置备份]${NC}"
    if [ -d "${BACKUP_DIR}/config" ]; then
        ls -lh "${BACKUP_DIR}/config/"*.tar.gz 2>/dev/null | awk '{print "  "$9" ("$5")"}' | tail -10
        local count=$(ls "${BACKUP_DIR}/config/"*.tar.gz 2>/dev/null | wc -l)
        echo -e "  ${CYAN}共 $count 个备份${NC}"
    else
        echo "  无备份"
    fi
    
    echo -e "\n${YELLOW}[JAR备份]${NC}"
    if [ -d "${BACKUP_DIR}/jar" ]; then
        ls -lh "${BACKUP_DIR}/jar/"*.jar 2>/dev/null | awk '{print "  "$9" ("$5")"}' | tail -10
        local count=$(ls "${BACKUP_DIR}/jar/"*.jar 2>/dev/null | wc -l)
        echo -e "  ${CYAN}共 $count 个备份${NC}"
    else
        echo "  无备份"
    fi
    
    echo -e "\n${YELLOW}[磁盘占用]${NC}"
    if [ -d "${BACKUP_DIR}" ]; then
        du -sh "${BACKUP_DIR}" 2>/dev/null | awk '{print "  备份总大小: "$1}'
    fi
}

# 清理旧备份
clean_backups() {
    local days="${1:-7}"
    
    echo -e "${YELLOW}[清理 $days 天前的备份...]${NC}"
    
    local count=0
    
    # 清理配置备份
    while IFS= read -r file; do
        [ -f "$file" ] && rm -f "$file" && count=$((count + 1))
    done < <(find "${BACKUP_DIR}/config" -name "*.tar.gz" -mtime +"$days" 2>/dev/null)
    
    # 清理JAR备份
    while IFS= read -r file; do
        [ -f "$file" ] && rm -f "$file" && count=$((count + 1))
    done < <(find "${BACKUP_DIR}/jar" -name "*.jar" -mtime +"$days" 2>/dev/null)
    
    echo -e "${GREEN}[完成] 已清理 $count 个旧备份${NC}"
}

# 主逻辑
init_backup_dir

case "$ACTION" in
    backup)
        echo -e "${BLUE}============================================================${NC}"
        echo -e "${GREEN}  执行备份${NC}"
        echo -e "${BLUE}============================================================${NC}"
        
        case "$TARGET" in
            config) backup_config ;;
            jar) backup_jar ;;
            all|"") backup_config; backup_jar ;;
            *) echo -e "${RED}未知目标: $TARGET${NC}"; show_help ;;
        esac
        ;;
    restore)
        echo -e "${BLUE}============================================================${NC}"
        echo -e "${GREEN}  执行还原${NC}"
        echo -e "${BLUE}============================================================${NC}"
        
        local date_filter="$3"
        case "$TARGET" in
            config) restore_config "$date_filter" ;;
            jar) restore_jar "$date_filter" ;;
            all|"") restore_config "$date_filter"; restore_jar "$date_filter" ;;
            *) echo -e "${RED}未知目标: $TARGET${NC}"; show_help ;;
        esac
        ;;
    list)
        list_backups
        ;;
    clean)
        clean_backups "$TARGET"
        ;;
    help|--help|-h|"")
        show_help
        ;;
    *)
        echo -e "${RED}未知操作: $ACTION${NC}"
        show_help
        exit 1
        ;;
esac
