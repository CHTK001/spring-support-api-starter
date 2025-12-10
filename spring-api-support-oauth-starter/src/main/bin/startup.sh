#!/bin/bash
# ============================================================
# OAuth 单点登录系统启动脚本
# 作者: CH
# 创建时间: 2024/12/08
# 版本: 2.0.0
# ============================================================

# 基础配置
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="spring-api-support-oauth-starter"
APP_VERSION="${APP_VERSION:-4.0.0}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# 目录配置
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"
BACKUP_DIR="${APP_HOME}/backup"

# 调试端口配置
DEFAULT_DEBUG_PORT=5005

# Docker 配置
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

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
    echo "  jvm         查看JVM信息"
    echo "  tuning      JVM调优建议"
    echo "  analyze     CPU/内存分析"
    echo "  dump        生成转储文件 (heap/thread/all)"
    echo "  arthas      启动Arthas诊断"
    echo "  backup      备份配置/JAR"
    echo "  restore     还原配置/JAR"
    echo "  upgrade         从压缩包升级 (支持 zip/tar.gz)"
    echo "  rollback        回滚到上一个还原点"
    echo "  rollback-status 查看还原点状态"
    echo "  debug       以调试模式启动"
    echo ""
    echo -e "${YELLOW}Docker 命令:${NC}"
    echo "  docker-build     构建 Docker 镜像"
    echo "  docker-deploy    构建并部署到本机 (一键发布)"
    echo "  publish          docker-deploy 的简写"
    echo "  docker-start     启动 Docker 容器"
    echo "  docker-stop      停止 Docker 容器"
    echo "  docker-restart   重启 Docker 容器"
    echo "  docker-status    查看容器状态"
    echo "  docker-logs      查看容器日志"
    echo "  docker-clean     清理旧镜像"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --jvm-opts <opts>     自定义JVM参数 (如: '-Xms1g -Xmx2g')"
    echo "  --debug-port <port>   指定调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --docker-port <port>  指定容器内部端口 (默认: 8080)"
    echo "  --host-port <port>    指定主机映射端口 (默认: 8080)"
    echo "  --image-tag <tag>     指定镜像标签 (默认: ${APP_VERSION})"
    echo "  --package <path>      升级包路径 (zip/tar.gz)"
    echo "  --no-restart          升级后不自动重启"
    echo "  --output <path>       dump 输出目录 (默认: logs/dump)"
    echo "  --help, -h            显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 start                              # 启动应用"
    echo "  $0 stop                               # 停止应用"
    echo "  $0 restart                            # 重启应用"
    echo "  $0 status                             # 查看状态"
    echo "  $0 debug --debug-port 5006            # 调试模式启动"
    echo "  $0 dump heap                          # 生成堆转储"
    echo "  $0 dump thread --output /tmp/dump     # 线程转储到指定目录"
    echo "  $0 analyze cpu                        # CPU 分析"
    echo "  $0 upgrade --package app-4.1.0.zip    # 从压缩包升级"
    echo "  $0 rollback                           # 回滚到还原点"
    echo "  $0 publish                            # Docker 一键发布"
    echo "  $0 docker-deploy --host-port 9090     # 自定义端口发布"
    echo "  $0 docker-logs                        # 查看容器日志"
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
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
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
    local mode="${1:-cpu}"
    bash "${SCRIPT_DIR}/script/cpu-analysis.sh" "$pid" "$mode" "$APP_NAME"
}

run_backup() {
    bash "${SCRIPT_DIR}/script/backup-restore.sh" backup "${1:-all}" "$APP_NAME" "$JAR_NAME"
}

run_restore() {
    bash "${SCRIPT_DIR}/script/backup-restore.sh" restore "${1:-all}" "$APP_NAME" "$JAR_NAME"
}

# ============================================================
# Dump 转储功能
# ============================================================

run_dump() {
    local mode="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[错误] ${APP_NAME} 未在运行${NC}"
        exit 1
    fi
    
    # 创建输出目录
    mkdir -p "${output_dir}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  生成 Dump 转储${NC}"
    echo -e "  PID: ${pid}"
    echo -e "  模式: ${mode}"
    echo -e "  输出目录: ${output_dir}"
    echo -e "${BLUE}==========================================${NC}"
    
    # 检查 jmap 和 jstack 命令
    local jmap_cmd="jmap"
    local jstack_cmd="jstack"
    if [ -n "$JAVA_HOME" ]; then
        jmap_cmd="$JAVA_HOME/bin/jmap"
        jstack_cmd="$JAVA_HOME/bin/jstack"
    fi
    
    case "${mode}" in
        heap)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            ;;
        thread)
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        all)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        *)
            echo -e "${RED}[错误] 未知的 dump 模式: ${mode}${NC}"
            echo "支持的模式: heap, thread, all"
            exit 1
            ;;
    esac
    
    echo -e "\n${GREEN}[完成] Dump 文件已保存到: ${output_dir}${NC}"
}

# 生成堆转储
dump_heap() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jmap_cmd="$4"
    local heap_file="${output_dir}/heapdump_${timestamp}.hprof"
    
    echo -e "\n${BLUE}[堆转储] 生成中...${NC}"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jmap 命令${NC}"
        echo "请确保已安装 JDK 并设置 JAVA_HOME"
        return 1
    fi
    
    # 生成堆转储
    "${jmap_cmd}" -dump:format=b,file="${heap_file}" "${pid}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${heap_file}" ]; then
        local size=$(du -h "${heap_file}" | cut -f1)
        echo -e "${GREEN}  ✓ 堆转储: ${heap_file} (${size})${NC}"
    else
        echo -e "${RED}  ✗ 堆转储失败${NC}"
    fi
}

