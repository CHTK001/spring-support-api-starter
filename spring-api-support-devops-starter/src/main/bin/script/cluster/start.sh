#!/bin/bash
# ============================================================
# 集群启动脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# 重新加载 cluster.sh 中的函数
CLUSTER_SCRIPT="${SCRIPT_DIR}/../../cluster.sh"

# ===========================================
# 帮助信息
# ===========================================
# 默认配置
DEFAULT_JAVA_OPTS="-Xms256m -Xmx512m"
DEFAULT_DEBUG_PORT="5005"

show_help() {
    echo -e "${BLUE}集群启动${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh start [选项]"
    echo ""
    echo -e "${YELLOW}集群选项:${NC}"
    echo "  --ports <ports>    指定本地端口 (逗号分隔，如: 8080,8081,8082)"
    echo "  --local            仅启动本地实例"
    echo "  --remote           仅启动远程节点"
    echo "  --node <name>      启动指定节点"
    echo "  --exclude <node>   排除指定节点 (可多次使用)"
    echo "  --parallel         并行启动"
    echo "  --rolling          滚动启动 (逐个启动，健康检查通过后再启动下一个)"
    echo "  --rolling-interval <s>  滚动间隔 (秒，默认: 5)"
    echo "  --timeout <s>      启动超时时间 (秒)"
    echo "  --retry <n>        失败重试次数"
    echo ""
    echo -e "${YELLOW}启动选项:${NC}"
    echo "  -j <java_path>     指定 Java 路径"
    echo "  --memory <size>    设置内存 (如: 2g → -Xms2g -Xmx2g)"
    echo "  --gc <type>        选择 GC 类型 (g1/zgc/shenandoah/parallel/serial)"
    echo "  --jvm <opts>       自定义 JVM 参数 (追加)"
    echo "  --config <path>    指定额外配置文件路径"
    echo "  --env <K=V>        设置环境变量 (可多次使用)"
    echo "  --log-level <level> 设置日志级别 (debug/info/warn/error)"
    echo "  --dev              开发环境 (spring.profiles.active=dev)"
    echo "  --prod             生产环境 (默认)"
    echo "  --test             测试环境"
    echo "  --profile <name>   自定义环境"
    echo "  --debug            调试模式"
    echo "  --debug-port <port> 调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --skywalking       启用 Skywalking Agent (需先执行 'cluster.sh skywalking install')"
    echo "  --skywalking-path <path>  指定 Skywalking 根目录 (优先级高于 --skywalking，无需下载)"
    echo "  --dry-run          只显示启动命令，不执行"
    echo "  --wait-ready       等待应用就绪后返回"
    echo "  --arthas-port <port>   启用 Arthas 并指定基础端口"
    echo "  --prometheus-port <port> 启用 Prometheus metrics 基础端口"
    echo "  --async-profiler   启用 async-profiler"
    echo "  -f                 启动后跟踪日志"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh start --ports 8080,8081         首次启动，指定端口"
    echo "  cluster.sh start --dev                     开发环境启动"
    echo "  cluster.sh start --memory 2g --gc zgc      指定内存和GC"
    echo "  cluster.sh start --parallel --retry 3      并行启动并重试3次"
    echo "  cluster.sh start --exclude 8081            排除端口 8081"
    echo "  cluster.sh start --dry-run                 查看启动命令"
    echo ""
}

# ===========================================
# 解析远程节点
# ===========================================
parse_remote_node() {
    local node_str="$1"
    local field="$2"
    
    IFS='|' read -ra parts <<< "$node_str"
    
    case "$field" in
        alias)    echo "${parts[0]}" ;;
        host)     echo "${parts[1]}" ;;
        port)     echo "${parts[2]:-8080}" ;;
        user)     echo "${parts[3]:-${DEFAULT_SSH_USER}}" ;;
        ssh_port) echo "${parts[4]:-${DEFAULT_SSH_PORT}}" ;;
        ssh_key)  echo "${parts[5]:-${DEFAULT_SSH_KEY}}" ;;
    esac
}

# ===========================================
# 启动本地实例
# ===========================================
# ===========================================
# 检查是否在排除列表中
# ===========================================
is_excluded() {
    local node="$1"
    for excluded in "${EXCLUDE_NODES[@]}"; do
        [ "$node" = "$excluded" ] && return 0
    done
    return 1
}

