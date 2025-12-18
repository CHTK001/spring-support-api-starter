#!/bin/bash
# ============================================================
# Docker 构建和部署脚本
# 作者: CH
# 创建时间: 2024/12/10
# 版本: 1.0.0
# 描述: 支持 Docker 镜像构建、容器管理、一键部署
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 获取脚本目录和应用目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/../.." && pwd)"

# 应用配置 (可通过环境变量覆盖)
APP_NAME="${APP_NAME:-$(basename "$APP_HOME")}"
APP_VERSION="${APP_VERSION:-4.0.0}"
JAR_NAME="${JAR_NAME:-${APP_NAME}-${APP_VERSION}.jar}"

# 目录配置
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"

# Docker 配置
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"
DOCKERFILE_PATH="${APP_HOME}/Dockerfile"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Docker 构建和部署脚本${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  build       构建 Docker 镜像"
    echo "  deploy      一键构建并部署 (构建 + 停止 + 启动)"
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
    echo "  --help, -h            显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 build                          # 构建镜像"
    echo "  $0 deploy                         # 一键部署"
    echo "  $0 deploy --host-port 9090        # 自定义端口部署"
    echo "  $0 start                          # 启动容器"
    echo "  $0 stop                           # 停止容器"
    echo "  $0 status                         # 查看状态"
    echo "  $0 logs --lines 200               # 查看最近 200 行日志"
    echo "  $0 clean                          # 清理旧镜像"
    echo ""
}

# 检查 Docker 环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[错误] 未找到 Docker 环境${NC}"
        echo "请先安装 Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}[错误] Docker 服务未运行${NC}"
        echo "请启动 Docker 服务"
        exit 1
    fi
    echo -e "${BLUE}Docker: $(docker --version)${NC}"
}

# 生成 Dockerfile
generate_dockerfile() {
    if [ -f "${DOCKERFILE_PATH}" ]; then
        echo -e "${BLUE}使用现有 Dockerfile: ${DOCKERFILE_PATH}${NC}"
        return 0
    fi
    
    echo -e "${BLUE}生成 Dockerfile...${NC}"
    cat > "${DOCKERFILE_PATH}" << 'EOF'
# ============================================================
# Dockerfile - 自动生成
# 作者: CH
# 创建时间: 2024/12/10
# ============================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="CH"
LABEL description="Spring Boot Application Docker Image"

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建必要目录
RUN mkdir -p /app/config /app/lib /app/logs

# 复制应用文件
COPY *.jar /app/app.jar
COPY config/ /app/config/

# 复制 lib 目录 (如果存在)
COPY lib/ /app/lib/

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"
ENV SPRING_OPTS="--spring.config.location=classpath:/,file:/app/config/ --spring.profiles.active=prod"

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=/app/config,/app/lib -jar /app/app.jar $SPRING_OPTS"]
EOF
    echo -e "${GREEN}[成功] Dockerfile 已生成${NC}"
}

# 获取完整镜像名称
get_full_image_name() {
    if [ -n "${DOCKER_REGISTRY}" ]; then
        echo "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
    else
        echo "${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
    fi
}

