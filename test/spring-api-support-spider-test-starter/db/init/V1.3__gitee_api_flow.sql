-- 将 Gitee 示例从易触发验证码的 Explore HTML 页面切换为稳定的公开 API。
-- 目标：默认 smoke 可无人值守出数，反爬人工介入能力仍保留在节点能力中，不绑定示例成功路径。

DELETE FROM spider_flow_edge WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_flow_node WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_flow WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_definition WHERE spider_code = 'gitee-explore-agent';

INSERT INTO spider_definition (
    spider_code, spider_name, spider_entry_url, spider_site_host,
    spider_executor_type, spider_executor_rounds, spider_executor_job_enabled,
    spider_executor_config,
    spider_collect_deep_url, spider_notify_on_failure,
    spider_dedup_strategy, spider_url_storage_type, spider_status,
    spider_remark
) VALUES (
    'gitee-explore-agent', 'Gitee API Agent 项目采集',
    'https://gitee.com/api/v5/users/georgecao/repos?page=1&per_page=20&type=all',
    'gitee.com',
    'ONCE', 1, 0,
    '{"orchestrationMode":"MANUAL"}',
    0, 0,
    'URL_FINGERPRINT', 'DATABASE', 1,
    '人工编排：Gitee API 项目采集流程，HTTP 访问公开仓库列表，JSON 确定性解析并写入 gitee_explore_project'
);

INSERT INTO spider_flow (
    spider_definition_code, spider_flow_version, spider_flow_name,
    spider_flow_scope, spider_flow_schema, spider_flow_published
) VALUES (
    'gitee-explore-agent', 1, 'Gitee API 项目采集', 'PRIVATE',
'{
  "spiderDefinitionCode": "gitee-explore-agent",
  "spiderFlowVersion": 1,
  "spiderFlowName": "Gitee API 项目采集",
  "spiderFlowScope": "PRIVATE",
  "orchestrationType": "MANUAL",
  "nodes": [
    {"id":"start","type":"START","title":"开始","position":{"x":70,"y":260},"config":{"entryUrl":"https://gitee.com/api/v5/users/georgecao/repos?page=1&per_page=20&type=all"}},
    {"id":"page-access","type":"PAGE_ACCESS","title":"页面访问","position":{"x":210,"y":260},"config":{"accessMode":"HTTP","targetUrl":"https://gitee.com/api/v5/users/georgecao/repos?page={pageNum}&per_page=20&type=all","pageNumAdvanceAfterAccess":true,"headerJson":"{\"Accept\":\"application/json\",\"User-Agent\":\"vue-support-spider-test\"}"}},
    {"id":"data-parse","type":"DATA_PARSE","title":"结构化解析数据","position":{"x":430,"y":260},"config":{"schemaCode":"gitee-explore","parseMode":"JSON_PATH","parseSource":"PAGE_HTML"}},
    {"id":"save-result","type":"SAVE_RESULT","title":"保存结果","position":{"x":650,"y":140},"config":{"schemaCode":"gitee-explore","saveMode":"INCREMENTAL","outputTableName":"gitee_explore_project","dedupFields":["repo_url"],"resultMappings":[{"contextField":"name","columnName":"name"},{"contextField":"owner","columnName":"owner"},{"contextField":"stars","columnName":"stars"},{"contextField":"language","columnName":"language"},{"contextField":"description","columnName":"description"},{"contextField":"repoUrl","columnName":"repo_url"}]}},
    {"id":"check-count","type":"CONDITION","title":"数据量≥20？","position":{"x":650,"y":380},"config":{"expression":"collectedCount >= 20","trueLabel":"满足，结束","falseLabel":"不满足，继续翻页"}},
    {"id":"page-next","type":"PAGE_ACCESS","title":"翻页","position":{"x":870,"y":380},"config":{"accessMode":"HTTP","targetUrl":"https://gitee.com/api/v5/users/georgecao/repos?page={pageNum}&per_page=20&type=all","pageNumAdvanceAfterAccess":true,"headerJson":"{\"Accept\":\"application/json\",\"User-Agent\":\"vue-support-spider-test\"}"}},
    {"id":"end","type":"END","title":"结束","position":{"x":1090,"y":380},"config":{}}
  ],
  "edges": [
    {"id":"e1","source":"start","target":"page-access","sourceOutput":"output","targetInput":"input"},
    {"id":"e2","source":"page-access","target":"data-parse","sourceOutput":"output","targetInput":"input"},
    {"id":"e3","source":"data-parse","target":"check-count","sourceOutput":"output","targetInput":"input"},
    {"id":"e4","source":"data-parse","target":"save-result","sourceOutput":"output","targetInput":"input","label":"保存数据"},
    {"id":"e5","source":"check-count","target":"end","sourceOutput":"true","targetInput":"input","label":"满足"},
    {"id":"e6","source":"check-count","target":"page-next","sourceOutput":"false","targetInput":"input","label":"不满足"},
    {"id":"e7","source":"page-next","target":"data-parse","sourceOutput":"output","targetInput":"input"}
  ]
}', 1);

