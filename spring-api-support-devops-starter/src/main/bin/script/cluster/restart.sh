#!/bin/bash
# ============================================================
# 集群重启脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群重启${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh restart [选项]"
    echo ""
    echo -e "${YELLOW}集群选项:${NC}"
    echo "  --local            仅重启本地实例"
    echo "  --remote           仅重启远程节点"
    echo "  --node <name>      重启指定节点"
    echo "  --exclude <node>   排除指定节点 (可多次使用)"
    echo "  --rolling          滚动重启 (逐个重启)"
    echo "  --rolling-interval <s>  滚动间隔 (秒，默认: 5)"
    echo "  --parallel         并行重启"
    echo ""
    echo -e "${YELLOW}启动选项 (透传给 start):${NC}"
    echo "  --memory <size>    设置内存 (如: 2g → -Xms2g -Xmx2g)"
    echo "  --gc <type>        选择 GC 类型 (g1/zgc/shenandoah/parallel/serial)"
    echo "  --jvm <opts>       自定义 JVM 参数"
    echo "  --config <path>    指定额外配置文件"
    echo "  --env <K=V>        设置环境变量"
    echo "  --log-level <level> 设置日志级别"
    echo "  --skywalking       启用 Skywalking Agent"
    echo "  --skywalking-path <path>  指定 Skywalking 根目录"
    echo "  --wait-ready       等待应用就绪后返回"
    echo "  --arthas-port <port>   启用 Arthas"
    echo "  --prometheus-port <port> 启用 Prometheus"
    echo ""
    echo -e "${YELLOW}停止选项 (透传给 stop):${NC}"
    echo "  --graceful-timeout <s>  优雅停机超时时间 (秒)"
    echo "  --force            强制停止"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh restart --rolling                  滚动重启"
    echo "  cluster.sh restart --memory 2g --gc zgc       重启并更新JVM参数"
    echo "  cluster.sh restart --exclude 8081             排除某个实例"
    echo "  -h, --help         显示帮助"
    echo ""
}

# ===========================================
# 参数解析
# ===========================================
TARGET="all"
TARGET_NODE=""
ROLLING="false"
PARALLEL="false"
TARGET_ARGS=""
START_ARGS=""
STOP_ARGS=""

while [ $# -gt 0 ]; do
    case "$1" in
        --local)
            TARGET="local"
            TARGET_ARGS="$TARGET_ARGS --local"
            shift
            ;;
        --remote)
            TARGET="remote"
            TARGET_ARGS="$TARGET_ARGS --remote"
            shift
            ;;
        --node)
            TARGET="node"
            TARGET_NODE="$2"
            TARGET_ARGS="$TARGET_ARGS --node $2"
            shift 2
            ;;
        --exclude)
            TARGET_ARGS="$TARGET_ARGS --exclude $2"
            shift 2
            ;;
        --rolling)
            ROLLING="true"
            shift
            ;;
        --parallel)
            PARALLEL="true"
            TARGET_ARGS="$TARGET_ARGS --parallel"
            shift
            ;;
        --rolling-interval)
            START_ARGS="$START_ARGS --rolling-interval $2"
            shift 2
            ;;
        # 启动参数透传
        --memory|--gc|--jvm|--config|--env|--log-level|--skywalking-path|--arthas-port|--prometheus-port|--profile|--timeout|--retry)
            START_ARGS="$START_ARGS $1 $2"
            shift 2
            ;;
        --skywalking|--wait-ready|--async-profiler|--debug)
            START_ARGS="$START_ARGS $1"
            shift
            ;;
        # 停止参数透传
        --graceful-timeout)
            STOP_ARGS="$STOP_ARGS --graceful-timeout $2"
            shift 2
            ;;
        --force)
            STOP_ARGS="$STOP_ARGS --force"
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
# 执行重启
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${MAGENTA}                     集群重启                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

if [ "$ROLLING" = "true" ]; then
    # 滚动重启：逐个停止再启动
    echo -e "${CYAN}滚动重启模式${NC}"
    echo ""
    
    # 获取所有节点
    nodes=()
    
    if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
        if [ -n "$LOCAL_PORTS" ]; then
            IFS=',' read -ra ports <<< "$LOCAL_PORTS"
            for port in "${ports[@]}"; do
                port=$(echo "$port" | tr -d ' ')
                nodes+=("local:$port")
            done
        fi
    fi
    
    if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
        for node in "${REMOTE_NODES[@]}"; do
            [ -n "$node" ] && nodes+=("remote:$node")
        done
    fi
    
    if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
        if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
            nodes=("local:$TARGET_NODE")
        else
            for node in "${REMOTE_NODES[@]}"; do
                [ -z "$node" ] && continue
                IFS='|' read -ra parts <<< "$node"
                if [ "${parts[0]}" = "$TARGET_NODE" ]; then
                    nodes=("remote:$node")
                    break
                fi
            done
        fi
    fi
    
    # 逐个重启
    for item in "${nodes[@]}"; do
        type="${item%%:*}"
        value="${item#*:}"
        
        if [ "$type" = "local" ]; then
            echo -e "${CYAN}重启本地实例: ${APP_NAME}-${value}${NC}"
            bash "${SCRIPT_DIR}/stop.sh" --node "$value"
            sleep 2
            bash "${SCRIPT_DIR}/start.sh" --node "$value"
        else
            IFS='|' read -ra parts <<< "$value"
            alias="${parts[0]}"
            echo -e "${CYAN}重启远程节点: ${alias}${NC}"
            bash "${SCRIPT_DIR}/stop.sh" --node "$alias"
            sleep 2
            bash "${SCRIPT_DIR}/start.sh" --node "$alias"
        fi
        
        echo ""
        sleep "${ROLLING_UPGRADE_INTERVAL:-5}"
    done
else
    # 普通重启：先全部停止，再全部启动
    log_step "停止集群..."
    bash "${SCRIPT_DIR}/stop.sh" $TARGET_ARGS $STOP_ARGS
    
    echo ""
    sleep 2
    
    log_step "启动集群..."
    bash "${SCRIPT_DIR}/start.sh" $TARGET_ARGS $START_ARGS
fi

echo ""
log_info "集群重启完成"