# 构建 Docker 镜像
do_build() {
    check_docker
    generate_dockerfile
    
    local image_name=$(get_full_image_name)
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  构建 Docker 镜像${NC}"
    echo -e "  镜像名称: ${image_name}"
    echo -e "  构建目录: ${APP_HOME}"
    echo -e "${BLUE}==========================================${NC}"
    
    # 检查 JAR 文件是否存在
    local jar_file=$(ls -1 "${APP_HOME}"/*.jar 2>/dev/null | head -1)
    if [ -z "${jar_file}" ]; then
        echo -e "${RED}[错误] 未找到 JAR 文件${NC}"
        exit 1
    fi
    echo -e "${BLUE}JAR 文件: $(basename "${jar_file}")${NC}"
    
    # 构建镜像
    cd "${APP_HOME}"
    docker build -t "${image_name}" -t "${DOCKER_IMAGE_NAME}:latest" . 
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 镜像构建完成: ${image_name}${NC}"
        docker images | grep "${DOCKER_IMAGE_NAME}"
    else
        echo -e "${RED}[失败] 镜像构建失败${NC}"
        exit 1
    fi
}

# 停止并删除容器
do_stop() {
    check_docker
    
    if docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${BLUE}停止容器: ${DOCKER_CONTAINER_NAME}...${NC}"
        docker stop "${DOCKER_CONTAINER_NAME}" 2>/dev/null
        docker rm "${DOCKER_CONTAINER_NAME}" 2>/dev/null
        echo -e "${GREEN}[成功] 容器已停止并删除${NC}"
    else
        echo -e "${YELLOW}[提示] 容器 ${DOCKER_CONTAINER_NAME} 不存在${NC}"
    fi
}

# 启动容器
do_start() {
    check_docker
    
    local image_name=$(get_full_image_name)
    
    # 检查容器是否已运行
    if docker ps --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[警告] 容器 ${DOCKER_CONTAINER_NAME} 已在运行${NC}"
        return 1
    fi
    
    # 检查镜像是否存在
    if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${image_name}$"; then
        echo -e "${YELLOW}[警告] 镜像不存在，开始构建...${NC}"
        do_build
    fi
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  启动 Docker 容器${NC}"
    echo -e "  容器名称: ${DOCKER_CONTAINER_NAME}"
    echo -e "  端口映射: ${DOCKER_HOST_PORT}:${DOCKER_PORT}"
    echo -e "${BLUE}==========================================${NC}"
    
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

# 重启容器
do_restart() {
    do_stop
    sleep 2
    do_start
}

# 一键部署
do_deploy() {
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  一键部署 Docker 应用${NC}"
    echo -e "${BLUE}==========================================${NC}"
    
    # 步骤1: 构建镜像
    echo -e "\n${YELLOW}[步骤 1/3] 构建镜像...${NC}"
    do_build
    
    # 步骤2: 停止旧容器
    echo -e "\n${YELLOW}[步骤 2/3] 停止旧容器...${NC}"
    do_stop
    
    # 步骤3: 启动新容器
    echo -e "\n${YELLOW}[步骤 3/3] 启动新容器...${NC}"
    do_start
    
    echo -e "\n${GREEN}==========================================${NC}"
    echo -e "${GREEN}  部署完成!${NC}"
    echo -e "${GREEN}  访问地址: http://localhost:${DOCKER_HOST_PORT}${NC}"
    echo -e "${GREEN}==========================================${NC}"
}

# 查看容器状态
do_status() {
    check_docker
    
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${GREEN}  Docker 容器状态${NC}"
    echo -e "${BLUE}==========================================${NC}"
    
    if docker ps --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${GREEN}[运行中]${NC} ${DOCKER_CONTAINER_NAME}"
        echo ""
        docker ps --filter "name=${DOCKER_CONTAINER_NAME}" --format "table {{.ID}}\t{{.Status}}\t{{.Ports}}"
        echo ""
        echo -e "${BLUE}容器资源使用:${NC}"
        docker stats "${DOCKER_CONTAINER_NAME}" --no-stream --format "table {{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
    elif docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[已停止]${NC} ${DOCKER_CONTAINER_NAME}"
        docker ps -a --filter "name=${DOCKER_CONTAINER_NAME}" --format "table {{.ID}}\t{{.Status}}"
    else
        echo -e "${YELLOW}[不存在]${NC} 容器 ${DOCKER_CONTAINER_NAME} 未创建"
    fi
}

# 查看容器日志
do_logs() {
    check_docker
    
    local lines="${LOG_LINES:-100}"
    
    if ! docker ps -a --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}[警告] 容器 ${DOCKER_CONTAINER_NAME} 不存在${NC}"
        return 1
    fi
    
    echo -e "${BLUE}显示最近 ${lines} 行日志...${NC}"
    docker logs --tail "${lines}" -f "${DOCKER_CONTAINER_NAME}"
}

# 清理旧镜像
do_clean() {
    check_docker
    
    echo -e "${BLUE}清理旧镜像...${NC}"
    
    # 保留最新版本，删除其他版本
    local images=$(docker images "${DOCKER_IMAGE_NAME}" --format "{{.ID}} {{.Tag}}" | grep -v "latest" | grep -v "${DOCKER_IMAGE_TAG}" | awk '{print $1}')
    
    if [ -n "${images}" ]; then
        echo "${images}" | xargs docker rmi -f 2>/dev/null
        echo -e "${GREEN}[成功] 旧镜像已清理${NC}"
    else
        echo -e "${YELLOW}[提示] 没有需要清理的旧镜像${NC}"
    fi
    
    # 清理悬空镜像
    docker image prune -f 2>/dev/null
    
    echo -e "\n${BLUE}当前镜像列表:${NC}"
    docker images | grep "${DOCKER_IMAGE_NAME}"
}

# ============================================================
# 主程序
# ============================================================

COMMAND=""
LOG_LINES="100"

# 解析参数
while [ $# -gt 0 ]; do
    case "$1" in
        build|deploy|start|stop|restart|status|logs|clean)
            COMMAND="$1"
            shift
            ;;
        --name)
            APP_NAME="$2"
            DOCKER_IMAGE_NAME="$2"
            DOCKER_CONTAINER_NAME="$2"
            shift 2
            ;;
        --version)
            APP_VERSION="$2"
            DOCKER_IMAGE_TAG="$2"
            shift 2
            ;;
        --port)
            DOCKER_PORT="$2"
            shift 2
            ;;
        --host-port)
            DOCKER_HOST_PORT="$2"
            shift 2
            ;;
        --tag)
            DOCKER_IMAGE_TAG="$2"
            shift 2
            ;;
        --registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --lines)
            LOG_LINES="$2"
            shift 2
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}[错误] 未知参数: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 检查命令
if [ -z "${COMMAND}" ]; then
    show_help
    exit 1
fi

# 执行命令
case "${COMMAND}" in
    build)
        do_build
        ;;
    deploy)
        do_deploy
        ;;
    start)
        do_start
        ;;
    stop)
        do_stop
        ;;
    restart)
        do_restart
        ;;
    status)
        do_status
        ;;
    logs)
        do_logs
        ;;
    clean)
        do_clean
        ;;
    *)
        show_help
        exit 1
        ;;
esac
