#!/bin/bash
# ============================================================
# 应用管理主脚本
# 版本: 3.0.0
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}              ${APP_NAME} 管理脚本 v3.0                  ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}基础命令:${NC}"
    echo "  start              启动应用 (默认)"
    echo "  stop               停止应用"
    echo "  restart            重启应用"
    echo "  status             查看应用状态"
    echo ""
    echo -e "${YELLOW}子命令:${NC} (使用 '$0 <子命令> -h' 查看详细帮助)"
    echo "  info               显示应用信息"
    echo "  logs               日志管理"
    echo "  java               Java 环境管理"
    echo "  arthas             Arthas 诊断工具"
    echo "  skywalking         Skywalking Agent 管理"
    echo "  backup             备份管理"
    echo "  restore            还原管理"
    echo "  upgrade            升级管理"
    echo "  service            Systemd 服务管理"
    echo "  dump               转储管理"
    echo "  gc                 GC 触发"
    echo "  top                CPU/内存分析"
    echo "  jvm                JVM 诊断与调优"
    echo "  docker             Docker 管理"
    echo ""
    echo -e "${YELLOW}启动选项:${NC}"
    echo "  -f                 启动后实时跟踪日志"
    echo "  -j <path>          指定 Java 路径 (自动持久化)"
    echo "  --port <port>      指定启动端口"
    echo "  --memory <size>    设置内存 (如: 2g → -Xms2g -Xmx2g)"
    echo "  --gc <type>        选择 GC 类型 (g1/zgc/shenandoah/parallel/serial)"
    echo "  --config <path>    指定额外配置文件路径"
    echo "  --env <K=V>        设置环境变量 (可多次使用)"
    echo "  --log-level <level> 设置日志级别 (debug/info/warn/error)"
    echo "  --skywalking       启用 Skywalking Agent"
    echo "  --skywalking-path <path>  指定 Skywalking 根目录 (优先级高于 --skywalking，无需下载)"
    echo "  --dev              开发环境 (spring.profiles.active=dev)"
    echo "  --prod             生产环境 (默认)"
    echo "  --test             测试环境"
    echo "  --profile <name>   自定义环境"
    echo "  --debug            调试模式"
    echo "  --debug-port <port> 调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --jvm <opts>       自定义 JVM 参数 (追加)"
    echo "  --dry-run          只显示启动命令，不执行"
    echo "  --wait-ready       等待应用就绪后返回"
    echo "  --graceful-timeout <s>  优雅停机超时时间 (秒)"
    echo "  --arthas-port <port>   启用 Arthas 并指定端口"
    echo "  --prometheus-port <port> 启用 Prometheus metrics 端口"
    echo "  --async-profiler   启用 async-profiler"
    echo "  -h, --help         显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 start                    启动应用"
    echo "  $0 start -f                 启动并跟踪日志"
    echo "  $0 start --dev              开发环境启动"
    echo "  $0 start -j /opt/jdk21/bin/java  指定 Java 启动"
    echo "  $0 start --debug            调试模式启动"
    echo "  $0 stop                     停止应用"
    echo "  $0 restart                  重启应用"
    echo "  $0 status                   查看状态"
    echo "  $0 info                     查看应用信息"
    echo "  $0 logs -f                  实时跟踪日志"
    echo ""
}

