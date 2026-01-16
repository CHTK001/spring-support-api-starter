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

## 运维脚本

本模块提供统一的运维脚本（作为发布包的一部分），支持 Linux / Windows。

### 脚本目录结构

```
bin/
├── start.sh / start.bat          # 启动应用
├── stop.sh / stop.bat            # 停止应用
├── restart.sh / restart.bat      # 重启应用
├── logs.sh / logs.bat            # 查看日志
├── status.sh / status.bat        # 查看状态
├── info.sh / info.bat            # 查看信息
├── upgrade.sh / upgrade.bat      # 升级应用
├── rollback.sh / rollback.bat    # 回滚应用
├── backup.sh / backup.bat        # 备份
├── history.sh / history.bat      # 备份历史
├── env.sh / env.bat              # 环境变量管理
├── analyze.sh / analyze.bat      # 资源分析
├── debug.sh / debug.bat          # 调试模式启动
├── cluster.sh / cluster.bat      # 集群管理
├── skywalking.sh                 # SkyWalking 管理
├── arthas.sh                     # Arthas 管理
├── app.env                       # 环境配置文件
└── script/                       # 脚本工具库
    ├── linux/
    │   └── common.sh
    └── cluster/
        └── cluster.sh
```

---

## 命令详细说明

### 1. start.sh - 启动应用

启动 Spring Boot 应用，支持多种启动参数和配置。

**用法：**
```bash
start.sh [-f] [-j JAVA_HOME] [-jvm "JVM参数"] [-t 时区] [--charset 编码] [-p 端口] [--skywalking] [--arthas] [--dump [路径]]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-f` | 启动后实时查看日志（等同执行 `logs.sh -f`） | `start.sh -f` |
| `-j JAVA_HOME` | 指定 JAVA_HOME（也可直接传 java 可执行文件路径） | `start.sh -j /opt/java/openjdk` |
| `-jvm "参数"` | 追加任意 JVM 参数（会拼到 java 命令后） | `start.sh -jvm "-Xms512m -Xmx1024m"` |
| `-t 时区` | 设置时区，例如 `Asia/Shanghai` | `start.sh -t Asia/Shanghai` |
| `--charset 编码` | 设置文件编码，例如 `UTF-8` | `start.sh --charset UTF-8` |
| `-p 端口` | 设置应用端口（通过 SERVER_PORT 环境变量传递） | `start.sh -p 8080` |
| `--skywalking` | 启用 SkyWalking 探针（等同于 `SKYWALKING_ENABLED=true`） | `start.sh --skywalking` |
| `--arthas` | 启动后自动附加 Arthas（等同于 `ARTHAS_ENABLED=true`） | `start.sh --arthas` |
| `--dump [路径]` | 启用 OOM 时自动生成堆转储（等同于 `DUMP_ON_OOM=true`） | `start.sh --dump` 或 `start.sh --dump /path/to/dump.hprof` |
| `-h, --help` | 显示帮助信息 | `start.sh -h` |

**说明：**
- 所有参数设置后会自动保存到 `app.env`，下次启动时自动读取
- 如果命令行未提供参数，将从 `app.env` 读取上次保存的配置
- `--dump` 会在 OOM 时自动生成堆转储文件，默认路径为 `<base>/logs/heapdump.hprof`

**示例：**
```bash
# 基础启动
./bin/start.sh

# 指定 Java 路径和 JVM 参数
./bin/start.sh -j /opt/java/openjdk -jvm "-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启用 SkyWalking 和 Arthas
./bin/start.sh --skywalking --arthas

# 设置时区和编码
./bin/start.sh -t Asia/Shanghai --charset UTF-8

# 启动后查看日志
./bin/start.sh -f
```

---

### 2. stop.sh - 停止应用

优雅停止应用，支持超时和强制停止。

**用法：**
```bash
stop.sh [-t 秒] [--force] [--no-cleanup]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-t 秒` | 优雅停止等待时间，默认 30 秒 | `stop.sh -t 5` |
| `--force` | 立即强制停止（跳过优雅停止，直接 kill -9） | `stop.sh --force` |
| `--no-cleanup` | 停止后不清理子进程和 PID 文件 | `stop.sh --no-cleanup` |
| `-h, --help` | 显示帮助信息 | `stop.sh -h` |