start_local_instance() {
    local port="$1"
    local instance_name="${APP_NAME}-${port}"
    local pid_file="${APP_HOME}/${instance_name}.pid"
    local startup_log="${LOG_DIR}/startup-${port}.log"
    
    # 检查是否已运行
    if [ -f "$pid_file" ]; then
        local existing_pid=$(cat "$pid_file" 2>/dev/null)
        if [ -n "$existing_pid" ] && ps -p "$existing_pid" > /dev/null 2>&1; then
            echo -e "  ${YELLOW}⊚${NC} ${instance_name} 已在运行 (PID: ${existing_pid})"
            return 0
        fi
    fi
    
    # 设置环境变量
    for env_var in "${ENV_VARS[@]}"; do
        export "$env_var"
    done
    
    # 构建启动参数
    local start_opts="--spring.config.additional-location=file:${CONFIG_DIR}/,file:${CONFIG_DIR}/config/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE:-prod}"
    start_opts="${start_opts} --spring.output.ansi.enabled=always"
    start_opts="${start_opts} --server.port=${port}"
    
    # 额外配置文件
    [ -n "${EXTRA_CONFIG}" ] && start_opts="${start_opts} --spring.config.additional-location=file:${EXTRA_CONFIG}"
    
    # 日志级别
    [ -n "${LOG_LEVEL}" ] && start_opts="${start_opts} --logging.level.root=${LOG_LEVEL}"
    
    # JVM 参数
    local java_opts="${JAVA_OPTS:-${DEFAULT_JAVA_OPTS}}"
    
    # 内存设置
    if [ -n "${MEMORY_SIZE}" ]; then
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
        esac
    fi
    
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${java_opts} ${CUSTOM_JVM_OPTS}"
    
    # 调试模式
    if [ "${DEBUG_MODE}" = "true" ]; then
        local first_port=$(echo "$LOCAL_PORTS" | cut -d',' -f1 | tr -d ' ')
        local debug_port=$((DEBUG_PORT + port - first_port))
        java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${debug_port}"
    fi
    
    # Arthas 支持 (基于端口偏移)
    if [ -n "${ARTHAS_PORT}" ]; then
        local first_port=$(echo "$LOCAL_PORTS" | cut -d',' -f1 | tr -d ' ')
        local arthas_offset=$((port - first_port))
        local arthas_telnet=$((ARTHAS_PORT + arthas_offset * 2))
        local arthas_http=$((arthas_telnet + 1))
        java_opts="${java_opts} -Darthas.telnetPort=${arthas_telnet} -Darthas.httpPort=${arthas_http}"
    fi
    
    # Prometheus 支持 (基于端口偏移)
    if [ -n "${PROMETHEUS_PORT}" ]; then
        local first_port=$(echo "$LOCAL_PORTS" | cut -d',' -f1 | tr -d ' ')
        local prom_offset=$((port - first_port))
        local prom_port=$((PROMETHEUS_PORT + prom_offset))
        start_opts="${start_opts} --management.server.port=${prom_port}"
        start_opts="${start_opts} --management.endpoints.web.exposure.include=prometheus,health,info,metrics"
    fi
    
    # Async-profiler 支持
    if [ "${ASYNC_PROFILER}" = "true" ]; then
        local ap_lib="${APP_HOME}/async-profiler/lib/libasyncProfiler.so"
        if [ -f "${ap_lib}" ]; then
            java_opts="${java_opts} -agentpath:${ap_lib}=start,event=cpu,file=${LOG_DIR}/profile-${port}.jfr"
        fi
    fi
    
    # Skywalking 支持
    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        local sw_jar=""
        if [ -n "${SKYWALKING_PATH}" ]; then
            sw_jar="${SKYWALKING_PATH}/skywalking-agent/skywalking-agent.jar"
            if [ ! -f "${sw_jar}" ]; then
                echo -e "  ${RED}✗${NC} ${instance_name} Skywalking Agent 不存在: ${sw_jar}"
                return 1
            fi
        else
            sw_jar="${APP_HOME}/skywalking/skywalking-agent/skywalking-agent.jar"
        fi
        if [ -f "${sw_jar}" ]; then
            java_opts="${java_opts} -javaagent:${sw_jar}"
            java_opts="${java_opts} -Dskywalking.agent.service_name=${APP_NAME}-${port}"
        fi
    fi
    
    # Loader path
    local loader_opts=""
    [ -n "${LOADER_PATH}" ] && loader_opts="-Dloader.path=${LOADER_PATH}"
    
    # Dry-run 模式
    if [ "${DRY_RUN}" = "true" ]; then
        echo -e "  ${CYAN}[dry-run]${NC} ${instance_name}:"
        echo "    $JAVA_CMD ${java_opts} ${loader_opts} -jar ${APP_HOME}/${JAR_NAME} ${start_opts}"
        return 0
    fi
    
    # 启动
    nohup "$JAVA_CMD" ${java_opts} ${loader_opts} -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${startup_log}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${pid_file}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} ${instance_name} 启动成功 (PID: ${new_pid}, 端口: ${port})"
        
        # 等待就绪
        if [ "${WAIT_READY}" = "true" ]; then
            local count=0
            while [ $count -lt $START_TIMEOUT ]; do
                if curl -sf "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
                    echo -e "    ${GREEN}✓${NC} 应用已就绪"
                    break
                fi
                sleep 1
                count=$((count + 1))
            done
            if [ $count -ge $START_TIMEOUT ]; then
                echo -e "    ${YELLOW}⚠${NC} 等待超时"
            fi
        fi
        return 0
    else
        echo -e "  ${RED}✗${NC} ${instance_name} 启动失败"
        [ -f "${startup_log}" ] && echo -e "    ${YELLOW}日志:${NC} ${startup_log}"
        return 1
    fi
}

