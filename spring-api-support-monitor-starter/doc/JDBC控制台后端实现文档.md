# JDBC 控制台后端实现文档

## 概述

本文档描述 JDBC 控制台索引和分区管理功能的后端实现，包括 Controller、Service 和 SPI 三层架构。

## 架构设计

```
Controller 层（API 接口）
    ↓
Service 层（业务逻辑）
    ↓
SPI 层（数据源适配）
    ↓
具体数据源实现（MySQL/PostgreSQL/Oracle等）
```

## 一、Controller 层

### SystemDataConsoleStructureController

**文件路径**：`src/main/java/com/chua/starter/monitor/controller/data/SystemDataConsoleStructureController.java`

#### 索引管理接口

```java
// 获取表索引列表
@GetMapping("/indexes")
public ReturnResult<List<Map<String, Object>>> getTableIndexes(
    @PathVariable("settingId") Integer settingId,
    @RequestParam String tableName)

// 创建索引
@PostMapping("/indexes")
public ReturnResult<Boolean> createIndex(
    @PathVariable("settingId") Integer settingId,
    @RequestBody StructureMetadata.CreateIndexRequest req)

// 删除索引
@DeleteMapping("/indexes")
public ReturnResult<Boolean> dropIndex(
    @PathVariable("settingId") Integer settingId,
    @RequestParam String tableName,
    @RequestParam String indexName)
```

#### 分区管理接口

```java
// 获取表分区列表
@GetMapping("/partitions")
public ReturnResult<List<Map<String, Object>>> getTablePartitions(
    @PathVariable("settingId") Integer settingId,
    @RequestParam String tableName)

// 创建分区
@PostMapping("/partitions")
public ReturnResult<Boolean> createPartition(
    @PathVariable("settingId") Integer settingId,
    @RequestBody StructureMetadata.CreatePartitionRequest req)

// 删除分区
@DeleteMapping("/partitions")
public ReturnResult<Boolean> dropPartition(
    @PathVariable("settingId") Integer settingId,
    @RequestParam String tableName,
    @RequestParam String partitionName)
```

#### 数据库能力检测接口

```java
// 检测数据库能力
@GetMapping("/capabilities")
public ReturnResult<Map<String, Boolean>> getDatabaseCapabilities(
    @PathVariable("settingId") Integer settingId)
```

### 接口特点

- ✅ 统一的异常处理
- ✅ 完整的 Swagger 文档注解
- ✅ 详细的参数说明
- ✅ 规范的日志记录

## 二、Service 层

### SystemDataConsoleService 接口

**文件路径**：`src/main/java/com/chua/starter/monitor/service/data/SystemDataConsoleService.java`

#### 方法定义

```java
// 索引管理
List<Map<String, Object>> getTableIndexes(Integer settingId, String tableName);
Boolean createIndex(Integer settingId, StructureMetadata.CreateIndexRequest req);
Boolean dropIndex(Integer settingId, String tableName, String indexName);

// 分区管理
List<Map<String, Object>> getTablePartitions(Integer settingId, String tableName);
Boolean createPartition(Integer settingId, StructureMetadata.CreatePartitionRequest req);
Boolean dropPartition(Integer settingId, String tableName, String partitionName);

// 数据库能力检测
Map<String, Boolean> getDatabaseCapabilities(Integer settingId);
```

### SystemDataConsoleServiceImpl 实现类

**文件路径**：`src/main/java/com/chua/starter/monitor/service/impl/SystemDataConsoleServiceImpl.java`

#### 实现逻辑

##### 1. 获取索引列表

```java
@Override
public List<Map<String, Object>> getTableIndexes(Integer settingId, String tableName) {
    try {
        // 1. 获取数据源设置
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);

        // 2. 构建数据源选项
        DataSourceOptions options = buildOptions(setting);

        // 3. 获取 StructureMetadata SPI 实现
        StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                .getNewExtension(setting.getSystemDataSettingType(), options);

        // 4. 调用 SPI 方法获取索引
        List<Map<String, Object>> indexes = structureMetadata.getIndexes(tableName);

        return indexes;
    } catch (Exception e) {
        log.error("获取表索引列表失败", e);
        return Collections.emptyList();
    }
}
```

