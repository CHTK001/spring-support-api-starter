#!/usr/bin/env pwsh
#requires -Version 7.0
<#
.SYNOPSIS
  一键构建脚本 — 直接打包所有模块（无交互）

.PARAMETER SkipTests
  跳过测试

.PARAMETER Goal
  Maven 目标，默认 install

.EXAMPLE
  .\build-all.ps1
  .\build-all.ps1 -SkipTests
  .\build-all.ps1 -Goal package
#>

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

param(
    [switch]$SkipTests,
    [string]$Goal = 'install'
)

$ErrorActionPreference = 'Stop'
$rootDir = (Resolve-Path "$PSScriptRoot").Path
Set-Location $rootDir

$args = @('-B', $Goal)
if ($SkipTests) { $args += '-DskipTests' }

Write-Host ""
Write-Host "┌─ Spring API Support 一键构建 ─────────────────────────┐" -ForegroundColor DarkCyan
Write-Host "  Goal: $Goal  SkipTests: $SkipTests" -ForegroundColor Gray
Write-Host "└──────────────────────────────────────────────────────────┘" -ForegroundColor DarkCyan
Write-Host ""

& mvn @args
exit $LASTEXITCODE