# ===========================================
# 带重试的启动
# ===========================================
start_with_retry() {
    local port="$1"
    local attempt=0
    local max_attempts=$((RETRY_COUNT + 1))
    
    while [ $attempt -lt $max_attempts ]; do
        if start_local_instance "$port"; then
            return 0
        fi
        attempt=$((attempt + 1))
        if [ $attempt -lt $max_attempts ]; then
            echo -e "    ${YELLOW}重试 ${attempt}/${RETRY_COUNT}...${NC}"
            sleep 2
        fi
    done
    return 1
}

# ===========================================
# 启动远程节点
# ===========================================
start_remote_node() {
    local node_str="$1"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    # 构建 SSH 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    local remote_script=$(echo "$REMOTE_STARTUP_SCRIPT" | sed "s/\${APP_NAME}/${APP_NAME}/g" | sed "s|\${REMOTE_APP_HOME}|${remote_home}|g")
    
    echo -e "  ${CYAN}→${NC} 启动 ${alias} (${host}:${port})..."
    
    # 执行远程启动
    if ssh $ssh_opts "${user}@${host}" "${remote_script} start" 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} ${alias} 启动成功"
        return 0
    else
        echo -e "  ${RED}✗${NC} ${alias} 启动失败"
        return 1
    fi
}

# ===========================================
# 保存配置
# ===========================================
save_cluster_conf() {
    local conf_file="${APP_HOME}/cluster.conf"
    
    cat > "${conf_file}" << EOF
#!/bin/bash
# 集群配置文件 (自动生成)
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

# 集群名称
CLUSTER_NAME="${CLUSTER_NAME:-${APP_NAME}-cluster}"

# 本地实例端口
LOCAL_PORTS="${LOCAL_PORTS}"

# 远程节点
REMOTE_NODES=(
EOF
    
    if [ -n "${REMOTE_NODES}" ]; then
        for node in "${REMOTE_NODES[@]}"; do
            [ -n "$node" ] && echo "    \"${node}\"" >> "${conf_file}"
        done
    fi
    
    cat >> "${conf_file}" << EOF
)

# SSH 配置
DEFAULT_SSH_USER="${DEFAULT_SSH_USER:-root}"
DEFAULT_SSH_PORT="${DEFAULT_SSH_PORT:-22}"
DEFAULT_SSH_KEY="${DEFAULT_SSH_KEY}"
SSH_TIMEOUT="${SSH_TIMEOUT:-10}"

# 远程路径
REMOTE_APP_HOME="${REMOTE_APP_HOME:-/opt/apps/\${APP_NAME}}"
REMOTE_STARTUP_SCRIPT="${REMOTE_STARTUP_SCRIPT:-\${REMOTE_APP_HOME}/bin/startup.sh}"

# 升级配置
ROLLING_UPGRADE_INTERVAL="${ROLLING_UPGRADE_INTERVAL:-10}"
AUTO_BACKUP_BEFORE_UPGRADE="${AUTO_BACKUP_BEFORE_UPGRADE:-true}"
HEALTH_CHECK_RETRIES="${HEALTH_CHECK_RETRIES:-30}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-2}"

# 健康检查
HEALTH_CHECK_PATH="${HEALTH_CHECK_PATH:-/actuator/health}"
HEALTH_CHECK_PROTOCOL="${HEALTH_CHECK_PROTOCOL:-http}"

# 日志
AGGREGATE_LOG_LINES="${AGGREGATE_LOG_LINES:-50}"
EOF
    
    log_info "配置已保存: ${conf_file}"
}

