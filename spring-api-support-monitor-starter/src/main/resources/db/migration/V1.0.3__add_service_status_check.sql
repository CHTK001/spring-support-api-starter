-- 为soft_service_install表添加服务状态检查相关字段
ALTER TABLE soft_service_install
    ADD COLUMN install_status_check_command VARCHAR(1000) COMMENT '服务状态检查命令';
ALTER TABLE soft_service_install
    ADD COLUMN install_status_check_command_type INT COMMENT '服务状态检查命令类型（0:单行命令，1:脚本）';
ALTER TABLE soft_service_install
    ADD COLUMN install_status_check_success_flag VARCHAR(255) COMMENT '服务状态检查成功标识';
ALTER TABLE soft_service_install
    ADD COLUMN install_last_check_time BIGINT COMMENT '最后一次状态检查时间';