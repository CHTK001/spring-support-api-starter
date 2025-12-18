#!/bin/bash
# ============================================================
# 升级和回滚脚本
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# 描述: 支持从压缩包升级应用，并提供回滚功能
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 进度条宽度
PROGRESS_WIDTH=40

# 显示进度条
# 用法: show_progress 当前值 总值 描述
show_progress() {
    local current=$1
    local total=$2
    local desc="${3:-进度}"
    
    local percent=$((current * 100 / total))
    local filled=$((current * PROGRESS_WIDTH / total))
    local empty=$((PROGRESS_WIDTH - filled))
    
    # 构建进度条
    local bar=""
    for ((i=0; i<filled; i++)); do bar+="█"; done
    for ((i=0; i<empty; i++)); do bar+="░"; done
    
    # 输出进度条 (使用 \r 回到行首)
    printf "\r  ${CYAN}[${bar}]${NC} ${percent}%% ${desc}"
    
    # 完成时换行
    [ $current -eq $total ] && echo ""
}

# 显示旋转动画
# 用法: show_spinner PID 描述
show_spinner() {
    local pid=$1
    local desc="${2:-处理中}"
    local spinstr='|/-\\'
    
    while ps -p $pid > /dev/null 2>&1; do
        local temp=${spinstr#?}
        printf "\r  ${CYAN}[%c]${NC} %s..." "$spinstr" "$desc"
        spinstr=$temp${spinstr%"$temp"}
        sleep 0.1
    done
    printf "\r  ${GREEN}[✓]${NC} %s    \n" "$desc"
}

# 带进度的文件复制
# 用法: copy_with_progress 源目录 目标目录 描述
copy_with_progress() {
    local src="$1"
    local dest="$2"
    local desc="${3:-复制文件}"
    
    if [ ! -e "$src" ]; then
        return 1
    fi
    
    # 获取文件列表
    local files=()
    if [ -d "$src" ]; then
        while IFS= read -r -d '' file; do
            files+=("$file")
        done < <(find "$src" -type f -print0 2>/dev/null)
    else
        files=("$src")
    fi
    
    local total=${#files[@]}
    if [ $total -eq 0 ]; then
        return 0
    fi
    
    local current=0
    for file in "${files[@]}"; do
        current=$((current + 1))
        show_progress $current $total "$desc"
        
        if [ -d "$src" ]; then
            local rel_path="${file#$src/}"
            local dest_file="$dest/$rel_path"
            mkdir -p "$(dirname "$dest_file")" 2>/dev/null
            cp "$file" "$dest_file" 2>/dev/null
        else
            cp "$file" "$dest" 2>/dev/null
        fi
    done
}

# 获取脚本目录和应用目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"

# 目录配置
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
BIN_DIR="${APP_HOME}/bin"
LOG_DIR="${APP_HOME}/logs"
ROLLBACK_DIR="${APP_HOME}/.rollback"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  应用升级/回滚脚本${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  upgrade     从压缩包升级应用"
    echo "  rollback    回滚到上一个还原点"
    echo "  status      查看还原点状态"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --package <path>    升级包路径 (zip/tar.gz/tar)"
    echo "  --no-restart        升级后不自动重启"
    echo "  --force             强制回滚，不询问确认"
    echo "  --help, -h          显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 upgrade --package /path/to/app-4.1.0.zip"
    echo "  $0 upgrade --package /path/to/app.tar.gz --no-restart"
    echo "  $0 rollback"
    echo "  $0 rollback --force"
    echo "  $0 status"
    echo ""
}

# 获取应用 PID
get_pid() {
    local jar_pattern="*.jar"
    pgrep -f "${APP_HOME}/${jar_pattern}" 2>/dev/null | head -1
}

# 停止应用
stop_app() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${BLUE}停止应用 (PID: ${pid})...${NC}"
        kill "$pid" 2>/dev/null
        
        local count=0
        while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            count=$((count + 1))
        done
        
        if ps -p "$pid" > /dev/null 2>&1; then
            kill -9 "$pid" 2>/dev/null
        fi
        
        echo -e "${GREEN}[成功] 应用已停止${NC}"
        return 0
    fi
    return 1
}

# 启动应用
start_app() {
    if [ -f "${BIN_DIR}/startup.sh" ]; then
        echo -e "${BLUE}启动应用...${NC}"
        bash "${BIN_DIR}/startup.sh" start
    else
        echo -e "${YELLOW}[警告] 未找到启动脚本，请手动启动应用${NC}"
    fi
}

# 创建还原点
create_rollback_point() {
    echo -e "${BLUE}创建还原点...${NC}"
    
    # 清理旧的还原点（只保留一个）
    rm -rf "${ROLLBACK_DIR}"
    mkdir -p "${ROLLBACK_DIR}"
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local rollback_info="${ROLLBACK_DIR}/rollback.info"
    
    # 获取当前 JAR 版本
    local current_jar=$(ls -1 "${APP_HOME}"/*.jar 2>/dev/null | head -1)
    local jar_name=""
    if [ -n "$current_jar" ]; then
        jar_name=$(basename "$current_jar")
    fi
    
    # 记录还原点信息
    cat > "${rollback_info}" << EOF
ROLLBACK_TIME=${timestamp}
JAR_NAME=${jar_name}
CREATED_BY=upgrade-rollback.sh
EOF
    
    # 计算总步骤数
    local total_steps=1  # 写入 info 文件
    [ -d "${APP_HOME}" ] && [ -n "$(ls -1 "${APP_HOME}"/*.jar 2>/dev/null)" ] && total_steps=$((total_steps + 1))
    [ -d "${CONFIG_DIR}" ] && total_steps=$((total_steps + 1))
    [ -d "${LIB_DIR}" ] && total_steps=$((total_steps + 1))
    [ -d "${BIN_DIR}" ] && total_steps=$((total_steps + 1))
    
    local current_step=1
    show_progress $current_step $total_steps "保存还原点信息"
    
    # 备份 JAR 文件
    local jar_count=0
    for jar in "${APP_HOME}"/*.jar; do
        if [ -f "$jar" ]; then
            cp "$jar" "${ROLLBACK_DIR}/"
            jar_count=$((jar_count + 1))
        fi
    done
    if [ $jar_count -gt 0 ]; then
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "备份 JAR 文件 (${jar_count}个)"
    fi
    
    # 备份 config 目录
    if [ -d "${CONFIG_DIR}" ]; then
        cp -r "${CONFIG_DIR}" "${ROLLBACK_DIR}/config"
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "备份 config 目录"
    fi
    
    # 备份 lib 目录
    if [ -d "${LIB_DIR}" ]; then
        cp -r "${LIB_DIR}" "${ROLLBACK_DIR}/lib"
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "备份 lib 目录"
    fi
    
    # 备份 bin 目录
    if [ -d "${BIN_DIR}" ]; then
        cp -r "${BIN_DIR}" "${ROLLBACK_DIR}/bin"
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "备份 bin 目录"
    fi
    
    echo -e "${GREEN}[成功] 还原点已创建: ${timestamp}${NC}"
}

# 从压缩包升级
do_upgrade() {
    local package_path="$1"
    local no_restart="$2"
    
    # 检查压缩包路径
    if [ -z "${package_path}" ]; then
        echo -e "${RED}[错误] 请指定升级包路径${NC}"
        echo "用法: $0 upgrade --package <path.zip|path.tar.gz>"
        exit 1
    fi
    
    if [ ! -f "${package_path}" ]; then
        echo -e "${RED}[错误] 升级包不存在: ${package_path}${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  应用升级${NC}"
    echo -e "  升级包: ${package_path}"
    echo -e "  应用目录: ${APP_HOME}"
    echo -e "${BLUE}==========================================${NC}"
    
    # 步骤1: 创建还原点
    echo -e "\n${YELLOW}[步骤 1/4] 创建还原点...${NC}"
    create_rollback_point
    
    # 步骤2: 停止应用
    local was_running=false
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        was_running=true
        echo -e "\n${YELLOW}[步骤 2/4] 停止应用...${NC}"
        stop_app
    else
        echo -e "\n${YELLOW}[步骤 2/4] 应用未运行，跳过停止${NC}"
    fi
    
    # 步骤3: 解压并覆盖
    echo -e "\n${YELLOW}[步骤 3/4] 解压升级包...${NC}"
    
    local temp_dir=$(mktemp -d)
    local extract_success=false
    
    # 根据文件类型解压
    case "${package_path}" in
        *.zip)
            if command -v unzip &> /dev/null; then
                unzip -q "${package_path}" -d "${temp_dir}" && extract_success=true
            else
                echo -e "${RED}[错误] 未找到 unzip 命令${NC}"
                rm -rf "${temp_dir}"
                exit 1
            fi
            ;;
        *.tar.gz|*.tgz)
            tar -xzf "${package_path}" -C "${temp_dir}" && extract_success=true
            ;;
        *.tar)
            tar -xf "${package_path}" -C "${temp_dir}" && extract_success=true
            ;;
        *)
            echo -e "${RED}[错误] 不支持的压缩包格式，仅支持 zip/tar.gz/tar${NC}"
            rm -rf "${temp_dir}"
            exit 1
            ;;
    esac
    
    if [ "${extract_success}" != "true" ]; then
        echo -e "${RED}[错误] 解压失败${NC}"
        rm -rf "${temp_dir}"
        exit 1
    fi
    
    # 查找解压后的根目录（可能有单层目录）
    local extract_root="${temp_dir}"
    local dir_count=$(find "${temp_dir}" -maxdepth 1 -type d | wc -l)
    if [ "${dir_count}" -eq 2 ]; then
        # 只有一个子目录，进入该目录
        extract_root=$(find "${temp_dir}" -maxdepth 1 -type d | tail -1)
    fi
    
    echo -e "${BLUE}复制文件到应用目录...${NC}"
    
    # 计算总步骤数
    local total_steps=0
    [ -n "$(ls -1 "${extract_root}"/*.jar 2>/dev/null)" ] && total_steps=$((total_steps + 1))
    [ -d "${extract_root}/config" ] && total_steps=$((total_steps + 1))
    [ -d "${extract_root}/lib" ] && total_steps=$((total_steps + 1))
    [ -d "${extract_root}/bin" ] && total_steps=$((total_steps + 1))
    
    [ $total_steps -eq 0 ] && total_steps=1
    local current_step=0
    
    # 复制 JAR 文件
    local jar_copied=false
    for jar in "${extract_root}"/*.jar; do
        if [ -f "$jar" ]; then
            cp "$jar" "${APP_HOME}/"
            jar_copied=true
        fi
    done
    if [ "${jar_copied}" = "true" ]; then
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "复制 JAR 文件"
    fi
    
    # 复制 config 目录
    if [ -d "${extract_root}/config" ]; then
        mkdir -p "${CONFIG_DIR}"
        cp -r "${extract_root}/config/"* "${CONFIG_DIR}/" 2>/dev/null || true
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "复制 config 目录"
    fi
    
    # 复制 lib 目录
    if [ -d "${extract_root}/lib" ]; then
        mkdir -p "${LIB_DIR}"
        cp -r "${extract_root}/lib/"* "${LIB_DIR}/" 2>/dev/null || true
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "复制 lib 目录"
    fi
    
    # 复制 bin 目录
    if [ -d "${extract_root}/bin" ]; then
        mkdir -p "${BIN_DIR}"
        cp -r "${extract_root}/bin/"* "${BIN_DIR}/" 2>/dev/null || true
        chmod +x "${BIN_DIR}"/*.sh 2>/dev/null || true
        chmod +x "${BIN_DIR}/script/"*.sh 2>/dev/null || true
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "复制 bin 目录"
    fi
    
    # 清理临时目录
    rm -rf "${temp_dir}"
    
    if [ "${jar_copied}" != "true" ]; then
        echo -e "${YELLOW}[警告] 未找到 JAR 文件，可能需要手动检查${NC}"
    fi
    
    # 步骤4: 重启应用
    if [ "${no_restart}" != "true" ] && [ "${was_running}" = "true" ]; then
        echo -e "\n${YELLOW}[步骤 4/4] 启动应用...${NC}"
        sleep 2
        start_app
    else
        echo -e "\n${YELLOW}[步骤 4/4] 跳过启动 (--no-restart 或应用之前未运行)${NC}"
    fi
    
    echo -e "\n${GREEN}==========================================${NC}"
    echo -e "${GREEN}  升级完成!${NC}"
    echo -e "${GREEN}  如需回滚，请执行: $0 rollback${NC}"
    echo -e "${GREEN}==========================================${NC}"
}

# 回滚到还原点
do_rollback() {
    local force="$1"
    local rollback_info="${ROLLBACK_DIR}/rollback.info"
    
    # 检查还原点是否存在
    if [ ! -d "${ROLLBACK_DIR}" ] || [ ! -f "${rollback_info}" ]; then
        echo -e "${RED}[错误] 没有可用的还原点${NC}"
        echo "请先执行 upgrade 命令创建还原点"
        exit 1
    fi
    
    # 读取还原点信息
    source "${rollback_info}"
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  回滚到还原点${NC}"
    echo -e "  还原点时间: ${ROLLBACK_TIME}"
    echo -e "  原 JAR: ${JAR_NAME}"
    echo -e "${BLUE}==========================================${NC}"
    
    # 确认回滚
    if [ "${force}" != "true" ]; then
        echo -e "\n${YELLOW}确认要回滚吗？这将覆盖当前文件 [y/N]${NC}"
        read -r confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            echo -e "${YELLOW}[取消] 回滚已取消${NC}"
            exit 0
        fi
    fi
    
    # 停止应用
    local was_running=false
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        was_running=true
        echo -e "\n${BLUE}停止应用...${NC}"
        stop_app
    fi
    
    echo -e "\n${BLUE}还原文件...${NC}"
    
    # 计算总步骤数
    local total_steps=0
    [ -n "$(ls -1 "${ROLLBACK_DIR}"/*.jar 2>/dev/null)" ] && total_steps=$((total_steps + 1))
    [ -d "${ROLLBACK_DIR}/config" ] && total_steps=$((total_steps + 1))
    [ -d "${ROLLBACK_DIR}/lib" ] && total_steps=$((total_steps + 1))
    [ -d "${ROLLBACK_DIR}/bin" ] && total_steps=$((total_steps + 1))
    
    [ $total_steps -eq 0 ] && total_steps=1
    local current_step=0
    
    # 还原 JAR 文件
    local jar_restored=false
    for jar in "${ROLLBACK_DIR}"/*.jar; do
        if [ -f "$jar" ]; then
            cp "$jar" "${APP_HOME}/"
            jar_restored=true
        fi
    done
    if [ "${jar_restored}" = "true" ]; then
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "还原 JAR 文件"
    fi
    
    # 还原 config 目录
    if [ -d "${ROLLBACK_DIR}/config" ]; then
        rm -rf "${CONFIG_DIR}"
        cp -r "${ROLLBACK_DIR}/config" "${CONFIG_DIR}"
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "还原 config 目录"
    fi
    
    # 还原 lib 目录
    if [ -d "${ROLLBACK_DIR}/lib" ]; then
        rm -rf "${LIB_DIR}"
        cp -r "${ROLLBACK_DIR}/lib" "${LIB_DIR}"
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "还原 lib 目录"
    fi
    
    # 还原 bin 目录
    if [ -d "${ROLLBACK_DIR}/bin" ]; then
        cp -r "${ROLLBACK_DIR}/bin/"* "${BIN_DIR}/" 2>/dev/null || true
        chmod +x "${BIN_DIR}"/*.sh 2>/dev/null || true
        chmod +x "${BIN_DIR}/script/"*.sh 2>/dev/null || true
        current_step=$((current_step + 1))
        show_progress $current_step $total_steps "还原 bin 目录"
    fi
    
    # 删除还原点（已使用）
    rm -rf "${ROLLBACK_DIR}"
    
    # 重启应用
    if [ "${was_running}" = "true" ]; then
        echo -e "\n${BLUE}启动应用...${NC}"
        sleep 2
        start_app
    fi
    
    echo -e "\n${GREEN}==========================================${NC}"
    echo -e "${GREEN}  回滚完成!${NC}"
    echo -e "${GREEN}==========================================${NC}"
}

# 查看还原点状态
show_status() {
    local rollback_info="${ROLLBACK_DIR}/rollback.info"
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  还原点状态${NC}"
    echo -e "${BLUE}==========================================${NC}"
    
    if [ ! -d "${ROLLBACK_DIR}" ] || [ ! -f "${rollback_info}" ]; then
        echo -e "\n${YELLOW}[提示] 当前没有可用的还原点${NC}"
        echo -e "执行 upgrade 命令时会自动创建还原点"
        return 0
    fi
    
    # 读取还原点信息
    source "${rollback_info}"
    
    echo -e "\n${GREEN}[可用]${NC} 还原点信息:"
    echo -e "  创建时间: ${ROLLBACK_TIME}"
    echo -e "  JAR 文件: ${JAR_NAME}"
    echo -e "  创建来源: ${CREATED_BY:-unknown}"
    
    # 显示备份内容
    echo -e "\n${BLUE}备份内容:${NC}"
    
    local jar_count=$(ls -1 "${ROLLBACK_DIR}"/*.jar 2>/dev/null | wc -l)
    echo -e "  JAR 文件: ${jar_count} 个"
    
    [ -d "${ROLLBACK_DIR}/config" ] && echo -e "  config/: ${GREEN}✓${NC}" || echo -e "  config/: ${YELLOW}✗${NC}"
    [ -d "${ROLLBACK_DIR}/lib" ] && echo -e "  lib/:    ${GREEN}✓${NC}" || echo -e "  lib/:    ${YELLOW}✗${NC}"
    [ -d "${ROLLBACK_DIR}/bin" ] && echo -e "  bin/:    ${GREEN}✓${NC}" || echo -e "  bin/:    ${YELLOW}✗${NC}"
    
    # 显示备份大小
    local backup_size=$(du -sh "${ROLLBACK_DIR}" 2>/dev/null | cut -f1)
    echo -e "\n  总大小: ${backup_size}"
    
    echo -e "\n${BLUE}可用操作:${NC}"
    echo -e "  回滚: $0 rollback"
    echo -e "  强制回滚: $0 rollback --force"
}

# ============================================================
# 主程序
# ============================================================

COMMAND=""
PACKAGE_PATH=""
NO_RESTART="false"
FORCE="false"

# 解析参数
while [ $# -gt 0 ]; do
    case "$1" in
        upgrade|rollback|status)
            COMMAND="$1"
            shift
            ;;
        --package)
            PACKAGE_PATH="$2"
            shift 2
            ;;
        --no-restart)
            NO_RESTART="true"
            shift
            ;;
        --force)
            FORCE="true"
            shift
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}[错误] 未知参数: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 检查命令
if [ -z "${COMMAND}" ]; then
    show_help
    exit 1
fi

# 执行命令
case "${COMMAND}" in
    upgrade)
        do_upgrade "${PACKAGE_PATH}" "${NO_RESTART}"
        ;;
    rollback)
        do_rollback "${FORCE}"
        ;;
    status)
        show_status
        ;;
    *)
        show_help
        exit 1
        ;;
esac
