#!/bin/bash
# ============================================================
# Docker 部署/升级脚本
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

APP_NAME="${APP_NAME:-spring-app}"
APP_VERSION="${APP_VERSION:-latest}"
REGISTRY="${REGISTRY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-$APP_NAME}"
HOST_PORT="${HOST_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

get_image() { [ -n "$REGISTRY" ] && echo "$REGISTRY/$APP_NAME:$APP_VERSION" || echo "$APP_NAME:$APP_VERSION"; }

deploy() {
    local image=$(get_image)
    echo -e "${BLUE}部署: ${image}${NC}"
    docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$" && docker stop "$CONTAINER_NAME" && docker rm "$CONTAINER_NAME"
    [ -n "$REGISTRY" ] && docker pull "$image"
    docker run -d --name "$CONTAINER_NAME" --restart unless-stopped \
        -p "${HOST_PORT}:8080" -e JAVA_OPTS="$JAVA_OPTS" -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILE" \
        -v "${CONTAINER_NAME}-logs:/app/logs" -v "${CONTAINER_NAME}-config:/app/config" "$image" && \
        echo -e "${GREEN}[成功]${NC}" || echo -e "${RED}[失败]${NC}"
}

upgrade() {
    local image=$(get_image)
    local old=$(docker inspect --format='{{.Config.Image}}' "$CONTAINER_NAME" 2>/dev/null)
    [ -n "$old" ] && echo "$old" > "/tmp/${CONTAINER_NAME}_rollback"
    [ -n "$REGISTRY" ] && docker pull "$image"
    docker stop "$CONTAINER_NAME" 2>/dev/null
    docker rename "$CONTAINER_NAME" "${CONTAINER_NAME}_old" 2>/dev/null
    deploy && docker rm "${CONTAINER_NAME}_old" 2>/dev/null || rollback
}

rollback() {
    local rf="/tmp/${CONTAINER_NAME}_rollback"
    [ ! -f "$rf" ] && { echo -e "${RED}无回滚信息${NC}"; return 1; }
    local old=$(cat "$rf")
    docker stop "$CONTAINER_NAME" 2>/dev/null; docker rm "$CONTAINER_NAME" 2>/dev/null
    docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}_old$" && \
        docker rename "${CONTAINER_NAME}_old" "$CONTAINER_NAME" && docker start "$CONTAINER_NAME" || \
        { APP_VERSION=$(echo "$old" | cut -d: -f2); deploy; }
    echo -e "${GREEN}[回滚完成]${NC}"
}

status() { docker ps --filter "name=^${CONTAINER_NAME}$" --format "状态: {{.Status}}\n镜像: {{.Image}}\n端口: {{.Ports}}"; }

CMD="$1"; shift
while [ $# -gt 0 ]; do
    case "$1" in
        --name) APP_NAME="$2"; CONTAINER_NAME="$2"; shift 2 ;;
        --version) APP_VERSION="$2"; shift 2 ;;
        --registry) REGISTRY="$2"; shift 2 ;;
        --port) HOST_PORT="$2"; shift 2 ;;
        *) shift ;;
    esac
done

case "$CMD" in
    deploy) deploy ;; upgrade) upgrade ;; rollback) rollback ;;
    stop) docker stop "$CONTAINER_NAME" ;; restart) docker restart "$CONTAINER_NAME" ;;
    status) status ;; logs) docker logs -f --tail 100 "$CONTAINER_NAME" ;;
    clean) docker image prune -f ;;
    *) echo "用法: $0 [deploy|upgrade|rollback|stop|restart|status|logs|clean]" ;;
esac
