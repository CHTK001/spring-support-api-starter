# DevOps 配置模块

## 目录结构

```
resources/
├── ops-default/                # 兜底配置（所有环境共用）
│   ├── application-actuator.yml
│   ├── application-mybatis.yml
│   ├── application-swagger.yml
│   └── application-jackson.yml
├── ops-dev/                    # dev 环境配置
│   ├── application-mybatis.yml
│   ├── application-jackson.yml
│   └── application-logging.yml
└── ops-prod/                   # prod 环境配置
    ├── application-actuator.yml
    ├── application-mybatis.yml
    ├── application-swagger.yml
    ├── application-jackson.yml
    └── application-logging.yml
```

## 配置加载机制

通过 `spring-support-common-starter` 的 `ProfileEnvironmentPostProcessor` 自动加载。

### 配置优先级（高 → 低）

```
业务 application.yml > 配置中心 > ops-{profile}/ > ops-default/
```

### 属性级合并

同名配置文件会进行属性级深度合并，而非文件级覆盖：

```yaml
# ops-default/application-mybatis.yml
mybatis:
  type-aliases-package: com.example
  mapper-locations: classpath:mapper/*.xml

# ops-dev/application-mybatis.yml
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# 合并结果
mybatis:
  type-aliases-package: com.example           # 来自 ops-default
  mapper-locations: classpath:mapper/*.xml    # 来自 ops-default
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 来自 ops-dev
```

## IDE 开发环境

业务模块依赖 devops-starter 后，IDEA 可直接运行：

```yaml
# 业务模块 application.yml
spring:
  profiles:
    active: dev
```

启动时自动加载：
- `ops-default/application-*.yml`（兜底）
- `ops-dev/application-*.yml`（合并覆盖）

## 开关配置

可通过以下配置禁用 OPS 配置加载：

```yaml
plugin:
  ops-config:
    enabled: false
```