**说明：**
- 默认使用优雅停止（SIGTERM），等待进程自行退出
- 如果超时仍未退出，会自动强制停止（SIGKILL）
- 使用 `--force` 可跳过等待，直接强制停止

**示例：**
```bash
# 优雅停止（默认等待 30 秒）
./bin/stop.sh

# 快速停止（等待 5 秒）
./bin/stop.sh -t 5

# 强制停止
./bin/stop.sh --force
```

---

### 3. restart.sh - 重启应用

重启应用，支持透传 start.sh 的所有参数。

**用法：**
```bash
restart.sh [-t 秒] [start.sh 的其他参数]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-t 秒` | 停止等待时间，默认 30 秒（传递给 stop.sh） | `restart.sh -t 5` |
| 其他参数 | 会透传给 start.sh，例如 `-f`, `-j`, `-jvm`, `-p`, `--skywalking`, `--arthas` 等 | `restart.sh -f --skywalking` |

**说明：**
- 先执行 `stop.sh` 停止应用，再执行 `start.sh` 启动应用
- 所有 `start.sh` 的参数都可以透传

**示例：**
```bash
# 基础重启
./bin/restart.sh

# 重启后查看日志
./bin/restart.sh -f

# 重启并启用 SkyWalking
./bin/restart.sh --skywalking

# 快速重启（5 秒超时）
./bin/restart.sh -t 5
```

---

### 4. logs.sh - 查看日志

查看和过滤应用日志。

**用法：**
```bash
logs.sh [-f] [-n 行数] [-g 关键词] [-e 关键词] [-i] [-c]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-f` | 实时滚动日志 | `logs.sh -f` |
| `-n 行数` | 显示最后 N 行，默认 200 | `logs.sh -n 50` |
| `-g 关键词` | 过滤包含关键词的行（grep） | `logs.sh -g ERROR` |
| `-e 关键词` | 排除包含关键词的行（grep -v） | `logs.sh -e DEBUG` |
| `-i` | 忽略大小写（配合 `-g` 或 `-e` 使用） | `logs.sh -g error -i` |
| `-c` | 显示行号 | `logs.sh -c` |
| `-h, --help` | 显示帮助信息 | `logs.sh -h` |

**说明：**
- 可以组合使用多个参数，例如：`logs.sh -n 500 -g ERROR -c`
- `-g` 和 `-e` 可以多次使用，实现多条件过滤

**示例：**
```bash
# 查看最后 50 行日志
./bin/logs.sh -n 50

# 实时查看日志
./bin/logs.sh -f

# 查看包含 ERROR 的日志（忽略大小写）
./bin/logs.sh -g error -i

# 查看日志（排除 DEBUG，显示行号）
./bin/logs.sh -e DEBUG -c

# 组合使用：查看最后 100 行包含 ERROR 的日志
./bin/logs.sh -n 100 -g ERROR
```

---

### 5. status.sh - 查看状态

查看应用运行状态，支持 JVM 信息、内存详情、线程转储等。

**用法：**
```bash
status.sh [-f] [-i 秒] [-j] [-m] [-d]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-f` | 实时监控模式，无闪屏更新（按 Ctrl+C 退出） | `status.sh -f` |
| `-i 秒` | 监控刷新间隔（秒），默认 2 秒，仅在 `-f` 模式下生效 | `status.sh -f -i 5` |
| `-j` | 显示 JVM 信息（堆内存、GC 等，需要 jstat 支持） | `status.sh -j` |
| `-m` | 显示内存详细信息 | `status.sh -m` |
| `-d` | 显示线程转储信息（需要 jstack 支持） | `status.sh -d` |
| `-h, --help` | 显示帮助信息 | `status.sh -h` |

**说明：**
- 默认显示基本状态信息（PID、CPU、内存、运行时间等）
- 可以组合使用多个参数来显示详细信息

**示例：**
```bash
# 查看基本状态
./bin/status.sh

# 查看 JVM 信息
./bin/status.sh -j

# 查看内存详情
./bin/status.sh -m

# 查看线程转储
./bin/status.sh -d

# 实时监控（每 5 秒刷新）
./bin/status.sh -f -i 5

# 组合查看（JVM + 内存 + 线程）
./bin/status.sh -j -m -d
```

