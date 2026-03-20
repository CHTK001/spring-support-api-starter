@echo off
set "JAVA_HOME=C:\Program Files\Amazon Corretto\jdk25.0.1_8"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d H:\workspace\2\spring-support-api-parent\spring-api-support-monitor-starter
mvn -Dmaven.test.skip=true spring-boot:run -Dspring-boot.run.arguments="--server.port=8080 --server.servlet.context-path=/api --plugin.sync.job-integration.enabled=true --plugin.oauth.client.enable=false"
