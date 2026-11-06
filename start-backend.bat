@echo off
cd /d D:\ch\project\spring-support-api-starter\spring-api-support-monitor-starter\target
set MS_DB_TYPE=h2
java -jar spring-api-support-monitor-starter-4.0.0.42.jar --server.port=8082 --server.servlet.context-path=/ --spring.profiles.active=h2 --plugin.socket.enable=true --plugin.datasource.script.enable=true --plugin.datasource.script.scan-mode=ALWAYS --plugin.datasource.script.validate-checksum=false --plugin.oauth.local-admin-fallback=true --plugin.oauth.protocol=static --plugin.oauth.temp.user=admin:admin --plugin.oauth.server.security.enabled=false