---

### 6. info.sh - 查看信息

查看应用版本、环境配置、文件路径等信息。

**用法：**
```bash
info.sh [-v] [-e] [-f]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-v` | 显示版本信息（应用名称、版本） | `info.sh -v` |
| `-e` | 显示环境变量配置 | `info.sh -e` |
| `-f` | 显示文件路径信息 | `info.sh -f` |
| `-h, --help` | 显示帮助信息 | `info.sh -h` |

**说明：**
- 默认显示所有信息
- 可以组合使用多个参数来显示特定信息

**示例：**
```bash
# 查看所有信息
./bin/info.sh

# 仅查看版本信息
./bin/info.sh -v

# 仅查看环境配置
./bin/info.sh -e

# 仅查看文件路径
./bin/info.sh -f
```

---

### 7. upgrade.sh - 升级应用

升级应用包，支持 jar、tar.gz、zip 格式，支持自动备份和回滚。

**用法：**
```bash
upgrade.sh -p 包路径 [--backup] [-b 备份目录] [--no-restart] [--verify] [--no-rollback]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-p 路径` | 升级包路径（.jar/.tar.gz/.zip） | `upgrade.sh -p app-new.jar` |
| `--backup` | 启用备份（不指定 `-b` 时默认 `<base>/backup/<时间戳>`） | `upgrade.sh -p app-new.jar --backup` |
| `-b 目录` | 备份目录（可选，需配合 `--backup`） | `upgrade.sh -p app-new.jar --backup -b /backup` |
| `--no-restart` | 升级完成后不自动重启 | `upgrade.sh -p app-new.jar --no-restart` |
| `--verify` | 验证升级包完整性（MD5/SHA256，需要提供 .md5 或 .sha256 文件） | `upgrade.sh -p app-new.jar --verify` |
| `--no-rollback` | 升级失败时不自动回滚 | `upgrade.sh -p app-new.jar --no-rollback` |
| `-h, --help` | 显示帮助信息 | `upgrade.sh -h` |

**说明：**
- 支持 jar 直接替换
- 支持 tar.gz / zip（解压后覆盖到当前发布目录）
- 升级失败时自动回滚到备份（除非使用 `--no-rollback`）

**示例：**
```bash
# 升级 jar 包（自动备份并重启）
./bin/upgrade.sh -p /path/to/app-new.jar --backup

# 升级 tar.gz 包（不重启）
./bin/upgrade.sh -p /path/to/app-new.tar.gz --backup --no-restart

# 升级并验证完整性
./bin/upgrade.sh -p /path/to/app-new.jar --backup --verify

# 升级到指定备份目录
./bin/upgrade.sh -p /path/to/app-new.jar --backup -b /data/backup
```

---

### 8. rollback.sh - 回滚应用

回滚到指定版本的包。

**用法：**
```bash
rollback.sh -p 包路径 [--backup] [-b 备份目录] [--no-restart]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-p 路径` | 回滚包路径（.jar/.tar.gz/.zip） | `rollback.sh -p app-old.jar` |
| `--backup` | 启用备份（不指定 `-b` 时默认 `<base>/backup/<时间戳>`） | `rollback.sh -p app-old.jar --backup` |
| `-b 目录` | 备份目录（可选，需配合 `--backup`） | `rollback.sh -p app-old.jar --backup -b /backup` |
| `--no-restart` | 回滚完成后不自动重启 | `rollback.sh -p app-old.jar --no-restart` |
| `-h, --help` | 显示帮助信息 | `rollback.sh -h` |

**说明：**
- 支持 jar 直接替换
- 支持 tar.gz / zip（解压后覆盖到当前发布目录）

**示例：**
```bash
# 回滚到旧版本（自动备份并重启）
./bin/rollback.sh -p /path/to/app-old.jar --backup

# 回滚不重启
./bin/rollback.sh -p /path/to/app-old.jar --backup --no-restart
```

---

### 9. backup.sh - 备份

备份应用文件（jar、config、logs）。

