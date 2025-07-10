# 文件管理模式数据迁移指南

## 问题背景

在修复文件管理配置保存问题时，发现数据库中存在旧的数字格式数据（如 `0`, `1`, `2` 等），而新的实现期望字符串格式（如 `NONE`,
`SSH`, `API` 等）。这导致了以下错误：

```
No enum constant com.chua.starter.monitor.starter.enums.FileManagementMode.0
```

## 解决方案

### 立即修复方案（SQL 级别转换）

我们在 MyBatis Mapper XML 中添加了 SQL 级别的数据转换，这样可以立即解决问题而无需修改数据库：

```sql
SELECT *,
       CASE
           WHEN monitor_sys_gen_server_setting_file_management_mode = '0' THEN 'NONE'
           WHEN monitor_sys_gen_server_setting_file_management_mode = '1' THEN 'SSH'
           WHEN monitor_sys_gen_server_setting_file_management_mode = '2' THEN 'API'
           WHEN monitor_sys_gen_server_setting_file_management_mode = '3' THEN 'NODE'
           WHEN monitor_sys_gen_server_setting_file_management_mode = '4' THEN 'LOCAL'
           WHEN monitor_sys_gen_server_setting_file_management_mode IS NULL THEN 'NONE'
           ELSE monitor_sys_gen_server_setting_file_management_mode
       END AS monitor_sys_gen_server_setting_file_management_mode
FROM monitor_sys_gen_server_setting
```

这个方案的优点：

- ✅ 立即生效，无需数据迁移
- ✅ 向后兼容，支持新旧格式
- ✅ 不影响现有数据
- ✅ 查询时自动转换

## 当前状态

**问题已解决！** 通过在 MyBatis Mapper XML 中添加 SQL 级别的数据转换，现在系统可以：

1. **正常读取旧数据**：数字格式的数据在查询时自动转换为字符串格式
2. **保存新数据**：新保存的数据使用字符串格式
3. **向后兼容**：同时支持新旧两种格式的数据

### 修改的文件

- `MonitorSysGenServerSettingMapper.xml` - 添加了 CASE 语句进行数据转换
- 所有查询方法都已更新：
    - `selectByServerId`
    - `selectByServerIds`
    - `selectMonitorEnabledSettings`
    - `selectAlertEnabledSettings`
    - `selectReportEnabledSettings`

### 1. 兼容性处理（备选方案）

已在 `FileManagementMode` 枚举中添加了向后兼容性支持：

- `fromCode(String code)` 方法现在可以处理数字格式的输入
- `fromLegacyCode(int legacyCode)` 方法专门处理旧的数字格式
- `FileManagementModeTypeHandler` 支持数据库中的数字和字符串格式

### 2. 数据格式映射

| 旧格式（数字） | 新格式（字符串） | 说明      |
|---------|----------|---------|
| 0       | NONE     | 不启用文件管理 |
| 1       | SSH      | SSH 模式  |
| 2       | API      | API 模式  |
| 3       | NODE     | NODE 模式 |
| 4       | LOCAL    | 本地模式    |

## 迁移步骤

### 方式一：使用迁移接口（推荐）

1. **检查迁移状态**

   ```bash
   GET /v1/gen/server/setting/migration/status
   ```

2. **执行数据迁移**

   ```bash
   POST /v1/gen/server/setting/migration/execute
   ```

3. **验证迁移结果**
   ```bash
   GET /v1/gen/server/setting/migration/status
   ```

### 方式二：手动执行 SQL

如果需要手动执行迁移，可以运行以下 SQL：

```sql
-- 更新数字格式为字符串格式
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_file_management_mode =
    CASE
        WHEN monitor_sys_gen_server_setting_file_management_mode = '0' THEN 'NONE'
        WHEN monitor_sys_gen_server_setting_file_management_mode = '1' THEN 'SSH'
        WHEN monitor_sys_gen_server_setting_file_management_mode = '2' THEN 'API'
        WHEN monitor_sys_gen_server_setting_file_management_mode = '3' THEN 'NODE'
        WHEN monitor_sys_gen_server_setting_file_management_mode = '4' THEN 'LOCAL'
        ELSE 'NONE'
    END
WHERE monitor_sys_gen_server_setting_file_management_mode IN ('0', '1', '2', '3', '4')
   OR monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$';

-- 处理 NULL 值
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_file_management_mode = 'NONE'
WHERE monitor_sys_gen_server_setting_file_management_mode IS NULL;
```

## 验证迁移结果

执行以下查询来验证迁移是否成功：

```sql
-- 查看数据分布
SELECT
    monitor_sys_gen_server_setting_file_management_mode,
    COUNT(*) as count
FROM monitor_sys_gen_server_setting
GROUP BY monitor_sys_gen_server_setting_file_management_mode
ORDER BY monitor_sys_gen_server_setting_file_management_mode;

-- 检查是否还有数字格式的数据
SELECT COUNT(*) as remaining_numeric_count
FROM monitor_sys_gen_server_setting
WHERE monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$'
   OR monitor_sys_gen_server_setting_file_management_mode IS NULL;
```

## 注意事项

1. **备份数据**：在执行迁移前，建议备份相关数据表
2. **维护窗口**：建议在维护窗口期间执行迁移
3. **事务保护**：迁移过程使用事务保护，确保数据一致性
4. **兼容性**：即使不执行迁移，系统也能正常工作（通过兼容性处理）

## 迁移接口详情

### 检查迁移状态

**接口**: `GET /v1/gen/server/setting/migration/status`

**响应示例**:

```json
{
  "code": "00000",
  "data": {
    "distribution": [
      {
        "monitor_sys_gen_server_setting_file_management_mode": "0",
        "count": 5
      },
      {
        "monitor_sys_gen_server_setting_file_management_mode": "NONE",
        "count": 10
      }
    ],
    "needMigrationCount": 5,
    "migrationNeeded": true
  }
}
```

### 执行迁移

**接口**: `POST /v1/gen/server/setting/migration/execute`

**响应示例**:

```json
{
  "code": "00000",
  "data": "数据迁移成功！更新了 5 行数字格式数据，0 行 NULL 值数据"
}
```

### 获取帮助信息

**接口**: `GET /v1/gen/server/setting/migration/help`

返回详细的迁移帮助信息和操作步骤。

## 故障排除

### 常见问题

1. **迁移后仍有错误**

    - 检查是否有新的数字格式数据写入
    - 验证应用程序是否使用了最新的代码

2. **迁移失败**

    - 检查数据库连接和权限
    - 查看应用程序日志获取详细错误信息

3. **数据不一致**
    - 重新执行迁移
    - 检查是否有并发的数据修改操作

### 回滚方案

如果需要回滚到数字格式（不推荐），可以执行：

```sql
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_file_management_mode =
    CASE
        WHEN monitor_sys_gen_server_setting_file_management_mode = 'NONE' THEN '0'
        WHEN monitor_sys_gen_server_setting_file_management_mode = 'SSH' THEN '1'
        WHEN monitor_sys_gen_server_setting_file_management_mode = 'API' THEN '2'
        WHEN monitor_sys_gen_server_setting_file_management_mode = 'NODE' THEN '3'
        WHEN monitor_sys_gen_server_setting_file_management_mode = 'LOCAL' THEN '4'
        ELSE '0'
    END
WHERE monitor_sys_gen_server_setting_file_management_mode IN ('NONE', 'SSH', 'API', 'NODE', 'LOCAL');
```

## 总结

通过这个迁移方案，可以安全地将旧的数字格式数据转换为新的字符串格式，同时保持系统的向后兼容性。建议尽快执行迁移以获得最佳的系统性能和数据一致性。
