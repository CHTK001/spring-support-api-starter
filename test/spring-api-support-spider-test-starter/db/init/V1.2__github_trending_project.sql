-- GitHub Trending 项目采集表。
-- 独立迁移用于已执行过 V1.0__init_spider.sql 的测试库补表。

CREATE TABLE IF NOT EXISTS `github_trending_project` (
    `spider_id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code` VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`    VARCHAR(128) NOT NULL COMMENT '执行批次号',

    `name`                   VARCHAR(512) DEFAULT NULL COMMENT '项目名称',
    `owner`                  VARCHAR(255) DEFAULT NULL COMMENT '项目所有者',
    `stars`                  VARCHAR(64) DEFAULT '0' COMMENT '星标数',
    `language`               VARCHAR(128) DEFAULT NULL COMMENT '编程语言',
    `description`            TEXT DEFAULT NULL COMMENT '项目描述',
    `repo_url`               VARCHAR(1024) DEFAULT NULL COMMENT '项目地址',

    `spider_create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_ghp_def_exec`   (`spider_definition_code`, `spider_execution_no`),
    KEY `idx_ghp_repo_url`   (`repo_url`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT ='GitHub Trending 项目采集数据';