# 生成线程转储
dump_thread() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jstack_cmd="$4"
    local thread_file="${output_dir}/threaddump_${timestamp}.txt"
    
    echo -e "\n${BLUE}[线程转储] 生成中...${NC}"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jstack 命令${NC}"
        echo "请确保已安装 JDK 并设置 JAVA_HOME"
        return 1
    fi
    
    # 生成线程转储
    "${jstack_cmd}" -l "${pid}" > "${thread_file}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${thread_file}" ]; then
        local lines=$(wc -l < "${thread_file}")
        echo -e "${GREEN}  ✓ 线程转储: ${thread_file} (${lines} 行)${NC}"
        
        # 简单分析线程状态
        echo -e "\n${BLUE}[线程状态统计]${NC}"
        echo -e "  RUNNABLE:      $(grep -c 'java.lang.Thread.State: RUNNABLE' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  WAITING:       $(grep -c 'java.lang.Thread.State: WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  TIMED_WAITING: $(grep -c 'java.lang.Thread.State: TIMED_WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  BLOCKED:       $(grep -c 'java.lang.Thread.State: BLOCKED' "${thread_file}" 2>/dev/null || echo 0)"
        
        # 检查死锁
        if grep -q "Found.*deadlock" "${thread_file}" 2>/dev/null; then
            echo -e "\n${RED}[警告] 检测到死锁!${NC}"
        fi
    else
        echo -e "${RED}  ✗ 线程转储失败${NC}"
    fi
}

# ============================================================
# 升级和回滚功能 (调用外部脚本)
# ============================================================

run_upgrade() {
    local package_path="$1"
    local no_restart="$2"
    local opts=""
    
    [ -n "${package_path}" ] && opts="${opts} --package ${package_path}"
    [ "${no_restart}" = "true" ] && opts="${opts} --no-restart"
    
    bash "${SCRIPT_DIR}/script/upgrade-rollback.sh" upgrade ${opts}
}

run_rollback() {
    bash "${SCRIPT_DIR}/script/upgrade-rollback.sh" rollback
}

run_rollback_status() {
    bash "${SCRIPT_DIR}/script/upgrade-rollback.sh" status
}

# ============================================================
# Docker 相关函数 (调用外部脚本)
# ============================================================

docker_build() {
    bash "${SCRIPT_DIR}/script/docker.sh" build --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
}

docker_deploy() {
    bash "${SCRIPT_DIR}/script/docker.sh" deploy --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
}

docker_start_container() {
    bash "${SCRIPT_DIR}/script/docker.sh" start --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
}

docker_stop_container() {
    bash "${SCRIPT_DIR}/script/docker.sh" stop --name "$APP_NAME"
}

docker_restart_container() {
    bash "${SCRIPT_DIR}/script/docker.sh" restart --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
}

docker_status() {
    bash "${SCRIPT_DIR}/script/docker.sh" status --name "$APP_NAME"
}

docker_logs() {
    local lines="${1:-100}"
    bash "${SCRIPT_DIR}/script/docker.sh" logs --name "$APP_NAME" --lines "$lines"
}

docker_clean() {
    bash "${SCRIPT_DIR}/script/docker.sh" clean --name "$APP_NAME" --tag "$DOCKER_IMAGE_TAG"
}

COMMAND="start"; DEBUG_PORT="${DEFAULT_DEBUG_PORT}"; DEBUG_MODE="false"; CUSTOM_JVM_OPTS=""

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|jvm|tuning|analyze|dump|arthas|backup|restore|upgrade|rollback|rollback-status|debug) COMMAND="$1"; shift ;;
        docker-build|docker-deploy|docker-start|docker-stop|docker-restart|docker-status|docker-logs|docker-clean|publish) COMMAND="$1"; shift ;;
        cpu|mem|all|config|jar|heap|thread) EXTRA_MODE="$1"; shift ;;
        --jvm-opts) CUSTOM_JVM_OPTS="$2"; shift 2 ;;
        --debug-port) DEBUG_PORT="$2"; shift 2 ;;
        --docker-port) DOCKER_PORT="$2"; shift 2 ;;
        --host-port) DOCKER_HOST_PORT="$2"; shift 2 ;;
        --image-tag) DOCKER_IMAGE_TAG="$2"; shift 2 ;;
        --package) UPGRADE_PACKAGE="$2"; shift 2 ;;
        --no-restart) NO_RESTART="true"; shift ;;
        --output) DUMP_OUTPUT="$2"; shift 2 ;;
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
    jvm) show_jvm_info ;;
    tuning) show_tuning ;;
    arthas) start_arthas ;;
    analyze) run_analyze "${EXTRA_MODE:-cpu}" ;;
    dump) run_dump "${EXTRA_MODE:-all}" "${DUMP_OUTPUT}" ;;
    backup) run_backup "${EXTRA_MODE:-all}" ;;
    restore) run_restore "${EXTRA_MODE:-all}" ;;
    upgrade) run_upgrade "${UPGRADE_PACKAGE}" "${NO_RESTART}" ;;
    rollback) run_rollback ;;
    rollback-status) run_rollback_status ;;
    docker-build) docker_build ;;
    docker-deploy|publish) docker_deploy ;;
    docker-start) docker_start_container ;;
    docker-stop) docker_stop_container ;;
    docker-restart) docker_restart_container ;;
    docker-status) docker_status ;;
    docker-logs) docker_logs "${EXTRA_MODE:-100}" ;;
    docker-clean) docker_clean ;;
    *) show_help; exit 1 ;;
esac