# ===========================================
# 启动横幅
# ===========================================
show_startup_banner() {
    check_java
    local java_version=$(get_java_version)
    
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                     应用启动中...                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}应用名称${NC}    │ ${GREEN}${APP_NAME}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}应用版本${NC}    │ ${GREEN}${APP_VERSION}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}运行环境${NC}    │ ${YELLOW}${SPRING_PROFILE}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}Java 版本${NC}   │ ${MAGENTA}${java_version}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}Java 路径${NC}   │ ${MAGENTA}${JAVA_CMD}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}JVM 参数${NC}    │ ${MAGENTA}${JAVA_OPTS}${NC}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "${CYAN}│${NC} ${WHITE}调试端口${NC}    │ ${RED}${DEBUG_PORT}${NC}"
    [ "${SKYWALKING_ENABLED}" = "true" ] && echo -e "${CYAN}│${NC} ${WHITE}Skywalking${NC}  │ ${GREEN}已启用${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}Loader 路径${NC} │ ${MAGENTA}${LOADER_PATH:-<空>}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}配置目录${NC}    │ ${CONFIG_DIR}"
    echo -e "${CYAN}│${NC} ${WHITE}日志目录${NC}    │ ${LOG_DIR}"
    echo -e "${CYAN}│${NC} ${WHITE}启动时间${NC}    │ $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${CYAN}└─────────────────────────────────────────────────────────────┘${NC}"
    echo ""
}

