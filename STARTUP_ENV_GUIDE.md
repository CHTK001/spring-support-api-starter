# startup.sh 环境参数使用指南

## 概述

所有模块的 `startup.sh` 脚本现已支持通过命令行参数指定 Spring Boot 运行环境。

## 支持的模块

- ✅ spring-api-support-monitor-starter
- ✅ spring-api-support-system-starter
- ✅ spring-api-support-oauth-starter
- ✅ spring-api-support-tenanted-starter

## 环境参数

### 可用选项

| 参数               | 环境   | 说明                                          |
| ------------------ | ------ | --------------------------------------------- |
| `--dev`            | dev    | 开发环境                                      |
| `--prod`           | prod   | 生产环境（默认）                              |
| `--test`           | test   | 测试环境                                      |
| `--profile <name>` | 自定义 | 指定任意自定义环境（如 uat、pre、staging 等） |

### 默认行为

如果不指定环境参数，脚本将使用 **生产环境（prod）** 作为默认值。

## 使用示例

### 1. 基础使用

```bash
# 使用默认生产环境启动
./startup.sh start

# 使用开发环境启动
./startup.sh start --dev

# 使用测试环境启动
./startup.sh start --test

# 使用生产环境启动（显式指定）
./startup.sh start --prod

# 使用自定义环境启动（UAT）
./startup.sh start --profile uat

# 使用自定义环境启动（预发布）
./startup.sh start --profile pre
```

### 2. 结合其他参数

```bash
# 开发环境 + 自定义端口
./startup.sh start --dev -port 8080

# 生产环境 + JVM 参数
./startup.sh start --prod --jvm-opts '-Xms2g -Xmx4g'

# 测试环境 + 调试模式
./startup.sh debug --test --debug-port 5006

# 开发环境 + 跟踪日志
./startup.sh start --dev --tail

# UAT 环境 + 自定义端口
./startup.sh start --profile uat -port 8090

# 预发布环境 + JVM 参数
./startup.sh start --profile pre --jvm-opts '-Xms1g -Xmx2g'
```

### 3. 重启应用并切换环境

```bash
# 停止当前应用
./startup.sh stop

# 使用新环境启动
./startup.sh start --test
```

## 启动信息显示

启动时会显示当前使用的环境：

```
==========================================
  spring-api-support-monitor-starter 启动中...
  版本: 4.0.0
  环境: dev
==========================================
```

## Spring Profile 配置文件

确保项目中存在对应的配置文件：

```
src/main/resources/
├── application.yml              # 通用配置
├── application-dev.yml          # 开发环境配置
├── application-test.yml         # 测试环境配置
├── application-prod.yml         # 生产环境配置
├── application-uat.yml          # UAT 环境配置（可选）
└── application-pre.yml          # 预发布环境配置（可选）
```

## 环境变量方式

除了命令行参数，也可以通过环境变量指定：

```bash
# 设置环境变量
export SPRING_PROFILE=dev

# 启动应用（将使用 dev 环境）
./startup.sh start

# 命令行参数优先级更高
./startup.sh start --prod  # 将使用 prod 而不是 dev
```

## 完整参数列表

```bash
./startup.sh --help
```

查看所有可用参数和命令。

## 注意事项

1. **环境参数必须在其他参数之前或之后**，位置灵活
2. **只能指定一个环境参数**，后面的会覆盖前面的
3. **环境参数对所有命令生效**，包括 start、restart、debug 等
4. **配置文件必须存在**，否则启动会失败
5. **`--profile` 参数支持任意环境名称**，只要对应的 `application-<name>.yml` 文件存在即可

## 常见问题

### Q: 如何查看当前运行的环境？

A: 查看启动日志或应用日志中的 `spring.profiles.active` 配置。

### Q: 可以同时激活多个环境吗？

A: 当前脚本只支持单一环境，如需多环境可修改 `SPRING_OPTS` 变量。

### Q: `--profile` 和 `--dev/--prod/--test` 有什么区别？

A: `--dev/--prod/--test` 是快捷方式，`--profile` 更灵活，可以指定任意自定义环境名称。

### Q: 如何使用多个自定义环境？

A: 创建对应的配置文件，然后使用 `--profile` 指定：

```bash
# UAT 环境
./startup.sh start --profile uat

# 预发布环境
./startup.sh start --profile pre

# Staging 环境
./startup.sh start --profile staging
```

### Q: 环境参数不生效怎么办？

A: 检查：

1. 参数拼写是否正确（--dev 不是 -dev）
2. 对应的配置文件是否存在
3. 查看启动日志确认实际使用的环境

## 更新日志

- **2024-12-10**: 为所有 startup.sh 添加环境参数支持
  - 支持 --dev、--prod、--test 快捷参数
  - 支持 --profile 自定义环境参数
  - 启动时显示当前环境
  - 更新帮助文档和使用示例
