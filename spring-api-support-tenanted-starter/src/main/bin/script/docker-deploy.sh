#!/bin/bash
# ============================================================
# Docker 部署/升级脚本
# 支持自动升级和回滚
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 默认配置
APP_NAME="${APP_NAME:-spring-app}"
APP_VERSION="${APP_VERSION:-latest}"
REGISTRY="${REGISTRY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-$APP_NAME}"
APP_PORT="${APP_PORT:-8080}"
HOST_PORT="${HOST_PORT:-8080}"

# JVM 和 Spring 配置
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Docker 部署工具${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  deploy      部署/启动容器"
    echo "  upgrade     升级到最新版本"
    echo "  rollback    回滚到上一版本"
    echo "  stop        停止容器"
    echo "  restart     重启容器"
    echo "  status      查看状态"
    echo "  logs        查看日志"
    echo "  clean       清理旧镜像"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --name <name>         容器/镜像名称"
    echo "  --version <version>   镜像版本"
    echo "  --registry <url>      镜像仓库"
    echo "  --port <port>         主机端口 (默认: 8080)"
    echo "  --jvm-opts <opts>     JVM参数"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 deploy --name myapp --port 9090"
    echo "  $0 upgrade --registry harbor.example.com"
    echo "  $0 rollback"
    echo ""
}

get_image_name() {
    local name="$APP_NAME"
    [ -n "$REGISTRY" ] && name="$REGISTRY/$APP_NAME"
    echo "${name}:${APP_VERSION}"
}