# ===========================================
# 参数解析
# ===========================================
NEW_PORTS=""
TARGET="all"  # all, local, remote, node
TARGET_NODE=""
FOLLOW_LOG="false"
SPRING_PROFILE="prod"
DEBUG_MODE="false"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
SKYWALKING_ENABLED="false"
SKYWALKING_PATH=""
CUSTOM_JVM_OPTS=""

# 新增参数
MEMORY_SIZE=""
GC_TYPE=""
EXTRA_CONFIG=""
ENV_VARS=()
LOG_LEVEL=""
DRY_RUN="false"
WAIT_READY="false"
ARTHAS_PORT=""
PROMETHEUS_PORT=""
ASYNC_PROFILER="false"
EXCLUDE_NODES=()
PARALLEL="false"
ROLLING="false"
ROLLING_INTERVAL="5"
START_TIMEOUT="60"
RETRY_COUNT="0"

while [ $# -gt 0 ]; do
    case "$1" in
        --ports)
            NEW_PORTS="$2"
            shift 2
            ;;
        --local)
            TARGET="local"
            shift
            ;;
        --remote)
            TARGET="remote"
            shift
            ;;
        --node)
            TARGET="node"
            TARGET_NODE="$2"
            shift 2
            ;;
        -j)
            CUSTOM_JAVA="$2"
            shift 2
            ;;
        --jvm)
            CUSTOM_JVM_OPTS="$2"
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
        --skywalking)
            SKYWALKING_ENABLED="true"
            shift
            ;;
        --skywalking-path)
            SKYWALKING_PATH="$2"
            SKYWALKING_ENABLED="true"
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
        --exclude)
            EXCLUDE_NODES+=("$2")
            shift 2
            ;;
        --parallel)
            PARALLEL="true"
            shift
            ;;
        --rolling)
            ROLLING="true"
            shift
            ;;
        --rolling-interval)
            ROLLING_INTERVAL="$2"
            shift 2
            ;;
        --timeout)
            START_TIMEOUT="$2"
            shift 2
            ;;
        --retry)
            RETRY_COUNT="$2"
            shift 2
            ;;
        -f)
            FOLLOW_LOG="true"
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
# 执行启动
# ===========================================
# 如果指定了新端口，更新配置
if [ -n "$NEW_PORTS" ]; then
    LOCAL_PORTS="$NEW_PORTS"
fi

# 验证配置
if [ -z "$LOCAL_PORTS" ] && [ ${#REMOTE_NODES[@]} -eq 0 ]; then
    log_error "未配置任何集群节点"
    echo ""
    echo -e "${YELLOW}请使用以下方式之一配置集群:${NC}"
    echo "  1. 指定端口: cluster.sh start --ports 8080,8081,8082"
    echo "  2. 编辑配置: vim ${APP_HOME}/cluster.conf"
    exit 1
fi

# 检查 Java
check_java
build_loader_path
mkdir -p "${LOG_DIR}"

# 保存/更新配置
if [ -n "$NEW_PORTS" ]; then
    save_cluster_conf
fi

# 显示启动信息
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}                     集群启动                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}"
echo -e "${CYAN}│${NC} ${WHITE}应用名称${NC}    │ ${GREEN}${APP_NAME}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}本地端口${NC}    │ ${YELLOW}${LOCAL_PORTS:-<无>}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}远程节点${NC}    │ ${YELLOW}${#REMOTE_NODES[@]} 个${NC}"
echo -e "${CYAN}│${NC} ${WHITE}目标${NC}        │ ${MAGENTA}${TARGET}${NC}"
echo -e "${CYAN}└─────────────────────────────────────────────────────────────┘${NC}"
echo ""

success_count=0
fail_count=0

