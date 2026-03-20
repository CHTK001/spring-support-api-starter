-- =====================================================
-- 增量迁移脚本：添加支付模块菜单和权限
-- 适用于已初始化的系统环境
-- 版本: V1.1
-- 日期: 2026-03-20
-- =====================================================

-- =====================================================
-- 支付菜单树结构
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 42, 'payment', '/payment', NULL, '支付管理', NULL, 0, '0,42', '/payment', 'ri:secure-payment-line', NULL, 0, 7, 0, 1, 0, 0, '/payment/merchant', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, 'ADMIN,SUPER_ADMIN', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 42);

INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 43, 'paymentMerchantManage', '/payment/merchant', NULL, '商户管理', 'payment:merchant:view,payment:channel:view', 42, '0,42,43', '/manage/payment-merchant', 'ri:store-2-line', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 43);

INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 44, 'paymentOrderManage', '/payment/order', NULL, '订单管理', 'payment:order:view', 42, '0,42,44', '/manage/payment-order', 'ri:file-list-3-line', NULL, 0, 2, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 44);

INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 45, 'paymentTransactionManage', '/payment/transaction', NULL, '交易流水', 'payment:transaction:view', 42, '0,42,45', '/manage/payment-transaction', 'ri:exchange-funds-line', NULL, 0, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 45);

INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 46, 'paymentRefundManage', '/payment/refund', NULL, '退款管理', 'payment:refund:view', 42, '0,42,46', '/manage/payment-refund', 'ri:refund-2-line', NULL, 0, 4, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 46);

INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 73, 'paymentWalletManage', '/payment/wallet', NULL, '钱包管理', 'payment:wallet:view', 42, '0,42,73', '/manage/payment-wallet', 'ri:wallet-3-line', NULL, 0, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 73);

-- =====================================================
-- 商户管理按钮权限
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT * FROM (
  SELECT 47, '', '', '', '新增商户', 'payment:merchant:add', 43, '0,42,43,47', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 48, '', '', '', '编辑商户', 'payment:merchant:edit', 43, '0,42,43,48', '', '', NULL, 3, 2, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 49, '', '', '', '激活商户', 'payment:merchant:activate', 43, '0,42,43,49', '', '', NULL, 3, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 50, '', '', '', '停用商户', 'payment:merchant:deactivate', 43, '0,42,43,50', '', '', NULL, 3, 4, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 51, '', '', '', '删除商户', 'payment:merchant:delete', 43, '0,42,43,51', '', '', NULL, 3, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 52, '', '', '', '新增支付方式', 'payment:channel:add', 43, '0,42,43,52', '', '', NULL, 3, 6, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 53, '', '', '', '编辑支付方式', 'payment:channel:edit', 43, '0,42,43,53', '', '', NULL, 3, 7, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 54, '', '', '', '启用支付方式', 'payment:channel:enable', 43, '0,42,43,54', '', '', NULL, 3, 8, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 55, '', '', '', '禁用支付方式', 'payment:channel:disable', 43, '0,42,43,55', '', '', NULL, 3, 9, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 56, '', '', '', '开通指引', 'payment:channel:guide', 43, '0,42,43,56', '', '', NULL, 3, 10, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 66, '', '', '', '商户查看', 'payment:merchant:view', 43, '0,42,43,66', '', '', NULL, 3, 12, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 67, '', '', '', '支付方式查看', 'payment:channel:view', 43, '0,42,43,67', '', '', NULL, 3, 11, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` IN (47,48,49,50,51,52,53,54,55,56,66,67));

-- =====================================================
-- 订单管理按钮权限
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT * FROM (
  SELECT 57, '', '', '', '创建订单', 'payment:order:create', 44, '0,42,44,57', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 58, '', '', '', '支付处理', 'payment:order:pay', 44, '0,42,44,58', '', '', NULL, 3, 2, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 59, '', '', '', '同步状态', 'payment:order:sync', 44, '0,42,44,59', '', '', NULL, 3, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 60, '', '', '', '取消', 'payment:order:cancel', 44, '0,42,44,60', '', '', NULL, 3, 4, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 61, '', '', '', '完成', 'payment:order:complete', 44, '0,42,44,61', '', '', NULL, 3, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 62, '', '', '', '退款处理', 'payment:order:refund', 44, '0,42,44,62', '', '', NULL, 3, 6, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 63, '', '', '', '删除', 'payment:order:delete', 44, '0,42,44,63', '', '', NULL, 3, 7, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 68, '', '', '', '订单查看', 'payment:order:view', 44, '0,42,44,68', '', '', NULL, 3, 8, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` IN (57,58,59,60,61,62,63,68));

