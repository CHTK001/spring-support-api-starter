# 服务器监控系统功能实现总结

## 概述

本次实现为服务器监控系统添加了以下核心功能：
1. 客户端上报配置（默认启用）
2. Guacamole代理支持
3. 多种数据上报方式（不支持上报、接口上报、Prometheus）
4. 服务器详情页面，支持可拖拽的监控组件

## 后端实现

### 1. 数据库扩展

#### 服务器表新增字段
- `monitor_sys_gen_server_report_enabled` - 是否需要上报（默认1）
- `monitor_sys_gen_server_proxy_type` - 代理类型（GUACAMOLE等）
- `monitor_sys_gen_server_data_report_method` - 数据上报方式
- `monitor_sys_gen_server_prometheus_host` - Prometheus服务器地址
- `monitor_sys_gen_server_prometheus_port` - Prometheus服务器端口
- `monitor_sys_gen_server_proxy_host` - 代理服务器地址
- `monitor_sys_gen_server_proxy_port` - 代理服务器端口

#### 新增组件配置表
- `monitor_sys_gen_server_detail_component` - 服务器详情页组件配置表
- 支持多种组件类型：卡片、仪表盘、折线图、柱状图、饼图、表格
- 支持Prometheus和SQL两种表达式类型
- 支持组件位置、样式配置

### 2. API接口扩展

#### 服务器管理接口
- `GET /v1/gen/server/proxy/guacamole` - 获取Guacamole代理URL
- `POST /v1/gen/server/proxy/test` - 测试代理连接
- `GET /v1/gen/server/report/methods` - 获取支持的上报方式
- `POST /v1/gen/server/query/data` - 根据配置查询数据
- `POST /v1/gen/server/report/config` - 更新上报配置
- `GET /v1/gen/server/report/config` - 获取上报配置

#### 组件管理接口
- `GET /v1/gen/server/detail/component/list` - 获取组件列表
- `POST /v1/gen/server/detail/component/save` - 保存组件
- `POST /v1/gen/server/detail/component/batch/position` - 批量更新位置
- `DELETE /v1/gen/server/detail/component/delete` - 删除组件
- `GET /v1/gen/server/detail/component/templates` - 获取组件模板
- `POST /v1/gen/server/detail/component/init` - 初始化默认组件

### 3. 实体类和服务

#### 核心实体
- `MonitorSysGenServer` - 扩展了新的配置字段
- `MonitorSysGenServerDetailComponent` - 组件配置实体

#### 服务层
- `MonitorSysGenServerDetailComponentService` - 组件管理服务
- 扩展了 `MonitorSysGenServerService` - 添加代理和上报相关方法

## 前端实现

### 1. 服务器编辑表单扩展

在 `ServerEditDialog.vue` 中添加了：
- 数据上报开关配置
- 上报方式选择（不支持上报/接口上报/Prometheus）
- Prometheus服务器配置（地址、端口）
- 代理类型选择（无代理/Guacamole）
- 代理服务器配置（地址、端口）

### 2. 服务器详情页面

#### 主要文件
- `src/views/server/detail.vue` - 详情页主页面
- `src/views/server/components/` - 组件目录

#### 核心功能
- 可拖拽的网格布局系统（基于grid-layout-plus）
- 支持6种组件类型：
  - CardComponent - 数值卡片
  - GaugeComponent - 仪表盘
  - LineChartComponent - 折线图
  - BarChartComponent - 柱状图
  - PieChartComponent - 饼图
  - TableComponent - 数据表格

#### 组件管理
- `ComponentEditDialog.vue` - 组件编辑对话框
- `ComponentManageDialog.vue` - 组件管理对话框
- `ExpressionHelpDialog.vue` - 表达式语法帮助
- `ConfigTemplateDialog.vue` - 配置模板选择

### 3. 路由配置

添加了服务器详情页路由：
```typescript
{
  path: "/server/detail/:id",
  name: "serverDetail",
  component: () => import("@/views/server/detail.vue"),
  meta: {
    icon: "ri:dashboard-3-line",
    title: "服务器详情页",
    showLink: false,
    showParent: true
  }
}
```

### 4. API集成

扩展了 `src/api/monitor/gen/server.ts`：
- 添加了新的接口类型定义
- 扩展了ServerDisplayData接口
- 添加了组件管理相关的API函数

## 功能特性

### 1. 配置灵活性
- 支持按服务器单独配置是否上报
- 支持多种数据源（API、Prometheus）
- 支持代理访问（Guacamole）

### 2. 可视化组件
- 拖拽式布局编辑
- 多种图表类型支持
- 实时数据刷新
- 响应式设计

### 3. 表达式支持
- Prometheus PromQL表达式
- SQL查询表达式
- 表达式验证功能
- 语法帮助文档

### 4. 用户体验
- 直观的编辑界面
- 丰富的配置模板
- 实时预览功能
- 移动端适配

## 技术栈

### 后端
- Spring Boot + MyBatis Plus
- MySQL数据库
- RESTful API设计

### 前端
- Vue 3 + TypeScript
- Element Plus UI框架
- ECharts图表库
- grid-layout-plus布局组件
- Pinia状态管理

## 部署说明

1. 执行数据库迁移脚本：
   - `V1.0.8__add_server_proxy_and_report_fields.sql`
   - `V1.0.9__create_server_detail_component_table.sql`

2. 重启后端服务以加载新的API接口

3. 前端无需额外配置，支持热更新

## 使用说明

1. 在服务器编辑页面配置上报方式和代理设置
2. 只有启用上报的服务器才会显示"详情页面"菜单项
3. 在详情页面可以添加、编辑、移动监控组件
4. 支持从模板快速创建组件
5. 组件支持实时数据刷新和交互操作

## 后续扩展

1. 添加更多组件类型（地图、热力图等）
2. 支持组件间数据联动
3. 添加告警规则配置
4. 支持自定义主题样式
5. 添加数据导出功能
