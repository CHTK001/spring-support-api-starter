#!/usr/bin/env pwsh
#requires -Version 7.0
<#
.SYNOPSIS
  Spring API Support 交互式构建脚本 — 键盘方向键选择打包模块
.DESCRIPTION
  - ↑/↓ 移动光标
  - Space 切换模块选中状态
  - Enter 确认打包所选模块
  - a 全选  n 全不选
  - q 退出
  - 0 / "all" 跳过选择，直接打包全部模块
#>

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding  = [System.Text.Encoding]::UTF8

$ErrorActionPreference = 'Stop'
$rootDir = (Resolve-Path "$PSScriptRoot").Path
Set-Location $rootDir

# ─────────────── 工具函数 ───────────────
function Write-HostUtf8 { param($msg) Write-Host $msg -NoNewline }

function Read-MenuKey {
    $key = [Console]::ReadKey($true)
    return $key
}

function Clear-Lines {
    param([int]$count)
    for ($i = 0; $i -lt $count; $i++) {
        [Console]::SetCursorPosition(0, [Console]::CursorTop - 1)
        [Console]::Write("`e[2K")
    }
}

function Move-Cursor {
    param([int]$row, [int]$col)
    [Console]::SetCursorPosition($col, $row)
}

function Show-Cursor { [Console]::CursorVisible = $false }
function Hide-Cursor { [Console]::CursorVisible = $true }

function Write-At { param($row, $col, $text) Move-Cursor $row $col; [Console]::Write($text) }

# ─────────────── 模块发现 ───────────────
function Get-Modules {
    # 优先:从根 pom.xml 解析 <module> 行（最稳定可靠）
    try {
        $pomXml = [xml](Get-Content "$rootDir\pom.xml" -Raw -Encoding UTF8)
        $names = $pomXml.project.modules.module
        $modules = @()
        foreach ($n in $names) {
            $v = if ($n.'#cdata-section') { $n.'#cdata-section' } else { [string]$n }
            if ($v) { $modules += $v.Trim() }
        }
        # 展开聚合 pom（如 business-parent）下嵌套模块
        $expanded = @()
        foreach ($m in $modules) {
            $pomPath = Join-Path $rootDir "$m\pom.xml"
            if (Test-Path $pomPath) {
                try {
                    $innerXml = [xml](Get-Content $pomPath -Raw -Encoding UTF8)
                    if ($innerXml.project.packaging -eq 'pom') {
                        $innerMods = $innerXml.project.modules.module
                        foreach ($im in $innerMods) {
                            $imv = if ($im.'#cdata-section') { $im.'#cdata-section' } else { [string]$im }
                            if ($imv) {
                                # 兼容 "../foo" 写法
                                $imv = $imv.Trim() -replace '^\.\./', ''
                                $expanded += $imv
                            }
                        }
                        continue
                    }
                } catch {}
            }
            $expanded += $m
        }
        if ($expanded.Count -gt 0) { return $expanded }
    } catch {}

    # 兜底:扫目录
    return (Get-ChildItem $rootDir -Directory |
            Where-Object { Test-Path (Join-Path $_.FullName 'pom.xml') } |
            ForEach-Object { $_.Name })
}

# ─────────────── Maven 调用 ───────────────
function Invoke-MavenBuild {
    param([string[]]$modules, [string]$profile = 'install', [switch]$SkipTests)

    if ($modules -and $modules.Count -gt 0) {
        # 检测所选模块的归属（common / business），选择正确的 pom 入口
        $commonParent = 'spring-api-support-common-parent'
        $businessParent = 'spring-api-support-business-parent'
        $hasCommon = $modules | Where-Object { $_ -like 'spring-api-support-common-*' }
        $hasBusiness = $modules | Where-Object { $modules -notcontains $_ -and ($modules | Where-Object { $_ -like 'spring-api-support-common-*' }) }

        # 简化策略：把 -pl 给到根 pom 即可（mvn 会扫描子 pom）
        $args = @('-B', $profile)
        if ($SkipTests) { $args += '-DskipTests' }

        # 拆分:common-* 用 common-parent 入口;业务用根 pom
        $commonMods = @($modules | Where-Object { $_ -like 'spring-api-support-common-*' })
        $businessMods = @($modules | Where-Object { $_ -notlike 'spring-api-support-common-*' })

        $targets = @()
        if ($commonMods.Count -gt 0) {
            $targets += [pscustomobject]@{
                Pom    = "spring-api-support-common-parent\pom.xml"
                Modules = $commonMods
            }
        }
        if ($businessMods.Count -gt 0) {
            $targets += [pscustomobject]@{
                Pom    = "pom.xml"
                Modules = $businessMods
            }
        }

        foreach ($t in $targets) {
            $mvnArgs = @('-B', '-f', $t.Pom, $profile)
            if ($SkipTests) { $mvnArgs += '-DskipTests' }
            $mvnArgs += '-pl', ($t.Modules -join ',')
            $mvnArgs += '-am'
            Write-Host ""
            Write-Host "▶ mvn $($mvnArgs -join ' ')" -ForegroundColor Cyan
            Write-Host ""
            & mvn @mvnArgs
            if ($LASTEXITCODE -ne 0) { return $LASTEXITCODE }
        }
        return 0
    }

    # 无模块选择 — 全部打包（根 pom）
    $args = @('-B', $profile)
    if ($SkipTests) { $args += '-DskipTests' }
    Write-Host ""
    Write-Host "▶ mvn $($args -join ' ')" -ForegroundColor Cyan
    Write-Host ""
    & mvn @args
    return $LASTEXITCODE
}

