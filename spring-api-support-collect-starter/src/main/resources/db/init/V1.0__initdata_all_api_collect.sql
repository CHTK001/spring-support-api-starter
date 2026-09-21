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