# ===========================================
# 启动应用
# ===========================================
do_start() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        log_warn "${APP_NAME} 已在运行 (PID: ${pid})"
        return 1
    fi
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    [ ! -f "${APP_ENV_FILE}" ] && save_app_env
    
    # 设置环境变量
    for env_var in "${ENV_VARS[@]}"; do
        export "$env_var"
    done
    
    # 构建 Spring 启动参数
    local start_opts="--spring.config.additional-location=file:${CONFIG_DIR}/,file:${CONFIG_DIR}/config/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE}"
    start_opts="${start_opts} --spring.output.ansi.enabled=always"
    
    # 指定端口
    [ -n "${SERVER_PORT}" ] && start_opts="${start_opts} --server.port=${SERVER_PORT}"
    
    # 额外配置文件
    [ -n "${EXTRA_CONFIG}" ] && start_opts="${start_opts} --spring.config.additional-location=file:${EXTRA_CONFIG}"
    
    # 日志级别
    [ -n "${LOG_LEVEL}" ] && start_opts="${start_opts} --logging.level.root=${LOG_LEVEL}"
    
    # 显示启动信息
    [ "${DRY_RUN}" != "true" ] && show_startup_banner
    
    # 构建 JVM 参数
    local java_opts="${JAVA_OPTS}"
    
    # 内存设置
    if [ -n "${MEMORY_SIZE}" ]; then
        # 移除默认的内存设置
        java_opts=$(echo "$java_opts" | sed -E 's/-Xms[^ ]+//g; s/-Xmx[^ ]+//g')
        java_opts="${java_opts} -Xms${MEMORY_SIZE} -Xmx${MEMORY_SIZE}"
    fi
    
    # GC 类型选择
    if [ -n "${GC_TYPE}" ]; then
        case "${GC_TYPE}" in
            g1)          java_opts="${java_opts} -XX:+UseG1GC" ;;
            zgc)         java_opts="${java_opts} -XX:+UseZGC" ;;
            shenandoah)  java_opts="${java_opts} -XX:+UseShenandoahGC" ;;
            parallel)    java_opts="${java_opts} -XX:+UseParallelGC" ;;
            serial)      java_opts="${java_opts} -XX:+UseSerialGC" ;;
            *)           log_warn "未知 GC 类型: ${GC_TYPE}" ;;
        esac
    fi
    
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${java_opts} ${CUSTOM_JVM_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    # Arthas 支持
    if [ -n "${ARTHAS_PORT}" ]; then
        java_opts="${java_opts} -Darthas.telnetPort=${ARTHAS_PORT} -Darthas.httpPort=$((ARTHAS_PORT + 1))"
    fi
    
    # Prometheus 支持
    if [ -n "${PROMETHEUS_PORT}" ]; then
        start_opts="${start_opts} --management.server.port=${PROMETHEUS_PORT}"
        start_opts="${start_opts} --management.endpoints.web.exposure.include=prometheus,health,info,metrics"
    fi
    
    # Async-profiler 支持
    if [ "${ASYNC_PROFILER}" = "true" ]; then
        local ap_lib="${APP_HOME}/async-profiler/lib/libasyncProfiler.so"
        if [ -f "${ap_lib}" ]; then
            java_opts="${java_opts} -agentpath:${ap_lib}=start,event=cpu,file=${LOG_DIR}/profile.jfr"
        else
            log_warn "async-profiler 未安装: ${ap_lib}"
        fi
    fi
    
    # Skywalking 支持
    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        local sw_jar=""
        # 优先使用自定义路径
        if [ -n "${SKYWALKING_PATH}" ]; then
            sw_jar="${SKYWALKING_PATH}/skywalking-agent/skywalking-agent.jar"
            if [ ! -f "${sw_jar}" ]; then
                log_error "Skywalking Agent 不存在: ${sw_jar}"
                return 1
            fi
        else
            sw_jar="${APP_HOME}/skywalking/skywalking-agent/skywalking-agent.jar"
            if [ ! -f "${sw_jar}" ]; then
                log_warn "Skywalking Agent 未安装，尝试自动下载..."
                bash "${SUBSCRIPT_DIR}/skywalking.sh" -i
            fi
        fi
        if [ -f "${sw_jar}" ]; then
            java_opts="${java_opts} -javaagent:${sw_jar}"
            java_opts="${java_opts} -Dskywalking.agent.service_name=${APP_NAME}"
        fi
    fi
    
    # 构建 loader.path 参数
    local loader_opts=""
    [ -n "${LOADER_PATH}" ] && loader_opts="-Dloader.path=${LOADER_PATH}"
    
    # 构建完整命令
    local full_cmd="\"$JAVA_CMD\" ${java_opts} ${loader_opts} -jar \"${APP_HOME}/${JAR_NAME}\" ${start_opts}"
    
    # Dry-run 模式
    if [ "${DRY_RUN}" = "true" ]; then
        echo -e "${CYAN}启动命令 (dry-run):${NC}"
        echo ""
        echo "$full_cmd"
        echo ""
        return 0
    fi
    
    # 启动应用
    nohup "$JAVA_CMD" ${java_opts} ${loader_opts} -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${STARTUP_LOG}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║                    ✓ 启动成功                                ║${NC}"
        echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "  ${WHITE}进程 ID:${NC}    ${new_pid}"
        echo -e "  ${WHITE}日志文件:${NC}   ${STARTUP_LOG}"
        [ -n "${SERVER_PORT}" ] && echo -e "  ${WHITE}服务端口:${NC}   ${SERVER_PORT}"
        echo ""
        
        # 等待应用就绪
        if [ "${WAIT_READY}" = "true" ]; then
            echo -e "${CYAN}等待应用就绪...${NC}"
            local health_port="${SERVER_PORT:-8080}"
            local max_wait=120
            local count=0
            while [ $count -lt $max_wait ]; do
                if curl -sf "http://localhost:${health_port}/actuator/health" > /dev/null 2>&1; then
                    echo -e "${GREEN}应用已就绪!${NC}"
                    break
                fi
                sleep 1
                count=$((count + 1))
                echo -ne "\r${YELLOW}等待中... ${count}s${NC}"
            done
            echo ""
            if [ $count -ge $max_wait ]; then
                log_warn "等待超时，应用可能未完全启动"
            fi
        fi
        
        # 实时跟踪日志
        if [ "${TAIL_LOG}" = "true" ]; then
            echo -e "${CYAN}开始实时跟踪日志...${NC}"
            tail -f "${STARTUP_LOG}"
        fi
    else
        echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║                    ✗ 启动失败                                ║${NC}"
        echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "${YELLOW}错误日志:${NC}"
        [ -f "${STARTUP_LOG}" ] && tail -30 "${STARTUP_LOG}"
        return 1
    fi
}