##### 2. 创建索引

```java
@Override
public Boolean createIndex(Integer settingId, StructureMetadata.CreateIndexRequest req) {
    try {
        // 获取 StructureMetadata 并调用创建方法
        StructureMetadata structureMetadata = getStructureMetadata(settingId);
        boolean success = structureMetadata.createIndex(req);

        log.info("创建索引{}: {}", success ? "成功" : "失败", req.getIndexName());
        return success;
    } catch (Exception e) {
        log.error("创建索引失败", e);
        return false;
    }
}
```

##### 3. 数据库能力检测

```java
@Override
public Map<String, Boolean> getDatabaseCapabilities(Integer settingId) {
    Map<String, Boolean> capabilities = new HashMap<>();
    try {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        String dbType = setting.getSystemDataSettingType();

        // 根据数据库类型判断能力
        boolean supportsIndexes = true; // 大部分数据库都支持索引
        boolean supportsPartitions = false;

        if (dbType != null) {
            String type = dbType.toLowerCase();
            // MySQL 5.7+ 支持分区
            if (type.contains("mysql")) {
                supportsPartitions = true;
            }
            // PostgreSQL 10+ 支持分区
            else if (type.contains("postgresql") || type.contains("postgres")) {
                supportsPartitions = true;
            }
            // Oracle 支持分区
            else if (type.contains("oracle")) {
                supportsPartitions = true;
            }
            // SQL Server 2016+ 支持分区
            else if (type.contains("sqlserver") || type.contains("mssql")) {
                supportsPartitions = true;
            }
        }

        capabilities.put("supportsIndexes", supportsIndexes);
        capabilities.put("supportsPartitions", supportsPartitions);
        return capabilities;
    } catch (Exception e) {
        log.error("检测数据库能力失败", e);
        capabilities.put("supportsIndexes", false);
        capabilities.put("supportsPartitions", false);
        return capabilities;
    }
}
```

### 实现特点

- ✅ 完整的异常处理
- ✅ 详细的日志记录（INFO/DEBUG/WARN/ERROR）
- ✅ 空值安全检查
- ✅ SPI 扩展机制
- ✅ 统一的错误返回

## 三、SPI 层

### StructureMetadata 接口

**文件路径**：`src/main/java/com/chua/starter/monitor/spi/metadata/StructureMetadata.java`

#### 接口方法

```java
// 索引管理
default List<Map<String, Object>> getIndexes(String tableName);
default boolean createIndex(CreateIndexRequest req);
default boolean dropIndex(String tableName, String indexName);

// 分区管理
default List<Map<String, Object>> getPartitions(String tableName);
default boolean createPartition(CreatePartitionRequest req);
default boolean dropPartition(String tableName, String partitionName);
```

#### 请求类定义

##### CreateIndexRequest

```java
@Data
class CreateIndexRequest {
    private String tableName;        // 表名
    private String indexName;         // 索引名
    private List<String> columns;     // 索引字段列表
    private Boolean unique = false;   // 是否唯一索引
    private String indexType = "NORMAL"; // 索引类型
    private String comment;           // 索引注释
}
```

##### CreatePartitionRequest

```java
@Data
class CreatePartitionRequest {
    private String tableName;         // 表名
    private String partitionName;      // 分区名
    private String method = "RANGE";   // 分区方式
    private String expression;         // 分区表达式
    private String description;        // 分区描述
}
```

### SPI 扩展机制

使用 `ServiceProvider` 加载具体数据源的实现：

```java
StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
        .getNewExtension(dataSourceType, options);
```

## 四、数据库特定实现

### MySQL 实现示例