# 部署容器
deploy() {
    local image=$(get_image_name)
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  部署容器${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo -e "  镜像: ${image}"
    echo -e "  容器: ${CONTAINER_NAME}"
    echo -e "  端口: ${HOST_PORT}:${APP_PORT}"
    echo -e "${BLUE}============================================================${NC}"
    
    # 检查容器是否已存在
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[警告] 容器已存在，先停止并删除${NC}"
        docker stop "$CONTAINER_NAME" 2>/dev/null
        docker rm "$CONTAINER_NAME" 2>/dev/null
    fi
    
    # 拉取镜像
    if [ -n "$REGISTRY" ]; then
        echo -e "\n${YELLOW}[拉取镜像...]${NC}"
        docker pull "$image"
    fi
    
    # 启动容器
    echo -e "\n${YELLOW}[启动容器...]${NC}"
    docker run -d \
        --name "$CONTAINER_NAME" \
        --restart unless-stopped \
        -p "${HOST_PORT}:${APP_PORT}" \
        -e JAVA_OPTS="$JAVA_OPTS" \
        -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILE" \
        -e TZ=Asia/Shanghai \
        -v "${CONTAINER_NAME}-logs:/app/logs" \
        -v "${CONTAINER_NAME}-config:/app/config" \
        "$image"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 容器已启动${NC}"
        sleep 3
        status
    else
        echo -e "${RED}[失败] 容器启动失败${NC}"
        return 1
    fi
}

# 升级
upgrade() {
    local image=$(get_image_name)
    local old_image=$(docker inspect --format='{{.Config.Image}}' "$CONTAINER_NAME" 2>/dev/null)
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  升级容器${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo -e "  当前: ${old_image:-无}"
    echo -e "  目标: ${image}"
    echo -e "${BLUE}============================================================${NC}"
    
    # 保存当前镜像信息用于回滚
    if [ -n "$old_image" ]; then
        echo "$old_image" > "/tmp/${CONTAINER_NAME}_rollback"
        echo -e "${YELLOW}[保存回滚信息...]${NC}"
    fi
    
    # 拉取新镜像
    if [ -n "$REGISTRY" ]; then
        echo -e "\n${YELLOW}[拉取新镜像...]${NC}"
        docker pull "$image" || { echo -e "${RED}[失败] 拉取镜像失败${NC}"; return 1; }
    fi
    
    # 停止旧容器
    echo -e "\n${YELLOW}[停止旧容器...]${NC}"
    docker stop "$CONTAINER_NAME" 2>/dev/null
    
    # 重命名旧容器
    docker rename "$CONTAINER_NAME" "${CONTAINER_NAME}_old" 2>/dev/null
    
    # 部署新容器
    deploy
    
    if [ $? -eq 0 ]; then
        echo -e "\n${YELLOW}[清理旧容器...]${NC}"
        docker rm "${CONTAINER_NAME}_old" 2>/dev/null
        echo -e "${GREEN}[成功] 升级完成${NC}"
    else
        echo -e "${RED}[失败] 升级失败，正在回滚...${NC}"
        rollback
    fi
}

# 回滚
rollback() {
    local rollback_file="/tmp/${CONTAINER_NAME}_rollback"
    
    if [ ! -f "$rollback_file" ]; then
        echo -e "${RED}[错误] 未找到回滚信息${NC}"
        return 1
    fi
    
    local old_image=$(cat "$rollback_file")
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  回滚容器${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo -e "  目标: ${old_image}"
    echo -e "${BLUE}============================================================${NC}"
    
    # 停止当前容器
    docker stop "$CONTAINER_NAME" 2>/dev/null
    docker rm "$CONTAINER_NAME" 2>/dev/null
    
    # 恢复旧容器
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}_old$"; then
        docker rename "${CONTAINER_NAME}_old" "$CONTAINER_NAME"
        docker start "$CONTAINER_NAME"
    else
        # 使用旧镜像重新部署
        APP_VERSION=$(echo "$old_image" | cut -d: -f2)
        deploy
    fi
    
    echo -e "${GREEN}[成功] 回滚完成${NC}"
}

# 停止
stop() {
    echo -e "${YELLOW}[停止容器...]${NC}"
    docker stop "$CONTAINER_NAME"
    echo -e "${GREEN}[成功] 容器已停止${NC}"
}

# 重启
restart() {
    echo -e "${YELLOW}[重启容器...]${NC}"
    docker restart "$CONTAINER_NAME"
    echo -e "${GREEN}[成功] 容器已重启${NC}"
}

# 状态
status() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  容器状态${NC}"
    echo -e "${BLUE}============================================================${NC}"
    
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "  状态: ${GREEN}运行中${NC}"
        docker ps --format "  ID: {{.ID}}\n  镜像: {{.Image}}\n  端口: {{.Ports}}\n  运行时间: {{.Status}}" \
            --filter "name=^${CONTAINER_NAME}$"
    else
        echo -e "  状态: ${RED}已停止${NC}"
    fi
}

# 日志
logs() {
    docker logs -f --tail 100 "$CONTAINER_NAME"
}

# 清理
clean() {
    echo -e "${YELLOW}[清理旧镜像...]${NC}"
    docker image prune -f
    docker images | grep "$APP_NAME" | grep -v "$APP_VERSION" | awk '{print $3}' | xargs -r docker rmi
    echo -e "${GREEN}[完成]${NC}"
}

# 解析参数
COMMAND="$1"
shift

while [ $# -gt 0 ]; do
    case "$1" in
        --name) APP_NAME="$2"; CONTAINER_NAME="$2"; shift 2 ;;
        --version) APP_VERSION="$2"; shift 2 ;;
        --registry) REGISTRY="$2"; shift 2 ;;
        --port) HOST_PORT="$2"; shift 2 ;;
        --jvm-opts) JAVA_OPTS="$2"; shift 2 ;;
        --help|-h) show_help; exit 0 ;;
        *) shift ;;
    esac
done

# 执行命令
case "$COMMAND" in
    deploy) deploy ;;
    upgrade) upgrade ;;
    rollback) rollback ;;
    stop) stop ;;
    restart) restart ;;
    status) status ;;
    logs) logs ;;
    clean) clean ;;
    help|--help|-h|"") show_help ;;
    *) echo -e "${RED}未知命令: $COMMAND${NC}"; show_help; exit 1 ;;
esac
