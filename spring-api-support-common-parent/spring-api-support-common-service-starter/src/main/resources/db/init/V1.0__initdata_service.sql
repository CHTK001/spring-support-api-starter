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
INSERT INTO `sys_setting_group` VALUES (1, '默认配置', 'default', 'ep:setting', 1, '系统默认配置', 1, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting_group` VALUES (2, '系统配置', 'config', 'ep:tools', 1, '系统基础运行配置', 2, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting_group` VALUES (3, '单点登录', 'sso', 'ri:shield-user-line', 1, '第三方登录总开关', 3, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting_group` VALUES (4, 'Gitee登录', 'gitee', 'simple-icons:gitee', 1, 'Gitee OAuth配置', 4, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting_group` VALUES (5, 'GitHub登录', 'github', 'simple-icons:github', 1, 'GitHub OAuth配置', 5, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting_group` VALUES (6, 'Google登录', 'google', 'simple-icons:google', 1, 'Google OAuth配置', 6, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);

INSERT INTO `sys_setting` VALUES (1, 'CheckCodeOpen', '', 'false', 'Boolean', 'default', 1, 4, 0, 1, 0, NULL, 15, NULL, '验证码开关', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (2, 'SystemName', '', '后台管理系统', 'String', 'default', 1, 0, 0, 1, 0, NULL, 15, NULL, '系统标题', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (3, 'SlidingBlockOpen', '', 'false', 'Boolean', 'default', 1, 5, 0, 1, 0, NULL, 15, NULL, '滑动验证码', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (17, 'OpenSmsLogin', '', 'true', 'Boolean', 'default', 1, 6, 0, 1, 0, NULL, 15, NULL, '短信登录开关', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (30, 'CheckTotpOpen', 'login.otp', 'false', 'Boolean', 'default', 1, 7, 0, 1, 0, NULL, 15, NULL, 'OTP动态口令', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (31, 'BrandName', 'login.brandName', '后台管理系统', 'String', 'default', 1, 1, 0, 1, 0, NULL, 15, NULL, '品牌名称', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (32, 'Welcome', 'login.welcome', '欢迎登录', 'String', 'default', 1, 2, 0, 1, 0, NULL, 15, NULL, '欢迎语', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (33, 'Description', 'login.description', '系统管理平台', 'String', 'default', 1, 3, 0, 1, 0, NULL, 15, NULL, '系统描述', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (34, 'LogoUrl', 'login.logoUrl', '/assets/logo.png', 'String', 'default', 1, 8, 0, 1, 0, NULL, 15, NULL, '品牌Logo', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (35, 'ImageUrl', 'login.imageUrl', '/assets/login/science.png', 'String', 'default', 1, 9, 0, 1, 0, NULL, 15, NULL, '主视觉图片', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (36, 'AvatarUrl', 'login.avatarUrl', '', 'String', 'default', 1, 10, 0, 1, 0, NULL, 15, NULL, '默认头像', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (37, 'BeiAnHao', 'login.beiAnHao', '', 'String', 'default', 1, 11, 0, 1, 0, NULL, 15, NULL, '备案号', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (38, 'FeatureTags', 'login.featureTags', '[{"icon":"DataAnalysis","text":"高效管理"},{"icon":"Brush","text":"现代设计"},{"icon":"Lock","text":"安全可靠"}]', 'String', 'default', 1, 12, 0, 1, 0, NULL, 15, NULL, '功能特性标签', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (5, 'CodecResponseOpen', '', 'false', 'Boolean', 'config', 1, 10, 0, 1, 0, NULL, 26, NULL, '返回加密', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (6, 'WatermarkOpen', '', 'false', 'Boolean', 'config', 1, 5, 0, 1, 0, NULL, 26, NULL, '水印', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (8, 'Version', '', '1', 'Number', 'config', 1, 4, 0, 1, 0, NULL, 123, 'fe7db08a-1001-4e7b-95ab-776a569a85c0', '系统版本', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (9, 'SocketUrl', '', 'http://127.0.0.1:29181', 'Array', 'config', 1, 2, 0, 1, 0, 1, 26, NULL, 'socket.io服务器地址', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (10, 'SocketOpen', '', 'false', 'Boolean', 'config', 1, 12, 0, 1, 0, 1, 26, NULL, 'socket.io是否开启', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (11, 'SocketPath', '', '/socket.io', 'String', 'config', 1, 13, 0, 1, 0, NULL, NULL, NULL, 'socket.io路径', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (12, 'WatermarkColor', '', '#409EFF', 'Color', 'config', 1, 6, 0, 1, 0, NULL, 26, NULL, '水印颜色', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (13, 'WatermarkAlpha', '', '0.35', 'Number', 'config', 1, 8, 0, 1, 0, NULL, 26, NULL, '水印透明度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (14, 'WatermarkRotate', '', '-30', 'Number', 'config', 1, 7, 0, 1, 0, NULL, 26, NULL, '水印角度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (15, 'CodecRequestOpen', '', 'false', 'Boolean', 'config', 1, 14, 0, 1, 0, NULL, 26, NULL, '请求加密', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (16, 'CodecRequestKey', '', '0123456789abcdef', 'String', 'config', 1, 3, 0, 1, 1, NULL, 26, NULL, '请求加密密钥', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (18, 'Gitee', '', 'false', 'Boolean', 'sso', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (19, 'Github', '', 'false', 'Boolean', 'sso', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (20, 'Google', '', 'false', 'Boolean', 'sso', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (21, 'clientId', '', '', 'String', 'gitee', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (22, 'clientSecret', '', '', 'Password', 'gitee', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Gitee Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (23, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/gitee/callback', 'String', 'gitee', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Gitee回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (24, 'clientId', '', '', 'String', 'github', 1, 1, 0, 1, 0, NULL, 1, NULL, 'GitHub Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (25, 'clientSecret', '', '', 'Password', 'github', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (26, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/github/callback', 'String', 'github', 1, 3, 0, 1, 0, NULL, 1, NULL, 'GitHub回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (27, 'clientId', '', '', 'String', 'google', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Google Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (28, 'clientSecret', '', '', 'Password', 'google', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Google Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO `sys_setting` VALUES (29, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/google/callback', 'String', 'google', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);

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
