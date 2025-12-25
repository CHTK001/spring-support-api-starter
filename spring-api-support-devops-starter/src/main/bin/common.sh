#!/bin/bash
# ============================================================
# 公共函数库
# 所有子脚本通过 source 引入此文件获取公共函数
# ============================================================

# ===========================================
# 颜色定义
# ===========================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
NC='\033[0m'

# ===========================================
# 日志函数
# ===========================================
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_warning() {
    log_warn "$1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

# ===========================================
# 路径初始化
# ===========================================
init_paths() {
    # 如果已经初始化过，跳过
    [ -n "$APP_HOME" ] && return 0
    
    # 获取脚本目录
    if [ -z "$SCRIPT_DIR" ]; then
        SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    fi
    
    # 根据调用位置确定 APP_HOME
    if [[ "$SCRIPT_DIR" == */bin/script/* ]]; then
        # 从 script/linux 或 script/cluster 等子目录调用
        APP_HOME="$(cd "$SCRIPT_DIR/../../.." && pwd)"
        BIN_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
    elif [[ "$SCRIPT_DIR" == */bin ]]; then
        # 从 bin 目录调用
        APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
        BIN_DIR="$SCRIPT_DIR"
    else
        # 默认情况
        APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
        BIN_DIR="$SCRIPT_DIR"
    fi
    
    # 目录配置
    CONFIG_DIR="${APP_HOME}/config"
    LIB_DIR="${APP_HOME}/lib"
    LOG_DIR="${APP_HOME}/logs"
    BACKUP_DIR="${APP_HOME}/backup"
    
    # 日志文件
    STARTUP_LOG="${LOG_DIR}/startup.log"
    
    # 配置文件
    APP_ENV_FILE="${APP_HOME}/app.env"
    JAVA_CONFIG_FILE="${APP_HOME}/.java_path"
    
    # 子脚本目录
    SUBSCRIPT_DIR="${BIN_DIR}/script/linux"
}

# ===========================================
# 应用元数据解析
# ===========================================
resolve_app_metadata() {
    # 默认值 (Maven 资源过滤替换)
    APP_NAME="${APP_NAME:-@project.artifactId@}"
    APP_VERSION="${APP_VERSION:-@project.version@}"
    JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"
    
    local needs_resolve="false"
    if echo "${APP_NAME}${APP_VERSION}${JAR_NAME}" | grep -q "@project."; then
        needs_resolve="true"
    fi
    if [ ! -f "${APP_HOME}/${JAR_NAME}" ]; then
        needs_resolve="true"
    fi

    if [ "${needs_resolve}" = "true" ]; then
        local found_jar=$(ls -1t "${APP_HOME}"/*.jar 2>/dev/null | grep -v -E '(sources|javadoc)\.jar$' | head -n 1)
        if [ -n "${found_jar}" ]; then
            JAR_NAME="$(basename "${found_jar}")"
            local base="${JAR_NAME%.jar}"
            if echo "${base}" | grep -qE -- '-[0-9]'; then
                APP_VERSION="${base##*-}"
                APP_NAME="${base%-${APP_VERSION}}"
            fi
        fi
    fi
}

# 读取 app.env
load_app_env() {
    local env_file="${APP_ENV_FILE}"
    [ ! -f "${env_file}" ] && env_file="${APP_HOME}/bin/app.env"
    if [ -f "${env_file}" ]; then
        while IFS='=' read -r k v; do
            case "$k" in
                \#*|'' ) continue ;;
                APP_NAME) APP_NAME="$v" ;;
                APP_VERSION) APP_VERSION="$v" ;;
                JAR_NAME) JAR_NAME="$v" ;;
            esac
        done < <(sed -e 's/\r$//' "$env_file")
    fi
}

# 保存 app.env
save_app_env() {
    echo "APP_NAME=${APP_NAME}" > "${APP_ENV_FILE}"
    echo "APP_VERSION=${APP_VERSION}" >> "${APP_ENV_FILE}"
    echo "JAR_NAME=${JAR_NAME}" >> "${APP_ENV_FILE}"
}

# 初始化依赖 APP_NAME 的变量
init_app_vars() {
    PID_FILE="${APP_HOME}/${APP_NAME}.pid"
    APP_LOG="${LOG_DIR}/${APP_NAME}.log"
}

# ===========================================
# Java 检测
# ===========================================
# 加载保存的 Java 路径
load_java_path() {
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        if [ -n "$saved_path" ] && [ -x "$saved_path" ]; then
            CUSTOM_JAVA="$saved_path"
        fi
    fi
}

# 保存 Java 路径
save_java_path() {
    local java_path="$1"
    if [ -n "$java_path" ] && [ -x "$java_path" ]; then
        echo "$java_path" > "${JAVA_CONFIG_FILE}"
        log_info "Java 路径已保存: ${java_path}"
    fi
}

# 检测 Java
check_java() {
    [ -z "$CUSTOM_JAVA" ] && load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -x "$CUSTOM_JAVA" ]; then
            JAVA_CMD="$CUSTOM_JAVA"
        else
            log_error "指定的 Java 路径不存在或不可执行: ${CUSTOM_JAVA}"
            exit 1
        fi
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java &> /dev/null; then
        JAVA_CMD="java"
    elif [ -x "/usr/bin/java" ]; then
        JAVA_CMD="/usr/bin/java"
    elif [ -x "/usr/local/bin/java" ]; then
        JAVA_CMD="/usr/local/bin/java"
    else
        log_error "未找到 Java 环境"
        echo "请设置 JAVA_HOME 或使用 -j 指定 Java 路径"
        exit 1
    fi
}

# 获取 Java 版本
get_java_version() {
    "$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}'
}

# ===========================================
# 进程管理
# ===========================================
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

is_running() {
    local pid=$(get_pid)
    [ -n "$pid" ]
}

# ===========================================
# Loader Path 构建
# ===========================================
build_loader_path() {
    local acc=()
    [ -d "${CONFIG_DIR}" ] && acc+=("${CONFIG_DIR}")
    [ -d "${LIB_DIR}" ] && acc+=("${LIB_DIR}")

    if [ ${#acc[@]} -gt 0 ]; then
        LOADER_PATH=$(IFS=, ; echo "${acc[*]}")
    else
        LOADER_PATH=""
    fi
}

# ===========================================
# 默认配置
# ===========================================
# JVM 配置
DEFAULT_JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
DEFAULT_DEBUG_PORT=5005

# Spring 配置
DEFAULT_SPRING_PROFILE="prod"

# Skywalking 配置
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.3.0}"

# Docker 配置
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

# ===========================================
# 工具函数
# ===========================================
# 获取本机 IP
get_local_ip() {
    local ip=""
    if command -v ip &> /dev/null; then
        ip=$(ip route get 1 2>/dev/null | awk '{print $7;exit}')
    fi
    if [ -z "$ip" ] && command -v hostname &> /dev/null; then
        ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    if [ -z "$ip" ] && command -v ifconfig &> /dev/null; then
        ip=$(ifconfig 2>/dev/null | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1)
    fi
    echo "${ip:-127.0.0.1}"
}

# 打印分隔线
print_line() {
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
}

# 打印标题
print_title() {
    local title="$1"
    print_line
    echo -e "${GREEN}  ${title}${NC}"
    print_line
}

# ===========================================
# 初始化
# ===========================================
# 自动初始化路径
init_paths
resolve_app_metadata
load_app_env
init_app_vars

# BASE_DIR 用于外部组件安装目录 (必须在 init_paths 之后设置)
BASE_DIR="${BASE_DIR:-${APP_HOME}}"
