/*
 * AI 模块 - 数据库初始化脚本
 *
 * 包含表:
 *   - sys_ai_group: AI会话分组表
 *   - sys_ai_group_message: AI会话消息表
 *   - sys_ai_module: AI模型分类表
 *   - sys_ai_vincent_setting: 文生图模型参数表
 *   - sys_ai_vincent_style: 文生图风格表
 *   - sys_ai_vincent_task: 文生图任务表
 *   - sys_ai_vincent_template: 文生图模板表
 *
 * 用于AI会话、模型配置、文生图/视频任务、AI模板等功能
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 10. AI会话分组表 (sys_ai_group)
-- 用途: 管理项目下用户自己的AI会话分组
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_group`;
CREATE TABLE `sys_ai_group` (
    `sys_ai_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    `sys_ai_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
    `sys_ai_group_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
    `sys_ai_group_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分组配置 - JSON格式',
    `sys_ai_group_default` int NULL DEFAULT 0 COMMENT '是否默认分组 - 0:否, 1:是',
    `sys_ai_group_sort` int NULL DEFAULT 0 COMMENT '排序号',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_group_id`) USING BTREE,
    INDEX `idx_project_user` (`sys_project_id`, `sys_user_id`) USING BTREE COMMENT '项目用户索引',
    INDEX `idx_group_default_sort` (`sys_ai_group_default`, `sys_ai_group_sort`) USING BTREE COMMENT '默认分组排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'AI会话分组表 - 管理项目下用户自己的AI会话分组' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. AI会话消息表 (sys_ai_group_message)
-- 用途: 记录会话分组下的消息内容
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_group_message`;
CREATE TABLE `sys_ai_group_message` (
    `sys_ai_group_message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID - 主键自增',
    `sys_ai_group_id` int NULL DEFAULT NULL COMMENT '分组ID - 关联sys_ai_group表',
    `sys_ai_group_message_role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色 - user/assistant/system',
    `sys_ai_group_message_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
    `sys_ai_group_message_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型 - TEXT/IMAGE等',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_group_message_id`) USING BTREE,
    INDEX `idx_group_id` (`sys_ai_group_id`) USING BTREE COMMENT '分组ID索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'AI会话消息表 - 记录会话分组下的消息内容' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. AI模型分类表 (sys_ai_module)
-- 用途: 管理各类AI模型配置
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_module`;
CREATE TABLE `sys_ai_module` (
    `sys_ai_module_id` int NOT NULL AUTO_INCREMENT COMMENT '模型ID - 主键自增',
    `sys_ai_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称 - 模型的显示名称',
    `sys_ai_module_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型编码 - 模型的API标识码',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_ai_module_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型说明 - 模型功能描述',
    `sys_ai_module_status` int NULL DEFAULT 1 COMMENT '模型状态 - 0:禁用, 1:启用',
    `sys_ai_module_sort` int NULL DEFAULT 0 COMMENT '排序号',
    `sys_ai_module_manufacturers` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '厂商编码 - 前端固定值',
    `sys_ai_module_role_setting` int NULL DEFAULT 0 COMMENT '是否存在角色设置 - 0:不存在, 1:存在',
    `sys_ai_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'LLM' COMMENT '模型类型 - LLM:大语言模型, VINCENT:文生图, VIDEO:文生视频, RESOLUTION:超分辨率, COLORIZATION:图像上色',
    `sys_ai_module_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型单独地址 - 独立的API端点URL',
    `sys_ai_module_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型版本',
    `sys_ai_module_vlm` int NULL DEFAULT 0 COMMENT '是否VLM模型 - 0:否, 1:是(视觉语言模型，支持图片输入)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_module_id`) USING BTREE,
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_module_type` (`sys_ai_module_type`) USING BTREE COMMENT '模型类型索引',
    INDEX `idx_module_status` (`sys_ai_module_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'AI模型分类表 - 管理各类AI模型配置' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 13. 文生图模型参数表 (sys_ai_vincent_setting)
-- 用途: 配置文生图/文生视频模型的详细参数
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_vincent_setting`;
CREATE TABLE `sys_ai_vincent_setting` (
    `sys_ai_vincent_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '所属模型ID - 关联sys_ai_module表',
    `sys_ai_vincent_support_async` int NULL DEFAULT 0 COMMENT '是否支持异步 - 0:同步, 1:异步任务',
    `sys_ai_vincent_support_ref_image` int NULL DEFAULT 0 COMMENT '支持参考图 - 0:不支持, 1:支持图生图',
    `sys_ai_vincent_support_audio` int NULL DEFAULT 0 COMMENT '支持AI音效 - 0:不支持, 1:支持',
    `sys_ai_vincent_supported_style` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的风格 - 逗号分隔的风格标识列表',
    `sys_ai_vincent_supported_number` int NULL DEFAULT 1 COMMENT '支持输出张数 - 单次生成的最大图片数量',
    `sys_ai_vincent_supported_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的尺寸 - 逗号分隔的尺寸列表(如"1024*1024,720*1280")',
    `sys_ai_vincent_supported_max_size` int NULL DEFAULT NULL COMMENT '支持的最大尺寸 - 最大边长像素值',
    `sys_ai_vincent_supported_fps` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的FPS - 视频帧率',
    `sys_ai_vincent_supported_quality` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的视频质量',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_vincent_id`) USING BTREE,
    UNIQUE INDEX `uk_module_id` (`sys_ai_module_id`) USING BTREE COMMENT '模型ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文生图模型参数表 - 配置文生图/文生视频模型的详细参数' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 14. 文生图风格表 (sys_ai_vincent_style)
-- 用途: 定义文生图模型支持的风格选项
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_vincent_style`;
CREATE TABLE `sys_ai_vincent_style` (
    `sys_ai_vincent_style_id` int NOT NULL AUTO_INCREMENT COMMENT '风格ID - 主键自增',
    `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '所属模型ID - 关联sys_ai_module表',
    `sys_ai_vincent_style_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格名称 - 显示给用户的风格名称',
    `sys_ai_vincent_style_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格编码 - 调用API时使用的风格标识',
    `sys_ai_vincent_style_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格示例图 - 风格效果预览图URL',
    PRIMARY KEY (`sys_ai_vincent_style_id`) USING BTREE,
    INDEX `idx_module_id` (`sys_ai_module_id`) USING BTREE COMMENT '模型ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文生图风格表 - 定义文生图模型支持的风格选项' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 文生图任务表 (sys_ai_vincent_task)
-- 用途: 记录文生图异步任务的执行状态
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_vincent_task`;
CREATE TABLE `sys_ai_vincent_task` (
    `sys_ai_vincent_task_id` int NOT NULL AUTO_INCREMENT COMMENT '任务ID - 主键自增',
    `sys_ai_vincent_task_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务唯一标识 - 第三方平台返回的任务ID',
    `sys_ai_vincent_task_status` int NULL DEFAULT 0 COMMENT '任务状态 - 0:进行中, 1:已完成, 2:失败',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '模型ID - 关联sys_ai_module表',
    `sys_ai_vincent_task_condition` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '生成参数 - JSON格式的生成条件',
    `sys_ai_vincent_task_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '生成结果URL - 生成的图片/视频URL',
    `sys_ai_vincent_task_local_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地保存URL - 下载到本地后的文件路径',
    `sys_ai_vincent_task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型 - VINCENT:文生图, VIDEO:文生视频',
    `sys_ai_vincent_task_request_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求ID - 本地生成的请求追踪ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_vincent_task_id`) USING BTREE,
    UNIQUE INDEX `uk_task_uid` (`sys_ai_vincent_task_uid`) USING BTREE COMMENT '任务UID唯一索引',
    INDEX `idx_task_status` (`sys_ai_vincent_task_status`) USING BTREE COMMENT '任务状态索引',
    INDEX `idx_create_by` (`create_by`) USING BTREE COMMENT '创建人索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文生图任务表 - 记录文生图异步任务的执行状态' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 16. 文生图模板表 (sys_ai_vincent_template)
-- 用途: 预设的文生图模板配置
-- =====================================================
DROP TABLE IF EXISTS `sys_ai_vincent_template`;
CREATE TABLE `sys_ai_vincent_template` (
    `sys_ai_vincent_template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID - 主键自增',
    `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '所属模型ID - 关联sys_ai_module表',
    `sys_ai_vincent_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
    `sys_ai_vincent_template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板类型标识',
    `sys_ai_vincent_template_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板预览图URL',
    `sys_ai_vincent_template_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板描述 - 模板用途说明',
    `sys_ai_vincent_template_sort` int NULL DEFAULT 0 COMMENT '排序号',
    `sys_ai_vincent_template_model_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型ID - 第三方平台的模型标识',
    `sys_ai_vincent_template_model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
    `sys_ai_vincent_template_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板分类 - combining_images:组合图, style:风格等',
    `sys_ai_vincent_template_train_model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '训练模型',
    `sys_ai_vincent_template_recommend_prompt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐提示词 - 示例prompt',
    `sys_ai_vincent_template_resolutions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐分辨率',
    `sys_ai_vincent_template_word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词说明',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ai_vincent_template_id`) USING BTREE,
    INDEX `idx_module_id` (`sys_ai_module_id`) USING BTREE COMMENT '模型ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文生图模板表 - 预设的文生图模板配置' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