# ===========================================
# 停止应用
# ===========================================
do_stop() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        log_warn "${APP_NAME} 未在运行"
        return 0
    fi
    
    echo -e "${BLUE}正在停止 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local timeout="${GRACEFUL_TIMEOUT:-30}"
    local count=0
    while [ $count -lt $timeout ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        echo -ne "\r${YELLOW}等待进程退出... ${count}s/${timeout}s${NC}"
    done
    echo ""
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    log_info "${APP_NAME} 已停止"
}

# ===========================================
# 重启应用
# ===========================================
do_restart() {
    do_stop
    sleep 2
    do_start
}

# ===========================================
# 查看状态
# ===========================================
do_status() {
    local pid=$(get_pid)
    build_loader_path
    
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[运行中]${NC} ${APP_NAME} (PID: ${pid})"
    else
        echo -e "${YELLOW}[已停止]${NC} ${APP_NAME}"
    fi
    echo -e "  Loader 路径:              ${WHITE}${LOADER_PATH:-<空>}${NC}"
    echo -e "  config.additional-location: ${WHITE}file:${CONFIG_DIR}/,file:${CONFIG_DIR}/config/${NC}"
}

# ===========================================
# 子命令路由
# ===========================================
route_subcommand() {
    local cmd="$1"
    shift
    
    local script="${SUBSCRIPT_DIR}/${cmd}.sh"
    if [ -f "$script" ]; then
        bash "$script" "$@"
    else
        log_error "子命令 '${cmd}' 的脚本不存在: ${script}"
        return 1
    fi
}

# ===========================================
# 自动补全管理
# ===========================================
do_completion() {
    local action="${1:-help}"
    local completion_script="${SCRIPT_DIR}/startup-completion.bash"
    
    case "$action" in
        install)
            if [ ! -f "$completion_script" ]; then
                log_error "补全脚本不存在: ${completion_script}"
                return 1
            fi
            
            # 尝试安装到系统目录
            if [ -d "/etc/bash_completion.d" ] && [ -w "/etc/bash_completion.d" ]; then
                cp "$completion_script" "/etc/bash_completion.d/startup-completion"
                log_info "补全脚本已安装到 /etc/bash_completion.d/"
            elif [ -d "${HOME}/.local/share/bash-completion/completions" ]; then
                mkdir -p "${HOME}/.local/share/bash-completion/completions"
                cp "$completion_script" "${HOME}/.local/share/bash-completion/completions/startup"
                log_info "补全脚本已安装到 ~/.local/share/bash-completion/completions/"
            else
                # 添加到 .bashrc
                local bashrc="${HOME}/.bashrc"
                local source_line="source \"${completion_script}\""
                if ! grep -q "startup-completion.bash" "$bashrc" 2>/dev/null; then
                    echo "" >> "$bashrc"
                    echo "# startup.sh 自动补全" >> "$bashrc"
                    echo "$source_line" >> "$bashrc"
                    log_info "补全脚本已添加到 ~/.bashrc"
                else
                    log_info "补全脚本已存在于 ~/.bashrc"
                fi
            fi
            echo ""
            echo -e "${YELLOW}请执行以下命令使补全生效:${NC}"
            echo "  source ~/.bashrc"
            echo "或重新登录 shell"
            ;;
        uninstall)
            rm -f "/etc/bash_completion.d/startup-completion" 2>/dev/null
            rm -f "${HOME}/.local/share/bash-completion/completions/startup" 2>/dev/null
            # 从 bashrc 移除
            if [ -f "${HOME}/.bashrc" ]; then
                sed -i '/startup-completion.bash/d' "${HOME}/.bashrc"
                sed -i '/# startup.sh 自动补全/d' "${HOME}/.bashrc"
            fi
            log_info "补全脚本已卸载"
            ;;
        help|*)
            echo -e "${BLUE}自动补全管理${NC}"
            echo ""
            echo -e "${YELLOW}用法:${NC} $0 completion <命令>"
            echo ""
            echo -e "${YELLOW}命令:${NC}"
            echo "  install     安装自动补全"
            echo "  uninstall   卸载自动补全"
            echo "  help        显示帮助"
            echo ""
            echo -e "${YELLOW}手动加载:${NC}"
            echo "  source ${completion_script}"
            echo ""
            ;;
    esac
}

