-- =====================================================
-- Service 模块初始化数据脚本
-- 版本: V1.0
-- 说明: 包含系统字典、字典项、系统配置、文件系统分组等基础数据
-- 执行方式: 异步执行（虚拟线程）
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 初始数据 - 字典数据
-- =====================================================
INSERT INTO `sys_dict` VALUES (1, '厂家', 'CHANG_JIA', 0, 1, 0, '', NULL, '三方厂家', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict` VALUES (2, '模板类型', 'MU_BAN_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict` VALUES (3, '短信', 'DUAN_XIN', 2, 1, 0, '', NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict` VALUES (4, '微信', 'WEI_XIN', 2, 1, 0, NULL, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (5, '组件功能', 'ZU_JIAN_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (6, '项目功能', 'XIANG_MU_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (7, '设备类型', 'SHE_BEI_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);

-- =====================================================
-- 初始数据 - 字典项数据
-- =====================================================
INSERT INTO `sys_dict_item` VALUES (1, 1, '海康云曜', 'HAI_KANG_YUN_YAO', NULL, 1, 1, '', 0, NULL, '海康云曜', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (2, 1, '阿里云', 'ALIYUN', NULL, 1, 1, NULL, 0, NULL, '阿里云', 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (3, 1, '百度云', 'BDYUN', NULL, 1, 1, NULL, 0, NULL, '百度云', 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (4, 2, '短信', 'SMS', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (5, 3, '短信通知', 'DUAN_XIN_TONG_ZHI', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (6, 2, '站内', 'WEB', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (7, 2, '微信', 'WECHAT', NULL, 1, 2, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (8, 3, '验证码', 'YAN_ZHENG_MA', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (9, 4, '微信通知', 'WEI_XIN_TONG_ZHI', NULL, 1, NULL, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (10, 1, '讯飞', 'XUN_FEI', NULL, 1, 1, '', 0, 'sysProjectAppId,sysProjectAppSecret', '', 'sa', 1, NOW(), NOW(), 'sa', 1);

-- =====================================================
-- 初始数据 - 系统配置
-- =====================================================
INSERT INTO `sys_setting` VALUES (1, 'CheckCodeOpen', 'false', 'Boolean', 'default', 1, 4, 0, 1, 0, NULL, 15, NULL, '验证码开关', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (2, 'SystemName', '后台管理系统', 'String', 'default', 1, 0, 0, 1, 0, NULL, 15, NULL, '系统标题', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (3, 'SlidingBlockOpen', 'false', 'Boolean', 'default', 1, 5, 0, 1, 0, NULL, 15, NULL, '滑动验证码', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (4, 'CrashPageOpen', 'false', 'Boolean', 'config', 1, 9, 0, 1, 0, NULL, 26, NULL, '调试模式崩溃当前页', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (5, 'CodecResponseOpen', 'false', 'Boolean', 'config', 1, 10, 0, 1, 0, NULL, 26, NULL, '返回加密', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (6, 'WatermarkOpen', 'false', 'Boolean', 'config', 1, 5, 0, 1, 0, NULL, 26, NULL, '水印', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (7, 'LoopDebuggerOpen', 'false', 'Boolean', 'config', 1, 11, 0, 1, 0, NULL, 26, NULL, '循环debugger', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (8, 'Version', '1', 'Number', 'config', 1, 4, 0, 1, 0, NULL, 123, 'fe7db08a-1001-4e7b-95ab-776a569a85c0', '系统版本', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (9, 'SocketUrl', 'http://127.0.0.1:29181', 'Array', 'config', 1, 2, 0, 1, 0, 1, 26, NULL, 'socket.io服务器地址', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (10, 'SocketOpen', 'false', 'Boolean', 'config', 1, 12, 0, 1, 0, 1, 26, NULL, 'socket.io是否开启', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (11, 'SocketPath', '/socket.io', 'String', 'config', 1, 13, 0, 1, 0, NULL, NULL, NULL, 'socket.io路径', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (12, 'WatermarkColor', '#409EFF', 'Color', 'config', 1, 6, 0, 1, 0, NULL, 26, NULL, '水印颜色', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (13, 'WatermarkAlpha', '0.35', 'Number', 'config', 1, 8, 0, 1, 0, NULL, 26, NULL, '水印透明度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (14, 'WatermarkRotate', '-30', 'Number', 'config', 1, 7, 0, 1, 0, NULL, 26, NULL, '水印角度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (15, 'CodecRequestOpen', 'false', 'Boolean', 'config', 1, 14, 0, 1, 0, NULL, 26, NULL, '请求加密', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (16, 'CodecRequestKey', '0123456789abcdef', 'String', 'config', 1, 3, 0, 1, 1, NULL, 26, NULL, '请求加密密钥', NULL, NULL, NULL, NOW(), 'sa', 1);

-- =====================================================
-- 初始数据 - 文件系统分组
-- =====================================================
INSERT INTO `sys_file_system_group` VALUES (1, '默认分组', 0, 'default', '/default', 1, 'default', NULL, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (2, '图片', 0, 'images', '/images', 1, 'images', NULL, 0, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (3, '视频', 0, 'videos', '/videos', 1, 'videos', NULL, 0, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (4, '文档', 0, 'documents', '/documents', 1, 'documents', NULL, 0, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (5, '音频', 0, 'audios', '/audios', 1, 'audios', NULL, 0, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- =====================================================
-- 初始数据 - 文件系统配置
-- =====================================================
INSERT INTO `sys_file_system_setting` VALUES (1, '默认配置', 1, 100, 1, 10, 1, 0, 8888, '0.0.0.0', '/file', 1, 1, NULL, NULL, 1024, NULL, NULL, 0, 1, 'local', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
