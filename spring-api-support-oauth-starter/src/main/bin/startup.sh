#!/bin/bash
# OAuth 单点登录系统启动脚本
# 作者: CH
# 创建时间: 2024

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="spring-api-support-oauth-starter"
JAR_NAME="${APP_NAME}-4.0.0.jar"

# 配置目录
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"

# JVM参数
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"

# 日志配置
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring配置
SPRING_OPTS="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
SPRING_OPTS="${SPRING_OPTS} --spring.profiles.active=${SPRING_PROFILE:-prod}"

# 创建日志目录
mkdir -p "${LOG_DIR}"

# 检查Java环境
if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# 检查Java版本
JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "错误: 需要Java 21或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi

# 构建classpath
CLASSPATH="${CONFIG_DIR}:${APP_HOME}/${JAR_NAME}"
for jar in "${LIB_DIR}"/*.jar; do
    CLASSPATH="${CLASSPATH}:${jar}"
done

echo "=========================================="
echo "  ${APP_NAME} 启动中..."
echo "=========================================="
echo "  APP_HOME: ${APP_HOME}"
echo "  JAVA_CMD: ${JAVA_CMD}"
echo "  JAVA_OPTS: ${JAVA_OPTS}"
echo "  CONFIG_DIR: ${CONFIG_DIR}"
echo "=========================================="

# 启动应用
exec "$JAVA_CMD" $JAVA_OPTS \
    -cp "${CLASSPATH}" \
    com.chua.starter.oauth.sso.AuthApplication \
    ${SPRING_OPTS} \
    "$@"
