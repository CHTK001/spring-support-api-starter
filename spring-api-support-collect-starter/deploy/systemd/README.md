# collect 节点 systemd unit（修正版）

2026-10-02 在共享主机 192.168.50.198 上修正。

## 修正内容

`collect-exec.service` 与 `collect-sched.service` 原先把 wrapper 的
stdout/stderr 都指向：

```ini
StandardOutput=append:/opt/collect/logs/.log
StandardError=append:/opt/collect/logs/.log
```

两个缺陷：

1. **角色名变量未展开**：生成了 `.log`（点开头是隐藏文件）。
2. **两节点共用一个文件**：`collect-exec` 与 `collect-sched` 都写同一个 `.log`，
   两个节点的输出混在一起。

而 `run-exec.sh` / `run-sched.sh` 自己已把 java 的 stdout/stderr 分流到
`exec_out.log` / `exec_err.log`（`sched_out.log` / `sched_err.log`），
这两个文件**已在 `deploy/logrotate/collect.logrotate` 管辖内**。

故修法是把 unit 的 wrapper 输出归位到同名文件（不新增文件、不动 logrotate）：

| unit | 修正前 | 修正后 |
|---|---|---|
| collect-exec | `...logs/.log`（stdout+stderr 同文件） | `exec_out.log` / `exec_err.log` |
| collect-sched | `...logs/.log`（stdout+stderr 同文件） | `sched_out.log` / `sched_err.log` |

## 应用（已执行，原始 unit 已备份）

```bash
sudo cp -a /etc/systemd/system/collect-exec.service \
           /etc/systemd/system/collect-sched.service \
           /home/kali/eduai/backups/collect-units-20261002/
sudo cp collect-exec.service  /etc/systemd/system/collect-exec.service
sudo cp collect-sched.service /etc/systemd/system/collect-sched.service
sudo chmod 644 /etc/systemd/system/collect-exec.service /etc/systemd/system/collect-sched.service
sudo systemctl daemon-reload
```

## 为何不重启服务

unit 注释明确：该应用停止需 30–40s，且**在停止第一阶段释放 leader 租约**，
过早 SIGKILL 会留下一个直到租约 TTL 才恢复的派发空窗。
`StandardOutput` / `StandardError` **只在服务下次启动时生效**，
本次改动不需要、也不值得为此重启 —— 故只 `daemon-reload`，
`collect-exec` / `collect-sched` 的 PID 保持不变、运行期零影响。

复核：`systemctl cat collect-exec.service | grep StandardOutput` 已是新路径。

## 遗留

- `/opt/collect/logs/.log`（16K）是旧重定向的产物，运行中的进程仍持有它的 fd；
  **下次重启 collect 后它不再被写入**，即可安全删除。当前保留备查。
- ⚠ **生成器 `collect-ops/units-roles-198.sh` 必须同步修改**，否则下次生成会重新
  写回 `.log`。该生成器不在本仓，需在其所在处修正。
- `systemd-analyze verify` 另报 `collect-rss-sample.service:5: Ignoring unknown
  escape sequences` —— 与本修正无关的既有问题（该 unit 的 ExecStart 里 awk 的
  `\$2` / `%%` 被 systemd 误解析），另行处理。