**用法：**
```bash
backup.sh [-d 目录] [--jar] [--config] [--logs] [--all] [--comment 备注] [--clean 天数]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-d 目录` | 备份目录（可选，默认 `<base>/backup/<时间戳>`） | `backup.sh -d /backup` |
| `--jar` | 备份 jar 文件 | `backup.sh --jar` |
| `--config` | 备份 config 目录 | `backup.sh --config` |
| `--logs` | 备份 logs 目录 | `backup.sh --logs` |
| `--all` | 备份所有内容（jar、config、logs） | `backup.sh --all` |
| `--comment 备注` | 添加备份备注信息 | `backup.sh --all --comment "升级前备份"` |
| `--clean 天数` | 清理指定天数之前的备份（默认保留 30 天） | `backup.sh --clean 30` |
| `-h, --help` | 显示帮助信息 | `backup.sh -h` |

**说明：**
- 如果没有指定任何备份项，默认备份所有
- 备份会生成 MD5 校验和文件

**示例：**
```bash
# 备份所有内容
./bin/backup.sh --all

# 仅备份 jar 和 config
./bin/backup.sh --jar --config

# 备份并添加备注
./bin/backup.sh --all --comment "升级前备份"

# 备份到指定目录
./bin/backup.sh -d /data/backup --all

# 清理 30 天前的备份
./bin/backup.sh --clean 30
```

---

### 10. history.sh - 备份历史

查看和管理备份历史，支持恢复备份。

**用法：**
```bash
history.sh [--backup] [--list] [--detail 备份目录] [--clean 天数] [--count 数量] [--restore 序号] [--jar-only] [--config-only] [--logs-only]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `--backup` | 查看备份历史（默认） | `history.sh --backup` |
| `--list` | 列表模式（简洁显示） | `history.sh --list` |
| `--detail 目录` | 查看指定备份的详细信息 | `history.sh --detail 20260115123456` |
| `--clean 天数` | 清理指定天数之前的备份（默认保留 30 天） | `history.sh --clean 30` |
| `--count 数量` | 显示最近 N 个备份（默认显示全部） | `history.sh --count 5` |
| `--restore 序号` | 恢复指定序号的备份（会先停止应用并备份当前状态） | `history.sh --restore 1` |
| `--jar-only` | 仅恢复 jar 文件（需配合 `--restore` 使用） | `history.sh --restore 1 --jar-only` |
| `--config-only` | 仅恢复 config 目录（需配合 `--restore` 使用） | `history.sh --restore 1 --config-only` |
| `--logs-only` | 仅恢复 logs 目录（需配合 `--restore` 使用） | `history.sh --restore 1 --logs-only` |
| `-h, --help` | 显示帮助信息 | `history.sh -h` |

**示例：**
```bash
# 查看备份列表
./bin/history.sh --list

# 查看最近 5 个备份
./bin/history.sh --count 5

# 查看备份详情
./bin/history.sh --detail 20260115123456

# 恢复第 1 个备份
./bin/history.sh --restore 1

# 仅恢复 jar 文件
./bin/history.sh --restore 1 --jar-only

# 清理 30 天前的备份
./bin/history.sh --clean 30
```

---

### 11. env.sh - 环境变量管理

管理应用环境变量配置（存储在 `app.env` 文件中）。

**用法：**
```bash
env.sh [选项] [变量名=值 ...]
```

**选项说明：**

| 选项 | 说明 | 示例 |
|------|------|------|
| `-l, --list` | 显示所有环境变量配置 | `env.sh --list` |
| `-s, --set` | 设置环境变量（格式: 变量名=值） | `env.sh --set BACKUP_DIR=/data/backup` |
| `-u, --unset` | 删除环境变量（格式: 变量名） | `env.sh --unset SKYWALKING_ENABLED` |
| `-r, --reset` | 重置环境变量配置为默认值 | `env.sh --reset` |
| `-h, --help` | 显示帮助信息 | `env.sh -h` |

**示例：**
```bash
# 显示所有配置
./bin/env.sh --list

# 设置备份目录
./bin/env.sh --set BACKUP_DIR=/data/backup

# 设置 JVM 参数
./bin/env.sh --set JVM_ARGS="-Xms512m -Xmx1024m"

# 删除配置项
./bin/env.sh --unset SKYWALKING_ENABLED

