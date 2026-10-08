-- =====================================================================
-- V1.0__initdata_all_api_collect.sql - spring-api-support-collect-starter 全量种子
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE 按主键/唯一索引去重）。
-- 内容: 一个示例分类、两个全局变量、两条示例流程（都不给调度开关，按需启用）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 示例分类
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `collect_category` (`collect_category_id`, `collect_category_name`, `collect_category_pid`,
                                       `collect_category_tree_id`, `collect_category_sort`, `collect_category_remark`,
                                       `create_name`, `create_time`)
VALUES (1, '示例采集', 0, '/1/', 1, '内置示例，可直接删除', 'system', NOW(6));

-- ---------------------------------------------------------------------
-- 全局变量：示例流程与表达式演示用
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `collect_variable` (`collect_variable_id`, `collect_variable_name`, `collect_variable_value`,
                                       `collect_variable_encrypted`, `collect_variable_description`,
                                       `create_name`, `create_time`)
VALUES (1, 'snapshotUrl', 'demo://collect-variable', 0, '示例流程的种子地址，仅作令牌占位', 'system', NOW(6));

INSERT IGNORE INTO `collect_variable` (`collect_variable_id`, `collect_variable_name`, `collect_variable_value`,
                                       `collect_variable_encrypted`, `collect_variable_description`,
                                       `create_name`, `create_time`)
VALUES (2, 'demoDate', '', 0, '演示日期变量，节点里用 ${demoDate} 引用', 'system', NOW(6));

-- ---------------------------------------------------------------------
-- 示例流程：读平台全局变量表并写成采集结果，不依赖外网，可离线跑通
-- DSL 为 nodes + edges 的 JSON，与前端设计器同一份结构
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `collect_flow` (`collect_flow_id`, `collect_flow_name`, `collect_flow_category_id`,
                                   `collect_flow_code`, `collect_flow_dsl`, `collect_flow_cron`,
                                   `collect_flow_status`, `collect_flow_concurrency`, `collect_flow_thread_count`,
                                   `collect_flow_remark`, `collect_flow_exec_count`,
                                   `collect_flow_is_deleted`, `create_name`, `create_time`)
VALUES (1, '示例-平台变量快照', 1, 'demo-variable-snapshot',
        '{"name":"示例-平台变量快照","threadCount":2,"variables":[],"headers":[],"cookies":[],"nodes":[{"id":"n1","shape":"start","name":"种子","x":60,"y":180,"params":[{"name":"urls","value":"${snapshotUrl}"}],"fields":[]},{"id":"n2","shape":"sql","name":"读取全局变量","x":260,"y":180,"params":[{"name":"statement","value":"select collect_variable_name as var_name, collect_variable_value as var_value from collect_variable"},{"name":"outputVariable","value":"rows"}],"fields":[]},{"id":"n3","shape":"output","name":"写入采集结果","x":470,"y":180,"params":[{"name":"each","value":"${rows}"},{"name":"cell:变量名","value":"${var_name}"},{"name":"cell:变量值","value":"${var_value}"},{"name":"key","value":"${var_name}"},{"name":"url","value":"${url}"}],"fields":[]},{"id":"n4","shape":"end","name":"结束","x":670,"y":180,"params":[],"fields":[]}],"edges":[{"id":"e1","from":"n1","to":"n2","condition":"","exceptionFlow":""},{"id":"e2","from":"n2","to":"n3","condition":"","exceptionFlow":""},{"id":"e3","from":"n3","to":"n4","condition":"","exceptionFlow":""}]}',
        '0 0/30 * * * ?', 0, 1, 2,
        '内置示例：开始 → SQL 读取 collect_variable → 输出为采集结果 → 结束。按需点“启用调度”',
        0, 0, 'system', NOW(6));

