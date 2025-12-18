#!/bin/bash
# ============================================================
# Docker 构建脚本
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"
APP_NAME="${APP_NAME:-spring-app}"
APP_VERSION="${APP_VERSION:-latest}"
REGISTRY="${REGISTRY:-}"
DOCKERFILE="${APP_HOME}/Dockerfile"

generate_dockerfile() {
    [ -f "$DOCKERFILE" ] && return 0
    cat > "$DOCKERFILE" << 'EOF'
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="CH"
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
COPY --chown=appuser:appgroup *.jar app.jar
COPY --chown=appuser:appgroup config/ config/
COPY --chown=appuser:appgroup lib/ lib/
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs
USER appuser
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=/app/config,/app/lib -jar /app/app.jar $SPRING_OPTS"]
EOF
    echo -e "${GREEN}[成功] Dockerfile 已生成${NC}"
}

build_image() {
    local image_name="$APP_NAME"; [ -n "$REGISTRY" ] && image_name="$REGISTRY/$APP_NAME"
    local full_name="${image_name}:${APP_VERSION}"
    
    echo -e "${BLUE}构建镜像: ${full_name}${NC}"
    generate_dockerfile
    
    local build_args=""; [ "$NO_CACHE" = "true" ] && build_args="--no-cache"
    docker build $build_args -t "$full_name" -f "$DOCKERFILE" "$APP_HOME" && \
        echo -e "${GREEN}[成功] 构建完成${NC}" || { echo -e "${RED}[失败]${NC}"; return 1; }
    
    [ "$APP_VERSION" != "latest" ] && docker tag "$full_name" "${image_name}:latest"
}

push_image() {
    [ -z "$REGISTRY" ] && { echo -e "${RED}未指定仓库${NC}"; return 1; }
    docker push "${REGISTRY}/${APP_NAME}:${APP_VERSION}" && echo -e "${GREEN}[成功] 推送完成${NC}"
}

PUSH_IMAGE="false"; NO_CACHE="false"
while [ $# -gt 0 ]; do
    case "$1" in
        --name) APP_NAME="$2"; shift 2 ;;
        --version) APP_VERSION="$2"; shift 2 ;;
        --registry) REGISTRY="$2"; shift 2 ;;
        --push) PUSH_IMAGE="true"; shift ;;
        --no-cache) NO_CACHE="true"; shift ;;
        --help|-h) echo "用法: $0 [--name NAME] [--version VER] [--registry URL] [--push]"; exit 0 ;;
        *) shift ;;
    esac
done

command -v docker &>/dev/null || { echo -e "${RED}未找到 Docker${NC}"; exit 1; }
build_image || exit 1
[ "$PUSH_IMAGE" = "true" ] && push_image
