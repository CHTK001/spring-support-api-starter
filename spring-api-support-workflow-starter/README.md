# spring-api-support-workflow-starter

轻量级审批工作流引擎 — 支持动态节点配置(ScFlowEditor)、幂等性、部门/角色/人员审批分配

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-workflow-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 功能概览

| 类/接口 | 说明 |
|---------|------|
| `WorkflowAutoConfiguration` | 工作流模块自动配置 |
| `WfDefController` | 流程定义 Controller |
| `WfInstController` | 流程实例 Controller |
| `WfTaskController` | 流程任务 Controller |
| `WfDefQuery` | 流程定义查询 |
| `WfDefVO` | 流程定义视图(含节点和边) |
| `WfInstCreateRequest` | 创建流程实例请求 |
| `WfInstVO` | 流程实例视图(含任务和历史) |
| `WfTaskProcessRequest` | 处理任务请求 |
| `AssigneeUser` | 审批人视图对象（含用户ID和姓名） |
| ... | 共 37 个类 |

---

## 配置说明

本模块为零配置模块，引入依赖后即可使用。

---

## 依赖关系

```
spring-api-support-workflow-starter
├── spring-api-support-common-entity-starter
├── spring-api-support-common-service-starter
├── spring-support-mybatis-starter
├── spring-support-swagger-starter
├── utils-support-common-starter
```