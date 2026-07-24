# spring-api-support-common-statistics-starter

请求统计

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-statistics-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 功能概览

| 类/接口 | 说明 |
|---------|------|
| `RequestStatisticsIngestionService` | RequestStatisticsIngestionService |
| `RequestStatisticsStorage` | RequestStatisticsStorage |
| `RequestStatisticsStorageRouter` | RequestStatisticsStorageRouter |
| `DatabaseRequestStatisticsStorage` | DatabaseRequestStatisticsStorage (SPI: `database`) |
| `DuckDbRequestStatisticsStorage` | DuckDbRequestStatisticsStorage (SPI: `duckdb`) |
| `MemoryRequestStatisticsStorage` | MemoryRequestStatisticsStorage (SPI: `memory`) |

---

## 配置说明

本模块为零配置模块，引入依赖后即可使用。

---

## 依赖关系

```
spring-api-support-common-statistics-starter
├── spring-api-support-common-configuration-starter
├── spring-api-support-common-entity-starter
├── spring-api-support-common-service-starter
├── spring-support-common-starter
├── spring-support-datasource-starter
├── spring-support-mybatis-starter
```