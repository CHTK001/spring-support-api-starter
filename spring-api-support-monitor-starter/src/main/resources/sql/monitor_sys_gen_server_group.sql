-- 服务器分组表
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_group` (
    `monitor_sys_gen_server_group_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `monitor_sys_gen_server_group_name` varchar(100) NOT NULL COMMENT '分组名称',
    `monitor_sys_gen_server_group_desc` varchar(500) DEFAULT NULL COMMENT '分组描述',
    `monitor_sys_gen_server_group_color` varchar(20) DEFAULT NULL COMMENT '分组颜色(十六进制颜色值)',
    `monitor_sys_gen_server_group_icon` varchar(100) DEFAULT NULL COMMENT '分组图标',
    `monitor_sys_gen_server_group_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '分组状态 0:禁用 1:启用',
    `monitor_sys_gen_server_group_is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认分组 0:否 1:是',
    `monitor_sys_gen_server_group_sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序号',
    `monitor_sys_gen_server_group_remark` varchar(1000) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(100) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(100) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`monitor_sys_gen_server_group_id`),
    UNIQUE KEY `uk_group_name` (`monitor_sys_gen_server_group_name`),
    KEY `idx_status` (`monitor_sys_gen_server_group_status`),
    KEY `idx_sort` (`monitor_sys_gen_server_group_sort`),
    KEY `idx_is_default` (`monitor_sys_gen_server_group_is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器分组表';

-- 插入默认分组
INSERT INTO `monitor_sys_gen_server_group` (
    `monitor_sys_gen_server_group_name`,
    `monitor_sys_gen_server_group_desc`,
    `monitor_sys_gen_server_group_color`,
    `monitor_sys_gen_server_group_icon`,
    `monitor_sys_gen_server_group_status`,
    `monitor_sys_gen_server_group_is_default`,
    `monitor_sys_gen_server_group_sort`,
    `monitor_sys_gen_server_group_remark`
) VALUES (
    '默认分组',
    '系统默认服务器分组',
    '#409EFF',
    'el-icon-server',
    1,
    1,
    1,
    '系统自动创建的默认分组'
) ON DUPLICATE KEY UPDATE
    `monitor_sys_gen_server_group_desc` = VALUES(`monitor_sys_gen_server_group_desc`),
    `monitor_sys_gen_server_group_color` = VALUES(`monitor_sys_gen_server_group_color`),
    `monitor_sys_gen_server_group_icon` = VALUES(`monitor_sys_gen_server_group_icon`);

-- 如果服务器表中还没有分组字段，添加分组字段
-- ALTER TABLE `monitor_sys_gen_server` 
-- ADD COLUMN `monitor_sys_gen_server_group_id` int(11) DEFAULT NULL COMMENT '服务器分组ID' AFTER `monitor_sys_gen_server_latency`;

-- 添加外键约束（可选）
-- ALTER TABLE `monitor_sys_gen_server` 
-- ADD CONSTRAINT `fk_server_group` 
-- FOREIGN KEY (`monitor_sys_gen_server_group_id`) 
-- REFERENCES `monitor_sys_gen_server_group` (`monitor_sys_gen_server_group_id`) 
-- ON DELETE SET NULL ON UPDATE CASCADE;
