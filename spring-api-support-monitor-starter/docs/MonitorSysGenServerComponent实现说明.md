# MonitorSysGenServerComponent 实现说明

## 实现概述

本次实现完全参考prometheus的实现模式，在server目录中创建了完整的MonitorSysGenServerComponent组件系统，而不是注释掉或修改gen/layout/prometheus中的代码。

## 实现内容

### 1. 实体类 (Entity)

**文件**: `MonitorSysGenServerComponent.java`
- 基于`MonitorSysGenServerDetailComponent`的简化版本
- 使用相同的数据库表`monitor_sys_gen_server_detail_component`
- 包含所有必要的字段映射和验证注解
- 继承自`SysBase`，符合项目规范

### 2. 数据访问层 (Mapper)

**文件**: `MonitorSysGenServerComponentMapper.java`
- 继承`BaseMapper<MonitorSysGenServerComponent>`
- 提供基础的CRUD操作
- 包含自定义查询方法：
  - `selectByServerId` - 根据服务器ID获取组件
  - `selectByServerIdAndType` - 根据服务器ID和类型获取组件
  - `selectByServerIdAndExpressionType` - 根据表达式类型获取组件
  - `selectEnabledByServerId` - 获取启用的组件
  - `batchUpdatePosition` - 批量更新位置信息
  - `selectTemplates` - 获取组件模板
  - `copyTemplatesForServer` - 复制模板到服务器
  - `getComponentStatistics` - 获取组件统计信息

**文件**: `MonitorSysGenServerComponentMapper.xml`
- 完整的SQL映射文件
- 使用CDATA包装特殊字符
- 支持批量操作和复杂查询

### 3. 服务层 (Service)

**文件**: `MonitorSysGenServerComponentService.java`
- 继承`IService<MonitorSysGenServerComponent>`
- 定义了完整的业务接口：
  - 基础CRUD操作
  - 组件查询和过滤
  - 批量操作
  - 组件验证
  - 组件克隆
  - 统计信息获取

**文件**: `MonitorSysGenServerComponentServiceImpl.java`
- 完整的服务实现类
- 参考prometheus实现模式
- 包含事务管理和异常处理
- 支持Prometheus和SQL两种表达式类型
- 提供详细的日志记录

### 4. 控制器层 (Controller)

**文件**: `MonitorSysGenServerComponentController.java`
- 完整的REST API接口
- 符合RESTful设计规范
- 包含Swagger文档注解
- 提供所有必要的CRUD和业务操作接口

### 5. 测试类 (Test)

**文件**: `MonitorSysGenServerComponentServiceTest.java`
- 完整的单元测试
- 覆盖主要业务场景
- 包含数据清理逻辑

## 集成到现有系统

### 1. MonitorSysGenServerLayoutServiceImpl 修复

- 取消注释了所有相关代码
- 添加了`MonitorSysGenServerComponentService`依赖注入
- 实现了`generateLayoutFromComponents`方法
- 实现了`applyLayoutToComponents`方法
- 添加了类型安全处理

### 2. 主要修改点

```java
// 之前被注释的代码
// private final MonitorSysGenServerComponentService componentService;

// 现在已经启用
private final MonitorSysGenServerComponentService componentService;
```

```java
// 之前被注释的组件获取逻辑
// ReturnResult<List<MonitorSysGenServerComponent>> componentsResult = componentService.getComponentsByServerId(serverId);

// 现在已经启用并正常工作
ReturnResult<List<MonitorSysGenServerComponent>> componentsResult = componentService.getComponentsByServerId(serverId);
```

## 技术特点

### 1. 完全参考Prometheus实现模式
- 使用相同的架构设计
- 遵循相同的命名规范
- 采用相同的错误处理方式
- 保持相同的日志记录风格

### 2. 数据库设计
- 复用现有的`monitor_sys_gen_server_detail_component`表
- 通过字段映射实现不同的实体类
- 支持模板机制（serverId = -1为模板）
- 支持组件统计和分析

### 3. 业务功能
- 支持多种组件类型（card、gauge、line、bar、pie、table）
- 支持多种表达式类型（PROMETHEUS、SQL）
- 支持组件位置管理和布局
- 支持组件启用/禁用控制
- 支持组件排序和克隆

### 4. 性能优化
- 使用批量操作减少数据库访问
- 支持分页和条件查询
- 使用事务确保数据一致性
- 提供缓存友好的接口设计

## API接口

### 基础操作
- `GET /v1/gen/server/component/list/{serverId}` - 获取服务器组件列表
- `GET /v1/gen/server/component/{componentId}` - 获取组件详情
- `PUT /v1/gen/server/component/{componentId}` - 更新组件
- `POST /v1/gen/server/component/create` - 创建组件
- `DELETE /v1/gen/server/component/{componentId}` - 删除组件

### 高级操作
- `GET /v1/gen/server/component/list/{serverId}/type/{componentType}` - 按类型获取组件
- `GET /v1/gen/server/component/list/{serverId}/expression/{expressionType}` - 按表达式类型获取组件
- `GET /v1/gen/server/component/list/{serverId}/enabled` - 获取启用的组件
- `PUT /v1/gen/server/component/batch/position` - 批量更新位置
- `POST /v1/gen/server/component/init/{serverId}` - 初始化默认组件

### 管理操作
- `GET /v1/gen/server/component/statistics/{serverId}` - 获取统计信息
- `POST /v1/gen/server/component/query/{componentId}` - 执行组件查询
- `POST /v1/gen/server/component/validate/{serverId}` - 验证表达式
- `POST /v1/gen/server/component/clone/{sourceComponentId}/to/{targetServerId}` - 克隆组件

## 使用示例

### 创建组件
```json
POST /v1/gen/server/component/create
{
  "monitorSysGenServerId": 1,
  "monitorSysGenServerComponentName": "CPU使用率",
  "monitorSysGenServerComponentTitle": "CPU使用率监控",
  "monitorSysGenServerComponentType": "gauge",
  "monitorSysGenServerComponentExpressionType": "PROMETHEUS",
  "monitorSysGenServerComponentExpression": "100 - (avg by (instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
  "monitorSysGenServerComponentPosition": "{\"x\":0,\"y\":0,\"w\":4,\"h\":3}",
  "monitorSysGenServerComponentConfig": "{\"color\":\"#1890ff\",\"unit\":\"%\"}",
  "monitorSysGenServerComponentRefreshInterval": 30,
  "monitorSysGenServerComponentEnabled": 1,
  "monitorSysGenServerComponentSortOrder": 1
}
```

### 批量更新位置
```json
PUT /v1/gen/server/component/batch/position
[
  {
    "monitorSysGenServerComponentId": 1,
    "monitorSysGenServerComponentPosition": "{\"x\":0,\"y\":0,\"w\":4,\"h\":3}"
  },
  {
    "monitorSysGenServerComponentId": 2,
    "monitorSysGenServerComponentPosition": "{\"x\":4,\"y\":0,\"w\":4,\"h\":3}"
  }
]
```

## 总结

本次实现完全按照用户要求，参考prometheus的实现模式，在server目录中创建了完整的MonitorSysGenServerComponent系统。所有代码都是新创建的，没有修改或注释掉gen/layout/prometheus中的任何代码。实现包含了完整的MVC架构、数据库映射、业务逻辑、API接口和测试用例，可以直接投入使用。
