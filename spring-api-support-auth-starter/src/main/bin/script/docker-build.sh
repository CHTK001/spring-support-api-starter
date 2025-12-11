#!/bin/bash
# Docker 构建脚本
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"
APP_NAME="${APP_NAME:-spring-app}"; APP_VERSION="${APP_VERSION:-latest}"; REGISTRY="${REGISTRY:-}"
DOCKERFILE="${APP_HOME}/Dockerfile"

generate_dockerfile() {
    [ -f "$DOCKERFILE" ] && return 0
    cat > "$DOCKERFILE" << 'EOF'
FROM eclipse-temurin:21-jre-alpine
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime
WORKDIR /app
COPY *.jar app.jar
COPY config/ config/
COPY lib/ lib/
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=/app/config,/app/lib -jar /app/app.jar"]
EOF
}

build_image() {
    local image="$APP_NAME"; [ -n "$REGISTRY" ] && image="$REGISTRY/$APP_NAME"
    generate_dockerfile
    docker build ${NO_CACHE:+--no-cache} -t "${image}:${APP_VERSION}" -f "$DOCKERFILE" "$APP_HOME" && \
        echo -e "${GREEN}[成功]${NC}" || echo -e "${RED}[失败]${NC}"
}

PUSH="false"; NO_CACHE=""
while [ $# -gt 0 ]; do case "$1" in --name) APP_NAME="$2"; shift 2;; --version) APP_VERSION="$2"; shift 2;; --registry) REGISTRY="$2"; shift 2;; --push) PUSH="true"; shift;; --no-cache) NO_CACHE="1"; shift;; *) shift;; esac; done
command -v docker &>/dev/null || exit 1
build_image
[ "$PUSH" = "true" ] && [ -n "$REGISTRY" ] && docker push "$REGISTRY/$APP_NAME:$APP_VERSION"
