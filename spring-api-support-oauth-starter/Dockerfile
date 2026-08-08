# ===========================================
# OAuth 认证服务镜像（spring-api-support-oauth-starter）
#
# 构建上下文：本工程目录（依赖 target/ 下已打包的 jar + lib + config）
#   docker build -t chua/oauth:4.0.0.42 .
#
# 运行（host 网络 + 端口环境变量覆盖为 19180）：
#   docker run -d --name oauth --restart=always --network host \
#     -e SERVER_PORT=19180 chua/oauth:4.0.0.42
#
# 说明：
#   - jar 的 manifest Class-Path 指向 config/ 与 lib/，故必须保证两者与 jar 同目录
#   - 数据库/Redis 均指向 172.16.0.40（见 application-dev.yml）
#   - 通过 SERVER_PORT 环境变量覆盖端口，避免改动源码配置
# ===========================================
# 基础镜像：jar 以 Java 25 编译(class file 69)，需 JRE 25
# 注意：此宿主(CentOS 7 / kernel 3.10)对 Ubuntu 系 JVM 需配合 seccomp=unconfined 运行，
#      见 compose.yaml 的 security_opt 说明
FROM eclipse-temurin:25-jre

LABEL maintainer="chua" \
      app="oauth" \
      version="4.0.0.42"

WORKDIR /app

# 复制构建产物（jar + 全量依赖 + 外置配置）
COPY target/spring-api-support-oauth-starter-4.0.0.42.jar app.jar
COPY target/lib/ lib/
COPY target/config/ config/

# 预留端口（容器实际以 --network host 运行，端口取决于 SERVER_PORT）
EXPOSE 19180 19181

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
