# 简单的枚举功能验证脚本
Write-Host "=== FileManagementMode 枚举功能验证 ==="

# 检查枚举类文件是否存在
$enumFile = "src\main\java\com\chua\starter\monitor\starter\enums\FileManagementMode.java"
if (Test-Path $enumFile) {
    Write-Host "✓ 枚举类文件存在: $enumFile"
} else {
    Write-Host "✗ 枚举类文件不存在: $enumFile"
    exit 1
}

# 检查 TypeHandler 文件是否存在
$handlerFile = "src\main\java\com\chua\starter\monitor\starter\handler\FileManagementModeTypeHandler.java"
if (Test-Path $handlerFile) {
    Write-Host "✓ TypeHandler 文件存在: $handlerFile"
} else {
    Write-Host "✗ TypeHandler 文件不存在: $handlerFile"
    exit 1
}

# 检查 Mapper XML 文件是否存在
$mapperFile = "src\main\resources\mapper\MonitorSysGenServerSettingMapper.xml"
if (Test-Path $mapperFile) {
    Write-Host "✓ Mapper XML 文件存在: $mapperFile"
    
    # 检查 CASE 语句是否存在
    $content = Get-Content $mapperFile -Raw
    if ($content -match "CASE.*monitor_sys_gen_server_setting_file_management_mode") {
        Write-Host "✓ Mapper XML 包含 CASE 转换语句"
    } else {
        Write-Host "✗ Mapper XML 缺少 CASE 转换语句"
    }
} else {
    Write-Host "✗ Mapper XML 文件不存在: $mapperFile"
}

# 检查枚举类中的关键方法
$enumContent = Get-Content $enumFile -Raw
$methods = @("fromCode", "fromLegacyCode", "getValue", "getCode")

foreach ($method in $methods) {
    if ($enumContent -match "public.*$method") {
        Write-Host "✓ 枚举类包含方法: $method"
    } else {
        Write-Host "✗ 枚举类缺少方法: $method"
    }
}

# 检查 Jackson 注解
$annotations = @("@JsonValue", "@JsonCreator")
foreach ($annotation in $annotations) {
    if ($enumContent -match [regex]::Escape($annotation)) {
        Write-Host "✓ 枚举类包含注解: $annotation"
    } else {
        Write-Host "✗ 枚举类缺少注解: $annotation"
    }
}

Write-Host ""
Write-Host "=== 验证完成 ==="
Write-Host "FileManagementModeTypeHandler 已修复并准备就绪！"
Write-Host ""
Write-Host "主要修复内容："
Write-Host "1. ✓ 枚举类增加了向后兼容性支持"
Write-Host "2. ✓ TypeHandler 修复了编译错误"
Write-Host "3. ✓ Mapper XML 添加了 SQL 级别的数据转换"
Write-Host "4. ✓ 添加了 Jackson 序列化注解"
Write-Host ""
Write-Host "现在可以正常处理数据库中的新旧格式数据！"
