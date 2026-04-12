@echo off
setlocal
cd /d g:\work\spring-support-api-starter
if not exist g:\work\spring-support-api-starter\tmp-logs mkdir g:\work\spring-support-api-starter\tmp-logs
call Z:\soft\work\apache-maven-3.9.8\bin\mvn.cmd -f g:\work\spring-support-parent-starter\spring-support-payment-starter\pom.xml -DskipTests -Dexec.mainClass=com.chua.payment.support.PaymentTestApplication -Dexec.classpathScope=test org.codehaus.mojo:exec-maven-plugin:3.5.0:java > g:\work\spring-support-api-starter\tmp-logs\payment-backend.out.log 2> g:\work\spring-support-api-starter\tmp-logs\payment-backend.err.log