# 启动本地实例
if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
    if [ -n "$LOCAL_PORTS" ]; then
        echo -e "${CYAN}本地实例:${NC}"
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        
        if [ "$PARALLEL" = "true" ]; then
            # 并行启动
            pids=()
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [[ "$port" =~ ^[0-9]+$ ]] && ! is_excluded "$port"; then
                    start_with_retry "$port" &
                    pids+=($!)
                fi
            done
            # 等待所有后台任务
            for pid in "${pids[@]}"; do
                if wait $pid; then
                    success_count=$((success_count + 1))
                else
                    fail_count=$((fail_count + 1))
                fi
            done
        elif [ "$ROLLING" = "true" ]; then
            # 滚动启动: 逐个启动，健康检查通过后再启动下一个
            local total=${#ports[@]}
            local current=0
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                current=$((current + 1))
                if [[ "$port" =~ ^[0-9]+$ ]]; then
                    if is_excluded "$port"; then
                        echo -e "  ${YELLOW}⊚${NC} ${APP_NAME}-${port} 已排除"
                        continue
                    fi
                    
                    echo -e "  ${CYAN}[$current/$total]${NC} 滚动启动 ${APP_NAME}-${port}..."
                    
                    if start_with_retry "$port"; then
                        success_count=$((success_count + 1))
                        
                        # 健康检查
                        echo -e "    ${CYAN}→${NC} 等待健康检查..."
                        local health_count=0
                        while [ $health_count -lt $START_TIMEOUT ]; do
                            if curl -sf "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
                                echo -e "    ${GREEN}✓${NC} 健康检查通过"
                                break
                            fi
                            sleep 1
                            health_count=$((health_count + 1))
                        done
                        
                        if [ $health_count -ge $START_TIMEOUT ]; then
                            echo -e "    ${YELLOW}⚠${NC} 健康检查超时，继续下一个..."
                        fi
                        
                        # 滚动间隔
                        if [ $current -lt $total ]; then
                            echo -e "    ${YELLOW}→${NC} 等待 ${ROLLING_INTERVAL} 秒后启动下一个..."
                            sleep $ROLLING_INTERVAL
                        fi
                    else
                        fail_count=$((fail_count + 1))
                        echo -e "    ${RED}✗${NC} 启动失败，停止滚动启动"
                        break
                    fi
                fi
            done
        else
            # 顺序启动
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                if [[ "$port" =~ ^[0-9]+$ ]]; then
                    if is_excluded "$port"; then
                        echo -e "  ${YELLOW}⊚${NC} ${APP_NAME}-${port} 已排除"
                        continue
                    fi
                    if start_with_retry "$port"; then
                        success_count=$((success_count + 1))
                    else
                        fail_count=$((fail_count + 1))
                    fi
                fi
            done
        fi
        echo ""
    fi
fi

# 启动远程节点
if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        echo -e "${CYAN}远程节点:${NC}"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            if start_remote_node "$node"; then
                success_count=$((success_count + 1))
            else
                fail_count=$((fail_count + 1))
            fi
        done
        echo ""
    fi
fi

# 启动指定节点
if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
    # 检查是否是本地端口
    if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
        if start_local_instance "$TARGET_NODE"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    else
        # 查找远程节点
        found="false"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            alias=$(parse_remote_node "$node" "alias")
            if [ "$alias" = "$TARGET_NODE" ]; then
                if start_remote_node "$node"; then
                    success_count=$((success_count + 1))
                else
                    fail_count=$((fail_count + 1))
                fi
                found="true"
                break
            fi
        done
        if [ "$found" = "false" ]; then
            log_error "未找到节点: $TARGET_NODE"
            fail_count=$((fail_count + 1))
        fi
    fi
fi

# 结果统计
echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}成功:${NC} ${success_count}  ${RED}失败:${NC} ${fail_count}"
echo ""

# 跟踪日志
if [ "$FOLLOW_LOG" = "true" ] && [ -n "$LOCAL_PORTS" ]; then
    IFS=',' read -ra ports <<< "$LOCAL_PORTS"
    local first_port=$(echo "${ports[0]}" | tr -d ' ')
    local log_file="${LOG_DIR}/startup-${first_port}.log"
    if [ -f "$log_file" ]; then
        echo -e "${CYAN}跟踪日志: ${log_file}${NC}"
        tail -f "$log_file"
    fi
fi

[ $fail_count -gt 0 ] && exit 1
exit 0
