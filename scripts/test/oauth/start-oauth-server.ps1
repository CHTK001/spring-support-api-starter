$ErrorActionPreference = 'Stop'

$module = 'H:\workspace\2\spring-support-api-parent\spring-api-support-oauth-starter'
Set-Location $module

mvn.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.profiles.active=dev --plugin.oauth.server.sign-verify=false --plugin.oauth.server.protocols=armeria,rsocket'
