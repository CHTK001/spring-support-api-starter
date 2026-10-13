# 采集流程引擎 验收测试

两层验收：**单元测试**守纯逻辑回归（随 `mvn test` 跑），**端到端验收脚本**守真实实例上的
流程行为（独立跑，不占构建时间）。

| 层 | 位置 | 场景数 | 运行方式 |
|----|------|--------|----------|
| 单元测试 | `src/test/java/**` | 12 | `mvn -pl spring-api-support-common-parent/spring-api-support-collect-starter test` |
| 端到端 | `docs/acceptance/production-acceptance.py` | 15 | `python3 docs/acceptance/production-acceptance.py` |

端到端脚本零第三方依赖（只用标准库），**读多写少**：建一次性流程 → 触发执行 → 轮询任务与
节点日志 → 按节点执行状态与摘要消息断言。示例流程（ID 1 / 2）只跑不改。

## 单元测试

| 测试类 | 覆盖 |
|--------|------|
| `com.chua.starter.collect.engine.CollectExpressionTest` | `${data.field}` 行列表投影、`${data[0].field}` 首行标量、`${list.field}` 投影、`${url[N]}` 定位、`${url.size()}` 计数、`${env.xxx}` 命名空间、`${data.toJson}` 转 JSON、越界下标与未知成员返回 null |
| `com.chua.starter.collect.support.CollectSqlKindTest` | `isReadOnlyStatement` 判定口径：普通 SELECT/WITH/SHOW、括号 UNION 只读、真写语句必须拦下、空与 null、跳过前导括号不得放松闸门 |

## 端到端场景

| # | 场景 | 断言口径 |
|---|------|----------|
| 1 | metas 口径 | 有 `filter`、无 `script`、有 `exists` |
| 2 | SQL IN 批次 + 行列表投影 | `IN (${data.id})` 拆片执行，SELECT 往返行数一致 |
| 3 | 过滤节点-放行 | 条件为真，下游节点执行 |
| 4 | 过滤节点-拦截 | 条件为假，下游不执行且节点落 `SKIPPED` |
| 5 | 多出边分叉 | 一个节点两条出边同时执行 |
| 6 | 跨节点产出留存 | `${nodes.nA.tag}` 读上游节点留到结束的产出 |
| 7 | condition 判断-真分支 | 表达式为真，`${conditionResult}` 边放行走真分支、`${!conditionResult}` 边拦下假分支 |
| 8 | condition 判断-假分支 | 表达式为假，真假分支对调 |
| 9 | 变量模型 | 多 URL 种子 + `${url[N]}` + `${url.size()}` + `${env.xxx}` |
| 10 | `${data.toJson}` | 当前数据转出含字段名的 JSON 串 |
| 11 | exists 查已存在 | 区分已存在与全新（`已存在 2 / 全新 1`） |
| 12 | 出参自定义名 | `outputVariable` 改名后 `${自定义名}` 可引用 |
| 13 | 括号 UNION 只读查询 | `(select …) union all (select …)` 不被写语句守卫误拒 |
| 14 | 回归-平台变量快照 | `SUCCESS`，2 条记录 |
| 15 | 回归-GitHub 项目采集 | `SUCCESS`，100 条记录 |

> condition 判断的两条出边都设 `transmitVariable=1`，下游 filter 直接读 `${conditionResult}`，
> 一并验证「上游变量经出边透传」——引擎 `dispatch` 的子作用域默认只含 root 变量，
> 上游变量必须靠 `transmitVariable` 才传得下去。

## 运行

```bash
# 默认连本机 18190
python3 docs/acceptance/production-acceptance.py

# 指定实例与账号
python3 docs/acceptance/production-acceptance.py \
    --base http://192.168.50.198:18190/collect/api \
    --user admin --password 123456

# 或用环境变量
COLLECT_BASE=http://192.168.50.198:18190/collect/api \
COLLECT_USER=admin COLLECT_PASSWORD=123456 \
    python3 docs/acceptance/production-acceptance.py
```

退出码 `0` = 全部通过，`1` = 有失败或登录失败，可直接接入 CI / 发布卡点。

登录走 `POST /collect/api/v2/user/login`（`loginType=WEB`），令牌写进 `x-oauth-token` 头。

## 两个被这两层测试守住的缺陷

1. **`${data.toJson}` 不可用**：`member()` 没有 `toJson` 分支，用户明确要求的写法返回 null。
   引擎原本只派生独立的 `${dataJson}` 变量，语义到位但写法对不上。
2. **括号 UNION 被误判为写语句**：`(select …) union all (select …)` 以左括号开头，
   两处「只读判定」都只看首词 —— SQL 节点在 `plugin.collect.sql-allow-write=false` 下拒收
   （「写语句已被配置禁用」），走到 JDBC 支撑层又抛「查询节点只能执行 SELECT 语句」，
   两头都堵死合法的只读 UNION。判定口径已抽成 `CollectJdbcSupport.isReadOnlyStatement`
   单一真源，SQL 节点执行器与 JDBC 支撑层共用。