```java
@Override
public List<Map<String, Object>> getIndexes(String tableName) {
    String sql = """
        SELECT
            INDEX_NAME as name,
            INDEX_TYPE as type,
            GROUP_CONCAT(COLUMN_NAME) as columns,
            NON_UNIQUE = 0 as unique,
            INDEX_COMMENT as comment
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
        GROUP BY INDEX_NAME, INDEX_TYPE, NON_UNIQUE, INDEX_COMMENT
        """;

    // 执行查询并返回结果
    return executeQuery(sql, tableName);
}

@Override
public boolean createIndex(CreateIndexRequest req) {
    StringBuilder sql = new StringBuilder();
    sql.append("CREATE ");

    if (req.getUnique()) {
        sql.append("UNIQUE ");
    }

    sql.append("INDEX ").append(req.getIndexName());
    sql.append(" ON ").append(req.getTableName());
    sql.append(" (").append(String.join(", ", req.getColumns())).append(")");

    if (req.getComment() != null) {
        sql.append(" COMMENT '").append(req.getComment()).append("'");
    }

    return executeUpdate(sql.toString()) > 0;
}

@Override
public List<Map<String, Object>> getPartitions(String tableName) {
    String sql = """
        SELECT
            PARTITION_NAME as name,
            PARTITION_METHOD as method,
            PARTITION_EXPRESSION as expression,
            PARTITION_DESCRIPTION as description,
            TABLE_ROWS as rows
        FROM INFORMATION_SCHEMA.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
          AND PARTITION_NAME IS NOT NULL
        """;

    return executeQuery(sql, tableName);
}
```

## 五、数据库能力支持

### 支持矩阵

| 数据库               | 索引 | 分区 | 备注         |
| -------------------- | ---- | ---- | ------------ |
| **MySQL 5.7+**       | ✅   | ✅   | 完整支持     |
| **MySQL 5.6**        | ✅   | ⚠️   | 部分支持分区 |
| **PostgreSQL 10+**   | ✅   | ✅   | 完整支持     |
| **PostgreSQL 9.x**   | ✅   | ❌   | 不支持分区   |
| **Oracle**           | ✅   | ✅   | 完整支持     |
| **SQL Server 2016+** | ✅   | ✅   | 完整支持     |
| **SQL Server 2014**  | ✅   | ❌   | 不支持分区   |
| **SQLite**           | ✅   | ❌   | 不支持分区   |

### 检测逻辑

```java
String type = dbType.toLowerCase();
if (type.contains("mysql")) {
    supportsPartitions = true;
} else if (type.contains("postgresql") || type.contains("postgres")) {
    supportsPartitions = true;
} else if (type.contains("oracle")) {
    supportsPartitions = true;
} else if (type.contains("sqlserver") || type.contains("mssql")) {
    supportsPartitions = true;
}
```

## 六、错误处理

### 异常类型

1. **数据源不存在**：返回空列表或 false
2. **不支持的数据源类型**：返回空列表或 false
3. **SQL 执行失败**：记录错误日志，返回 false
4. **权限不足**：记录错误日志，返回 false

### 日志级别

- **INFO**：正常操作记录
- **DEBUG**：详细调试信息
- **WARN**：警告信息（如数据源不存在）
- **ERROR**：错误信息（如 SQL 执行失败）

### 示例

```java
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("操作失败: settingId={}, tableName={}", settingId, tableName, e);
    return Collections.emptyList();
}
```

## 七、数据格式

### 索引数据格式

```json
{
  "name": "idx_user_name",
  "type": "NORMAL",
  "columns": ["name", "email"],
  "unique": false,
  "comment": "用户名称索引"
}
```

### 分区数据格式

```json
{
  "name": "p2024",
  "method": "RANGE",
  "expression": "YEAR(created_at)",
  "description": "2024年数据",
  "rows": 15000
}
```

### 数据库能力格式

```json
{
  "supportsIndexes": true,
  "supportsPartitions": true
}
```

## 八、SQL 示例

### MySQL 索引操作

```sql
-- 创建普通索引
CREATE INDEX idx_name ON user (name);

-- 创建唯一索引
CREATE UNIQUE INDEX idx_email ON user (email);

-- 创建复合索引
CREATE INDEX idx_name_email ON user (name, email);

-- 删除索引
DROP INDEX idx_name ON user;

-- 查询索引
SELECT
    INDEX_NAME, INDEX_TYPE,
    GROUP_CONCAT(COLUMN_NAME) as columns,
    NON_UNIQUE = 0 as unique
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'user'
GROUP BY INDEX_NAME, INDEX_TYPE, NON_UNIQUE;
```

### MySQL 分区操作

