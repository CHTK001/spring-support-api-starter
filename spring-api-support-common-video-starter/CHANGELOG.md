# 更新日志

本文档记录了 Spring API Support Common Video Starter 的所有重要更改。

## [1.1.0] - 2024-12-19

### 🆕 新增功能

#### PanSou 网盘搜索集成

- **PanSouResourceProvider**
  ：新增基于 [PanSou 项目](https://github.com/fish2018/pansou) <mcreference link="https://github.com/fish2018/pansou" index="0">
  0</mcreference> 的网盘资源搜索提供者
- **PanSouVideoSourceProvider**：新增网盘视频源提供者，支持从网盘获取视频资源
- **PanResource**：新增网盘资源实体类，用于封装网盘搜索结果
- **PanType**：新增网盘类型枚举，支持12种主流网盘类型
- **PanSouConfig**：新增 PanSou API 配置类，支持灵活的配置管理

#### 支持的网盘类型

- 百度网盘 (baidu)
- 阿里云盘 (aliyun)
- 夸克网盘 (quark)
- 天翼云盘 (tianyi)
- UC网盘 (uc)
- 移动云盘 (mobile)
- 115网盘 (115)
- PikPak (pikpak)
- 迅雷网盘 (xunlei)
- 123网盘 (123)
- 磁力链接 (magnet)
- 电驴链接 (ed2k)

### ✨ 功能特性

#### 高性能搜索

- 并发执行多个TG频道及异步插件搜索 <mcreference link="https://github.com/fish2018/pansou" index="0">0</mcreference>
- 支持异步搜索模式，快速响应用户请求
- 智能超时控制和重试机制

#### 智能分类和排序

- 自动识别多种网盘链接类型 <mcreference link="https://github.com/fish2018/pansou" index="0">0</mcreference>
- 基于插件等级、时间新鲜度和关键词的多维度排序 <mcreference link="https://github.com/fish2018/pansou" index="0">
  0</mcreference>
- 视频文件类型智能识别和过滤

#### 缓存机制

- 二级缓存系统：分片内存+分片磁盘缓存 <mcreference link="https://github.com/fish2018/pansou" index="0">0</mcreference>
- 可配置的缓存有效期和大小限制
- 大幅提升重复查询速度和并发性能

#### 插件系统

- 异步插件架构，支持扩展搜索来源 <mcreference link="https://github.com/fish2018/pansou" index="0">0</mcreference>
- 支持多种搜索插件：labi、zhizhen、shandian、duoduo、muou、wanou 等
- 可配置启用/禁用特定插件

### 🔧 配置增强

#### PanSou 配置项

```yaml
pansou:
  base-url: https://so.252035.xyz          # API 基础URL
  search-endpoint: /api/search             # 搜索端点
  connect-timeout: 10000                   # 连接超时（毫秒）
  read-timeout: 30000                      # 读取超时（毫秒）
  max-retries: 3                           # 最大重试次数
  page-size: 20                            # 每页结果数
  max-results: 100                         # 最大结果数
  enabled-plugins: [labi, zhizhen, ...]    # 启用的插件
  channels: [tgsearchers3, ...]            # TG频道列表
  async-enabled: true                      # 异步搜索
  cache-enabled: true                      # 缓存开关
  cache-ttl: 60                           # 缓存有效期（分钟）
```

### 📚 API 接口

#### ResourceProvider 接口实现

- `searchResource(String keyword)`：搜索网盘资源
- `getProviderName()`：获取提供者名称 "PanSou网盘搜索"
- `getPriority()`：获取优先级（1 - 高优先级）
- `isAvailable()`：检查服务可用性

#### VideoSourceProvider 接口实现

- `fetchVideos(VideoSyncConfig config, Consumer<VideoInfo> consumer)`：获取视频资源
- `getDownloadUrls(VideoInfo videoInfo, VideoSyncConfig config)`：获取下载地址
- `getProviderName()`：获取提供者名称 "pansou"

### 🛠️ 技术实现

#### 核心类结构

```
com.chua.starter.video.provider.impl
├── PanSouResourceProvider.java      # 资源搜索提供者
└── PanSouVideoSourceProvider.java   # 视频源提供者

com.chua.starter.video.entity
└── PanResource.java                  # 网盘资源实体

com.chua.starter.video.enums
└── PanType.java                      # 网盘类型枚举

com.chua.starter.video.config
└── PanSouConfig.java                 # 配置类
```

#### 关键特性实现

- **网盘类型识别**：基于URL模式匹配和API返回类型
- **视频文件过滤**：支持常见视频格式识别（mp4、avi、mkv等）
- **异常处理**：完善的错误处理和日志记录
- **性能优化**：连接池复用、请求超时控制

### 📖 文档更新

- 新增完整的 README.md 文档
- 详细的配置说明和使用示例
- API 接口文档和参数说明
- 实体类字段说明

### 🔍 代码质量

- 遵循阿里巴巴 Java 开发规范
- 完整的方法注释和类注释
- 使用 Lombok 简化代码
- 完善的异常处理和日志记录

### 🧪 测试和验证

- 支持服务可用性检查
- 配置有效性验证
- 网络连接状态检测

---

## [1.0.0] - 2024-06-21

### 🆕 初始版本

#### 基础功能

- 视频信息管理
- 视频下载管理
- 视频关键词管理
- 视频标记管理
- 视频同步配置管理

#### 资源提供者

- BilibiliResourceProvider：B站资源搜索
- DoubanResourceProvider：豆瓣资源搜索
- YoukuResourceProvider：优酷资源搜索

#### 视频源提供者

- GuanYingAcSourceProvider：观影AC源
- GuanYingMvSourceProvider：观影MV源
- GuanYingTvSourceProvider：观影TV源

#### 核心实体

- VideoInfo：视频信息实体
- VideoDownload：视频下载实体
- VideoKeyword：视频关键词实体
- VideoMark：视频标记实体
- VideoSyncConfig：视频同步配置实体

#### 服务层

- VideoInfoService：视频信息服务
- VideoDownloadService：视频下载服务
- VideoKeywordService：视频关键词服务
- VideoMarkService：视频标记服务
- VideoSyncConfigService：视频同步配置服务
- VideoOnlineService：视频在线服务
- VideoSyncTaskService：视频同步任务服务
- ResourceSearchService：资源搜索服务
- ResourceSearchManager：资源搜索管理器

#### 控制器

- VideoInfoController：视频信息控制器
- VideoDownloadController：视频下载控制器
- VideoKeywordController：视频关键词控制器
- VideoOnlineController：视频在线控制器
- VideoSyncConfigController：视频同步配置控制器

#### 定时任务

- VideoSyncScheduler：视频同步调度器

---

## 版本说明

### 版本号规则

本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/) 规范：

- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

### 更新类型说明

- 🆕 **新增功能**：全新的功能特性
- ✨ **功能增强**：现有功能的改进和优化
- 🔧 **配置变更**：配置项的新增或修改
- 🐛 **问题修复**：Bug 修复
- 📚 **文档更新**：文档的新增或修改
- 🔍 **代码质量**：代码重构、性能优化等
- ⚠️ **破坏性变更**：不向下兼容的修改
- 🗑️ **废弃功能**：标记为废弃的功能

### 兼容性说明

- v1.1.0 完全向下兼容 v1.0.0
- 新增的 PanSou 功能为可选功能，不影响现有功能
- 配置项均为新增，无破坏性变更

### 升级指南

从 v1.0.0 升级到 v1.1.0：

1. 更新依赖版本
2. 添加 PanSou 配置（可选）
3. 重启应用即可使用新功能

无需修改现有代码，所有现有功能保持不变。