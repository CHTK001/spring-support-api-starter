#!/bin/bash
# 运维监控系统启动脚本
# 作者: CH
# 创建时间: 2024

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="spring-api-support-monitor-starter"
JAR_NAME="${APP_NAME}-4.0.0.jar"

CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"

JAVA_OPTS="${JAVA_OPTS:--Xms1024m -Xmx2048m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

SPRING_OPTS="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
SPRING_OPTS="${SPRING_OPTS} --spring.profiles.active=${SPRING_PROFILE:-prod}"

mkdir -p "${LOG_DIR}"

if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

CLASSPATH="${CONFIG_DIR}:${APP_HOME}/${JAR_NAME}"
for jar in "${LIB_DIR}"/*.jar; do
    CLASSPATH="${CLASSPATH}:${jar}"
done

echo "=========================================="
echo "  ${APP_NAME} 启动中..."
echo "=========================================="

exec "$JAVA_CMD" $JAVA_OPTS \
    -cp "${CLASSPATH}" \
    com.chua.starter.monitor.MonitorServerApplication \
    ${SPRING_OPTS} \
    "$@"
