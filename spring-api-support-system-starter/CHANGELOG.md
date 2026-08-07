# 更新日志

## [4.0.0.42] - 2026-08-07

### 🚀 新增功能

#### 成就系统模块

**新增成就模块** — 完整的用户成就系统，支持多分组段位成就体系。

- **多分组段位体系** — 支持 5 个分组（登录/签到/菜单/消息/头像框），每组最多 4 段等级
- **JSON 结构化规则引擎** — 支持 and/or 逻辑组合和 6 种数值比较算子（>=/>/<=/</==/!=）
- **KV 存储抽象层** — FileKvEngine（本地文件，开发环境）和 RedisKvEngine（生产环境）可切换
- **Spring Event 事件驱动** — 业务模块通过 `UserActionEvent` 上报行为，成就模块异步消费判定
- **异步事件监听** — `@Async` 不阻塞调用方，提升系统整体性能
- **REST API** — 提供已解锁列表、统计概览、段位定义、未解锁段位 4 个查询接口
- **成就推送服务** — 默认日志推送，可通过 WebSocket 扩展实时推送前端
- **数据表初始化脚本** — 18 条段位定义数据预置，开箱即用

#### 数据库变更

- **新增表 `achievement_tier`** — 成就段位定义表
- **新增表 `user_achievement`** — 用户成就记录表
- **新增 SQL 脚本** — `src/main/resources/sql/achievement.sql` + `db/migration/achievement_tables.sql`

#### 配置项

```yaml
kvstore:
  type: file
  file-dir: data/kvstore

plugin:
  system:
    server:
      enable: true
```

#### API 接口

```
GET /achievement/my/{userId}
GET /achievement/stats/{userId}
GET /achievement/tiers/{type}
GET /achievement/uncompleted/{userId}/{type}
```

#### 架构文档

- [成就系统架构图](docs/架构图.md)

### 🔧 模块结构变更

```
spring-api-support-system-starter/
└── src/main/java/com/chua/starter/system/
    └── achievement/                    # 新增成就子模块
        ├── config/
        │   ├── AchievementAutoConfiguration.java   # Bean 装配
        │   └── KvEngineConfig.java                 # KvEngine Bean 注册
        ├── controller/
        │   └── AchievementController.java          # REST API
        ├── dto/
        │   ├── UnlockedAchievementDTO.java         # 解锁成就响应
        │   └── UserAchievementStatsDTO.java        # 统计概览
        ├── event/
        │   └── UserActionEvent.java                # Spring 事件
        ├── kvstore/
        │   ├── KvEngine.java                       # 存储接口
        │   └── impl/
        │       └── FileKvEngine.java               # 文件实现
        ├── listener/
        │   └── AchievementEventListener.java       # 事件监听
        ├── mapper/
        │   ├── AchievementTierMapper.java + .xml
        │   └── UserAchievementMapper.java + .xml
        ├── model/
        │   ├── AchievementTier.java                # 段位定义实体
        │   └── UserAchievement.java                # 用户记录实体
        ├── properties/
        │   └── KvEngineProperties.java             # 配置属性
        └── service/
            ├── AchievementService.java             # 核心业务逻辑
            ├── AchievementPushService.java         # 推送服务
            └── RuleEngine.java                     # 规则判定引擎
```

### 🔧 系统应用配置更新

- **SystemApplication** — 新增 `@EnableAsync`、扩展 `@MapperScan` 扫描成就 mapper 包
- **pom.xml** — 新增 `utils-support-common-starter` 依赖
- **application-dev.yml** — 新增 `plugin.kvstore` 和 `plugin.system.server.enable` 配置
- **README.md** — 新增成就系统功能说明、模块结构、API 接口、架构文档链接

---

## [4.0.0.41] - 2024-12-11

### ✨ 基础系统管理功能

- 用户管理（增删改查/状态/密码/角色）
- 角色管理（权限/菜单关联/数据权限）
- 菜单管理（树形结构/权限控制/动态路由）
- 部门管理（树形结构/层级/用户关联）
- 静态登录联调模式
- 多租户同步架构
- Swagger API 文档

---

_更多历史版本信息请查看 Git 提交记录_