# 重置为默认配置
./bin/env.sh --reset
```

---

### 12. analyze.sh - 资源分析

分析系统资源和使用情况，推荐最优 JVM 配置。

**用法：**
```bash
analyze.sh [-a] [-o 输出文件]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-a` | 使用 AI 分析（需要配置 AI API） | `analyze.sh -a` |
| `-o 文件` | 将分析结果输出到文件 | `analyze.sh -o config.txt` |
| `-h, --help` | 显示帮助信息 | `analyze.sh -h` |

**说明：**
- 分析当前系统资源和使用情况，推荐最优 JVM 配置
- 包括 Xms、Xmx、GC 参数等
- 可以结合 AI 进行更智能的分析

**示例：**
```bash
# 基础分析
./bin/analyze.sh

# 输出到文件
./bin/analyze.sh -o jvm-config.txt

# AI 分析（需要配置 AI API）
./bin/analyze.sh -a
```

---

### 13. debug.sh - 调试模式启动

以调试模式启动应用，支持远程调试（JDWP）。

**用法：**
```bash
debug.sh [-f] [-j JAVA_HOME] [-jvm "JVM参数"] [-t 时区] [--charset 编码] [-p 端口] [--skywalking] [--arthas] [--dump [路径]] [-d 端口]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-d 端口` | 设置调试端口（默认 5005，通过 JDWP 协议） | `debug.sh -d 5005` |
| 其他参数 | 与 `start.sh` 相同 | 见 `start.sh` 参数说明 |

**说明：**
- `debug.sh` 与 `start.sh` 功能一致，但会启用远程调试
- 调试端口默认 5005，可通过 `-d` 参数修改
- 所有参数设置后会自动保存到 `app.env`，下次启动时自动读取

**示例：**
```bash
# 使用默认调试端口（5005）
./bin/debug.sh

# 指定调试端口
./bin/debug.sh -d 5006

# 调试模式启动并查看日志
./bin/debug.sh -f -d 5005
```

---

### 14. cluster.sh - 集群管理

批量管理多节点应用，支持 SSH 远程执行和本地节点。

**用法：**
```bash
# 执行集群命令
cluster.sh -f hosts.txt -c start|stop|restart|logs|upgrade|rollback|status|test|health [-p 包路径] [--args "透传参数"]

# 节点管理
cluster.sh --node add <节点地址> [--file hosts.txt]
cluster.sh --node list [--file hosts.txt]
cluster.sh --node del <序号> [--file hosts.txt]
```

**参数说明：**

| 参数 | 说明 | 示例 |
|------|------|------|
| `-f 文件` | hosts 文件路径（默认: `<base>/bin/cluster_hosts.txt`） | `cluster.sh -f hosts.txt` |
| `-c 命令` | start/stop/restart/logs/upgrade/rollback/status/test/health | `cluster.sh -c start` |
| `-p 路径` | upgrade/rollback 使用的包路径（远程机器可访问的路径） | `cluster.sh -c upgrade -p app.jar` |
| `-P 密码` | SSH 密码（用于密码登录，需要安装 sshpass） | `cluster.sh -P password` |
| `--args 参数` | 透传给远程脚本的参数（例如 start 的 `-f/-jvm` 等） | `cluster.sh -c start --args "-f --skywalking"` |
| `--node 操作` | 节点管理操作: add/list/del | `cluster.sh --node add node1` |
| `--file 文件` | 节点管理时指定 hosts 文件 | `cluster.sh --node add node1 --file hosts.txt` |
| `--parallel` | 并发执行（upgrade/rollback/start/restart 默认串行以减少风险） | `cluster.sh -c start --parallel` |
| `--timeout 秒` | SSH 连接和执行超时（默认 30 秒） | `cluster.sh --timeout 60` |
| `--yes` | 跳过确认提示（危险操作时） | `cluster.sh -c upgrade --yes` |
| `-h, --help` | 显示帮助信息 | `cluster.sh -h` |

**节点地址格式：**
- `host` - 远程主机（默认端口 22）
- `host:port` - 远程主机（指定端口）
- `user@host` - 远程主机（指定用户，默认端口 22）
- `user@host:port` - 远程主机（指定用户和端口）
- `localhost` 或 `127.0.0.1` - 本地节点（直接执行，不使用 ssh）

**hosts 文件格式：**
```
# 注释行会被忽略
node1.example.com
user@node2.example.com:2222
localhost
127.0.0.1
```

**SSH 认证方式（按优先级）：**
1. SSH 密钥免密登录（推荐）
2. 密码登录（通过 `-P` 参数或 `CLUSTER_SSH_PASSWORD` 环境变量，需要安装 sshpass）

**说明：**
- hosts 文件每行一个主机，空行/以 `#` 开头会忽略
- 远程默认执行 `<remoteBase>/bin` 下脚本
- 本地节点（localhost/127.0.0.1）直接执行，不使用 SSH