INSERT INTO spider_flow_node (
    spider_definition_code, spider_flow_version, spider_node_code,
    spider_node_type, spider_node_name, spider_node_config,
    spider_notify_config, spider_sort_num
) VALUES
('gitee-explore-agent', 1, 'start', 'START', '开始',
 '{"entryUrl":"https://gitee.com/api/v5/users/georgecao/repos?page=1&per_page=20&type=all"}', '{}', 10),
('gitee-explore-agent', 1, 'page-access', 'PAGE_ACCESS', '页面访问',
 '{"accessMode":"HTTP","targetUrl":"https://gitee.com/api/v5/users/georgecao/repos?page={pageNum}&per_page=20&type=all","pageNumAdvanceAfterAccess":true,"headerJson":"{\"Accept\":\"application/json\",\"User-Agent\":\"vue-support-spider-test\"}"}', '{}', 20),
('gitee-explore-agent', 1, 'data-parse', 'DATA_PARSE', '结构化解析数据',
 '{"schemaCode":"gitee-explore","parseMode":"JSON_PATH","parseSource":"PAGE_HTML"}', '{}', 30),
('gitee-explore-agent', 1, 'save-result', 'SAVE_RESULT', '保存结果',
 '{"schemaCode":"gitee-explore","saveMode":"INCREMENTAL","outputTableName":"gitee_explore_project","dedupFields":["repo_url"],"resultMappings":[{"contextField":"name","columnName":"name"},{"contextField":"owner","columnName":"owner"},{"contextField":"stars","columnName":"stars"},{"contextField":"language","columnName":"language"},{"contextField":"description","columnName":"description"},{"contextField":"repoUrl","columnName":"repo_url"}]}', '{}', 40),
('gitee-explore-agent', 1, 'check-count', 'CONDITION', '数据量≥20？',
 '{"expression":"collectedCount >= 20","trueLabel":"满足，结束","falseLabel":"不满足，继续翻页"}', '{}', 50),
('gitee-explore-agent', 1, 'page-next', 'PAGE_ACCESS', '翻页',
 '{"accessMode":"HTTP","targetUrl":"https://gitee.com/api/v5/users/georgecao/repos?page={pageNum}&per_page=20&type=all","pageNumAdvanceAfterAccess":true,"headerJson":"{\"Accept\":\"application/json\",\"User-Agent\":\"vue-support-spider-test\"}"}', '{}', 60),
('gitee-explore-agent', 1, 'end', 'END', '结束', '{}', '{}', 70);

INSERT INTO spider_flow_edge (
    spider_definition_code, spider_flow_version, spider_edge_id,
    spider_source_node_code, spider_target_node_code, spider_edge_label,
    spider_source_output, spider_target_input, spider_sort_num
) VALUES
('gitee-explore-agent', 1, 'e1', 'start', 'page-access', NULL, 'output', 'input', 10),
('gitee-explore-agent', 1, 'e2', 'page-access', 'data-parse', NULL, 'output', 'input', 20),
('gitee-explore-agent', 1, 'e3', 'data-parse', 'check-count', NULL, 'output', 'input', 30),
('gitee-explore-agent', 1, 'e4', 'data-parse', 'save-result', '保存数据', 'output', 'input', 40),
('gitee-explore-agent', 1, 'e5', 'check-count', 'end', '满足', 'true', 'input', 50),
('gitee-explore-agent', 1, 'e6', 'check-count', 'page-next', '不满足', 'false', 'input', 60),
('gitee-explore-agent', 1, 'e7', 'page-next', 'data-parse', NULL, 'output', 'input', 70);