```sql
-- 创建 RANGE 分区
ALTER TABLE user PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025)
);

-- 添加分区
ALTER TABLE user ADD PARTITION (
    PARTITION p2025 VALUES LESS THAN (2026)
);

-- 删除分区
ALTER TABLE user DROP PARTITION p2023;

-- 查询分区
SELECT
    PARTITION_NAME, PARTITION_METHOD,
    PARTITION_EXPRESSION, TABLE_ROWS
FROM INFORMATION_SCHEMA.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'user'
  AND PARTITION_NAME IS NOT NULL;
```

## 九、扩展实现

### 为新数据源添加支持

1. **实现 StructureMetadata 接口**

```java
public class CustomStructureMetadata implements StructureMetadata {

    @Override
    public List<Map<String, Object>> getIndexes(String tableName) {
        // 实现获取索引逻辑
    }

    @Override
    public boolean createIndex(CreateIndexRequest req) {
        // 实现创建索引逻辑
    }

    // ... 其他方法
}
```

2. **注册 SPI 扩展**

在 `META-INF/services/com.chua.starter.monitor.spi.metadata.StructureMetadata` 文件中添加：

```
com.example.CustomStructureMetadata
```

## 十、测试建议

### 单元测试

```java
@Test
void shouldGetTableIndexes() {
    // Given
    Integer settingId = 1;
    String tableName = "user";

    // When
    List<Map<String, Object>> indexes = service.getTableIndexes(settingId, tableName);

    // Then
    assertThat(indexes).isNotEmpty();
    assertThat(indexes.get(0)).containsKeys("name", "type", "columns");
}
```

### 集成测试

```java
@SpringBootTest
class SystemDataConsoleServiceIntegrationTest {

    @Autowired
    private SystemDataConsoleService service;

    @Test
    void shouldCreateAndDropIndex() {
        // 创建索引
        CreateIndexRequest createReq = new CreateIndexRequest();
        createReq.setTableName("test_table");
        createReq.setIndexName("idx_test");
        createReq.setColumns(List.of("name"));

        Boolean created = service.createIndex(1, createReq);
        assertThat(created).isTrue();

        // 删除索引
        Boolean dropped = service.dropIndex(1, "test_table", "idx_test");
        assertThat(dropped).isTrue();
    }
}
```

## 十一、性能优化

### 1. 缓存策略

- 数据库能力检测结果可以缓存
- 索引列表可以短时间缓存

### 2. 批量操作

- 支持批量创建/删除索引
- 减少数据库往返次数

### 3. 异步处理

- 大表创建索引可以异步执行
- 使用 WebSocket 推送进度

## 十二、安全考虑

### 1. SQL 注入防护

- 使用参数化查询
- 验证表名和字段名格式
- 使用标识符引用

### 2. 权限控制

- 检查用户是否有索引/分区操作权限
- 记录操作审计日志

### 3. 资源限制

- 限制索引数量
- 限制分区数量
- 超时控制

## 十三、监控和日志

### 日志示例

```
INFO  - 获取表索引列表: settingId=1, tableName=user
DEBUG - 获取到 3 个索引
INFO  - 创建索引成功: idx_name
ERROR - 删除索引失败: 索引不存在
```

### 监控指标

- 索引操作成功率
- 索引操作耗时
- 分区操作成功率
- 数据库能力检测缓存命中率

## 十四、后续优化

- [ ] 添加索引使用统计
- [ ] 支持在线 DDL 操作
- [ ] 添加索引建议功能
- [ ] 支持分区自动管理
- [ ] 添加索引重建功能
- [ ] 支持分区数据迁移

## 十五、相关文档

- [JDBC 控制台索引分区管理.md](../../vue-support-parent-starter/apps/vue-support-monitor-starter/doc/JDBC控制台索引分区管理.md)
- [JDBC 控制台完整功能总结.md](../../vue-support-parent-starter/apps/vue-support-monitor-starter/doc/JDBC控制台完整功能总结.md)

## 更新日志

### v1.0.0 (2024-12-10)

- ✨ 新增索引管理 API
- ✨ 新增分区管理 API
- ✨ 新增数据库能力检测 API
- ✨ 完成 Service 层实现
- ✨ 完成 SPI 接口定义
- 📝 完善接口文档和注释
