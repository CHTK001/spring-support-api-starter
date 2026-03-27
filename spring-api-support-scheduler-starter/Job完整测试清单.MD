# Job完整测试清单

## 测试目标

先只验证 `spring-support-job-starter` 本身的核心功能，不掺支付业务。  
当前清单聚焦这 6 类能力：

- 任务编号生成，避免对外长期依赖自增 `job_id`
- 调度类型计算，覆盖 `CRON/FIXED/FIXED_MS/DELAY/AT/NONE`
- 编号化日志文件路径
- 任务详细日志自主写入
- 脚本任务执行
- 远程执行器直连执行
- Spring 简单控制台低内存启动与账号密码登录
- `browser + H2` 环境下的命名空间建表与控制台会话接口

## 测试环境

- 测试日期: 2026-03-25
- 模块: `spring-support-parent-starter/spring-support-job-starter`
- 测试方式: JUnit 5 单元测试
- 当前状态: 已完成

## 测试清单

| 编号 | 测试项 | 目标 | 状态 | 结果摘要 |
| --- | --- | --- | --- | --- |
| JUT01 | 任务编号生成 | `JobNumberGenerator` 生成可读且不重复的 `jobNo/jobLogNo` | 通过 | `JobNumberGeneratorTest` 已执行，`JOB*` / `JOBLOG*` 前缀正确，连续两次生成值不同 |
| JUT02 | 调度类型 | `JobHelper` 支持 `CRON/FIXED/FIXED_MS/DELAY/AT/NONE` | 通过 | `JobHelperTest` 已执行，6 种调度类型都能返回预期结果，`DELAY` 在已触发后返回 `null` |
| JUT03 | 编号化日志文件 | `JobFileAppender` 按 `日期/jobNo/jobLogNo.log` 生成并读取日志 | 通过 | `JobFileAppenderTest` 已执行，日志按 `日期/jobNo/jobLogNo.log` 创建并可增量读取 |
| JUT04 | 详细日志写入 | `JobLogDetailService` 写表字段和日志文件内容都完整 | 通过 | `JobLogDetailServiceTest` 已执行，`jobNo/jobLogNo/handler/profile/address/filePath` 都被正确写入并同步追加到日志文件 |
| JUT05 | 脚本任务执行 | `ScriptJobHandler` 能执行脚本并把输出写入编号化日志文件 | 通过 | `ScriptJobHandlerTest` 已执行，Python 脚本正常运行，输出进入编号化日志文件 |
| JUT06 | 远程执行器直连 | `RemoteJobExecutorDispatchService` 用 `jobNo/logNo` 串联一次远程执行 | 通过 | `RemoteJobExecutorDispatchServiceTest` 已执行，远程请求可用 `jobNo/logNo` 串联并成功落日志文件 |
| JUT07 | 平台编号化编译 | `scheduler` 平台侧的 `jobNo + REMOTE/LOCAL` 改造可编译 | 通过 | `mvn -pl spring-api-support-scheduler-starter -am -DskipTests compile` 已通过 |
| JUT08 | 控制台登录校验 | `scheduler` 简单控制台在低内存下可启动，并支持账号密码登录 | 通过 | `-Xmx768m -XX:MaxMetaspaceSize=256m` 启动成功；未登录访问 `/job-console/api/namespaces` 返回 `401`，登录后无需 OAuth Token 即可访问 `/job-console/api/namespaces` 与 `/job-console/api/default/task/list` |
| JUT09 | Browser H2 命名空间联调 | `scheduler(browser)` 在 H2 下自动建默认表和命名空间表，并通过控制台 Session 正常访问 | 通过 | `java -jar ... --spring.profiles.active=browser` 已通过，`default/payment` namespace 都能返回 `code=00000` 的空任务列表，说明 H2 命名空间表初始化和控制台 API 同时可用 |

## 执行记录

- 2026-03-25 08:49:08 JUT07 完成：`scheduler` 平台侧编号优先、远程/本地分发区分逻辑已编译通过。
- 2026-03-25 08:53:59 JUT01 完成：`JobNumberGeneratorTest` 执行通过，任务编号和日志编号均按预期生成。
- 2026-03-25 08:54:26 JUT02 完成：`JobHelperTest` 执行通过，`CRON/FIXED/FIXED_MS/DELAY/AT/NONE` 六种调度类型覆盖完成。
- 2026-03-25 08:54:57 JUT03 完成：`JobFileAppenderTest` 执行通过，编号化日志目录和读取逻辑验证完成。
- 2026-03-25 08:55:37 JUT04 完成：`JobLogDetailServiceTest` 执行通过，结构化详细日志写库字段和日志文件内容均符合预期。
- 2026-03-25 08:56:02 JUT05 完成：`ScriptJobHandlerTest` 执行通过，脚本任务输出成功写入按任务编号归档的日志文件。
- 2026-03-25 08:56:36 JUT06 完成：`RemoteJobExecutorDispatchServiceTest` 执行通过，远程执行器可基于 `jobNo/logNo` 直接执行并落编号化日志。
- 2026-03-25 20:13:16 JUT08 完成：`scheduler` 简单控制台已在 `-Xmx768m -XX:MaxMetaspaceSize=256m` 下启动成功；未登录访问控制台专用 API 返回 `401`，使用 `admin/admin123456` 登录后可直接访问 `/job-console/index.html`、`/job-console/api/namespaces` 和 `/job-console/api/default/task/list`，不再依赖额外 OAuth Token。
- 2026-03-25 21:07:45 JUT09 完成：新增 `application-browser.yml` 后，`scheduler` 以 `browser` profile 使用 H2 内存库低内存启动成功；`/job-console/auth/status` 返回未登录状态，登录后 `/job-console/api/namespaces` 返回 `["default","payment"]`，`/job-console/api/default/task/list` 与 `/job-console/api/payment/task/list` 都返回 `code=00000` 的空数组，说明 H2 下默认表和命名空间表已被自动创建且控制台链路正常。