**示例：**
```bash
# 启动所有节点
./bin/cluster.sh -f hosts.txt -c start

# 停止所有节点
./bin/cluster.sh -f hosts.txt -c stop

# 重启所有节点
./bin/cluster.sh -f hosts.txt -c restart

# 查看所有节点日志
./bin/cluster.sh -f hosts.txt -c logs --args "-f"

# 升级所有节点
./bin/cluster.sh -f hosts.txt -c upgrade -p /path/to/app.jar

# 添加节点
./bin/cluster.sh --node add node1.example.com

# 列出所有节点
./bin/cluster.sh --node list

# 删除节点（序号从 1 开始）
./bin/cluster.sh --node del 1

# 并发启动（减少等待时间）
./bin/cluster.sh -f hosts.txt -c start --parallel

# 使用密码登录
./bin/cluster.sh -f hosts.txt -c start -P mypassword
```

---

### 15. skywalking.sh - SkyWalking 管理

管理 SkyWalking 探针的下载、安装、配置等。

**用法：**
```bash
skywalking.sh <命令> [选项]
```

**命令说明：**

| 命令 | 说明 | 示例 |
|------|------|------|
| `download [版本]` | 下载指定版本的 SkyWalking 探针 | `skywalking.sh download 9.0.0` |
| `install [版本\|路径]` | 安装指定版本或本地包 | `skywalking.sh install 9.0.0` |
| `list` | 列出已安装的版本 | `skywalking.sh list` |
| `list --available` | 列出可用版本（需要配置 SKYWALKING_REPO_URL） | `skywalking.sh list --available` |
| `status` | 显示探针状态和版本信息 | `skywalking.sh status` |
| `config [key] [value]` | 修改配置项 | `skywalking.sh config SW_AGENT_NAME my-app` |
| `config --show` | 显示当前配置 | `skywalking.sh config --show` |
| `config --reset` | 重置为默认配置 | `skywalking.sh config --reset` |
| `uninstall [版本]` | 卸载指定版本 | `skywalking.sh uninstall 9.0.0` |
| `switch [版本]` | 切换到指定版本 | `skywalking.sh switch 9.0.0` |
| `enable` | 启用 SkyWalking（设置 SKYWALKING_ENABLED=true） | `skywalking.sh enable` |
| `disable` | 禁用 SkyWalking（设置 SKYWALKING_ENABLED=false） | `skywalking.sh disable` |

**选项说明：**

| 选项 | 说明 | 示例 |
|------|------|------|
| `--force` | 强制操作（覆盖已存在的版本） | `skywalking.sh install 9.0.0 --force` |
| `--dir 目录` | 指定安装目录（默认 `<base>/agents/skywalking`） | `skywalking.sh install 9.0.0 --dir /opt/skywalking` |

**示例：**
```bash
# 下载并安装 SkyWalking 探针
./bin/skywalking.sh download 9.0.0
./bin/skywalking.sh install 9.0.0

# 查看已安装版本
./bin/skywalking.sh list

# 查看状态
./bin/skywalking.sh status

# 配置探针
./bin/skywalking.sh config SW_AGENT_NAME my-app
./bin/skywalking.sh config SW_AGENT_COLLECTOR_BACKEND_SERVICES 127.0.0.1:11800

# 启用 SkyWalking
./bin/skywalking.sh enable

# 切换到指定版本
./bin/skywalking.sh switch 9.0.0
```

---

### 16. arthas.sh - Arthas 管理

管理 Arthas 的下载、安装、配置等。

**用法：**
```bash
arthas.sh <命令> [选项]
```

**命令说明：**

