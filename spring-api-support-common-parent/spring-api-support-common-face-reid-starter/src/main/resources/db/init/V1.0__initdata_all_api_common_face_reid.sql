-- =====================================================================
-- V1.0__initdata_all_api_common_face_reid.sql
-- spring-api-support-common-face-reid-starter 全量种子
-- 通道: INITDATA；全部幂等（INSERT IGNORE，主键去重）。
--
-- 内容：人脸重识别（Re-ID）菜单与按钮权限。
-- 由 FlywayLike 自动扫描 classpath*:db/init/*.sql 执行，无需额外配置。
--
-- ID 段位：800-899（避开 system 基线已占用的 1-226 与 data-h2.sql 的既有编号）。
-- 宿主为 remote 路由模式：侧边栏与路由由后端 /v2/user/menu 下发，
-- 因此这些行一旦入库，刷新页面即可看到「人脸识别」一级菜单。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 一级菜单：人脸识别
--    sys_menu_type   0=菜单
--    sys_menu_pid    0 表示顶级
--    sys_menu_component 为目录占位，实际子页面各自指定
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `sys_menu` (
    `sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_title`, `sys_menu_perm`,
    `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_type`,
    `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`,
    `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_keep_alive`,
    `create_name`, `create_by`, `create_time`, `update_time`,
    `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`
) VALUES
-- 一级：人脸识别
(800, 'faceReid', '/face', '人脸识别', NULL,
 0, '0,800', '/face', 'ep:avatar', 0,
 30, 0, 1, 0, 0, '', 0,
 'sa', 1, NOW(), NOW(),
 'SUPER_ADMIN,ADMIN', NULL, NULL),

-- ---------------------------------------------------------------------
-- 2. 二级菜单
--    component 指向 Vue 页面：pages/face/<名称>/index.vue
-- ---------------------------------------------------------------------
(801, 'faceReidTrack', '/face/track', '重识别轨迹', NULL,
 800, '0,800,801', 'face/track/index', 'ep:connection', 0,
 10, 0, 1, 0, 0, '', 0,
 'sa', 1, NOW(), NOW(),
 'SUPER_ADMIN,ADMIN', NULL, NULL),

(802, 'faceReidIdentify', '/face/identify', '以图搜人', NULL,
 800, '0,800,802', 'face/identify/index', 'ep:search', 0,
 20, 0, 1, 0, 0, '', 0,
 'sa', 1, NOW(), NOW(),
 'SUPER_ADMIN,ADMIN', NULL, NULL),

(803, 'faceReidConfig', '/face/config', '重识别配置', NULL,
 800, '0,800,803', 'face/config/index', 'ep:setting', 0,
 30, 0, 1, 0, 0, '', 0,
 'sa', 1, NOW(), NOW(),
 'SUPER_ADMIN,ADMIN', NULL, NULL),

-- ---------------------------------------------------------------------
-- 3. 按钮权限（type=3）
--    与 FaceReidController 的接口一一对应，前端可用 v-perm 控制显隐。
-- ---------------------------------------------------------------------
(810, '', '', '注册人脸', 'face:reid:register', 801, '0,800,801,810', '', '', 3,
 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL),

(811, '', '', '删除轨迹', 'face:reid:delete', 801, '0,800,801,811', '', '', 3,
 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL),

(812, '', '', '以图检索', 'face:reid:search', 802, '0,800,802,812', '', '', 3,
 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL),

(813, '', '', '修改配置', 'face:reid:config:update', 803, '0,800,803,813', '', '', 3,
 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL),

(814, '', '', '重置配置', 'face:reid:config:reset', 803, '0,800,803,814', '', '', 3,
 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL),

(815, '', '', '清空特征库', 'face:reid:clear', 803, '0,800,803,815', '', '', 3,
 3, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'SUPER_ADMIN,ADMIN', NULL, NULL);

-- ---------------------------------------------------------------------
-- 4. 角色-菜单授权
--
--    注意：sys_role_menu 依赖 sys_role 已存在，而角色由 system-starter 播种。
--    FlywayLike 跨模块脚本执行顺序不保证，因此这里用
--    「INSERT ... SELECT + WHERE NOT EXISTS」按角色编码动态取角色 ID，
--    既避免与执行顺序耦合，也不会因角色缺失（查到 0 行）而报错——重复执行安全。
--
--    授予范围：SUPER_ADMIN 与 ADMIN 拥有「人脸识别」整棵子树（含按钮）。
-- ---------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`)
SELECT (r.`sys_role_id` * 1000 + m.`sys_menu_id`), r.`sys_role_id`, m.`sys_menu_id`
FROM `sys_role` r
JOIN `sys_menu` m ON m.`sys_menu_id` BETWEEN 800 AND 899
WHERE r.`sys_role_code` IN ('SUPER_ADMIN', 'ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.`sys_role_id` = r.`sys_role_id`
        AND rm.`sys_menu_id` = m.`sys_menu_id`
  );
