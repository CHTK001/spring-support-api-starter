#!/bin/bash
# Docker 部署脚本
RED='\033[0;31m'; GREEN='\033[0;32m'; NC='\033[0m'
APP_NAME="${APP_NAME:-spring-app}"; APP_VERSION="${APP_VERSION:-latest}"; REGISTRY="${REGISTRY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-$APP_NAME}"; HOST_PORT="${HOST_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m}"; SPRING_PROFILE="${SPRING_PROFILE:-prod}"

get_image() { [ -n "$REGISTRY" ] && echo "$REGISTRY/$APP_NAME:$APP_VERSION" || echo "$APP_NAME:$APP_VERSION"; }

deploy() {
    local image=$(get_image)
    docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$" && docker stop "$CONTAINER_NAME" && docker rm "$CONTAINER_NAME"
    [ -n "$REGISTRY" ] && docker pull "$image"
    docker run -d --name "$CONTAINER_NAME" --restart unless-stopped -p "${HOST_PORT}:8080" \
        -e JAVA_OPTS="$JAVA_OPTS" -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILE" \
        -v "${CONTAINER_NAME}-logs:/app/logs" "$image" && echo -e "${GREEN}[成功]${NC}"
}

upgrade() {
    local old=$(docker inspect --format='{{.Config.Image}}' "$CONTAINER_NAME" 2>/dev/null)
    [ -n "$old" ] && echo "$old" > "/tmp/${CONTAINER_NAME}_rollback"
    [ -n "$REGISTRY" ] && docker pull "$(get_image)"
    docker stop "$CONTAINER_NAME" 2>/dev/null; docker rm "$CONTAINER_NAME" 2>/dev/null
    deploy
}

rollback() {
    local rf="/tmp/${CONTAINER_NAME}_rollback"; [ ! -f "$rf" ] && return 1
    APP_VERSION=$(cat "$rf" | cut -d: -f2)
    docker stop "$CONTAINER_NAME" 2>/dev/null; docker rm "$CONTAINER_NAME" 2>/dev/null
    deploy
}

CMD="$1"; shift
while [ $# -gt 0 ]; do case "$1" in --name) APP_NAME="$2"; CONTAINER_NAME="$2"; shift 2;; --version) APP_VERSION="$2"; shift 2;; --registry) REGISTRY="$2"; shift 2;; --port) HOST_PORT="$2"; shift 2;; *) shift;; esac; done

case "$CMD" in deploy) deploy;; upgrade) upgrade;; rollback) rollback;; stop) docker stop "$CONTAINER_NAME";; restart) docker restart "$CONTAINER_NAME";; status) docker ps --filter "name=$CONTAINER_NAME";; logs) docker logs -f --tail 100 "$CONTAINER_NAME";; clean) docker image prune -f;; esac
