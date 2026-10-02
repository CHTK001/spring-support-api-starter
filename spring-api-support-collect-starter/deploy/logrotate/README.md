# collect 日志轮转（logrotate）修复

2026-10-02 在共享主机 192.168.50.198 上实测：`logrotate.service` 自 00:14 起
持续失败，`/var/log` 与 `/opt/collect/logs` 的轮转受影响。根因是**两个独立缺陷**
叠加，任一单独存在都会出错。

## 现象

```
logrotate[492412]: error: collect-rss:3 duplicate log entry for /opt/collect/logs/rss-sample.log
logrotate[492412]: error: found error in file collect-rss, skipping
logrotate[492412]: error: error opening /opt/collect/logs/single_err.log: Permission denied
logrotate[492412]: error: error opening /opt/collect/logs/single_out.log: Permission denied
logrotate.service: Main process exited, code=exited, status=1/FAILURE
```

## 根因

| # | 缺陷 | 证据 |
|---|---|---|
| 1 | **重复条目**：`/etc/logrotate.d/collect` 用通配 `/opt/collect/logs/*.log`，`/etc/logrotate.d/collect-rss` 又显式写了 `rss-sample.log` → logrotate 判重并**整份跳过 collect-rss**（rss 日志从不轮转） | `logrotate -d /etc/logrotate.conf` 报 `duplicate log entry` |
| 2 | **属主不符**：`collect` 里写 `su kali kali`，但服务以 root 运行、日志属主是 `root:root` → kali 打不开 → `Permission denied` | `ls -ln /opt/collect/logs/` 全是 `-rw-r--r-- 1 0 0`；`systemctl show collect-* -p User` 为空 |

> 陷阱：`logrotate -d`（干跑）**不会真正打开文件**，所以缺陷 2 在干跑里看不出来，
> 只有实跑才暴露。定位这类问题必须 `-f` 实跑或看历史失败日志。

## 修复

- `collect.logrotate`：把通配改为**显式列文件**（排除 rss-sample.log）；`su kali kali` → `su root root`。
- `collect-rss.logrotate`：补 `su root root`（原无 su，默认即 root，显式化防漂移）；修正乱码注释。

安装步骤见 `collect.logrotate` 顶部注释。

## 验证（应用于主机后必须看到）

```bash
# 1) 干跑整个 logrotate.d 应无 duplicate
sudo logrotate -d /etc/logrotate.conf 2>&1 | grep -i error      # 期望：无输出

# 2) 实跑两份配置，应生成 .1 且原文件被截断（copytruncate）
sudo logrotate -f /etc/logrotate.d/collect /etc/logrotate.d/collect-rss
ls -l /opt/collect/logs/*.log*                                   # 期望：出现 *.log.1.gz

# 3) 服务不再 failed
sudo systemctl start logrotate.service
sudo systemctl status logrotate.service --no-pager | head -3      # 期望：active (exited) / 无 failed
```

## 备注（不属本次修复范围）

- `collect-exec` / `collect-sched` 的 unit 里
  `StandardOutput=append:/opt/collect/logs/.log` —— 路径中的变量未替换，
  多出一个隐藏文件 `.log`。glob `*.log` 不匹配点文件，故与轮转无关，但建议一并修正。
- 主配置 `/etc/logrotate.conf` 全局为 `weekly` + `rotate 4`；本目录两份配置
  各自 `daily` 覆盖，行为符合 collect 日志量预期。