@echo off
cd /d G:\work\spring-support-api-starter
java -Dspring.profiles.active=h2 -jar spring-api-support-monitor-starter\target\spring-api-support-monitor-starter-4.0.0.41.jar --server.port=19170 --server.servlet.context-path=/monitor/api > backend.log 2>&1
