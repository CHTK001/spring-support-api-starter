#!/bin/bash
# ============================================================
# 运维监控系统启动脚本
# 作者: CH
# 创建时间: 2024/12/08
# 版本: 1.0.0
# ============================================================

# 基础配置
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="spring-api-support-monitor-starter"
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
JAVA_OPTS="${JAVA_OPTS:--Xms1024m -Xmx2048m -XX:+UseG1GC}"
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

# 显示帮助信息
show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  ${APP_NAME} 管理脚本${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 [命令] [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  start       启动应用 (默认)"
    echo "  stop        停止应用"
    echo "  restart     重启应用"
    echo "  status      查看应用状态"
    echo "  jvm         查看JVM信息"
    echo "  tuning      JVM调优建议"
    echo "  analyze     CPU/内存分析"
    echo "  arthas      启动Arthas诊断"
    echo "  backup      备份配置/JAR"
    echo "  restore     还原配置/JAR"
    echo "  debug       以调试模式启动"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --jvm-opts <opts>     自定义JVM参数 (如: '-Xms1g -Xmx2g')"
    echo "  --debug-port <port>   指定调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --help, -h            显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 start                        # 启动应用"
    echo "  $0 debug --debug-port 8000      # 以调试模式启动"
    echo "  $0 restart                      # 重启应用"
    echo "  $0 stop                         # 停止应用"
    echo ""
    echo -e "${YELLOW}环境变量:${NC}"
    echo "  APP_VERSION       应用版本号 (默认: 4.0.0)"
    echo "  JAVA_HOME         Java安装目录"
    echo "  JAVA_OPTS         JVM参数"
    echo "  SPRING_PROFILE    Spring配置文件 (默认: prod)"
    echo ""
}

# 检查Java环境
check_java() {
    # 优先使用 JAVA_HOME
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
        echo -e "${YELLOW}请执行以下操作之一:${NC}"
        echo "  1. 设置 JAVA_HOME 环境变量: export JAVA_HOME=/path/to/java"
        echo "  2. 确保 java 在 PATH 中"
        echo ""
        echo -e "${YELLOW}常见 Java 路径:${NC}"
        echo "  - /usr/lib/jvm/java-21-openjdk"
        echo "  - /usr/lib/jvm/java-17-openjdk"
        echo "  - /usr/java/latest"
        exit 1
    fi
    
    # 验证 Java 版本
    echo -e "${BLUE}Java 路径: ${JAVA_CMD}${NC}"
    $JAVA_CMD -version 2>&1 | head -1
}

# 构建loader.path
build_loader_path() {
    LOADER_PATH="${CONFIG_DIR}"
    if [ -d "${LIB_DIR}" ]; then
        LOADER_PATH="${LOADER_PATH},${LIB_DIR}"
    fi
}

# 获取进程PID
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo "$pid"
            return 0
        fi
    fi
    # 通过进程名查找
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

# 检查应用状态
check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[运行中] ${APP_NAME} (PID: ${pid})${NC}"
        return 0
    else
        echo -e "${YELLOW}[已停止] ${APP_NAME}${NC}"
        return 1
    fi
}

# 启动应用
start_app() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}[警告] ${APP_NAME} 已在运行 (PID: ${pid})${NC}"
        return 1
    fi
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    
    local start_opts="${SPRING_OPTS}"
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  ${APP_NAME} 启动中...${NC}"
    echo -e "${BLUE}==========================================${NC}"
    echo -e "  版本: ${APP_VERSION}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "  调试端口: ${DEBUG_PORT}"
    echo -e "${BLUE}==========================================${NC}"
    
    local java_opts="${JAVA_OPTS}"
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
    if [ "${DEBUG_MODE}" = "true" ]; then
        java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    fi
    
    nohup "$JAVA_CMD" ${java_opts} \
        -Dloader.path="${LOADER_PATH}" \
        -jar "${APP_HOME}/${JAR_NAME}" \
        ${start_opts} \
        > "${LOG_DIR}/startup.log" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    
    sleep 2
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}[成功] ${APP_NAME} 已启动 (PID: ${new_pid})${NC}"
        echo -e "日志文件: ${LOG_DIR}/startup.log"
    else
        echo -e "${RED}[失败] ${APP_NAME} 启动失败，请查看日志${NC}"
        cat "${LOG_DIR}/startup.log"
        return 1
    fi
}

# 停止应用
stop_app() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}"
        return 0
    fi
    
    echo -e "${BLUE}正在停止 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ]; do
        if ! ps -p "$pid" > /dev/null 2>&1; then
            break
        fi
        sleep 1
        count=$((count + 1))
    done
    
    if ps -p "$pid" > /dev/null 2>&1; then
        echo -e "${YELLOW}[警告] 正在强制终止进程...${NC}"
        kill -9 "$pid" 2>/dev/null
    fi
    
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[成功] ${APP_NAME} 已停止${NC}"
}

# 重启应用
restart_app() {
    stop_app
    sleep 2
    start_app
}

# 查看JVM信息
show_jvm_info() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    bash "${SCRIPT_DIR}/script/jvm-info.sh" "$pid" "$APP_NAME"
}

show_tuning() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    bash "${SCRIPT_DIR}/script/jvm-tuning.sh" "$pid" "$APP_NAME"
}

start_arthas() {
    local pid=$(get_pid)
    bash "${SCRIPT_DIR}/script/arthas-install.sh" "$pid"
}

run_analyze() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    bash "${SCRIPT_DIR}/script/cpu-analysis.sh" "$pid" "${1:-cpu}" "$APP_NAME"
}

run_backup() { bash "${SCRIPT_DIR}/script/backup-restore.sh" backup "${1:-all}"; }
run_restore() { bash "${SCRIPT_DIR}/script/backup-restore.sh" restore "${1:-all}"; }

# 解析参数
COMMAND="start"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
DEBUG_MODE="false"
CUSTOM_JVM_OPTS=""

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|jvm|tuning|analyze|arthas|backup|restore|debug)
            COMMAND="$1"
            shift
            ;;
        cpu|mem|all|config|jar)
            EXTRA_MODE="$1"
            shift
            ;;
        --jvm-opts)
            CUSTOM_JVM_OPTS="$2"
            shift 2
            ;;
        --debug-port)
            DEBUG_PORT="$2"
            shift 2
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

# 调试模式处理
if [ "$COMMAND" = "debug" ]; then
    DEBUG_MODE="true"
    COMMAND="start"
fi

# 执行命令
case "$COMMAND" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        check_status
        ;;
    jvm)
        show_jvm_info
        ;;
    tuning)
        show_tuning
        ;;
    arthas)
        start_arthas
        ;;
    analyze)
        run_analyze "${EXTRA_MODE:-cpu}"
        ;;
    backup)
        run_backup "${EXTRA_MODE:-all}"
        ;;
    restore)
        run_restore "${EXTRA_MODE:-all}"
        ;;
    *)
        show_help
        exit 1
        ;;
esac
