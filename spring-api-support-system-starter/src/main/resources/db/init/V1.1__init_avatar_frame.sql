/*
 * 系统管理模块 - 增量初始化脚本 V1.1
 *
 * 背景: V1.0 初始版本仅内置 8 款头像框(ID 1-8), 后续扩充至 27 款。
 * 已有数据库执行过 V1.0 后不会自动获得新增的头像框, 本脚本按名称幂等补插,
 * 重复执行安全(已存在的框名跳过), 新插入的框沿用 AUTO_INCREMENT 得到 ID 9-27,
 * 与前端 baseFrames(1-27) 完全对齐。
 *
 * 使用: 直连已有 system 库执行一次即可(可重复执行)。
 */
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 渐变框扩充(ID 9-22)
INSERT INTO `sys_avatar_frame` (`sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`)
SELECT * FROM (
  SELECT '糖果' AS n, 'base' AS t, 'linear-gradient(135deg, #f6d365, #fda085)' AS c, '甜美糖果渐变' AS d, 'default' AS a, 8 AS s UNION ALL
  SELECT '樱粉', 'base', 'linear-gradient(135deg, #ff9a9e, #fecfef)', '浪漫樱粉渐变', 'default', 9 UNION ALL
  SELECT '薄荷', 'base', 'linear-gradient(135deg, #a1c4fd, #c2e9fb)', '清爽薄荷渐变', 'default', 10 UNION ALL
  SELECT '暮霭', 'base', 'linear-gradient(135deg, #d4a4fc, #ff9a9e)', '温柔暮霭渐变', 'default', 11 UNION ALL
  SELECT '柠檬', 'base', 'linear-gradient(135deg, #ffdfba, #feedb0)', '明快柠檬渐变', 'default', 12 UNION ALL
  SELECT '星空', 'base', 'linear-gradient(135deg, #0f2027, #203a43, #2c5364)', '深邃星空渐变', 'default', 13 UNION ALL
  SELECT '森林', 'base', 'linear-gradient(135deg, #134e5e, #71b280)', '自然森林渐变', 'default', 14 UNION ALL
  SELECT '熔岩', 'base', 'linear-gradient(135deg, #ff512f, #dd2476)', '炽热熔岩渐变', 'default', 15 UNION ALL
  SELECT '极光', 'base', 'linear-gradient(135deg, #00c9ff, #92fe9d)', '缥缈极光渐变', 'default', 16 UNION ALL
  SELECT '红枫', 'base', 'linear-gradient(135deg, #b31217, #e52d27)', '热烈红枫渐变', 'default', 17 UNION ALL
  SELECT '海洋', 'base', 'linear-gradient(135deg, #2193b0, #6dd5ed)', '蔚蓝海洋渐变', 'default', 18 UNION ALL
  SELECT '祖母绿', 'base', 'linear-gradient(135deg, #11998e, #38ef7d)', '剔透祖母绿渐变', 'default', 19 UNION ALL
  SELECT '晚霞', 'base', 'linear-gradient(135deg, #ff9966, #ff5e62)', '绚烂晚霞渐变', 'default', 20 UNION ALL
  SELECT '阿凡达', 'base', 'linear-gradient(135deg, #1488cc, #2b32b2)', '科幻阿凡达渐变', 'default', 21
) x
WHERE NOT EXISTS (SELECT 1 FROM `sys_avatar_frame` f WHERE f.`sys_avatar_frame_name` = n);

-- 图片/APNG 框(ID 23-27)
INSERT INTO `sys_avatar_frame` (`sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`)
SELECT * FROM (
  SELECT '鎏金' AS n, 'base' AS t, '/assets/avatar-frames/frame-gold.png' AS i, '金色图腾环' AS d, 'default' AS a, 22 AS s UNION ALL
  SELECT '皇室蓝', 'base', '/assets/avatar-frames/frame-royal.png', '经典皇室蓝环', 'default', 23 UNION ALL
  SELECT '玫瑰金', 'base', '/assets/avatar-frames/frame-rose.png', '优雅玫瑰金环', 'default', 24 UNION ALL
  SELECT '焰光', 'base', '/assets/avatar-frames/frame-flame.apng', '火焰呼吸动画(APNG)', 'default', 25 UNION ALL
  SELECT '晶蓝', 'base', '/assets/avatar-frames/frame-azure.apng', '晶蓝呼吸动画(APNG)', 'default', 26
) x
WHERE NOT EXISTS (SELECT 1 FROM `sys_avatar_frame` f WHERE f.`sys_avatar_frame_name` = n);

SET FOREIGN_KEY_CHECKS = 1;