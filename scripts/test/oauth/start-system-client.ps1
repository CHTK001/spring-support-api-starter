param(
    [Parameter(Mandatory = $true)]
    [string]$Protocol,

    [Parameter(Mandatory = $true)]
    [string]$EnableEncryption,

    [Parameter(Mandatory = $true)]
    [string]$Address
)

$ErrorActionPreference = 'Stop'

$module = 'H:\workspace\2\spring-support-api-parent\spring-api-support-system-starter'
Set-Location $module

mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev --plugin.oauth.protocol=$Protocol --plugin.oauth.enable-encryption=$EnableEncryption --plugin.oauth.address=$Address"