-- =====================================================
-- 退款管理按钮权限
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT * FROM (
  SELECT 64, '', '', '', '标记成功', 'payment:refund:success', 46, '0,42,46,64', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 65, '', '', '', '标记失败', 'payment:refund:fail', 46, '0,42,46,65', '', '', NULL, 3, 2, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 69, '', '', '', '退款查看', 'payment:refund:view', 46, '0,42,46,69', '', '', NULL, 3, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` IN (64,65,69));

-- =====================================================
-- 交易流水按钮权限
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT 70, '', '', '', '交易流水查看', 'payment:transaction:view', 45, '0,42,45,70', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` = 70);

-- =====================================================
-- 钱包管理按钮权限
-- =====================================================
INSERT INTO `sys_menu` (`sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`, `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_extra_icon`, `sys_menu_type`, `sys_menu_sort`, `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`, `sys_menu_enter_transition`, `sys_menu_leave_transition`, `sys_menu_active_path`, `sys_menu_frame_src`, `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`, `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`)
SELECT * FROM (
  SELECT 71, '', '', '', '钱包查看', 'payment:wallet:view', 73, '0,42,73,71', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL UNION ALL
  SELECT 72, '', '', '', '钱包充值', 'payment:wallet:recharge', 73, '0,42,73,72', '', '', NULL, 3, 2, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `sys_menu_id` IN (71,72));

-- =====================================================
-- 角色菜单关联 - ADMIN 角色 (role_id=2)
-- =====================================================
INSERT INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`)
SELECT * FROM (
  SELECT 189, 2, 42 UNION ALL
  SELECT 190, 2, 43 UNION ALL
  SELECT 191, 2, 44 UNION ALL
  SELECT 192, 2, 45 UNION ALL
  SELECT 193, 2, 46 UNION ALL
  SELECT 194, 2, 47 UNION ALL
  SELECT 195, 2, 48 UNION ALL
  SELECT 196, 2, 49 UNION ALL
  SELECT 197, 2, 50 UNION ALL
  SELECT 198, 2, 51 UNION ALL
  SELECT 199, 2, 52 UNION ALL
  SELECT 200, 2, 53 UNION ALL
  SELECT 201, 2, 54 UNION ALL
  SELECT 202, 2, 55 UNION ALL
  SELECT 203, 2, 56 UNION ALL
  SELECT 204, 2, 57 UNION ALL
  SELECT 205, 2, 58 UNION ALL
  SELECT 206, 2, 59 UNION ALL
  SELECT 207, 2, 60 UNION ALL
  SELECT 208, 2, 61 UNION ALL
  SELECT 209, 2, 62 UNION ALL
  SELECT 210, 2, 63 UNION ALL
  SELECT 230, 2, 64 UNION ALL
  SELECT 211, 2, 65 UNION ALL
  SELECT 212, 2, 66 UNION ALL
  SELECT 213, 2, 67 UNION ALL
  SELECT 214, 2, 68 UNION ALL
  SELECT 215, 2, 69 UNION ALL
  SELECT 216, 2, 70 UNION ALL
  SELECT 217, 2, 71 UNION ALL
  SELECT 218, 2, 72 UNION ALL
  SELECT 231, 2, 73
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu`
  WHERE `sys_role_id` = 2 AND `sys_menu_id` IN (42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73)
);

-- =====================================================
-- 角色菜单关联 - OPS 角色 (role_id=3)
-- =====================================================
INSERT INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`)
SELECT * FROM (
  SELECT 219, 3, 42 UNION ALL
  SELECT 220, 3, 43 UNION ALL
  SELECT 221, 3, 44 UNION ALL
  SELECT 222, 3, 45 UNION ALL
  SELECT 223, 3, 46 UNION ALL
  SELECT 224, 3, 66 UNION ALL
  SELECT 225, 3, 67 UNION ALL
  SELECT 226, 3, 68 UNION ALL
  SELECT 227, 3, 69 UNION ALL
  SELECT 228, 3, 70 UNION ALL
  SELECT 229, 3, 71 UNION ALL
  SELECT 232, 3, 73
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu`
  WHERE `sys_role_id` = 3 AND `sys_menu_id` IN (42,43,44,45,46,66,67,68,69,70,71,73)
);
