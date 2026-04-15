/*
 * 设备模块 - 数据库初始化脚本
 *
 * 包含表:
 *   - sys_device: 设备管理表
 *   - sys_device_channel: 设备通道表
 *   - sys_device_org: 设备组织表
 *   - sys_device_camera_record: 设备记录表
 *   - sys_device_card_event: 门禁事件表
 *   - sys_device_message_event: 设备消息事件表
 *
 * 用于设备接入、组织同步、门禁事件与消息事件管理
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 设备管理表 (sys_device)
-- 用途: 管理摄像头、门禁等IoT设备
-- =====================================================
DROP TABLE IF EXISTS `sys_device`;
CREATE TABLE `sys_device` (
    `sys_device_id` int NOT NULL AUTO_INCREMENT COMMENT '设备ID - 主键自增',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
    `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号 - 设备唯一标识',
    `sys_device_org_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码 - 所属组织的编码',
    `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
    `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道名称',
    `sys_device_channel_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道号',
    `sys_device_channel_count` int NULL DEFAULT NULL COMMENT '通道数量',
    `sys_device_status` int NULL DEFAULT 1 COMMENT '设备状态 - 0:禁用, 1:启用',
    `sys_device_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装位置 - 设备物理安装位置描述',
    `sys_device_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '固件版本',
    `sys_device_resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源类型',
    `sys_device_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备账号 - 设备登录账号',
    `sys_device_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备密码 - 设备登录密码(加密存储)',
    `sys_device_rtsp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'RTSP地址 - 视频流推流地址',
    `sys_device_rtsp_webrtc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'WebRTC地址 - RTSP转WebRTC的地址',
    `sys_device_channels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道列表 - JSON格式的通道信息',
    `sys_device_owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属者编码 - 设备所有者唯一标识',
    `sys_device_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备描述',
    `sys_device_online` int NULL DEFAULT 0 COMMENT '在线状态 - 0:离线, 1:在线',
    `sys_device_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型 - 关联字典表ID=7',
    `sys_device_latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度 - GPS纬度坐标',
    `sys_device_longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度 - GPS经度坐标',
    `sys_device_net_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网络地址 - 设备IP地址',
    `sys_device_net_physical_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MAC地址 - 设备物理地址',
    `sys_device_validate_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证码 - 设备绑定验证码',
    `sys_device_main_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主码流编码 - 高清主码流标识',
    `sys_device_sub_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子码流编码 - 标清子码流标识',
    `sys_dept_id` int NULL DEFAULT NULL COMMENT '机构ID - 关联组织机构表',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_id`) USING BTREE,
    UNIQUE INDEX `uk_serial_number` (`sys_device_serial_number`) USING BTREE COMMENT '序列号唯一索引',
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_device_type` (`sys_device_type`) USING BTREE COMMENT '设备类型索引',
    INDEX `idx_device_online` (`sys_device_online`) USING BTREE COMMENT '在线状态索引',
    INDEX `idx_dept_id` (`sys_dept_id`) USING BTREE COMMENT '机构ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '设备管理表 - 管理摄像头、门禁等IoT设备' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 设备通道表 (sys_device_channel)
-- 用途: 管理设备的视频/报警通道
-- =====================================================
DROP TABLE IF EXISTS `sys_device_channel`;
CREATE TABLE `sys_device_channel` (
    `sys_device_channel_id` int NOT NULL AUTO_INCREMENT COMMENT '通道ID - 主键自增',
    `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道名称',
    `sys_device_channel_no` int NULL DEFAULT NULL COMMENT '通道号',
    `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号 - 关联设备表',
    `sys_device_channel_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道类型 - 10300:视频通道, 10302:报警输入',
    `sys_device_channel_status` int NULL DEFAULT 1 COMMENT '通道状态 - -1:未上报, 0:离线, 1:在线',
    `sys_device_channel_use` int NULL DEFAULT 1 COMMENT '通道启用状态 - 0:禁用, 1:启用',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_channel_id`) USING BTREE,
    INDEX `idx_serial_number` (`sys_device_serial_number`) USING BTREE COMMENT '设备序列号索引',
    INDEX `idx_channel_status` (`sys_device_channel_status`) USING BTREE COMMENT '通道状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '设备通道表 - 管理设备的视频/报警通道' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 设备组织表 (sys_device_org)
-- 用途: 管理设备的组织架构
-- =====================================================
DROP TABLE IF EXISTS `sys_device_org`;
CREATE TABLE `sys_device_org` (
    `sys_device_org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织ID - 主键',
    `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
    `sys_device_org_pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上级组织ID',
    `sys_device_org_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织树路径',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_org_id`) USING BTREE,
    INDEX `idx_org_pid` (`sys_device_org_pid`) USING BTREE COMMENT '上级组织索引',
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '设备组织表 - 管理设备的组织架构' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 设备记录表 (sys_device_camera_record)
-- 用途: 记录设备操作历史
-- =====================================================
DROP TABLE IF EXISTS `sys_device_camera_record`;
CREATE TABLE `sys_device_camera_record` (
    `sys_device_camera_record_id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID - 主键自增',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_camera_record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '设备记录表 - 记录摄像头操作历史' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 门禁事件表 (sys_device_card_event)
-- 用途: 记录门禁刷卡事件
-- =====================================================
DROP TABLE IF EXISTS `sys_device_card_event`;
CREATE TABLE `sys_device_card_event` (
    `sys_device_card_event_id` int NOT NULL AUTO_INCREMENT COMMENT '事件ID - 主键自增',
    `sys_device_card_event_time` datetime NULL DEFAULT NULL COMMENT '事件时间',
    `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
    `sys_device_card_event_person_involved` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人ID',
    `sys_device_card_event_person_involved_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人姓名',
    `sys_device_card_event_major_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主事件类型',
    `sys_device_card_event_minor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '次事件类型',
    `sys_device_card_event_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_card_event_id`) USING BTREE,
    INDEX `idx_event_time` (`sys_device_card_event_time`) USING BTREE COMMENT '事件时间索引',
    INDEX `idx_serial_number` (`sys_device_serial_number`) USING BTREE COMMENT '设备序列号索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '门禁事件表 - 记录门禁刷卡事件' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 设备消息事件表 (sys_device_message_event)
-- 用途: 记录设备上报的消息事件
-- =====================================================
DROP TABLE IF EXISTS `sys_device_message_event`;
CREATE TABLE `sys_device_message_event` (
    `sys_device_message_event_id` int NOT NULL AUTO_INCREMENT COMMENT '事件ID - 主键自增',
    `sys_device_message_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
    `sys_device_message_event_event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件唯一ID',
    `sys_device_message_event_data_time` datetime NULL DEFAULT NULL COMMENT '事件数据时间',
    `sys_device_message_event_employee_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员工号',
    `sys_device_message_event_employee_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员姓名',
    `sys_device_message_event_card_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卡号',
    `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
    `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
    `sys_device_message_event_channel_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发通道号',
    `sys_device_message_event_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发通道名称',
    `sys_device_message_event_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发位置',
    `sys_device_message_event_card_reader_kind` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '读卡器类型 - 1:IC, 2:身份证, 3:二维码',
    `sys_device_message_event_card_reader_no` int NULL DEFAULT NULL COMMENT '读卡器编号',
    `sys_device_message_event_door_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门编号',
    `sys_device_message_event_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息事件类型',
    `sys_device_message_event_event_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息事件类型名称',
    `sys_device_message_event_event_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件码',
    `sys_device_message_event_event_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件描述',
    `sys_device_message_event_event_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件来源',
    `sys_device_message_event_event_picture_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件图片URL',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_message_event_id`) USING BTREE,
    INDEX `idx_data_time` (`sys_device_message_event_data_time`) USING BTREE COMMENT '事件时间索引',
    INDEX `idx_serial_number` (`sys_device_serial_number`) USING BTREE COMMENT '设备序列号索引',
    INDEX `idx_event_type` (`sys_device_message_event_type`) USING BTREE COMMENT '事件类型索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = '设备消息事件表 - 记录设备上报的消息事件' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