-- ---------------------------------------------------------------------
-- 示例流程：采集 GitHub 热门项目并整批入库（需要外网）
--
-- 这条专门演示「目标表还不存在」的编排配置，也是 database 节点最典型的用法：
--   开始       声明 GitHub 搜索接口的种子地址，首条同时写进 ${url}
--   抓取       GET 搜索接口，产出 resp / respJson
--   抽取       列表模式按 JSON 路径 items 切行，逐字段抽出标题/star/地址/描述…
--   数据库     整批写进 collect_github_repo
--   结束
--
-- 关于「表不存在」：
--   database 节点的目标表允许在库里不存在。设计器里表名可以直接新建（不是只能
--   从下拉里挑已有表），字段映射的左列也随之退化成自由输入。首次运行时后端按
--   这批实际取值推断列类型并建表：stars/forks 这类整数落 BIGINT，description
--   超过 200 字符落 TEXT，短的落 VARCHAR。之后再跑只补缺失列，不重建。
--   覆盖写入（mode=replace）会先清空目标表；来源集合解析不出任何行时直接报错
--   并保留原表，不会把唯一可信的历史清掉。
--
-- 限流：GitHub 匿名接口每 IP 每小时 60 次。示例刻意不带头部里的 Authorization，
-- 免得空令牌把请求打成 401；要提高额度就在抓取节点加一条
--   header:Authorization = Bearer <你的 token>
-- 令牌本身建议放全局变量（平台配置 → 变量），别写死在流程里。
--
-- 每轮抓取量：GitHub 搜索单页取满（per_page=100，接口单页上限），覆盖写入目标表。
-- 调度默认开启（collect_flow_status = 1，每日 03:30）：开箱即持续采集，
-- 例子表里始终有最新数据（2026-10-08 用户口径「应该是一直采集」）；
-- 不需要时在设计器或采集器列表点「停用调度」。
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `collect_flow` (`collect_flow_id`, `collect_flow_name`, `collect_flow_category_id`,
                                   `collect_flow_code`, `collect_flow_dsl`, `collect_flow_cron`,
                                   `collect_flow_status`, `collect_flow_concurrency`, `collect_flow_thread_count`,
                                   `collect_flow_remark`, `collect_flow_exec_count`,
                                   `collect_flow_is_deleted`, `create_name`, `create_time`)
VALUES (2, '示例-GitHub 项目采集', 1, 'demo-github-repo',
        '{"name":"示例-GitHub 项目采集","threadCount":2,"variables":[],"headers":[],"cookies":[],"nodes":[{"id":"n1","shape":"start","name":"GitHub 搜索接口","x":60,"y":180,"params":[{"name":"urls","value":"https://api.github.com/search/repositories?q=stars:%3E20000&sort=stars&order=desc&per_page=100"}],"fields":[]},{"id":"n2","shape":"request","name":"调用搜索接口","x":260,"y":180,"params":[{"name":"url","value":"${url}"},{"name":"method","value":"GET"},{"name":"header:Accept","value":"application/vnd.github+json"},{"name":"header:X-GitHub-Api-Version","value":"2022-11-28"},{"name":"timeout","value":"15000"},{"name":"saveDocument","value":"0"},{"name":"outputVariable","value":"resp"}],"fields":[]},{"id":"n3","shape":"extract","name":"抽取项目字段","x":470,"y":180,"params":[{"name":"source","value":"${respJson}"},{"name":"mode","value":"list"},{"name":"rowExpression","value":"items"},{"name":"outputVariable","value":"projects"}],"fields":[{"name":"title","parserType":"JSONPATH","expression":".name"},{"name":"full_name","parserType":"JSONPATH","expression":".full_name"},{"name":"description","parserType":"JSONPATH","expression":".description"},{"name":"stars","parserType":"JSONPATH","expression":".stargazers_count"},{"name":"forks","parserType":"JSONPATH","expression":".forks_count"},{"name":"issues","parserType":"JSONPATH","expression":".open_issues_count"},{"name":"url","parserType":"JSONPATH","expression":".html_url"},{"name":"homepage","parserType":"JSONPATH","expression":".homepage"},{"name":"language","parserType":"JSONPATH","expression":".language"},{"name":"owner","parserType":"JSONPATH","expression":".owner.login"},{"name":"pushed_at","parserType":"JSONPATH","expression":".pushed_at"}]},{"id":"n4","shape":"database","name":"写入 GitHub 项目表","x":700,"y":180,"params":[{"name":"source","value":"${projects}"},{"name":"datasource","value":"app"},{"name":"table","value":"collect_github_repo"},{"name":"mode","value":"replace"},{"name":"column:title","value":"${title}"},{"name":"column:full_name","value":"${full_name}"},{"name":"column:description","value":"${description}"},{"name":"column:stars","value":"${stars}"},{"name":"column:forks","value":"${forks}"},{"name":"column:issues","value":"${issues}"},{"name":"column:url","value":"${url}"},{"name":"column:homepage","value":"${homepage}"},{"name":"column:language","value":"${language}"},{"name":"column:owner","value":"${owner}"},{"name":"column:pushed_at","value":"${pushed_at}"},{"name":"batchSize","value":"200"}],"fields":[]},{"id":"n5","shape":"end","name":"结束","x":910,"y":180,"params":[],"fields":[]}],"edges":[{"id":"e1","from":"n1","to":"n2","condition":"","exceptionFlow":""},{"id":"e2","from":"n2","to":"n3","condition":"","exceptionFlow":""},{"id":"e3","from":"n3","to":"n4","condition":"","exceptionFlow":""},{"id":"e4","from":"n4","to":"n5","condition":"","exceptionFlow":""}]}',
        '0 30 3 * * ?', 1, 1, 2,
        '内置示例：开始 → 抓取 GitHub 搜索接口 → 抽取项目字段 → 数据库节点整批入库。'
        '目标表 collect_github_repo 不存在会在首次运行时自动创建，列类型按实际取值推断；'
        '需要外网与 GitHub 匿名接口配额；已启用每日调度（03:30）持续采集',
        0, 0, 'system', NOW(6));

