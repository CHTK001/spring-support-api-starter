#!/bin/bash
# ============================================================
# Docker 构建和部署脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# Docker 配置
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"
DOCKERFILE_PATH="${BASE_DIR}/Dockerfile"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}Docker 构建和部署${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  build       构建 Docker 镜像"
    echo "  deploy      一键构建并部署"
    echo "  start       启动容器"
    echo "  stop        停止并删除容器"
    echo "  restart     重启容器"
    echo "  status      查看容器状态"
    echo "  logs        查看容器日志"
    echo "  clean       清理旧镜像"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --name <name>         应用名称 (默认: ${APP_NAME})"
    echo "  --version <version>   应用版本 (默认: ${APP_VERSION})"
    echo "  --port <port>         容器内部端口 (默认: ${DOCKER_PORT})"
    echo "  --host-port <port>    主机映射端口 (默认: ${DOCKER_HOST_PORT})"
    echo "  --tag <tag>           镜像标签 (默认: ${DOCKER_IMAGE_TAG})"
    echo "  --registry <url>      Docker 仓库地址"
    echo "  --lines <n>           日志显示行数 (默认: 100)"
    echo ""
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[错误] 未找到 Docker 环境${NC}"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}[错误] Docker 服务未运行${NC}"
        exit 1
    fi
}

generate_dockerfile() {
    if [ -f "${DOCKERFILE_PATH}" ]; then
        echo -e "${BLUE}使用现有 Dockerfile${NC}"
        return 0
    fi
    
    echo -e "${BLUE}生成 Dockerfile...${NC}"
    cat > "${DOCKERFILE_PATH}" << 'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevOps"

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /app/config /app/lib /app/logs

COPY *.jar /app/app.jar
COPY config/ /app/config/

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_OPTS="--spring.config.location=classpath:/,file:/app/config/ --spring.profiles.active=prod"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=/app/config,/app/lib -jar /app/app.jar $SPRING_OPTS"]
EOF
    echo -e "${GREEN}[成功] Dockerfile 已生成${NC}"
}

get_full_image_name() {
    if [ -n "${DOCKER_REGISTRY}" ]; then
        echo "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
    else
        echo "${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
    fi
}

do_build() {
    check_docker
    generate_dockerfile
    
    local image_name=$(get_full_image_name)
    
    echo -e "${BLUE}构建镜像: ${image_name}${NC}"
    
    cd "${BASE_DIR}"
    docker build -t "${image_name}" -t "${DOCKER_IMAGE_NAME}:latest" .
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 镜像构建完成${NC}"
    else
        echo -e "${RED}[失败] 镜像构建失败${NC}"
        exit 1
    fi
}

do_stop() {
    check_docker
    
    if docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${BLUE}停止容器: ${DOCKER_CONTAINER_NAME}${NC}"
        docker stop "${DOCKER_CONTAINER_NAME}" 2>/dev/null
        docker rm "${DOCKER_CONTAINER_NAME}" 2>/dev/null
        echo -e "${GREEN}[成功] 容器已停止${NC}"
    else
        echo -e "${YELLOW}[提示] 容器不存在${NC}"
    fi
}

do_start() {
    check_docker
    
    local image_name=$(get_full_image_name)
    
    if docker ps --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[警告] 容器已在运行${NC}"
        return 1
    fi
    
    if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${image_name}$"; then
        echo -e "${YELLOW}[警告] 镜像不存在，开始构建...${NC}"
        do_build
    fi
    
    echo -e "${BLUE}启动容器: ${DOCKER_CONTAINER_NAME}${NC}"
    echo -e "端口映射: ${DOCKER_HOST_PORT}:${DOCKER_PORT}"
    
    mkdir -p "${LOG_DIR}"
    
    docker run -d \
        --name "${DOCKER_CONTAINER_NAME}" \
        -p "${DOCKER_HOST_PORT}:${DOCKER_PORT}" \
        -v "${LOG_DIR}:/app/logs" \
        -e TZ=Asia/Shanghai \
        --restart unless-stopped \
        "${image_name}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 容器已启动${NC}"
        sleep 2
        do_status
    else
        echo -e "${RED}[失败] 容器启动失败${NC}"
        exit 1
    fi
}

do_restart() {
    do_stop
    sleep 2
    do_start
}

do_deploy() {
    echo -e "${BLUE}一键部署 Docker 应用${NC}"
    do_build
    do_stop
    do_start
    echo -e "${GREEN}部署完成! 访问: http://localhost:${DOCKER_HOST_PORT}${NC}"
}

do_status() {
    check_docker
    
    if docker ps --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${GREEN}[运行中]${NC} ${DOCKER_CONTAINER_NAME}"
        docker ps --filter "name=${DOCKER_CONTAINER_NAME}" --format "table {{.ID}}\t{{.Status}}\t{{.Ports}}"
    elif docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[已停止]${NC} ${DOCKER_CONTAINER_NAME}"
    else
        echo -e "${YELLOW}[不存在]${NC} 容器未创建"
    fi
}

do_logs() {
    check_docker
    local lines="${LOG_LINES:-100}"
    
    if ! docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[警告] 容器不存在${NC}"
        return 1
    fi
    
    docker logs --tail "${lines}" -f "${DOCKER_CONTAINER_NAME}"
}

do_clean() {
    check_docker
    echo -e "${BLUE}清理旧镜像...${NC}"
    
    local images=$(docker images "${DOCKER_IMAGE_NAME}" --format "{{.ID}} {{.Tag}}" | grep -v "latest" | grep -v "${DOCKER_IMAGE_TAG}" | awk '{print $1}')
    
    if [ -n "${images}" ]; then
        echo "${images}" | xargs docker rmi -f 2>/dev/null
        echo -e "${GREEN}[成功] 旧镜像已清理${NC}"
    else
        echo -e "${YELLOW}[提示] 没有需要清理的旧镜像${NC}"
    fi
    
    docker image prune -f 2>/dev/null
}

COMMAND=""
LOG_LINES="100"

while [ $# -gt 0 ]; do
    case "$1" in
        build|deploy|start|stop|restart|status|logs|clean) COMMAND="$1"; shift ;;
        --name) APP_NAME="$2"; DOCKER_IMAGE_NAME="$2"; DOCKER_CONTAINER_NAME="$2"; shift 2 ;;
        --version) APP_VERSION="$2"; DOCKER_IMAGE_TAG="$2"; shift 2 ;;
        --port) DOCKER_PORT="$2"; shift 2 ;;
        --host-port) DOCKER_HOST_PORT="$2"; shift 2 ;;
        --tag) DOCKER_IMAGE_TAG="$2"; shift 2 ;;
        --registry) DOCKER_REGISTRY="$2"; shift 2 ;;
        --lines) LOG_LINES="$2"; shift 2 ;;
        --help|-h) show_help; exit 0 ;;
        *) echo -e "${RED}[错误] 未知参数: $1${NC}"; show_help; exit 1 ;;
    esac
done

[ -z "${COMMAND}" ] && show_help && exit 1

case "${COMMAND}" in
    build) do_build ;;
    deploy) do_deploy ;;
    start) do_start ;;
    stop) do_stop ;;
    restart) do_restart ;;
    status) do_status ;;
    logs) do_logs ;;
    clean) do_clean ;;
    *) show_help; exit 1 ;;
esac
