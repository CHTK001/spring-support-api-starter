# -*- coding: utf-8 -*-
"""
采集流程引擎 生产级验收脚本（端到端，11 个场景）

覆盖口径
--------
1. metas 口径：存在 filter（判断节点）、不存在 script（脚本节点已下线）、存在 exists（查已存在节点）
2. SQL IN 批次：IN (${列表}) 自动拆片 + ${data.id} 行列表投影，SELECT 合并行数
3. 过滤节点放行：条件为真时下游继续执行
4. 过滤节点拦截：条件为假时下游不执行、节点落 SKIPPED
5. 多出边分叉：一个节点两条出边同时执行（引擎原生支持）
6. 变量模型：多 URL 种子 + ${url[N]} 定位 + ${url.size()} 计数 + ${env.xxx} 命名空间
7. ${data.toJson}：当前数据转 JSON 串
8. exists 查已存在：区分已存在与全新（已存在 2 / 全新 1）
9. 出参自定义名：outputVariable 改名后 ${自定义名} 可直接引用
10. 括号 UNION 只读查询：(select …) union all (select …) 不被写语句守卫误拒
11. 示例流程回归：平台变量快照、GitHub 项目采集

断言方式
--------
流程跑在真实生产实例上。可观测断言用两种：
  * 节点执行状态（task/logs 里各节点 SUCCESS / SKIPPED）——分支是否走到；
  * 节点摘要消息（"返回 N 行" / "已存在 N / 全新 N"）——数量是否对上。

用法
----
    python3 docs/acceptance/production-acceptance.py
    python3 docs/acceptance/production-acceptance.py --base http://192.168.50.198:18190/collect/api

环境变量（均可用同名参数覆盖）
    COLLECT_BASE      服务根地址，默认 http://127.0.0.1:18190/collect/api
    COLLECT_USER      登录用户名，默认 admin
    COLLECT_PASSWORD  登录密码，默认 123456

退出码：0 = 全部通过；1 = 有失败或登录失败。
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request

RESULTS = []


def parse_args():
    """解析命令行参数。"""
    parser = argparse.ArgumentParser(description="采集流程引擎生产级验收")
    parser.add_argument("--base", default=os.environ.get("COLLECT_BASE", "http://127.0.0.1:18190/collect/api"),
                        help="服务根地址")
    parser.add_argument("--user", default=os.environ.get("COLLECT_USER", "admin"), help="登录用户名")
    parser.add_argument("--password", default=os.environ.get("COLLECT_PASSWORD", "123456"), help="登录密码")
    parser.add_argument("--timeout", type=int, default=90, help="单次请求超时秒数")
    return parser.parse_args()


ARGS = parse_args()


def call(path, method="GET", data=None, token=None):
    """调用 REST 接口。

    :param path: 接口路径，以 / 开头
    :param method: HTTP 方法
    :param data: 请求体，None 表示无体
    :param token: x-oauth-token，None 表示免登录接口
    :return: 解析后的响应体
    """
    headers = {"Content-Type": "application/json"}
    if token:
        headers["x-oauth-token"] = token
    body = json.dumps(data, ensure_ascii=False).encode("utf-8") if data is not None else None
    req = urllib.request.Request(ARGS.base + path, data=body, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=ARGS.timeout) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        try:
            return json.loads(raw)
        except ValueError:
            return {"ok": False, "msg": "HTTP %s: %s" % (exc.code, raw[:400])}
    except Exception as exc:  # noqa: BLE001 - 验收脚本需要把任何异常转成可读结果
        return {"ok": False, "msg": "%s: %s" % (type(exc).__name__, exc)}


def record(name, ok, detail):
    """记录一条断言结果。

    :param name: 场景名
    :param ok: 是否通过
    :param detail: 证据说明
    """
    RESULTS.append((name, ok, detail))
    print("[%s] %s :: %s" % ("PASS" if ok else "FAIL", name, detail))


def save_flow(token, name, dsl):
    """保存流程。

    :param token: 登录令牌
    :param name: 流程名
    :param dsl: 流程 DSL
    :return: 流程 ID，失败返回 None
    """
    resp = call("/v2/collect/flow/save", "POST", {
        "collectFlowName": name,
        "collectFlowCategoryId": 0,
        "collectFlowThreadCount": 1,
        "collectFlowRemark": "生产级验收",
        "dsl": dsl,
    }, token)
    return resp["data"]["collectFlowId"] if resp.get("ok") and resp.get("data") else None


def run_wait(token, flow_id, limit=150):
    """触发一次执行并轮询到终态。

    :param token: 登录令牌
    :param flow_id: 流程 ID
    :param limit: 最长等待秒数
    :return: (任务详情, {节点ID: [(状态, 消息)]}, 错误说明)
    """
    resp = call("/v2/collect/execution/run", "POST", {
        "collectFlowId": flow_id,
        "variables": None,
        "persist": False,
    }, token)
    if not resp.get("ok") or not resp.get("data"):
        return None, {}, json.dumps(resp, ensure_ascii=False)[:300]
    task_id = resp["data"]["collectTaskId"]
    final = resp["data"]
    deadline = time.time() + limit
    while final.get("collectTaskStatus") == "RUNNING" and time.time() < deadline:
        time.sleep(2)
        detail = call("/v2/collect/task/detail?taskId=%s" % task_id, "GET", None, token)
        if detail.get("ok") and detail.get("data"):
            final = detail["data"]
        else:
            break
    logs = call("/v2/collect/task/logs?taskId=%s" % task_id, "GET", None, token)
    rows = (logs.get("data") or []) if logs.get("ok") else []
    nodes = {}
    for row in rows:
        nodes.setdefault(row.get("collectTaskLogNodeId"), []).append(
            (row.get("collectTaskLogStatus"), row.get("collectTaskLogMessage")))
    return final, nodes, None


def edge(frm, to):
    """造一条无条件的普通出边。

    :param frm: 起始节点 ID
    :param to: 目标节点 ID
    :return: 边对象
    """
    return {"id": "e-%s-%s" % (frm, to), "from": frm, "to": to,
            "condition": "", "exceptionFlow": "", "transmitVariable": None}


def cond_edge(frm, to, condition, transmit=True):
    """造一条带流转条件的出边。

    :param frm: 起始节点 ID
    :param to: 目标节点 ID
    :param condition: 流转条件表达式
    :param transmit: 是否向下游传递上游变量
    :return: 边对象
    """
    return {"id": "e-%s-%s" % (frm, to), "from": frm, "to": to,
            "condition": condition, "exceptionFlow": 0, "transmitVariable": 1 if transmit else 0}


def start_node():
    """开始节点（单 URL）。"""
    return {"id": "n1", "shape": "start", "name": "开始", "x": 40, "y": 140,
            "params": [{"name": "urls", "value": "https://example.com/"}], "fields": []}


def end_node(x):
    """结束节点。

    :param x: 画布横坐标
    :return: 节点对象
    """
    return {"id": "nEnd", "shape": "end", "name": "结束", "x": x, "y": 140, "params": [], "fields": []}


def filter_node(node_id, expr, x):
    """判断节点。

    :param node_id: 节点 ID
    :param expr: 判断表达式
    :param x: 画布横坐标
    :return: 节点对象
    """
    return {"id": node_id, "shape": "filter", "name": node_id, "x": x, "y": 140,
            "params": [{"name": "expression", "value": expr},
                       {"name": "outputVariable", "value": "data"}], "fields": []}


def sql_node(node_id, statement, x, output="data", extra=None):
    """SQL 节点。

    :param node_id: 节点 ID
    :param statement: SQL 语句
    :param x: 画布横坐标
    :param output: 出参名
    :param extra: 额外参数
    :return: 节点对象
    """
    params = [{"name": "statement", "value": statement}, {"name": "outputVariable", "value": output}]
    if extra:
        params.extend(extra)
    return {"id": node_id, "shape": "sql", "name": node_id, "x": x, "y": 140, "params": params, "fields": []}


def chain(name, nodes, edges, variables=None, token=None):
    """建一条流程并跑到终态。

    :param name: 流程名
    :param nodes: 节点列表
    :param edges: 边列表
    :param variables: 流程变量
    :param token: 登录令牌
    :return: (任务详情, 节点状态表)；建流程失败时返回 (None, {})
    """
    dsl = {"name": name, "threadCount": 1, "headers": [], "cookies": [],
           "variables": variables or [], "nodes": nodes, "edges": edges}
    flow_id = save_flow(token, name, dsl)
    if not flow_id:
        return None, {}
    final, nodes, _ = run_wait(token, flow_id)
    return final, nodes


def reached(nodes, node_id):
    """某节点是否成功执行过。

    :param nodes: 节点状态表
    :param node_id: 节点 ID
    :return: 是否成功
    """
    return any(status == "SUCCESS" for status, _ in nodes.get(node_id, []))


def messages(nodes, node_id):
    """取某节点的摘要消息。

    :param nodes: 节点状态表
    :param node_id: 节点 ID
    :return: 消息列表
    """
    return [msg for _, msg in nodes.get(node_id, []) if msg]


def main():
    """跑全部场景并汇总。"""
    login = call("/v2/user/login", "POST",
                 {"username": ARGS.user, "password": ARGS.password, "loginType": "WEB"})
    if not login.get("ok") or not login.get("data"):
        print("LOGIN_FAILED", json.dumps(login, ensure_ascii=False))
        return 1
    token = login["data"]["accessToken"]
    print("LOGIN_OK base=%s" % ARGS.base)

    # 1. metas 口径
    metas = call("/v2/collect/designer/metas", "GET", None, token)
    shapes = [item.get("shape") for item in (metas.get("data") or [])]
    record("metas口径(有filter/无script/有exists)",
           "filter" in shapes and "script" not in shapes and "exists" in shapes,
           "count=%d filter=%s script=%s exists=%s"
           % (len(shapes), "filter" in shapes, "script" in shapes, "exists" in shapes))

    # 2. SQL IN 批次 + 行列表投影
    final, nodes = chain("ZZ-ACC-SQLBATCH", [
        start_node(),
        sql_node("n2", "select collect_task_id as id from collect_task", 200),
        sql_node("n3", "select collect_task_id as id from collect_task "
                       "where collect_task_id in (${data.id})", 380,
                 extra=[{"name": "batchSize", "value": "50"}]),
        end_node(680),
    ], [edge("n1", "n2"), edge("n2", "n3"), edge("n3", "nEnd")], token=token)
    record("SQL-IN批次+行列表投影",
           bool(final) and final.get("collectTaskStatus") == "SUCCESS"
           and messages(nodes, "n3") and messages(nodes, "n3")[0].startswith("返回"),
           "status=%s n2=%s n3=%s" % (final.get("collectTaskStatus") if final else None,
                                      messages(nodes, "n2")[:1], messages(nodes, "n3")[:1]))

    # 3 / 4. 过滤节点放行与拦截
    def filter_flow(name, expr):
        """造一条「取数 -> 过滤 -> 下游」的流程。

        :param name: 流程名
        :param expr: 过滤表达式
        :return: 跑完的结果
        """
        return chain(name, [
            start_node(),
            sql_node("n2", "select collect_task_id as id from collect_task", 200),
            filter_node("n3", expr, 360),
            sql_node("n4", "select 1 as marker", 520),
            end_node(680),
        ], [edge("n1", "n2"), edge("n2", "n3"), edge("n3", "n4"), edge("n4", "nEnd")], token=token)

    _, nodes = filter_flow("ZZ-ACC-FILTER-PASS", "${data.size() > 0}")
    record("过滤节点-放行", reached(nodes, "n4"), "下游节点执行=%s" % reached(nodes, "n4"))

    _, nodes = filter_flow("ZZ-ACC-FILTER-STOP", "${data.size() > 999999}")
    record("过滤节点-拦截", (not reached(nodes, "n4")) and "SKIPPED" in [s for s, _ in nodes.get("n3", [])],
           "下游未执行=%s 拦截态=%s" % (not reached(nodes, "n4"),
                                    "SKIPPED" in [s for s, _ in nodes.get("n3", [])]))

    # 5. 多出边分叉
    _, nodes = chain("ZZ-ACC-FORK", [
        start_node(),
        sql_node("nA", "select 'A' as b, collect_task_id as id from collect_task limit 5", 220),
        sql_node("nB", "select 'B' as b, collect_task_id as id from collect_task limit 3", 220),
        end_node(420),
    ], [edge("n1", "nA"), edge("n1", "nB"), edge("nA", "nEnd"), edge("nB", "nEnd")], token=token)
    record("多出边分叉", reached(nodes, "nA") and reached(nodes, "nB"),
           "分支A=%s 分支B=%s" % (reached(nodes, "nA"), reached(nodes, "nB")))

    # 5b. 跨节点产出留存：${nodes.nA.tag} 读上游节点留到结束的产出
    _, nodes = chain("ZZ-ACC-NODEREF", [
        start_node(),
        sql_node("nA", "select 'AAA' as tag", 180, output="dataA"),
        sql_node("nB", "select '${nodes.nA.tag}' as inherited", 360),
        filter_node("f1", "${data[0].inherited.contains('AAA')}", 520),
        end_node(680),
    ], [edge("n1", "nA"), edge("nA", "nB"), edge("nB", "f1"), edge("f1", "nEnd")], token=token)
    record("跨节点产出留存(${nodes.nA.tag})", reached(nodes, "nEnd"),
           "到结束=%s nB=%s" % (reached(nodes, "nEnd"), messages(nodes, "nB")[:1]))

    # 6b. condition 判断节点 + 出边流转条件真假分流（含 transmitVariable 传变量）
    def condition_flow(name, expr):
        """造一条「判断 -> 真/假两条带条件出边 -> 各自 filter」的流程。

        真边 condition=${conditionResult}，假边 condition=${!conditionResult}，
        两条边都 transmitVariable=1 —— 下游 filter 直接读 ${conditionResult}，
        顺带验证上游变量经出边透传。

        :param name: 流程名
        :param expr: 判断表达式
        :return: 跑完的结果
        """
        return chain(name, [
            start_node(),
            {"id": "n2", "shape": "condition", "name": "判断", "x": 180, "y": 140,
             "params": [{"name": "expression", "value": expr},
                        {"name": "outputVariable", "value": "conditionResult"}], "fields": []},
            filter_node("nT", "${conditionResult}", 380),   # 真分支目标
            filter_node("nF", "${conditionResult}", 380),   # 假分支目标
            end_node(560),
        ], [
            cond_edge("n1", "n2", ""),
            cond_edge("n2", "nT", "${conditionResult}"),
            cond_edge("n2", "nF", "${!conditionResult}"),
            edge("nT", "nEnd"), edge("nF", "nEnd"),
        ], token=token)

    # 真分支：表达式为真 -> 走 nT，nF 被边条件拦下
    _, nodes = condition_flow("ZZ-ACC-COND-TRUE", "${1 == 1}")
    record("condition判断-真分支走通", reached(nodes, "nT") and not reached(nodes, "nF"),
           "真分支nT=%s 假分支nF=%s" % (reached(nodes, "nT"), reached(nodes, "nF")))

    # 假分支：表达式为假 -> 走 nF，nT 被边条件拦下
    _, nodes = condition_flow("ZZ-ACC-COND-FALSE", "${1 == 2}")
    record("condition判断-假分支走通", reached(nodes, "nF") and not reached(nodes, "nT"),
           "真分支nT=%s 假分支nF=%s" % (reached(nodes, "nT"), reached(nodes, "nF")))

    # 6. 变量模型：多 URL + ${url[N]} + ${url.size()} + ${env.xxx}
    multi_start = {"id": "n1", "shape": "start", "name": "开始", "x": 40, "y": 140,
                   "params": [{"name": "urls", "value": "https://alpha.test/\nhttps://beta.test/"}],
                   "fields": []}
    _, nodes = chain("ZZ-ACC-URL-ENV", [
        multi_start,
        filter_node("f1", "${url.size() == 2}", 180),
        filter_node("f2", "${url[0].contains('alpha')}", 300),
        filter_node("f3", "${url[1].contains('beta')}", 420),
        filter_node("f4", "${env.myToken.contains('secret')}", 540),
        end_node(660),
    ], [edge("n1", "f1"), edge("f1", "f2"), edge("f2", "f3"), edge("f3", "f4"), edge("f4", "nEnd")],
        variables=[{"name": "myToken", "value": "secret123"}], token=token)
    record("多URL+${url[N]}+${url.size()}+${env.xxx}", reached(nodes, "nEnd"),
           "到结束=%s 未放行=%s" % (reached(nodes, "nEnd"),
                                  [nid for nid in ["f1", "f2", "f3", "f4"] if not reached(nodes, nid)]))

    # 7. ${data.toJson}
    _, nodes = chain("ZZ-ACC-TOJSON", [
        start_node(),
        sql_node("n2", "select collect_task_id as id from collect_task limit 3", 180),
        filter_node("f1", "${data.toJson.contains('id')}", 340),
        end_node(500),
    ], [edge("n1", "n2"), edge("n2", "f1"), edge("f1", "nEnd")], token=token)
    record("${data.toJson}转JSON", reached(nodes, "nEnd"), "到结束=%s" % reached(nodes, "nEnd"))

    # 8. exists 查已存在：区分已存在与全新
    _, nodes = chain("ZZ-ACC-EXISTS", [
        start_node(),
        sql_node("n2", "(select collect_flow_id as id from collect_flow limit 2) "
                       "union all (select 888888 as id)", 180),
        {"id": "n3", "shape": "exists", "name": "查已存在", "x": 340, "y": 140,
         "params": [{"name": "table", "value": "collect_flow"},
                    {"name": "keyColumn", "value": "collect_flow_id"},
                    {"name": "keyField", "value": "id"},
                    {"name": "outputVariable", "value": "data"}], "fields": []},
        filter_node("f1", "${data.existsCount == 2}", 500),
        filter_node("f2", "${data.freshCount == 1}", 620),
        end_node(740),
    ], [edge("n1", "n2"), edge("n2", "n3"), edge("n3", "f1"), edge("f1", "f2"), edge("f2", "nEnd")], token=token)
    record("exists查已存在(2存在/1全新)", reached(nodes, "nEnd"),
           "到结束=%s n3=%s" % (reached(nodes, "nEnd"), messages(nodes, "n3")[:1]))

    # 9. 出参自定义名
    _, nodes = chain("ZZ-ACC-CUSTOMOUT", [
        start_node(),
        sql_node("n2", "select 1 as x", 180, output="myResult"),
        filter_node("f1", "${myResult.size() == 1}", 340),
        end_node(500),
    ], [edge("n1", "n2"), edge("n2", "f1"), edge("f1", "nEnd")], token=token)
    record("出参自定义名(myResult)", reached(nodes, "nEnd"),
           "到结束=%s (${myResult}可引用)" % reached(nodes, "nEnd"))

    # 10. 括号 UNION 只读查询不被写语句守卫误拒
    _, nodes = chain("ZZ-ACC-UNION", [
        start_node(),
        sql_node("n2", "(select collect_flow_id as id from collect_flow limit 2) "
                       "union all (select 888888 as id)", 180),
        filter_node("f1", "${data.size() == 3}", 340),
        end_node(500),
    ], [edge("n1", "n2"), edge("n2", "f1"), edge("f1", "nEnd")], token=token)
    record("括号UNION只读查询", reached(nodes, "nEnd"),
           "到结束=%s n2=%s" % (reached(nodes, "nEnd"), messages(nodes, "n2")[:1]))

    # 11. 示例流程回归
    for flow_id, label in [(1, "平台变量快照"), (2, "GitHub项目采集")]:
        final, nodes, err = run_wait(token, flow_id)
        record("回归-%s" % label,
               bool(final) and final.get("collectTaskStatus") == "SUCCESS",
               "status=%s records=%s" % (final.get("collectTaskStatus") if final else None,
                                         final.get("collectTaskRecordCount") if final else None))

    passed = sum(1 for _, ok, _ in RESULTS if ok)
    total = len(RESULTS)
    print("\n===== 验收汇总: %d/%d PASS =====" % (passed, total))
    for name, ok, _ in RESULTS:
        print("  [%s] %s" % ("PASS" if ok else "FAIL", name))
    print("OVERALL:", "PASS" if passed == total else "FAIL")
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
