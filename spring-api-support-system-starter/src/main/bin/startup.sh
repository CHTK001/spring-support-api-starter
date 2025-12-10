#!/bin/bash
# ============================================================
# 平台系统启动脚本
# 作者: CH
# 创建时间: 2024/12/08
# 版本: 1.0.0
# ============================================================

# 基础配置
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="spring-api-support-system-starter"
APP_VERSION="${APP_VERSION:-4.0.0}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# 目录配置
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"

# 调试端口配置
DEFAULT_DEBUG_PORT=5005

# JVM配置
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring配置
SPRING_OPTS="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
SPRING_OPTS="${SPRING_OPTS} --spring.profiles.active=${SPRING_PROFILE:-prod}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  ${APP_NAME} 管理脚本${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令] [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  start       启动应用 (默认)"
    echo "  stop        停止应用"
    echo "  restart     重启应用"
    echo "  status      查看应用状态"
    echo "  debug       以调试模式启动"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --debug-port <port>   指定调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --help, -h            显示帮助信息"
    echo ""
}

check_java() {
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java &> /dev/null; then
        JAVA_CMD="java"
    elif [ -x "/usr/bin/java" ]; then
        JAVA_CMD="/usr/bin/java"
    elif [ -x "/usr/local/bin/java" ]; then
        JAVA_CMD="/usr/local/bin/java"
    else
        echo -e "${RED}[错误] 未找到Java环境${NC}"
        echo "请设置 JAVA_HOME 或确保 java 在 PATH 中"
        exit 1
    fi
    echo -e "${BLUE}Java: ${JAVA_CMD}${NC}"
}

build_loader_path() {
    LOADER_PATH="${CONFIG_DIR}"
    [ -d "${LIB_DIR}" ] && LOADER_PATH="${LOADER_PATH},${LIB_DIR}"
}

get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[运行中] ${APP_NAME} (PID: ${pid})${NC}"
    else
        echo -e "${YELLOW}[已停止] ${APP_NAME}${NC}"
    fi
}

start_app() {
    local pid=$(get_pid)
    [ -n "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 已在运行 (PID: ${pid})${NC}" && return 1
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    
    local start_opts="${SPRING_OPTS}"
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  ${APP_NAME} 启动中...${NC}"
    echo -e "  版本: ${APP_VERSION}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "  调试端口: ${DEBUG_PORT}"
    echo -e "${BLUE}==========================================${NC}"
    
    local java_opts="${JAVA_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    nohup "$JAVA_CMD" ${java_opts} -Dloader.path="${LOADER_PATH}" -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${LOG_DIR}/startup.log" 2>&1 &
    
    echo "$!" > "${PID_FILE}"
    sleep 2
    ps -p "$!" > /dev/null 2>&1 && echo -e "${GREEN}[成功] 已启动 (PID: $!)${NC}" || echo -e "${RED}[失败] 启动失败${NC}"
}

stop_app() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 0
    
    echo -e "${BLUE}正在停止 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
    done
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[成功] ${APP_NAME} 已停止${NC}"
}

restart_app() { stop_app; sleep 2; start_app; }

COMMAND="start"; DEBUG_PORT="${DEFAULT_DEBUG_PORT}"; DEBUG_MODE="false"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|debug) COMMAND="$1"; shift ;;
        --debug-port) DEBUG_PORT="$2"; shift 2 ;;
        --help|-h) show_help; exit 0 ;;
        *) echo -e "${RED}[错误] 未知参数: $1${NC}"; show_help; exit 1 ;;
    esac
done

[ "$COMMAND" = "debug" ] && DEBUG_MODE="true" && COMMAND="start"

case "$COMMAND" in
    start) start_app ;;
    stop) stop_app ;;
    restart) restart_app ;;
    status) check_status ;;
    *) show_help; exit 1 ;;
esac