-- ---------------------------------------------------------------------
-- 存量数据源迁移：collect_datasource -> ms_datasource
--
-- 采集平台的 SQL/入库节点已改为统一读数据源中心（ms_datasource），
-- collect_datasource 不再作为台账，仅保留只读以便回溯与对账。
--
-- 字段映射：
--   collect_datasource_id                -> id（ID 语义保持不变，流程参数无需改写）
--   collect_datasource_name              -> datasource_name
--   collect_datasource_type              -> datasource_type（统一归一为 jdbc）
--   collect_datasource_jdbc_url          -> datasource_database（jdbc: 开头的完整 URL）
--   collect_datasource_username          -> datasource_username
--   collect_datasource_password          -> datasource_password（b64: 前缀原样搬，两侧都能解）
--   collect_datasource_status            -> datasource_status
--   collect_datasource_remark            -> datasource_remark
--   collect_datasource_driver_class_name -> datasource_driver_class_name
--
-- 幂等：以 名称 + JDBC URL 判定已迁则跳过，重复执行不会产生重复行。
-- 旧表保留不删：迁移期 CollectDatasourceRegistry 仍会回落到它，
-- 核对完成、确认无流程仍指向旧 ID 之后再单独下线。
--
-- 排序规则：本条要在 collect_datasource(utf8mb4_unicode_ci) 与
-- ms_datasource(utf8mb4_general_ci) 之间做字符串等值比较。H2 的 MySQL 兼容模式
-- 会隐式转换放行，MariaDB/MySQL 直接报 1267
-- (Illegal mix of collations ... for operation '=')，整份 initdata 因此被记为失败。
-- 故显式把 collect 侧收敛到框架侧的 utf8mb4_general_ci 再比较，语义不变。
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `ms_datasource` (`id`, `datasource_name`, `datasource_type`, `datasource_host`, `datasource_port`,
                                   `datasource_database`, `datasource_username`, `datasource_password`,
                                   `datasource_remark`, `datasource_status`, `datasource_driver_class_name`,
                                   `datasource_create_time`, `datasource_update_time`)
SELECT c.`collect_datasource_id`,
       c.`collect_datasource_name`,
       'jdbc',
       '',
       NULL,
       c.`collect_datasource_jdbc_url`,
       c.`collect_datasource_username`,
       c.`collect_datasource_password`,
       c.`collect_datasource_remark`,
       COALESCE(c.`collect_datasource_status`, 1),
       c.`collect_datasource_driver_class_name`,
       c.`create_time`,
       c.`update_time`
FROM `collect_datasource` c
WHERE c.`collect_datasource_id` IS NOT NULL
  AND c.`collect_datasource_name` IS NOT NULL
  AND c.`collect_datasource_jdbc_url` LIKE 'jdbc:%'
  AND NOT EXISTS (
        SELECT 1
        FROM `ms_datasource` m
        WHERE m.`datasource_name` = c.`collect_datasource_name` COLLATE utf8mb4_general_ci
          AND m.`datasource_database` = c.`collect_datasource_jdbc_url` COLLATE utf8mb4_general_ci
  );