| 命令 | 说明 | 示例 |
|------|------|------|
| `download [版本]` | 下载指定版本的 Arthas | `arthas.sh download latest` |
| `install [版本\|路径]` | 安装指定版本或本地包 | `arthas.sh install latest` |
| `list` | 列出已安装的版本 | `arthas.sh list` |
| `list --available` | 列出可用版本 | `arthas.sh list --available` |
| `status` | 显示 Arthas 状态和版本信息 | `arthas.sh status` |
| `config [key] [value]` | 修改配置项 | `arthas.sh config ARTHAS_TELNET_PORT 3658` |
| `config --show` | 显示当前配置 | `arthas.sh config --show` |
| `config --reset` | 重置为默认配置 | `arthas.sh config --reset` |
| `uninstall [版本]` | 卸载指定版本 | `arthas.sh uninstall latest` |
| `switch [版本]` | 切换到指定版本 | `arthas.sh switch latest` |
| `enable` | 启用 Arthas（设置 ARTHAS_ENABLED=true） | `arthas.sh enable` |
| `disable` | 禁用 Arthas（设置 ARTHAS_ENABLED=false） | `arthas.sh disable` |
| `attach [PID]` | 附加到指定进程（需要进程正在运行） | `arthas.sh attach 12345` |

**选项说明：**

| 选项 | 说明 | 示例 |
|------|------|------|
| `--force` | 强制操作（覆盖已存在的版本） | `arthas.sh install latest --force` |
| `--dir 目录` | 指定安装目录（默认 `<base>/agents/arthas`） | `arthas.sh install latest --dir /opt/arthas` |

**示例：**
```bash
# 下载并安装 Arthas
./bin/arthas.sh download latest
./bin/arthas.sh install latest

# 查看已安装版本
./bin/arthas.sh list

# 查看状态
./bin/arthas.sh status

# 配置 Arthas
./bin/arthas.sh config ARTHAS_TELNET_PORT 3658
./bin/arthas.sh config ARTHAS_HTTP_PORT 8563

# 启用 Arthas
./bin/arthas.sh enable

# 附加到进程
./bin/arthas.sh attach 12345
```

---

## 环境配置文件 (app.env)

所有脚本的环境配置都存储在 `bin/app.env` 文件中，可以通过 `env.sh` 管理，也可以在启动时通过命令行参数设置（会自动保存到 `app.env`）。

**主要配置项：**

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `BASE_DIR` | 应用发布目录 | `${project.basedir}` |
| `APP_NAME` | 应用名称 | `${project.artifactId}` |
| `APP_VERSION` | 应用版本 | `${project.version}` |
| `JAR_FILE` | Jar 文件路径 | `${BASE_DIR}/${APP_NAME}-${APP_VERSION}.jar` |
| `PID_FILE` | PID 文件路径 | `${BASE_DIR}/run/${APP_NAME}.pid` |
| `CONSOLE_LOG` | 控制台日志路径 | `${BASE_DIR}/logs/console.log` |
| `BACKUP_DIR` | 备份目录 | `${BASE_DIR}/backup` |
| `JAVA_HOME` | Java 安装路径 | 自动检测 |
| `JVM_ARGS` | JVM 参数 | `-Dfile.encoding=UTF-8` |
| `SKYWALKING_ENABLED` | 是否启用 SkyWalking | `false` |
| `ARTHAS_ENABLED` | 是否启用 Arthas | `false` |
| `DUMP_ON_OOM` | OOM 时是否生成堆转储 | `false` |

---

## SkyWalking 与 Arthas 集成

启动脚本内置了对 SkyWalking 与 Arthas 的可选支持：

### SkyWalking 集成

**通过环境变量配置：**
```bash
SKYWALKING_ENABLED=true
SKYWALKING_DIST_URL=https://archive.apache.org/dist/skywalking/java-agent/8.16.0/apache-skywalking-java-agent-8.16.0.tgz
SKYWALKING_AGENT_DIR=/opt/app/agents/skywalking
SW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800
```

**通过启动参数开启：**
```bash
./bin/start.sh --skywalking
```

**说明：**
- 当开启 SkyWalking 时，脚本会在本地未找到 `skywalking-agent.jar` 且配置了 `SKYWALKING_DIST_URL` 的情况下自动下载探针
- 启动命令中会自动追加 `-javaagent` 与 `SW_AGENT_*` 相关参数

