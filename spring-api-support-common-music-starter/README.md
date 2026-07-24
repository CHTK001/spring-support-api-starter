# spring-api-support-common-music-starter

音乐播放器服务

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-music-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 功能概览

| 类/接口 | 说明 |
|---------|------|
| `MusicConfiguration` | 音乐模块自动扫描配置。 |
| `MusicController` | MusicController |
| `MusicFacadeSearchProvider` | 音乐搜索 SPI 提供者。 实现 ，委托 执行搜索， 将结构化的 格式化为文本条目。 Spring 启动时自动注册到 SPI 系统，供 聚合。 |
| `MusicFacadeService` | MusicFacadeService |
| `MusicSourceAvailabilityService` | MusicSourceAvailabilityService |
| `MusicSourceRegistry` | 音源 SPI 注册中心。 |
| `MusicStreamProxyService` | 将外部音频资源统一转成当前服务的 HTTP 流。 |

---

## 配置说明

本模块为零配置模块，引入依赖后即可使用。

---

## 依赖关系

```
spring-api-support-common-music-starter
├── spring-support-common-starter
├── utils-support-protocol-starter
├── spring-support-swagger-starter
```