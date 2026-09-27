-- =====================================================================
-- V1.0__initdata_all_api_collect.sql - spring-api-support-collect-starter 全量种子
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE 按主键/唯一索引去重）。
-- 内容: 一个示例分类、两个全局变量、一条离线可跑的示例流程（不给调度开关，按需启用）。
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
