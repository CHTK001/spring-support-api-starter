#!/bin/bash
# ============================================================
# 统一应用管理脚本
# 支持动态名称替换 (Maven 资源过滤)
# 作者: DevOps
# 版本: 2.0.0
# ============================================================

# ===========================================
# 基础配置 (Maven 资源过滤替换)
# ===========================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="${APP_NAME:-@project.artifactId@}"
APP_VERSION="${APP_VERSION:-@project.version@}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# app.env 默认路径
APP_ENV_FILE="${APP_HOME}/app.env"

# 解决仅替换脚本未经 Maven 过滤时的占位符问题
# 在运行时自动解析实际的 JAR 文件与应用元数据
resolve_app_metadata() {
    local needs_resolve="false"
    if echo "${APP_NAME}${APP_VERSION}${JAR_NAME}" | grep -q "@project."; then
        needs_resolve="true"
    fi
    if [ ! -f "${APP_HOME}/${JAR_NAME}" ]; then
        needs_resolve="true"
    fi

    if [ "${needs_resolve}" = "true" ]; then
        # 优先按时间倒序选择应用目录下的可执行 jar（排除 sources/javadoc）
        local found_jar=$(ls -1t "${APP_HOME}"/*.jar 2>/dev/null | grep -v -E '(sources|javadoc)\.jar$' | head -n 1)
        if [ -n "${found_jar}" ]; then
            JAR_NAME="$(basename "${found_jar}")"
            local base="${JAR_NAME%.jar}"
            # 尝试从文件名解析版本与名称: <artifactId>-<version>.jar
            if echo "${base}" | grep -qE '-[0-9]'; then
                APP_VERSION="${base##*-}"
                APP_NAME="${base%-${APP_VERSION}}"
            fi
        fi
    fi
}

# 读取 app.env（若存在）
load_app_env() {
    local env_file="${APP_ENV_FILE}"
    [ ! -f "${env_file}" ] && env_file="${APP_HOME}/bin/app.env"
    if [ -f "${env_file}" ]; then
        while IFS='=' read -r k v; do
            case "$k" in
                \#*|'' ) continue ;;
                APP_NAME) APP_NAME="$v" ;;
                APP_VERSION) APP_VERSION="$v" ;;
                JAR_NAME) JAR_NAME="$v" ;;
                LOADER_PATH) LOADER_PATH_RAW="$v" ;;
            esac
        done < <(sed -e 's/\r$//' "$env_file")
    fi
}

# 保存 app.env（若不存在时生成一次，便于仅替换脚本的场景）
save_app_env_if_absent() {
    if [ ! -f "${APP_ENV_FILE}" ]; then
        echo "APP_NAME=${APP_NAME}" > "${APP_ENV_FILE}"
        echo "APP_VERSION=${APP_VERSION}" >> "${APP_ENV_FILE}"
        echo "JAR_NAME=${JAR_NAME}" >> "${APP_ENV_FILE}"
        echo "LOADER_PATH=${LOADER_PATH_RAW:-config,lib}" >> "${APP_ENV_FILE}"
    fi
}

# 构建 loader path：仅根据固定的 CONFIG_DIR / LIB_DIR；两者存在哪个就拼哪个
# 仅当目录真实存在时才启用 -Dloader.path（忽略 app.env 的 LOADER_PATH）
build_loader_path() {
    local acc=()
    [ -d "${CONFIG_DIR}" ] && acc+=("${CONFIG_DIR}")
    [ -d "${LIB_DIR}" ] && acc+=("${LIB_DIR}")

    if [ ${#acc[@]} -gt 0 ]; then
        LOADER_PATH=$(IFS=, ; echo "${acc[*]}")
    else
        LOADER_PATH=""
    fi
}

# 目录配置
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"
BACKUP_DIR="${APP_HOME}/backup"

# 日志文件配置
STARTUP_LOG="${LOG_DIR}/startup.log"
APP_LOG="${LOG_DIR}/${APP_NAME}.log"

# 调试端口配置
DEFAULT_DEBUG_PORT=5005

# Docker 配置
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

# Skywalking 配置
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SW_AGENT_NAME="${SW_AGENT_NAME:-${APP_NAME}}"
SW_AGENT_COLLECTOR_BACKEND_SERVICES="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-127.0.0.1:11800}"

# JVM配置
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring配置
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

# 服务端口配置
SERVER_PORT="${SERVER_PORT:-8080}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
NC='\033[0m'

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}              ${APP_NAME} 管理脚本                    ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}║${NC}${CYAN}              版本: ${APP_VERSION}                              ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令] [选项]"
    echo ""
    echo -e "${YELLOW}基础命令:${NC}"
    echo "  start              启动应用 (默认)"
    echo "  stop               停止应用"
    echo "  restart            重启应用"
    echo "  status             查看应用状态"
    echo "  info               显示详细运行信息"
    echo ""
    echo -e "${YELLOW}日志命令:${NC}"
    echo "  logs [N]           查看最近 N 条日志 (默认: 100)"
    echo "  tail               实时跟踪日志输出"
    echo ""
    echo -e "${YELLOW}诊断命令:${NC}"
    echo "  jvm                查看JVM信息"
    echo "  gc                 触发 GC (young/full)"
    echo "  tuning             JVM调优建议"
    echo "  analyze            CPU/内存分析"
    echo "  top-cpu            分析高CPU线程及代码堆栈 (Top N)"
    echo "  top-mem            分析高内存对象 (Top N)"
    echo "  dump               生成转储文件 (heap/thread/all)"
    echo "  arthas             启动Arthas诊断"
    echo ""
    echo -e "${YELLOW}服务管理命令:${NC}"
    echo "  install-service    安装为 systemd 服务"
    echo "  uninstall-service  卸载 systemd 服务"
    echo "  enable-service     启用开机自启"
    echo "  disable-service    禁用开机自启"
    echo "  service-status     查看服务状态"
    echo ""
    echo -e "${YELLOW}运维命令:${NC}"
    echo "  backup             备份配置/JAR"
    echo "  backup list        列出所有备份"
    echo "  restore            从最新备份还原"
    echo "  restore list       列出所有备份"
    echo "  restore <N>        还原到序号N的备份"
    echo "  upgrade            从压缩包升级 (支持 zip/tar.gz)"
    echo "  rollback           回滚到上一个还原点"
    echo "  rollback-status    查看还原点状态"
    echo "  debug              以调试模式启动"
    echo ""
echo -e "${YELLOW}环境管理命令:${NC}"
    echo "  java-install       管理 Java 环境 (install/list/check/remove)"
    echo "  skywalking         管理 Skywalking Agent (install/check/clean/info)"
    echo ""
    echo -e "${YELLOW}Docker 命令:${NC}"
    echo "  docker-build       构建 Docker 镜像"
    echo "  docker-deploy      构建并部署到本机 (一键发布)"
    echo "  publish            docker-deploy 的简写"
    echo "  docker-start       启动 Docker 容器"
    echo "  docker-stop        停止 Docker 容器"
    echo "  docker-restart     重启 Docker 容器"
    echo "  docker-status      查看容器状态"
    echo "  docker-logs        查看容器日志"
    echo "  docker-clean       清理旧镜像"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --java [path]         指定 Java 路径 (无参数时显示当前 Java)"
    echo "  --jvm-opts <opts>     自定义JVM参数"
    echo "  --debug-port <port>   指定调试端口 (默认: ${DEFAULT_DEBUG_PORT})"
    echo "  --docker-port <port>  指定容器内部端口 (默认: 8080)"
    echo "  --host-port <port>    指定主机映射端口 (默认: 8080)"
    echo "  --image-tag <tag>     指定镜像标签 (默认: ${APP_VERSION})"
    echo "  --skywalking          启用 Skywalking APM"
    echo "  --sw-backend <addr>   指定 Skywalking OAP 地址"
    echo "  --sw-name <name>      指定 Skywalking 服务名"
    echo "  --package <path>      升级包路径(zip/tar.gz)"
    echo "  --no-restart          升级后不自动重启"
    echo "  --dev                 使用开发环境(dev)"
    echo "  --prod                使用生产环境 (prod, 默认)"
    echo "  --test                使用测试环境 (test)"
    echo "  --profile <name>      指定自定义环境"
    echo "  --output <path>       dump 输出目录"
    echo "  --dump-path <path>    dump 输出目录 (--output 别名)"
    echo "  --logs <N>            启动后显示最近 N 条日志"
    echo "  --tail                启动后实时跟踪日志"
    echo "  --help, -h            显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo ""
    echo -e "  ${CYAN}基础命令:${NC}"
    echo "    $0 start                             启动应用"
    echo "    $0 start --dev                       开发环境启动"
    echo "    $0 start --prod                      生产环境启动"
    echo "    $0 start --test                      测试环境启动"
    echo "    $0 start --profile staging           自定义环境启动"
    echo "    $0 start --tail                      启动并实时跟踪日志"
    echo "    $0 start --logs 200                  启动后显示最近 200 条日志"
    echo "    $0 start --java /opt/jdk17/bin/java  指定 Java 路径启动 (并保存)"
    echo "    $0 --java                            显示当前使用的 Java 版本"
    echo "    $0 stop                              停止应用"
    echo "    $0 restart                           重启应用"
    echo "    $0 status                            查看状态"
    echo "    $0 info                              查看详细信息"
    echo ""
    echo -e "  ${CYAN}日志命令:${NC}"
    echo "    $0 logs                              查看最近 100 条日志"
    echo "    $0 logs 50                           查看最近 50 条日志"
    echo "    $0 tail                              实时跟踪日志"
    echo ""
    echo -e "  ${CYAN}调试命令:${NC}"
    echo "    $0 debug                             调试模式启动 (端口 5005)"
    echo "    $0 debug --debug-port 5006           调试模式启动 (指定端口)"
    echo ""
    echo -e "  ${CYAN}JVM 诊断:${NC}"
    echo "    $0 jvm                               查看 JVM 信息"
    echo "    $0 gc                                触发 Full GC"
    echo "    $0 gc young                          触发 Young GC"
    echo "    $0 top-cpu                           高 CPU 线程分析 (Top 10)"
    echo "    $0 top-cpu 20                        高 CPU 线程分析 (Top 20)"
    echo "    $0 top-mem                           高内存对象分析 (Top 10)"
    echo "    $0 top-mem 20                        高内存对象分析 (Top 20)"
    echo "    $0 tuning                            JVM 调优建议"
    echo "    $0 arthas                            启动 Arthas 诊断"
    echo ""
    echo -e "  ${CYAN}Dump 转储:${NC}"
    echo "    $0 dump                              生成堆+线程转储"
    echo "    $0 dump heap                         仅堆转储"
    echo "    $0 dump thread                       仅线程转储"
    echo "    $0 dump --output /tmp/dump           指定输出目录"
    echo ""
    echo -e "  ${CYAN}服务管理 (需 root 权限):${NC}"
    echo "    $0 install-service                   安装为 systemd 服务"
    echo "    $0 uninstall-service                 卸载服务"
    echo "    $0 enable-service                    启用开机自启"
    echo "    $0 disable-service                   禁用开机自启"
    echo "    $0 service-status                    查看服务状态"
    echo ""
    echo -e "  ${CYAN}升级回滚:${NC}"
    echo "    $0 upgrade --package /tmp/app.zip    从压缩包升级"
    echo "    $0 upgrade --package /tmp/app.tar.gz --no-restart  升级但不重启"
    echo "    $0 rollback                          回滚到上一个版本"
    echo "    $0 rollback-status                   查看还原点状态"
    echo "    $0 backup                            创建新备份"
    echo "    $0 backup list                       列出所有备份"
    echo "    $0 restore                           从最新备份还原"
    echo "    $0 restore list                      列出所有备份"
    echo "    $0 restore 1                         还原到备份 #1"
    echo ""
    echo -e "  ${CYAN}Docker 部署:${NC}"
    echo "    $0 docker-build                      构建 Docker 镜像"
    echo "    $0 docker-deploy                     构建并部署 (一键发布)"
    echo "    $0 publish                           docker-deploy 的简写"
    echo "    $0 docker-start                      启动容器"
    echo "    $0 docker-stop                       停止容器"
    echo "    $0 docker-restart                    重启容器"
    echo "    $0 docker-status                     查看容器状态"
    echo "    $0 docker-logs                       查看容器日志"
    echo "    $0 docker-clean                      清理旧镜像"
    echo ""
echo -e "  ${CYAN}Java 环境管理:${NC}"
    echo "    $0 java-install                      安装 Java (默认 21)"
    echo "    $0 java-install install --version 17 安装指定版本 Java"
    echo "    $0 java-install list                 列出可用 Java 版本"
    echo "    $0 java-install check                检查当前 Java 环境"
    echo ""
    echo -e "  ${CYAN}Skywalking APM:${NC}"
    echo "    $0 start --skywalking                启用 Skywalking 启动"
    echo "    $0 start --skywalking --sw-backend 192.168.1.100:11800  指定 OAP 地址"
    echo "    $0 skywalking install                安装 Skywalking Agent"
    echo "    $0 skywalking check                  检查 Agent 状态"
    echo ""
}

# ===========================================
# Java 检测
# ===========================================

# Java 配置文件路径
JAVA_CONFIG_FILE="${APP_HOME}/.java_path"

# 保存 Java 路径
save_java_path() {
    local java_path="$1"
    if [ -n "$java_path" ]; then
        echo "$java_path" > "${JAVA_CONFIG_FILE}"
        echo -e "${GREEN}[成功] Java 路径已保存: ${java_path}${NC}"
    fi
}

# 加载 Java 路径
load_java_path() {
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        if [ -n "$saved_path" ] && [ -x "$saved_path" ]; then
            CUSTOM_JAVA="$saved_path"
        fi
    fi
}

check_java() {
    [ -z "$CUSTOM_JAVA" ] && load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -x "$CUSTOM_JAVA" ]; then
            JAVA_CMD="$CUSTOM_JAVA"
        else
            echo -e "${RED}[错误] 指定的 Java 路径不存在或不可执行: ${CUSTOM_JAVA}${NC}"
            exit 1
        fi
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java &> /dev/null; then
        JAVA_CMD="java"
    elif [ -x "/usr/bin/java" ]; then
        JAVA_CMD="/usr/bin/java"
    elif [ -x "/usr/local/bin/java" ]; then
        JAVA_CMD="/usr/local/bin/java"
    else
        echo -e "${RED}[错误] 未找到Java环境${NC}"
        echo "请设置 JAVA_HOME 或使用 --java 指定 Java 路径"
        exit 1
    fi
}

get_java_version() {
    "$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}'
}

show_java_info() {
    check_java
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}              当前 Java 环境信息${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    local source="系统 PATH"
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -f "${JAVA_CONFIG_FILE}" ]; then
            source="配置文件 (.java_path)"
        else
            source="命令行 --java 参数"
        fi
    elif [ -n "$JAVA_HOME" ]; then
        source="JAVA_HOME 环境变量"
    fi
    
    echo -e "  ${CYAN}Java 路径:${NC}    ${WHITE}${JAVA_CMD}${NC}"
    echo -e "  ${CYAN}路径来源:${NC}    ${WHITE}${source}${NC}"
    echo ""
    echo -e "  ${CYAN}Java 版本:${NC}"
    "$JAVA_CMD" -version 2>&1 | while read -r line; do
        echo -e "    ${WHITE}${line}${NC}"
    done
    echo ""
    
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        echo -e "  ${CYAN}已保存的 Java:${NC} ${WHITE}${saved_path}${NC}"
    else
        echo -e "  ${YELLOW}提示: 使用 --java <path> 指定并保存 Java 路径${NC}"
    fi
    echo ""
}

# 获取本机 IP 地址
get_local_ip() {
    local ip=""
    # 尝试多种方式获取 IP
    if command -v ip &> /dev/null; then
        ip=$(ip route get 1 2>/dev/null | awk '{print $7;exit}')
    fi
    if [ -z "$ip" ] && command -v hostname &> /dev/null; then
        ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    if [ -z "$ip" ] && command -v ifconfig &> /dev/null; then
        ip=$(ifconfig 2>/dev/null | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1)
    fi
    echo "${ip:-127.0.0.1}"
}

# 从配置文件读取端口
get_server_port() {
    local port="${SERVER_PORT}"
    # 尝试从 application.yml 读取
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        local yml_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$yml_port" ] && port="$yml_port"
    fi
    # 尝试从 application-${profile}.yml 读取
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_port" ] && port="$profile_port"
    fi
    echo "${port:-8080}"
}

# 获取 context-path
get_context_path() {
    local ctx_path=""
    # 从配置文件读取 context-path
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        ctx_path=$(grep -E 'context-path:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
    fi
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_ctx=$(grep -E 'context-path:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_ctx" ] && ctx_path="$profile_ctx"
    fi
    echo "${ctx_path}"
}

# 检测是否启用 Swagger
check_swagger_enabled() {
    local swagger_enabled="false"
    # 检查配置文件中是否有 swagger/springdoc/knife4j 配置
    if [ -d "${CONFIG_DIR}" ]; then
        if grep -rq -E '(springdoc|swagger|knife4j)' "${CONFIG_DIR}/" 2>/dev/null; then
            swagger_enabled="true"
        fi
    fi
    # 检查 JAR 中是否包含 swagger 相关依赖
    if [ -f "${APP_HOME}/${JAR_NAME}" ]; then
        if unzip -l "${APP_HOME}/${JAR_NAME}" 2>/dev/null | grep -qE '(springdoc|swagger|knife4j)'; then
            swagger_enabled="true"
        fi
    fi
    echo "$swagger_enabled"
}

# 构建 loader path（根据 app.env 或默认 config,lib，并且仅拼接存在的目录）
# 注意：实际逻辑已在上方的 build_loader_path() 中实现，这里保留占位注释。

# ===========================================
# 进程管理
# ===========================================
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

# ===========================================
# 状态检查
# ===========================================
check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[运行中]${NC} ${APP_NAME} (PID: ${pid})"
        return 0
    else
        echo -e "${YELLOW}[已停止]${NC} ${APP_NAME}"
        return 1
    fi
}

# ===========================================
# 详细信息显示
# ===========================================
show_info() {
    local pid=$(get_pid)
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                    应用运行信息                              ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    echo -e "${CYAN}【基础信息】${NC}"
    echo -e "  应用名称:     ${WHITE}${APP_NAME}${NC}"
    echo -e "  应用版本:     ${WHITE}${APP_VERSION}${NC}"
    echo -e "  JAR 文件:     ${WHITE}${JAR_NAME}${NC}"
    echo -e "  应用目录:     ${WHITE}${APP_HOME}${NC}"
    echo -e "  配置目录:     ${WHITE}${CONFIG_DIR}${NC}"
    echo -e "  日志目录:     ${WHITE}${LOG_DIR}${NC}"
    echo ""
    
    if [ -n "$pid" ]; then
        echo -e "${CYAN}【运行状态】${NC}"
        echo -e "  状态:         ${GREEN}运行中${NC}"
        echo -e "  进程 ID:      ${WHITE}${pid}${NC}"
        
        # 获取启动时间
        local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
        [ -n "$start_time" ] && echo -e "  启动时间:     ${WHITE}${start_time}${NC}"
        
        # 获取运行时长
        local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
        [ -n "$etime" ] && echo -e "  运行时长:     ${WHITE}${etime}${NC}"
        
        # 获取内存使用
        local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
        if [ -n "$mem" ]; then
            local mem_mb=$((mem / 1024))
            echo -e "  内存使用:     ${WHITE}${mem_mb} MB${NC}"
        fi
        
        # 获取 CPU 使用率
        local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
        [ -n "$cpu" ] && echo -e "  CPU 使用:     ${WHITE}${cpu}%${NC}"
        
        echo ""
        echo -e "${CYAN}【JVM 信息】${NC}"
        check_java
        echo -e "  Java 路径:    ${WHITE}${JAVA_CMD}${NC}"
        echo -e "  Java 版本:    ${WHITE}$(get_java_version)${NC}"
        
        # 尝试获取 JVM 参数
        local jvm_opts=$(ps -o args= -p "$pid" 2>/dev/null | grep -oE '\-X[^ ]+' | head -5 | tr '\n' ' ')
        [ -n "$jvm_opts" ] && echo -e "  JVM 参数:     ${WHITE}${jvm_opts}${NC}"
    else
        echo -e "${CYAN}【运行状态】${NC}"
        echo -e "  状态:         ${YELLOW}已停止${NC}"
    fi
    
    echo ""
    echo -e "${CYAN}【日志文件】${NC}"
    if [ -f "${STARTUP_LOG}" ]; then
        local log_size=$(du -h "${STARTUP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  启动日志:     ${WHITE}${STARTUP_LOG} (${log_size})${NC}"
    fi
    if [ -f "${APP_LOG}" ]; then
        local log_size=$(du -h "${APP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  应用日志:     ${WHITE}${APP_LOG} (${log_size})${NC}"
    fi
    echo ""
}

# ===========================================
# 启动信息显示
# ===========================================
show_startup_banner() {
    local java_version=$(get_java_version)
    local local_ip=$(get_local_ip)
    local server_port=$(get_server_port)
    local context_path=$(get_context_path)
    local swagger_enabled=$(check_swagger_enabled)
    local base_url="http://${local_ip}:${server_port}${context_path}"
    local local_url="http://localhost:${server_port}${context_path}"
    
    echo ""
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                     应用启动中...                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}应用名称${NC}    │ ${GREEN}${APP_NAME}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}应用版本${NC}    │ ${GREEN}${APP_VERSION}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}运行环境${NC}    │ ${YELLOW}${SPRING_PROFILE}${NC}"
    # 不再输出推断的网址，避免端口误判导致的脏输出
    echo -e "${CYAN}│${NC} ${WHITE}Java 版本${NC}   │ ${MAGENTA}${java_version}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}Java 路径${NC}   │ ${MAGENTA}${JAVA_CMD}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}JVM 参数${NC}    │ ${MAGENTA}${JAVA_OPTS}${NC}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "${CYAN}│${NC} ${WHITE}调试端口${NC}    │ ${RED}${DEBUG_PORT}${NC}"
    [ "${SKYWALKING_ENABLED}" = "true" ] && echo -e "${CYAN}│${NC} ${WHITE}Skywalking${NC}  │ ${GREEN}已启用 (${SW_AGENT_NAME})${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}Loader 路径${NC} │ ${MAGENTA}${LOADER_PATH:-<空>}${NC}"
    echo -e "${CYAN}│${NC} ${WHITE}配置目录${NC}    │ ${CONFIG_DIR}"
    echo -e "${CYAN}│${NC} ${WHITE}日志目录${NC}    │ ${LOG_DIR}"
    echo -e "${CYAN}│${NC} ${WHITE}启动时间${NC}    │ $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${CYAN}└─────────────────────────────────────────────────────────────┘${NC}"
    echo ""
}

# ===========================================
# 日志管理
# ===========================================

# 查看最近 N 条日志
show_logs() {
    local lines="${1:-100}"
    local log_file="${STARTUP_LOG}"
    
    # 优先使用应用日志，如果存在的话
    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[警告] 日志文件不存在: ${log_file}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  最近 ${lines} 条日志 - ${log_file}${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    tail -n "${lines}" "${log_file}"
}

# 实时跟踪日志
tail_logs() {
    local log_file="${STARTUP_LOG}"
    
    # 优先使用应用日志
    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[警告] 日志文件不存在: ${log_file}${NC}"
        echo -e "${YELLOW}等待日志文件生成...${NC}"
        # 等待日志文件生成
        local count=0
        while [ ! -f "${log_file}" ] && [ $count -lt 30 ]; do
            sleep 1
            count=$((count + 1))
            [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
        done
        if [ ! -f "${log_file}" ]; then
            echo -e "${RED}[错误] 等待超时，日志文件仍不存在${NC}"
            return 1
        fi
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}  实时日志跟踪 - ${log_file}${NC}"
    echo -e "${YELLOW}  按 Ctrl+C 退出${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    tail -f "${log_file}"
}

# ===========================================
# Skywalking 支持
# ===========================================
ensure_skywalking() {
    if [ ! -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[Skywalking] Agent 未安装，开始自动下载...${NC}"
        bash "${SCRIPT_DIR}/script/linux/skywalking.sh" install
        if [ $? -ne 0 ]; then
            echo -e "${RED}[错误] Skywalking Agent 下载失败${NC}"
            return 1
        fi
    fi
    return 0
}

run_skywalking() {
    bash "${SCRIPT_DIR}/script/linux/skywalking.sh" "$@"
}

# ===========================================
# Java 环境管理
# ===========================================
run_java_install() {
    local script_path="${SCRIPT_DIR}/script/linux/java-install.sh"
    if [ -f "$script_path" ]; then
        bash "$script_path" "$@"
    else
        echo -e "${RED}[错误] Java 安装脚本不存在: ${script_path}${NC}"
        return 1
    fi
}

# ===========================================
# 应用启动
# ===========================================
start_app() {
    local pid=$(get_pid)
    [ -n "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 已在运行 (PID: ${pid})${NC}" && return 1
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    save_app_env_if_absent
    
    # 构建 Spring 启动参数
    local start_opts="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE}"
    
    # 显示启动信息
    show_startup_banner
    
    local java_opts="${JAVA_OPTS}"
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    # Skywalking 支持
    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        ensure_skywalking || return 1
        java_opts="${java_opts} -javaagent:${SKYWALKING_AGENT_JAR}"
        java_opts="${java_opts} -Dskywalking.agent.service_name=${SW_AGENT_NAME}"
        java_opts="${java_opts} -Dskywalking.collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES}"
    fi
    
# 构建 loader.path 参数（仅在存在有效目录时传递）
local loader_opts=""
[ -n "${LOADER_PATH}" ] && loader_opts="-Dloader.path=${LOADER_PATH}"
    
    # 启动应用
    nohup "$JAVA_CMD" ${java_opts} ${loader_opts} -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${STARTUP_LOG}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║                    ✓ 启动成功                                ║${NC}"
        echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "  ${WHITE}进程 ID:${NC}    ${new_pid}"
        echo -e "  ${WHITE}日志文件:${NC}   ${STARTUP_LOG}"
        echo ""
        
        # 根据参数显示日志
        if [ "${TAIL_LOG}" = "true" ]; then
            echo -e "${CYAN}开始实时跟踪日志...${NC}"
            tail_logs
        elif [ -n "${SHOW_LOGS}" ] && [ "${SHOW_LOGS}" -gt 0 ]; then
            echo -e "${CYAN}显示最近 ${SHOW_LOGS} 条日志...${NC}"
            sleep 3  # 等待一些日志输出
            show_logs "${SHOW_LOGS}"
        fi
    else
        echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║                    ✗ 启动失败                                ║${NC}"
        echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "${YELLOW}错误日志:${NC}"
        [ -f "${STARTUP_LOG}" ] && tail -30 "${STARTUP_LOG}"
    fi
}

# ===========================================
# 应用停止
# ===========================================
stop_app() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 0
    
    echo -e "${BLUE}正在停止 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        echo -ne "\r${YELLOW}等待进程退出... ${count}s${NC}"
    done
    echo ""
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[成功] ${APP_NAME} 已停止${NC}"
}

# 重启
restart_app() { 
    stop_app
    sleep 2
    start_app
}

# ===========================================
# JVM 诊断
# ===========================================
show_jvm_info() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-info.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-info.sh" "$pid" "$APP_NAME"
    else
        # 内置简单的 JVM 信息显示
        echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${BLUE}║${NC}${GREEN}                    JVM 运行信息                              ${NC}${BLUE}║${NC}"
        echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "${CYAN}进程信息:${NC}"
        echo -e "  PID: ${pid}"
        
        if command -v jps &> /dev/null; then
            echo ""
            echo -e "${CYAN}JVM 进程:${NC}"
            jps -lvm | grep "$pid" || echo "  无法获取详细信息"
        fi
        
        if command -v jstat &> /dev/null; then
            echo ""
            echo -e "${CYAN}GC 统计:${NC}"
            jstat -gcutil "$pid" 2>/dev/null || echo "  无法获取 GC 信息"
        fi
    fi
}

show_tuning() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" "$pid" "$APP_NAME"
    else
        echo -e "${YELLOW}[提示] JVM 调优脚本不存在${NC}"
    fi
}

start_arthas() {
    local pid=$(get_pid)
    if [ -f "${SCRIPT_DIR}/script/linux/arthas-install.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/arthas-install.sh" "$pid"
    else
        echo -e "${YELLOW}[提示] Arthas 脚本不存在，尝试直接启动...${NC}"
        if [ -f "${APP_HOME}/arthas/as.sh" ]; then
            bash "${APP_HOME}/arthas/as.sh" "$pid"
        else
            echo -e "${RED}[错误] Arthas 未安装${NC}"
        fi
    fi
}

run_analyze() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[警告] ${APP_NAME} 未在运行${NC}" && return 1
    local mode="${1:-cpu}"
    
    if [ -f "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" "$pid" "$mode" "$APP_NAME"
    else
        echo -e "${YELLOW}[提示] 分析脚本不存在${NC}"
    fi
}

# ===========================================
# Dump 转储功能
# ===========================================
run_dump() {
    local mode="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[错误] ${APP_NAME} 未在运行${NC}"
        exit 1
    fi
    
    mkdir -p "${output_dir}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                    生成 Dump 转储                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  PID:        ${pid}"
    echo -e "  模式:       ${mode}"
    echo -e "  输出目录:   ${output_dir}"
    echo ""
    
    local jmap_cmd="jmap"
    local jstack_cmd="jstack"
    if [ -n "$JAVA_HOME" ]; then
        jmap_cmd="$JAVA_HOME/bin/jmap"
        jstack_cmd="$JAVA_HOME/bin/jstack"
    fi
    
    case "${mode}" in
        heap)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            ;;
        thread)
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        all)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        *)
            echo -e "${RED}[错误] 未知的 dump 模式: ${mode}${NC}"
            echo "支持的模式: heap, thread, all"
            exit 1
            ;;
    esac
    
    echo -e "\n${GREEN}[完成] Dump 文件已保存到: ${output_dir}${NC}"
}

dump_heap() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jmap_cmd="$4"
    local heap_file="${output_dir}/heapdump_${timestamp}.hprof"
    
    echo -e "${BLUE}[堆转储] 生成中...${NC}"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jmap 命令${NC}"
        return 1
    fi
    
    "${jmap_cmd}" -dump:format=b,file="${heap_file}" "${pid}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${heap_file}" ]; then
        local size=$(du -h "${heap_file}" | cut -f1)
        echo -e "${GREEN}  ✓ 堆转储: ${heap_file} (${size})${NC}"
    else
        echo -e "${RED}  ✗ 堆转储失败${NC}"
    fi
}

dump_thread() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jstack_cmd="$4"
    local thread_file="${output_dir}/threaddump_${timestamp}.txt"
    
    echo -e "${BLUE}[线程转储] 生成中...${NC}"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jstack 命令${NC}"
        return 1
    fi
    
    "${jstack_cmd}" -l "${pid}" > "${thread_file}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${thread_file}" ]; then
        local lines=$(wc -l < "${thread_file}")
        echo -e "${GREEN}  ✓ 线程转储: ${thread_file} (${lines} 行)${NC}"
        
        echo -e "\n${CYAN}[线程状态统计]${NC}"
        echo -e "  RUNNABLE:      $(grep -c 'java.lang.Thread.State: RUNNABLE' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  WAITING:       $(grep -c 'java.lang.Thread.State: WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  TIMED_WAITING: $(grep -c 'java.lang.Thread.State: TIMED_WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  BLOCKED:       $(grep -c 'java.lang.Thread.State: BLOCKED' "${thread_file}" 2>/dev/null || echo 0)"
        
        if grep -q "Found.*deadlock" "${thread_file}" 2>/dev/null; then
            echo -e "\n${RED}[警告] 检测到死锁!${NC}"
        fi
    else
        echo -e "${RED}  ✗ 线程转储失败${NC}"
    fi
}

# ===========================================
# 备份还原
# ===========================================

# 列出所有备份
list_backups() {
    if [ ! -d "${BACKUP_DIR}" ]; then
        echo -e "${YELLOW}[警告] 备份目录不存在: ${BACKUP_DIR}${NC}"
        return 1
    fi
    
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | grep -v "^$"))
    
    if [ ${#backups[@]} -eq 0 ]; then
        echo -e "${YELLOW}[提示] 没有找到备份${NC}"
        return 1
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}                    备份列表${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    printf "  ${WHITE}%-6s %-24s %-10s %s${NC}\n" "序号" "时间" "大小" "路径"
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    
    local index=1
    for backup in "${backups[@]}"; do
        local backup_name=$(basename "$backup")
        local backup_size=$(du -sh "$backup" 2>/dev/null | cut -f1)
        local backup_time=""
        
        if [[ "$backup_name" =~ ^([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})([0-9]{2})$ ]]; then
            backup_time="${BASH_REMATCH[1]}-${BASH_REMATCH[2]}-${BASH_REMATCH[3]} ${BASH_REMATCH[4]}:${BASH_REMATCH[5]}:${BASH_REMATCH[6]}"
        else
            backup_time="$backup_name"
        fi
        
        local latest_marker=""
        if [ -f "${BACKUP_DIR}/latest" ]; then
            local latest_path=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
            if [ "$backup" = "$latest_path" ] || [ "$backup" = "${latest_path}/" ]; then
                latest_marker=" ${GREEN}(最新)${NC}"
            fi
        fi
        
        printf "  %-6s %-24s %-10s %s%b\n" "$index" "$backup_time" "$backup_size" "$backup" "$latest_marker"
        index=$((index + 1))
    done
    
    echo ""
    echo -e "${CYAN}用法:${NC}"
    echo "  $0 backup           创建新备份"
    echo "  $0 restore <N>      还原到序号N的备份"
    echo ""
}

# 根据序号获取备份路径
get_backup_by_index() {
    local index="$1"
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | grep -v "^$"))
    
    if [ ${#backups[@]} -eq 0 ]; then
        return 1
    fi
    
    if [ "$index" -lt 1 ] || [ "$index" -gt ${#backups[@]} ]; then
        return 1
    fi
    
    echo "${backups[$((index - 1))]}"
}

# 从备份路径还原
restore_from_path() {
    local restore_path="$1"
    
    if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
        echo -e "${RED}[错误] 无效的备份路径: ${restore_path}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}                    还原操作${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  还原来源: ${WHITE}${restore_path}${NC}"
    echo ""
    
    if [ -d "${restore_path}/config" ]; then
        mkdir -p "${CONFIG_DIR}"
        cp -r "${restore_path}/config/"* "${CONFIG_DIR}/" 2>/dev/null
        echo -e "${GREEN}  ✓ 配置文件已还原${NC}"
    else
        echo -e "${YELLOW}  - 备份中无配置文件${NC}"
    fi
    
    local backup_jar="${restore_path}/${JAR_NAME}"
    if [ -f "${backup_jar}" ]; then
        cp "${backup_jar}" "${APP_HOME}/"
        echo -e "${GREEN}  ✓ JAR 文件已还原${NC}"
    else
        echo -e "${YELLOW}  - 备份中无 JAR 文件${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[完成] 还原完成${NC}"
}

run_backup() {
    local subcmd="${1:-}"
    
    if [ "$subcmd" = "list" ]; then
        list_backups
        return $?
    fi
    
    # 创建新备份
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="${BACKUP_DIR}/${timestamp}"
    
    mkdir -p "${backup_path}"
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}                    备份操作${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  备份目录: ${WHITE}${backup_path}${NC}"
    echo ""
    
    if [ -d "${CONFIG_DIR}" ]; then
        cp -r "${CONFIG_DIR}" "${backup_path}/"
        echo -e "${GREEN}  ✓ 配置文件已备份${NC}"
    else
        echo -e "${YELLOW}  - 配置目录不存在${NC}"
    fi
    
    local jar_file="${APP_HOME}/${JAR_NAME}"
    if [ -f "${jar_file}" ]; then
        cp "${jar_file}" "${backup_path}/"
        echo -e "${GREEN}  ✓ JAR 文件已备份${NC}"
    else
        echo -e "${YELLOW}  - JAR 文件不存在: ${jar_file}${NC}"
    fi
    
    echo "${backup_path}" > "${BACKUP_DIR}/latest"
    
    echo ""
    echo -e "${GREEN}[完成] 备份完成: ${backup_path}${NC}"
}

run_restore() {
    local index="${1:-}"
    local restore_path=""
    
    if [ "$index" = "list" ]; then
        list_backups
        return $?
    fi
    
    if [[ "$index" =~ ^[0-9]+$ ]]; then
        restore_path=$(get_backup_by_index "$index")
        if [ -z "$restore_path" ]; then
            echo -e "${RED}[错误] 备份 #${index} 不存在${NC}"
            echo -e "${CYAN}使用 '$0 backup list' 查看可用备份${NC}"
            return 1
        fi
    else
        if [ -f "${BACKUP_DIR}/latest" ]; then
            restore_path=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
        fi
        
        if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
            echo -e "${RED}[错误] 未找到可还原的备份${NC}"
            echo -e "${CYAN}使用 '$0 backup list' 查看可用备份${NC}"
            return 1
        fi
    fi
    
    restore_from_path "$restore_path"
}

# ===========================================
# 升级回滚
# ===========================================
run_upgrade() {
    local package_path="$1"
    local no_restart="$2"
    local opts=""
    
    [ -n "${package_path}" ] && opts="${opts} --package ${package_path}"
    [ "${no_restart}" = "true" ] && opts="${opts} --no-restart"
    
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" upgrade ${opts}
    else
        echo -e "${YELLOW}[提示] 升级脚本不存在${NC}"
    fi
}

run_rollback() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" rollback
    else
        echo -e "${YELLOW}[提示] 回滚脚本不存在${NC}"
    fi
}

run_rollback_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" status
    else
        echo -e "${YELLOW}[提示] 回滚脚本不存在${NC}"
    fi
}

# ===========================================
# Docker 相关函数
# ===========================================
docker_build() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" build --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_deploy() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" deploy --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_start_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" start --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_stop_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" stop --name "$APP_NAME"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_restart_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" restart --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" status --name "$APP_NAME"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_logs() {
    local lines="${1:-100}"
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" logs --name "$APP_NAME" --lines "$lines"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

docker_clean() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" clean --name "$APP_NAME" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[提示] Docker 脚本不存在${NC}"
    fi
}

# ===========================================
# GC 触发
# ===========================================
run_gc() {
    local gc_type="${1:-full}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[错误] ${APP_NAME} 未在运行${NC}"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    if ! command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jcmd 命令${NC}"
        return 1
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}                    触发 GC${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  PID:      ${pid}"
    echo -e "  GC 类型: ${gc_type}"
    echo ""
    
    # GC 前状态
    echo -e "${CYAN}[GC 前状态]${NC}"
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    case "${gc_type}" in
        young|minor)
            echo -e "${YELLOW}触发 Young GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        full|major)
            echo -e "${YELLOW}触发 Full GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        *)
            echo -e "${RED}[错误] 未知的 GC 类型: ${gc_type}${NC}"
            echo "支持的类型: young, full"
            return 1
            ;;
    esac
    
    sleep 2
    
    # GC 后状态
    echo ""
    echo -e "${CYAN}[GC 后状态]${NC}"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    
    echo ""
    echo -e "${GREEN}[完成] GC 已触发${NC}"
}

# ===========================================
# 高 CPU 线程分析
# ===========================================
run_top_cpu() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[错误] ${APP_NAME} 未在运行${NC}"
        return 1
    fi
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[错误] 未找到 jstack 命令${NC}"
        return 1
    fi
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}                高 CPU 线程分析 (Top ${top_n})                  ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  进程 PID: ${pid}"
    echo -e "  应用名称: ${APP_NAME}"
    echo ""
    
    # 获取高 CPU 线程
    echo -e "${CYAN}[步骤 1] 获取高 CPU 线程...${NC}"
    local tmp_threads=$(mktemp)
    local tmp_jstack=$(mktemp)
    
    # 获取线程 CPU 使用率
    top -b -n 1 -H -p "$pid" 2>/dev/null | grep -E "^\s*[0-9]+" | head -n "$top_n" > "$tmp_threads"
    
    if [ ! -s "$tmp_threads" ]; then
        echo -e "${YELLOW}[警告] 无法通过 top 获取线程信息，尝试 ps...${NC}"
        ps -eLo pid,lwp,%cpu,comm --sort=-%cpu | grep "$pid" | head -n "$top_n" > "$tmp_threads"
    fi
    
    # 获取线程堆栈
    echo -e "${CYAN}[步骤 2] 获取线程堆栈...${NC}"
    "${jstack_cmd}" -l "$pid" > "$tmp_jstack" 2>/dev/null
    
    if [ ! -s "$tmp_jstack" ]; then
        echo -e "${RED}[错误] 无法获取线程堆栈${NC}"
        rm -f "$tmp_threads" "$tmp_jstack"
        return 1
    fi
    
    # 分析并显示
    echo ""
    echo -e "${CYAN}[步骤 3] 分析高 CPU 线程堆栈...${NC}"
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    
    local count=0
    while read -r line; do
        # 解析线程 ID 和 CPU
        local lwp=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        
        # 如果是 ps 输出格式
        if [[ "$line" =~ ^[[:space:]]*[0-9]+[[:space:]]+[0-9]+ ]]; then
            lwp=$(echo "$line" | awk '{print $2}')
            cpu=$(echo "$line" | awk '{print $3}')
        fi
        
        [ -z "$lwp" ] && continue
        
        # 转换为 16 进制
        local nid=$(printf "%x" "$lwp" 2>/dev/null)
        [ -z "$nid" ] && continue
        
        count=$((count + 1))
        echo ""
        echo -e "${YELLOW}#${count} 线程 ID: ${lwp} (0x${nid})  CPU: ${cpu}%${NC}"
        
        # 在 jstack 中查找对应的线程堆栈
        local found=false
        local in_thread=false
        local stack_lines=0
        
        while IFS= read -r stack_line; do
            if [[ "$stack_line" =~ nid=0x${nid}[[:space:]] ]]; then
                in_thread=true
                found=true
                echo -e "${WHITE}$stack_line${NC}"
            elif [ "$in_thread" = true ]; then
                if [[ "$stack_line" =~ ^[[:space:]]*$ ]] || [[ "$stack_line" =~ ^\".*\".*nid= ]]; then
                    break
                fi
                # 显示堆栈 (最多 15 行)
                if [ $stack_lines -lt 15 ]; then
                    # 高亮显示业务代码
                    if [[ "$stack_line" =~ at\ com\. ]] || [[ "$stack_line" =~ at\ cn\. ]] || [[ "$stack_line" =~ at\ org\..*\.service ]] || [[ "$stack_line" =~ at\ org\..*\.controller ]]; then
                        echo -e "${GREEN}$stack_line${NC}"
                    else
                        echo "$stack_line"
                    fi
                    stack_lines=$((stack_lines + 1))
                fi
            fi
        done < "$tmp_jstack"
        
        if [ "$found" = false ]; then
            echo -e "${YELLOW}  无法找到线程堆栈 (nid=0x${nid})${NC}"
        fi
        
        echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
        
        [ $count -ge $top_n ] && break
    done < "$tmp_threads"
    
    rm -f "$tmp_threads" "$tmp_jstack"
    
    echo ""
    echo -e "${GREEN}[完成] 分析结束${NC}"
    echo -e "${CYAN}提示: 绿色高亮的是业务代码，通常需要重点关注${NC}"
}

# ===========================================
# 高内存对象分析
# ===========================================
run_top_mem() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[错误] ${APP_NAME} 未在运行${NC}"
        return 1
    fi
    
    check_java
    local jmap_cmd="jmap"
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap" && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}               高内存对象分析 (Top ${top_n})                   ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  进程 PID: ${pid}"
    echo -e "  应用名称: ${APP_NAME}"
    echo ""
    
    # 尝试使用 jcmd (更安全)
    local histo_output=""
    if command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${CYAN}[步骤 1] 使用 jcmd 获取对象直方图...${NC}"
        histo_output=$("${jcmd_cmd}" "$pid" GC.class_histogram 2>/dev/null)
    fi
    
    # 如果 jcmd 失败，尝试 jmap
    if [ -z "$histo_output" ] && command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${CYAN}[步骤 1] 使用 jmap 获取对象直方图...${NC}"
        histo_output=$("${jmap_cmd}" -histo:live "$pid" 2>/dev/null)
    fi
    
    if [ -z "$histo_output" ]; then
        echo -e "${RED}[错误] 无法获取对象直方图${NC}"
        return 1
    fi
    
    echo ""
    echo -e "${CYAN}[步骤 2] 分析高内存对象...${NC}"
    echo ""
    
    # 显示标题
    echo -e "${WHITE}  排名   实例数          占用字节            类名${NC}"
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    
    # 解析并显示 Top N
    local count=0
    echo "$histo_output" | grep -E "^\s*[0-9]+:" | head -n "$top_n" | while read -r line; do
        count=$((count + 1))
        local num=$(echo "$line" | awk '{print $1}' | tr -d ':')
        local instances=$(echo "$line" | awk '{print $2}')
        local bytes=$(echo "$line" | awk '{print $3}')
        local class_name=$(echo "$line" | awk '{print $4}')
        
        # 转换字节为可读格式
        local size_human=""
        if [ "$bytes" -ge 1073741824 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f GB\", $bytes/1073741824}")
        elif [ "$bytes" -ge 1048576 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f MB\", $bytes/1048576}")
        elif [ "$bytes" -ge 1024 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f KB\", $bytes/1024}")
        else
            size_human="${bytes} B"
        fi
        
        # 高亮业务类
        if [[ "$class_name" =~ ^(com\.|cn\.|org\..*\.model|org\..*\.entity|org\..*\.dto) ]]; then
            printf "${GREEN}  %4s   %12s   %12s   %s${NC}\n" "$num" "$instances" "$size_human" "$class_name"
        else
            printf "  %4s   %12s   %12s   %s\n" "$num" "$instances" "$size_human" "$class_name"
        fi
    done
    
    echo -e "${BLUE}────────────────────────────────────────────────────────────────${NC}"
    
    # 显示内存概览
    echo ""
    echo -e "${CYAN}[内存概览]${NC}"
    local total_instances=$(echo "$histo_output" | grep -E "^Total" | awk '{print $2}')
    local total_bytes=$(echo "$histo_output" | grep -E "^Total" | awk '{print $3}')
    if [ -n "$total_instances" ]; then
        local total_human=""
        if [ "$total_bytes" -ge 1073741824 ]; then
            total_human=$(awk "BEGIN {printf \"%.2f GB\", $total_bytes/1073741824}")
        elif [ "$total_bytes" -ge 1048576 ]; then
            total_human=$(awk "BEGIN {printf \"%.2f MB\", $total_bytes/1048576}")
        else
            total_human=$(awk "BEGIN {printf \"%.2f KB\", $total_bytes/1024}")
        fi
        echo -e "  总实例数: ${WHITE}${total_instances}${NC}"
        echo -e "  总占用:   ${WHITE}${total_human}${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[完成] 分析结束${NC}"
    echo -e "${CYAN}提示: 绿色高亮的是业务类，通常需要重点关注${NC}"
    echo -e "${CYAN}      如需更详细分析，请使用: $0 dump heap${NC}"
}

# ===========================================
# 服务管理 (systemd)
# ===========================================
SERVICE_NAME="${APP_NAME}.service"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}"

install_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[错误] 需要 root 权限执行此命令${NC}"
        echo "请使用: sudo $0 install-service"
        return 1
    fi
    
    if [ -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[警告] 服务已存在: ${SERVICE_NAME}${NC}"
        read -p "是否覆盖? (y/N): " confirm
        [ "$confirm" != "y" ] && [ "$confirm" != "Y" ] && return 0
    fi
    
    check_java
    local java_path=$(which "$JAVA_CMD" 2>/dev/null || echo "$JAVA_CMD")
    local run_user=$(stat -c '%U' "${APP_HOME}" 2>/dev/null || echo "root")
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}              安装 Systemd 服务${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  服务名称: ${SERVICE_NAME}"
    echo -e "  运行用户: ${run_user}"
    echo -e "  Java 路径: ${java_path}"
    echo -e "  应用目录: ${APP_HOME}"
    echo ""
    
    # 生成 systemd 服务文件
    cat > "${SERVICE_FILE}" << EOF
[Unit]
Description=${APP_NAME} Spring Boot Application
After=network.target

[Service]
Type=simple
User=${run_user}
Group=${run_user}
WorkingDirectory=${APP_HOME}
Environment="JAVA_HOME=$(dirname $(dirname $java_path))"
Environment="SPRING_PROFILE=${SPRING_PROFILE}"
ExecStart=${SCRIPT_DIR}/startup.sh start
ExecStop=${SCRIPT_DIR}/startup.sh stop
ExecReload=/bin/kill -HUP \$MAINPID
Restart=on-failure
RestartSec=10
SuccessExitStatus=143
TimeoutStopSec=30
KillMode=process

[Install]
WantedBy=multi-user.target
EOF
    
    # 重新加载 systemd
    systemctl daemon-reload
    
    echo -e "${GREEN}[成功] 服务已安装: ${SERVICE_NAME}${NC}"
    echo ""
    echo -e "${CYAN}可用命令:${NC}"
    echo "  systemctl start ${SERVICE_NAME}     # 启动服务"
    echo "  systemctl stop ${SERVICE_NAME}      # 停止服务"
    echo "  systemctl restart ${SERVICE_NAME}   # 重启服务"
    echo "  systemctl status ${SERVICE_NAME}    # 查看状态"
    echo "  systemctl enable ${SERVICE_NAME}    # 开机自启"
    echo "  journalctl -u ${SERVICE_NAME} -f    # 查看日志"
}

uninstall_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[错误] 需要 root 权限执行此命令${NC}"
        echo "请使用: sudo $0 uninstall-service"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[警告] 服务不存在: ${SERVICE_NAME}${NC}"
        return 0
    fi
    
    echo -e "${BLUE}卸载服务: ${SERVICE_NAME}...${NC}"
    
    # 停止服务
    systemctl stop "${SERVICE_NAME}" 2>/dev/null
    systemctl disable "${SERVICE_NAME}" 2>/dev/null
    
    # 删除服务文件
    rm -f "${SERVICE_FILE}"
    systemctl daemon-reload
    
    echo -e "${GREEN}[成功] 服务已卸载${NC}"
}

enable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[错误] 需要 root 权限执行此命令${NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${RED}[错误] 服务未安装，请先执行: $0 install-service${NC}"
        return 1
    fi
    
    systemctl enable "${SERVICE_NAME}"
    echo -e "${GREEN}[成功] 已启用开机自启${NC}"
}

disable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[错误] 需要 root 权限执行此命令${NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[警告] 服务未安装${NC}"
        return 0
    fi
    
    systemctl disable "${SERVICE_NAME}"
    echo -e "${GREEN}[成功] 已禁用开机自启${NC}"
}

show_service_status() {
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[警告] 服务未安装: ${SERVICE_NAME}${NC}"
        echo "使用 '$0 install-service' 安装服务"
        return 0
    fi
    
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}              Systemd 服务状态${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    systemctl status "${SERVICE_NAME}" --no-pager
}

# ===========================================
# 参数解析
# ===========================================
# 在参数解析前解析一次元数据与 app.env，并准备 LOADER_PATH 默认值
resolve_app_metadata
load_app_env
# 初始不构建 LOADER_PATH，等 start_app 再根据实际目录计算

COMMAND="start"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
DEBUG_MODE="false"
CUSTOM_JVM_OPTS=""
CUSTOM_JAVA=""
TAIL_LOG="false"
SKYWALKING_ENABLED="false"
SHOW_LOGS=""
JAVA_CMD_ARGS=()
SKYWALKING_SUBCMD_ARGS=()
BACKUP_CMD_ARGS=()
RESTORE_CMD_ARGS=()

TOP_N="10"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|info|jvm|tuning|analyze|dump|arthas|upgrade|rollback|rollback-status|debug)
            COMMAND="$1"; shift ;;
        skywalking)
            COMMAND="skywalking"
            shift
            SKYWALKING_SUBCMD_ARGS=("$@")
            break
            ;;
        java-install)
            COMMAND="java-install"
            shift
            JAVA_CMD_ARGS=("$@")
            break
            ;;
        backup)
            COMMAND="backup"
            shift
            BACKUP_CMD_ARGS=("$@")
            break
            ;;
        restore)
            COMMAND="restore"
            shift
            RESTORE_CMD_ARGS=("$@")
            break
            ;;
        gc)
            COMMAND="gc"
            shift
            # 检查下一个参数是否是 GC 类型
            if [[ "$1" =~ ^(young|full|minor|major)$ ]]; then
                GC_TYPE="$1"
                shift
            else
                GC_TYPE="full"
            fi
            ;;
        top-cpu)
            COMMAND="top-cpu"
            shift
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                TOP_N="$1"
                shift
            fi
            ;;
        top-mem)
            COMMAND="top-mem"
            shift
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                TOP_N="$1"
                shift
            fi
            ;;
        install-service|uninstall-service|enable-service|disable-service|service-status)
            COMMAND="$1"; shift ;;
        logs)
            COMMAND="logs"
            shift
            # 检查下一个参数是否是数字
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                SHOW_LOGS="$1"
                shift
            else
                SHOW_LOGS="100"
            fi
            ;;
        tail)
            COMMAND="tail"; shift ;;
        docker-build|docker-deploy|docker-start|docker-stop|docker-restart|docker-status|docker-logs|docker-clean|publish)
            COMMAND="$1"; shift ;;
        young|full|minor|major)
            GC_TYPE="$1"; shift ;;
        cpu|mem|all|config|jar|heap|thread)
            EXTRA_MODE="$1"; shift ;;
        --dev) SPRING_PROFILE="dev"; shift ;;
        --prod) SPRING_PROFILE="prod"; shift ;;
        --test) SPRING_PROFILE="test"; shift ;;
        --profile) SPRING_PROFILE="$2"; shift 2 ;;
        --java)
            shift
            if [ -n "$1" ] && [[ ! "$1" =~ ^- ]]; then
                CUSTOM_JAVA="$1"
                if [ -x "$CUSTOM_JAVA" ]; then
                    save_java_path "$CUSTOM_JAVA"
                fi
                shift
            else
                show_java_info
                exit 0
            fi
            ;;
        --jvm-opts) CUSTOM_JVM_OPTS="$2"; shift 2 ;;
        --debug-port) DEBUG_PORT="$2"; shift 2 ;;
        --docker-port) DOCKER_PORT="$2"; shift 2 ;;
        --host-port) DOCKER_HOST_PORT="$2"; shift 2 ;;
        --image-tag) DOCKER_IMAGE_TAG="$2"; shift 2 ;;
        --skywalking) SKYWALKING_ENABLED="true"; shift ;;
        --sw-backend) SW_AGENT_COLLECTOR_BACKEND_SERVICES="$2"; shift 2 ;;
        --sw-name) SW_AGENT_NAME="$2"; shift 2 ;;
        --package) UPGRADE_PACKAGE="$2"; shift 2 ;;
        --no-restart) NO_RESTART="true"; shift ;;
        --output|--dump-path) DUMP_OUTPUT="$2"; shift 2 ;;
        --logs)
            if [[ "$2" =~ ^[0-9]+$ ]]; then
                SHOW_LOGS="$2"
                shift 2
            else
                SHOW_LOGS="100"
                shift
            fi
            ;;
        --tail) TAIL_LOG="true"; shift ;;
        --help|-h) show_help; exit 0 ;;
        *)
            # 检查是否是数字 (用于 logs/top-cpu/top-mem 命令)
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                case "$COMMAND" in
                    logs) SHOW_LOGS="$1" ;;
                    top-cpu|top-mem) TOP_N="$1" ;;
                    *) echo -e "${RED}[错误] 未知参数: $1${NC}"; show_help; exit 1 ;;
                esac
                shift
            else
                echo -e "${RED}[错误] 未知参数: $1${NC}"
                show_help
                exit 1
            fi
            ;;
    esac
done

[ "$COMMAND" = "debug" ] && DEBUG_MODE="true" && COMMAND="start"

# ===========================================
# 命令执行
# ===========================================
case "$COMMAND" in
    start) start_app ;;
    stop) stop_app ;;
    restart) restart_app ;;
    status) check_status ;;
    info) show_info ;;
    logs) show_logs "${SHOW_LOGS:-100}" ;;
    tail) tail_logs ;;
    jvm) show_jvm_info ;;
    gc) run_gc "${GC_TYPE:-full}" ;;
    tuning) show_tuning ;;
    arthas) start_arthas ;;
    analyze) run_analyze "${EXTRA_MODE:-cpu}" ;;
    top-cpu) run_top_cpu "${TOP_N:-10}" ;;
    top-mem) run_top_mem "${TOP_N:-10}" ;;
    dump) run_dump "${EXTRA_MODE:-all}" "${DUMP_OUTPUT}" ;;
    backup) run_backup "${BACKUP_CMD_ARGS[@]}" ;;
    restore) run_restore "${RESTORE_CMD_ARGS[@]}" ;;
    upgrade) run_upgrade "${UPGRADE_PACKAGE}" "${NO_RESTART}" ;;
    rollback) run_rollback ;;
    rollback-status) run_rollback_status ;;
    skywalking) run_skywalking "${SKYWALKING_SUBCMD_ARGS[@]}" ;;
    java-install) run_java_install "${JAVA_CMD_ARGS[@]}" ;;
    install-service) install_service ;;
    uninstall-service) uninstall_service ;;
    enable-service) enable_service ;;
    disable-service) disable_service ;;
    service-status) show_service_status ;;
    docker-build) docker_build ;;
    docker-deploy|publish) docker_deploy ;;
    docker-start) docker_start_container ;;
    docker-stop) docker_stop_container ;;
    docker-restart) docker_restart_container ;;
    docker-status) docker_status ;;
    docker-logs) docker_logs "${EXTRA_MODE:-100}" ;;
    docker-clean) docker_clean ;;
    *) show_help; exit 1 ;;
esac