# ===========================================
# 参数解析
# ===========================================
COMMAND=""
TAIL_LOG="false"
DEBUG_MODE="false"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
SKYWALKING_ENABLED="false"
SKYWALKING_PATH=""
SPRING_PROFILE="${DEFAULT_SPRING_PROFILE}"
JAVA_OPTS="${JAVA_OPTS:-${DEFAULT_JAVA_OPTS}}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"
CUSTOM_JVM_OPTS=""

# 新增参数
SERVER_PORT=""
MEMORY_SIZE=""
GC_TYPE=""
EXTRA_CONFIG=""
ENV_VARS=()
LOG_LEVEL=""
DRY_RUN="false"
WAIT_READY="false"
GRACEFUL_TIMEOUT="30"
ARTHAS_PORT=""
PROMETHEUS_PORT=""
ASYNC_PROFILER="false"

# 子命令列表
SUBCOMMANDS="info logs java arthas skywalking backup restore upgrade service dump gc top jvm docker completion"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status)
            COMMAND="$1"
            shift
            ;;
        -f)
            TAIL_LOG="true"
            shift
            ;;
        -j)
            CUSTOM_JAVA="$2"
            if [ -x "$CUSTOM_JAVA" ]; then
                save_java_path "$CUSTOM_JAVA"
            fi
            shift 2
            ;;
        --skywalking)
            SKYWALKING_ENABLED="true"
            shift
            ;;
        --skywalking-path)
            SKYWALKING_PATH="$2"
            SKYWALKING_ENABLED="true"
            shift 2
            ;;
        --dev)
            SPRING_PROFILE="dev"
            shift
            ;;
        --prod)
            SPRING_PROFILE="prod"
            shift
            ;;
        --test)
            SPRING_PROFILE="test"
            shift
            ;;
        --profile)
            SPRING_PROFILE="$2"
            shift 2
            ;;
        --debug)
            DEBUG_MODE="true"
            shift
            ;;
        --debug-port)
            DEBUG_PORT="$2"
            shift 2
            ;;
        --jvm)
            CUSTOM_JVM_OPTS="$2"
            shift 2
            ;;
        --port)
            SERVER_PORT="$2"
            shift 2
            ;;
        --memory)
            MEMORY_SIZE="$2"
            shift 2
            ;;
        --gc)
            GC_TYPE="$2"
            shift 2
            ;;
        --config)
            EXTRA_CONFIG="$2"
            shift 2
            ;;
        --env)
            ENV_VARS+=("$2")
            shift 2
            ;;
        --log-level)
            LOG_LEVEL="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN="true"
            shift
            ;;
        --wait-ready)
            WAIT_READY="true"
            shift
            ;;
        --graceful-timeout)
            GRACEFUL_TIMEOUT="$2"
            shift 2
            ;;
        --arthas-port)
            ARTHAS_PORT="$2"
            shift 2
            ;;
        --prometheus-port)
            PROMETHEUS_PORT="$2"
            shift 2
            ;;
        --async-profiler)
            ASYNC_PROFILER="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        completion)
            shift
            do_completion "$@"
            exit $?
            ;;
        *)
            # 检查是否是子命令
            for sub in $SUBCOMMANDS; do
                if [ "$1" = "$sub" ]; then
                    route_subcommand "$@"
                    exit $?
                fi
            done
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
done

# ===========================================
# 命令执行
# ===========================================
case "${COMMAND:-start}" in
    start)   do_start ;;
    stop)    do_stop ;;
    restart) do_restart ;;
    status)  do_status ;;
    *)       show_help; exit 1 ;;
esac
