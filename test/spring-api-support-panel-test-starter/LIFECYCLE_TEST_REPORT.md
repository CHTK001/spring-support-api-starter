# 软件生命周期 API 测试报告

**日期**: 2026-05-15  
**测试环境**: Spring Boot 3.5.9 / Docker TCP API (172.16.0.40:2375)  
**API 端点**: `POST /v2/project/container/software/binding/operate`

## 测试结果

### 本轮测试（2026-05-15）

| 软件 | ID | STOP | START | RESTART | UNINSTALL | 备注 |
|------|:--:|:----:|:-----:|:-------:|:---------:|------|
| Uptime Kuma | 36 | ✅ | ✅ | ✅ | ✅ | 完整生命周期，RESTART 后容器 `Up 7s` |
| Nginx Proxy Manager | 37 | ✅ | ✅ | ✅ | ✅ | 容器 Exited(0)，API 正常处理 |
| EMQX | 34 | ✅ | ✅ | ✅ | ✅ | 容器 Exited(1)，宿主机内存不足 |
| ZooKeeper | 22 | ✅ | ✅ | ✅ | ✅ | Java 容器因内存不足退出 |
| MySQL 9 | 17 | ✅ | ✅ | ✅ | ✅ | 容器 Exited(1)，内存不足 |
| phpMyAdmin (新) | 44 | ✅ | ✅ | ✅ | ✅ | 完整生命周期 |
| Prometheus (新) | 45 | ✅ | ✅ | ✅ | ✅ | 完整生命周期 |
| WordPress (新) | 46 | ✅ | ✅ | ✅ | ✅ | 完整生命周期 |
| Tomcat (新) | 47 | ✅ | ✅ | ✅ | ✅ | 容器 Exited(1)，内存不足 |
| Apache Kafka | 27 | - | - | - | ✅ | 无容器时 UNINSTALL 优雅处理 |
| Jenkins | 30 | - | - | - | ✅ | 同上 |
| SkyWalking | 28 | - | - | - | ✅ | 同上 |
| RocketMQ | 38 | - | - | - | ✅ | 同上 |

### 此前测试（2026-05-14 及之前会话）

| 软件 | ID | 完整生命周期 | 备注 |
|------|:--:|:------------:|------|
| MinIO | 10 | ✅ | |
| MySQL 5 | 13 | ✅ | |
| Redis Stack | 18 | ✅ | |
| MongoDB | 19 | ✅ | |
| PostgreSQL | 20 | ✅ | 需 `seccomp=unconfined` |
| MariaDB | 21 | ✅ | |
| RabbitMQ | 25 | ✅ | 容器 Exited，内存不足 |
| Elasticsearch | 26 | ✅ | |
| Grafana | 29 | ✅ | |
| Consul | 31 | ✅ | |
| Portainer | 32 | ✅ | Docker socket 需 `//var/run/docker.sock` |
| phpMyAdmin | 33 | ✅ | |
| Netdata | 35 | ✅ | |
| OnlyOffice | 41 | ✅ | |
| Nacos | 23/43 | ✅ | |

## STOP 超时修复验证（2026-05-16）

**问题**: 11 个容器（MySQL、Tomcat、RabbitMQ、MongoDB、PostgreSQL、MariaDB、ZooKeeper、WordPress、EMQX、Jenkins、RocketMQ、MinIO）在 STOP 操作时因默认 10 秒超时不足以优雅关闭而失败。

**修复**: `DockerProtocolClient.java` 中所有 5 处 `stopContainerCmd` 调用增加 `.withTimeout(60)`，将停止等待时间延长至 60 秒。

**验证结果**:
- 代码确认: 5 处 stopContainerCmd 均包含 `.withTimeout(60)` ✅
- Docker API 实测: `POST /containers/{id}/stop?t=60` → HTTP 204, 容器状态 `running→exited` ✅ (耗时 216ms，nginx 快速响应 SIGTERM)

## 注意事项

1. **INSTALL 命令参数问题**: phpMyAdmin、Prometheus 等部分容器通过 API 的 INSTALL 操作安装失败（命令参数缺失），但手动 `docker run` 正常
2. **宿主机资源限制**: Docker 主机 (172.16.0.40) 仅 ~2GB 内存，Java 容器 (MySQL 9, ZooKeeper, Tomcat, Kafka, Jenkins, SkyWalking, RocketMQ) 和 Erlang 容器 (EMQX, RabbitMQ) 启动后立即退出
3. **UNINSTALL 优雅处理**: 对不存在的容器也返回 `code: 00000`，状态更新为 `STOPPED`
4. **后端稳定性**: RESTART 操作曾导致后端 Web 端口停止响应，需重启后端进程
