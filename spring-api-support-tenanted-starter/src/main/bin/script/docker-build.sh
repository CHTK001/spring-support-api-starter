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

# 默认配置
APP_NAME="${APP_NAME:-spring-app}"
APP_VERSION="${APP_VERSION:-latest}"
REGISTRY="${REGISTRY:-}"
DOCKERFILE="${APP_HOME}/Dockerfile"

show_help() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  Docker 构建工具${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --name <name>         镜像名称 (默认: $APP_NAME)"
    echo "  --version <version>   镜像版本 (默认: $APP_VERSION)"
    echo "  --registry <url>      镜像仓库地址"
    echo "  --push                构建后推送到仓库"
    echo "  --no-cache            不使用缓存构建"
    echo "  --help                显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                                    # 构建镜像"
    echo "  $0 --name myapp --version 1.0.0      # 指定名称和版本"
    echo "  $0 --registry harbor.example.com --push  # 构建并推送"
    echo ""
}

# 生成 Dockerfile
generate_dockerfile() {
    if [ -f "$DOCKERFILE" ]; then
        echo -e "${YELLOW}[提示] 使用已存在的 Dockerfile${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}[生成 Dockerfile...]${NC}"
    
    # 查找 JAR 文件
    local jar_file=$(find "${APP_HOME}" -maxdepth 1 -name "*.jar" -type f 2>/dev/null | head -1)
    local jar_name=$(basename "$jar_file" 2>/dev/null)
    
    cat > "$DOCKERFILE" << 'EOF'
# ============================================================
# Spring Boot 应用 Docker 镜像
# ============================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="CH"
LABEL version="1.0.0"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /app

# 创建非 root 用户
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser

# 复制文件
COPY --chown=appuser:appgroup *.jar app.jar
COPY --chown=appuser:appgroup config/ config/
COPY --chown=appuser:appgroup lib/ lib/

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# 切换用户
USER appuser

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_OPTS="--spring.config.location=classpath:/,file:/app/config/"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=/app/config,/app/lib -jar /app/app.jar $SPRING_OPTS"]
EOF

    echo -e "${GREEN}[成功] Dockerfile 已生成${NC}"
}

# 构建镜像
build_image() {
    local image_name="$APP_NAME"
    [ -n "$REGISTRY" ] && image_name="$REGISTRY/$APP_NAME"
    local full_name="${image_name}:${APP_VERSION}"
    
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${GREEN}  构建 Docker 镜像${NC}"
    echo -e "${BLUE}============================================================${NC}"
    echo -e "  镜像: ${full_name}"
    echo -e "  目录: ${APP_HOME}"
    echo -e "${BLUE}============================================================${NC}"
    
    # 生成 Dockerfile
    generate_dockerfile
    
    # 构建参数
    local build_args=""
    [ "$NO_CACHE" = "true" ] && build_args="$build_args --no-cache"
    
    # 执行构建
    echo -e "\n${YELLOW}[构建中...]${NC}"
    docker build $build_args -t "$full_name" -f "$DOCKERFILE" "$APP_HOME"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 镜像构建完成: $full_name${NC}"
        
        # 同时打 latest 标签
        if [ "$APP_VERSION" != "latest" ]; then
            docker tag "$full_name" "${image_name}:latest"
            echo -e "${GREEN}[成功] 已添加 latest 标签${NC}"
        fi
        
        # 显示镜像信息
        echo -e "\n${YELLOW}[镜像信息]${NC}"
        docker images | grep "$APP_NAME" | head -5
        
        return 0
    else
        echo -e "${RED}[失败] 镜像构建失败${NC}"
        return 1
    fi
}

# 推送镜像
push_image() {
    if [ -z "$REGISTRY" ]; then
        echo -e "${RED}[错误] 未指定镜像仓库，使用 --registry 指定${NC}"
        return 1
    fi
    
    local full_name="${REGISTRY}/${APP_NAME}:${APP_VERSION}"
    
    echo -e "\n${YELLOW}[推送镜像...]${NC}"
    docker push "$full_name"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[成功] 镜像已推送: $full_name${NC}"
        
        # 推送 latest
        if [ "$APP_VERSION" != "latest" ]; then
            docker push "${REGISTRY}/${APP_NAME}:latest"
        fi
        return 0
    else
        echo -e "${RED}[失败] 镜像推送失败${NC}"
        return 1
    fi
}

# 解析参数
PUSH_IMAGE="false"
NO_CACHE="false"

while [ $# -gt 0 ]; do
    case "$1" in
        --name) APP_NAME="$2"; shift 2 ;;
        --version) APP_VERSION="$2"; shift 2 ;;
        --registry) REGISTRY="$2"; shift 2 ;;
        --push) PUSH_IMAGE="true"; shift ;;
        --no-cache) NO_CACHE="true"; shift ;;
        --help|-h) show_help; exit 0 ;;
        *) echo -e "${RED}未知参数: $1${NC}"; show_help; exit 1 ;;
    esac
done

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[错误] 未找到 Docker，请先安装${NC}"
    exit 1
fi

# 执行构建
build_image || exit 1

# 推送
[ "$PUSH_IMAGE" = "true" ] && push_image