# ─────────────── 主交互循环 ───────────────
function Show-Menu {
    param([string[]]$modules, [bool[]]$selected, [int]$cursor)

    $startRow = [Console]::CursorTop
    Write-Host ""
    Write-Host "  ┌─ Spring API Support 构建脚本 ─────────────────────────────────┐" -ForegroundColor DarkCyan
    Write-Host "  │ ↑/↓ 移动  Space 选中/取消  Enter 打包所选  a 全选  n 全不选  │" -ForegroundColor DarkGray
    Write-Host "  │ q 退出   0 全部打包(不选择)                                   │" -ForegroundColor DarkGray
    Write-Host "  └────────────────────────────────────────────────────────────────┘" -ForegroundColor DarkCyan
    Write-Host ""

    for ($i = 0; $i -lt $modules.Count; $i++) {
        $marker = if ($selected[$i]) { '●' } else { '○' }
        $name   = $modules[$i].PadRight(56)
        $cursorMark = if ($i -eq $cursor) { '▶' } else { ' ' }
        $color = if ($i -eq $cursor) { 'Yellow' } elseif ($selected[$i]) { 'Green' } else { 'Gray' }

        $line = "  $cursorMark [$marker] $name"
        if ($i -eq $cursor) {
            Write-Host $line -BackgroundColor DarkGray -ForegroundColor $color
        } else {
            Write-Host $line -ForegroundColor $color
        }
    }

    $selCount = ($selected | Where-Object { $_ }).Count
    Write-Host ""
    Write-Host "  已选: $selCount / $($modules.Count) 个模块" -ForegroundColor DarkCyan
    Write-Host ""
    Write-Host "  操作: [↑/↓]移动  [Space]选中  [a]全选  [n]全清  [Enter]打包  [0]全部  [q]退出" -ForegroundColor DarkGray
}

function Select-And-Build {
    param([string[]]$modules)

    if (-not $modules -or $modules.Count -eq 0) {
        Write-Host "✗ 未发现任何模块" -ForegroundColor Red
        return
    }

    $selected = New-Object bool[] $modules.Count
    $cursor = 0
    Show-Cursor

    while ($true) {
        # 渲染：清屏后重绘
        [Console]::Clear()
        Show-Menu $modules $selected $cursor

        $key = Read-MenuKey
        switch ($key.Key) {
            'UpArrow'    { if ($cursor -gt 0) { $cursor-- } else { $cursor = $modules.Count - 1 } }
            'DownArrow'  { if ($cursor -lt $modules.Count - 1) { $cursor++ } else { $cursor = 0 } }
            'Spacebar'   { $selected[$cursor] = -not $selected[$cursor] }
            'Enter'      {
                Hide-Cursor
                [Console]::Clear()
                $picked = for ($i = 0; $i -lt $modules.Count; $i++) { if ($selected[$i]) { $modules[$i] } }
                if ($picked.Count -eq 0) {
                    Write-Host "✗ 未选中任何模块,已取消" -ForegroundColor Yellow
                    return
                }
                Write-Host "▶ 将打包以下模块:" -ForegroundColor Cyan
                $picked | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
                Write-Host ""
                $rc = Invoke-MavenBuild -modules $picked -profile 'install' -SkipTests
                Write-Host ""
                if ($rc -eq 0) {
                    Write-Host "✓ 构建成功" -ForegroundColor Green
                } else {
                    Write-Host "✗ 构建失败 (退出码 $rc)" -ForegroundColor Red
                }
                return
            }
            'D0'         {
                Hide-Cursor
                [Console]::Clear()
                Write-Host "▶ 打包全部模块..." -ForegroundColor Cyan
                $rc = Invoke-MavenBuild -modules @() -profile 'install' -SkipTests
                Write-Host ""
                if ($rc -eq 0) { Write-Host "✓ 构建成功" -ForegroundColor Green } else { Write-Host "✗ 构建失败 ($rc)" -ForegroundColor Red }
                return
            }
            'A'          { for ($i = 0; $i -lt $selected.Length; $i++) { $selected[$i] = $true } }
            'N'          { for ($i = 0; $i -lt $selected.Length; $i++) { $selected[$i] = $false } }
            'Q'          { Hide-Cursor; Write-Host ""; Write-Host "已退出" -ForegroundColor Yellow; return }
        }
    }
}

# ─────────────── 入口 ───────────────
Write-Host ""
Write-Host "┌─ Spring API Support 构建脚本 ─────────────────────────────────────┐" -ForegroundColor DarkCyan
Write-Host "│ 探测模块列表...                                                    │" -ForegroundColor DarkGray
Write-Host "└────────────────────────────────────────────────────────────────────┘" -ForegroundColor DarkCyan

$modules = Get-Modules
if (-not $modules -or $modules.Count -eq 0) {
    Write-Host "✗ 无法识别任何模块,请检查 $rootDir\pom.xml" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✓ 发现 $($modules.Count) 个模块:" -ForegroundColor Green
$modules | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
Write-Host ""

Select-And-Build -modules $modules