### Arthas 集成

**通过环境变量配置：**
```bash
ARTHAS_ENABLED=true
ARTHAS_BOOT_URL=https://arthas.aliyun.com/download/arthas-boot.jar
ARTHAS_HOME=/opt/app/agents/arthas
ARTHAS_TELNET_PORT=3658
ARTHAS_HTTP_PORT=8563
```

**通过启动参数开启：**
```bash
./bin/start.sh --arthas
```

**说明：**
- 当开启 Arthas 时，脚本会在应用启动后自动下载/启动 `arthas-boot.jar` 并附着到当前进程

---

## Docker 场景测试建议

以示例工程 `spring-demo-app` 为例，推荐按如下步骤在本地通过 Docker 验证脚本能力：

### 1. 构建镜像

```bash
docker build -f spring-demo-app/Dockerfile -t spring-demo-app:dev .
```

### 2. 启动容器

```bash
# 基础启动
docker run --rm -d --name spring-demo-app -p 18080:8080 spring-demo-app:dev

# 启用 SkyWalking
docker run --rm -d --name spring-demo-app-sky \
  -e SKYWALKING_ENABLED=true \
  -e SKYWALKING_DIST_URL=https://archive.apache.org/dist/skywalking/java-agent/8.16.0/apache-skywalking-java-agent-8.16.0.tgz \
  -e SW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800 \
  -p 18081:8080 spring-demo-app:dev

# 启用 Arthas
docker run --rm -d --name spring-demo-app-arthas \
  -e ARTHAS_ENABLED=true \
  -e ARTHAS_BOOT_URL=https://arthas.aliyun.com/download/arthas-boot.jar \
  -p 18082:8080 -p 3658:3658 -p 8563:8563 spring-demo-app:dev
```

### 3. 容器内验证脚本

```bash
# 进入容器
docker exec -it spring-demo-app /bin/sh

# 执行脚本
cd /opt/app
./bin/start.sh -h
./bin/logs.sh -n 50
./bin/status.sh
./bin/info.sh
```

### 4. 集群测试（多容器）

```bash
# 创建 hosts 文件
cat > cluster_hosts.txt <<EOF
localhost
127.0.0.1
EOF

# 启动多个容器（模拟多节点）
docker run -d --name node1 -p 18080:8080 spring-demo-app:dev
docker run -d --name node2 -p 18081:8080 spring-demo-app:dev

# 在容器内执行集群命令
docker exec -it node1 /bin/sh -c "cd /opt/app && ./bin/cluster.sh -f cluster_hosts.txt -c start"
```

---

## 测试清单

详见 [TEST_CHECKLIST.md](../../../../TEST_CHECKLIST.md)

---

## 常见问题

### 1. 启动失败：找不到 Java

**问题：** `start.sh` 提示找不到 Java

**解决：**
- 使用 `-j` 参数指定 Java 路径：`./bin/start.sh -j /opt/java/openjdk`
- 或设置 `JAVA_HOME` 环境变量：`export JAVA_HOME=/opt/java/openjdk`

### 2. 日志文件不存在

**问题：** `logs.sh` 提示日志文件不存在

**解决：**
- 确保应用已启动：`./bin/status.sh`
- 检查日志目录权限：`ls -l logs/`

### 3. 集群脚本 SSH 连接失败

**问题：** `cluster.sh` 无法连接远程节点

**解决：**
- 配置 SSH 密钥免密登录（推荐）
- 或使用密码登录：`cluster.sh -P password`
- 检查 hosts 文件格式是否正确

### 4. SkyWalking 探针未生效

**问题：** 启用 SkyWalking 后探针未生效

**解决：**
- 检查探针是否下载：`./bin/skywalking.sh status`
- 检查配置是否正确：`./bin/skywalking.sh config --show`
- 查看启动日志：`./bin/logs.sh -g skywalking`

### 5. Arthas 无法连接

**问题：** Arthas 无法连接到进程

**解决：**
- 检查进程是否运行：`./bin/status.sh`
- 检查端口是否被占用：`netstat -tlnp | grep 3658`
- 手动附加：`./bin/arthas.sh attach <PID>`