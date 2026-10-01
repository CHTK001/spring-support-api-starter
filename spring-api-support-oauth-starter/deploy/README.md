# oauth 服务端部署

从源码到运行的可重放部署。**目标形态**：目标主机上的 systemd 服务 `oauth-server.service`。

## 产物与脚本

| 文件 | 作用 | 在哪执行 |
|---|---|---|
| `package.sh` | 构建 + 打包，产出 `dist/oauth-app.tar.gz` | 开发机 |
| `deploy.sh` | 解包 → 建库 → 落库 → 装 unit → 重启 → 自检 | 目标主机（root） |
| `oauth-server.service` | systemd unit（凭据走 `EnvironmentFile`，不含明文） | — |
| `oauth.env.example` | 凭据模板 | — |

产物包内容：`<ver>.jar` + `lib/`（302 个依赖）+ `config/`（外置 yml）+ `deploy/`。
jar 的 manifest `Class-Path` 指向 `lib/` 与 `config/`，三者**必须同目录**。

## 前置条件

1. 目标机已装 **JDK 25**（jar 以 Java 25 编译，class 版本 69）。
2. 目标机有 **MariaDB/MySQL**，且本机可用 **root 的 unix_socket** 建库（`sudo mariadb` 能连）。
3. 目标机有 **Redis**（`application-dev.yml` 默认 `127.0.0.1:6379` db15）。
4. 已准备凭据文件：

   ```bash
   sudo install -d -m 0700 /etc/oauth
   sudo install -m 0600 deploy/oauth.env.example /etc/oauth/oauth.env
   sudo vi /etc/oauth/oauth.env     # 填真实口令
   ```

   `application-dev.yml` 里两组口令都是**无默认值的占位符**，未注入时 Spring 会抛
   `Could not resolve placeholder` **直接启动失败**——这是刻意的：宁可起不来，
   也不要用一个仓库里写死的口令跑起来。

## 首次部署

```bash
# 1) 开发机：构建打包
bash deploy/package.sh            # 或 pwsh -File deploy/package.ps1

# 2) 上传（放到根分区；不要把大包放 /tmp —— 见下方「注意」）
pscp -batch -pw <pass> dist/oauth-app.tar.gz <user>@<host>:/home/<user>/oauth/oauth-app.tar.gz
pscp -batch -pw <pass> deploy/deploy.sh       <user>@<host>:/home/<user>/oauth/deploy.sh

# 3) 目标机：执行（首次时 deploy.sh 还没在 app 目录里，用上传的那份）
ssh <user>@<host>
sudo BUNDLE=/home/<user>/oauth/oauth-app.tar.gz bash /home/<user>/oauth/deploy.sh
```

> **注意**：本仓库的部署主机把 `/tmp` 挂成了小容量 **tmpfs**（实测 2.9G，且吃内存）。
> 产物包 ~153MB，放 `/tmp` 会因 tmpfs 占满而报 `No space left on device`。
> 请放到根分区（`/home/<user>/...`）。


`deploy.sh` 会：解包到 `/home/kali/oauth/app` → 建库 `oauth` 并授权 →
从产物 jar 抽出各模块 `db/init` 脚本按序落库 → 安装 unit → 重启 → 自检
（`19181/oauth/actuator/health` 必须 200，并逐一确认 19180/19181/19182/19185 在听）。

## 升级部署

重复「首次部署」的第 1~2 步，第 3 步改用 app 目录里的脚本：

```bash
sudo BUNDLE=/tmp/oauth-app.tar.gz bash /home/kali/oauth/app/deploy/deploy.sh
```

脚本是**幂等**的：建库用 `CREATE DATABASE IF NOT EXISTS`，DDL 用
`CREATE TABLE IF NOT EXISTS` 与幂等 ALTER，种子数据用 `INSERT IGNORE`，
unit 覆盖安装，服务重启。重复执行不会破坏已有数据。

## 建库方式与取舍

**为什么不用框架自带的编排**（`plugin.datasource.script.enable=true` + Flyway）：
该切面会遍历**所有** `DataSource` Bean（实测有 `dataSource` / `master` /
`dynamicDataSource` / `eduAuthDataSource` 四个），其中 `eduAuthDataSource`
指向 **eduai 库**。一旦打开，oauth 的 ~68 张表会被灌进 eduai 库；
编排器没有「排除某个数据源」的开关（`DataSourceScriptProperties` 只有
`excludeTablePrefixes`），所以只能保持 `enable=false`，由本脚本显式落库。

**代价**：不写 `sys_database_version` 历史表。因应用侧 `enable=false`，
该表本就不参与运行，故不影响功能；若将来要让框架接管迁移，需先在
目标库补齐历史表记录，或在**仅含 oauth 库**的环境里跑一次原生迁移。

## 验证

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:19181/oauth/actuator/health  # 200
ss -ltn | grep -E '1918[0-6]'
```

端到端（经 eduai 走 tcp 到 oauth）：
```bash
curl -s -X POST http://127.0.0.1:19095/v2/user/login \
  -H 'Content-Type: application/json' \
  --data-binary '{"username":"admin","password":"admin123"}'
# 期望 ok=true 且 data.accessToken 非空；oauth 日志里线程名为 [aio-tcp-iocp]
```

## 回滚

`deploy.sh` 不保留旧产物。升级前请自行快照：

```bash
sudo cp -a /home/kali/oauth/app /home/kali/oauth/app.bak-$(date +%Y%m%d-%H%M%S)
```

回滚即把快照换回 `app/` 后 `sudo systemctl restart oauth-server.service`。

## 已知缺口

- 不写 `sys_database_version`（见上）。
- unit 里的堆上限（`-Xmx512m`）是为「与多个 java 服务共存」定的经验值，
  换环境需按可用内存调整。
- `deploy.sh` 依赖本机 root 的 unix_socket 建库；库在远端时需预先建库并授权。
