-- 为soft_service表添加安装次数和收藏次数字段
ALTER TABLE soft_service
    ADD COLUMN install_count INT DEFAULT 0 COMMENT '安装次数';
ALTER TABLE soft_service
    ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏次数';