package com.chua.starter.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.chua.starter.system.util.PasswordUtils;

/**
 * 在原始 H2DataInitializer（ApplicationRunner）建表并插入基础数据之后，
 * 补充演示数据。监听 ApplicationReadyEvent 确保一定在所有 Runner 之后执行。
 * 每条 SQL 独立 try/catch，失败不阻断启动与登录。
 *
 * @author CH
 * @since 4.0.0.51
 */
@Profile("h2")
@Component
public class DataSeeder implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    /**
     * 演示账号统一口令与盐值，写库时按校验端算法生成摘要
     */
    private static final String DEMO_PASSWORD = "admin123";
    private static final String DEMO_SALT = "123456";
    private final JdbcTemplate jdbc;
    public DataSeeder(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 摘要算法必须与登录校验端一致，否则演示账号写入口令后无法登录
        String adminPwd = "'" + PasswordUtils.encrypt(DEMO_PASSWORD, DEMO_SALT) + "'";
        log.info("[DataSeeder] 开始补充演示数据（ApplicationReady）");

        // 补齐当前实体引用但旧建表 DDL 缺失的列
        alter("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS remark VARCHAR(500)");
        alter("ALTER TABLE sys_post ADD COLUMN IF NOT EXISTS remark VARCHAR(500)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_key VARCHAR(100)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_message VARCHAR(500)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_filename VARCHAR(200)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_first_at BIGINT");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_count INT DEFAULT 0");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_report_time BIGINT");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_source VARCHAR(100)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_stack VARCHAR(2000)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_images VARCHAR(1000)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_deal_by INT");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_deal_name VARCHAR(100)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_content TEXT");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_time TIMESTAMP");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_images VARCHAR(1000)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_user_agent VARCHAR(500)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_platform VARCHAR(100)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS create_name VARCHAR(100)");
        alter("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS update_name VARCHAR(100)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_level VARCHAR(50)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_send_time TIMESTAMP");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_sender_name VARCHAR(100)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_receiver_name VARCHAR(100)");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_username VARCHAR(100)");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_password VARCHAR(200)");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_sign VARCHAR(200)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_lineno VARCHAR(50)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_read_time TIMESTAMP");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_corporation VARCHAR(200)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_colno VARCHAR(50)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_expire_time TIMESTAMP");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_gid VARCHAR(100)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_resource_url VARCHAR(500)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_biz_type VARCHAR(50)");
        alter("ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS sys_tenant_home_url VARCHAR(500)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_tag_name VARCHAR(100)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_biz_id VARCHAR(100)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_url VARCHAR(500)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS sys_error_status INT DEFAULT 0");
        // tenant_service 关联表
        alter("CREATE TABLE IF NOT EXISTS sys_tenant_service (sys_tenant_service_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_tenant_id BIGINT, sys_service_id BIGINT, sys_service_name VARCHAR(200), sys_service_code VARCHAR(100), sys_service_status INT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS create_name VARCHAR(100)");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS sys_tenant_service_valid_time TIMESTAMP");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS sys_message_extra VARCHAR(500)");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS create_by INT");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS update_by INT");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS update_time TIMESTAMP");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS create_name VARCHAR(100)");
        alter("ALTER TABLE sys_message ADD COLUMN IF NOT EXISTS update_name VARCHAR(100)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS create_name VARCHAR(100)");
        alter("ALTER TABLE sys_error_log ADD COLUMN IF NOT EXISTS update_name VARCHAR(100)");
        alter("ALTER TABLE sys_tenant_service ADD COLUMN IF NOT EXISTS update_name VARCHAR(100)");

        // 角色（id=1 已是 SuperAdmin，补 2/3）
        safe("INSERT INTO sys_role (sys_role_name, sys_role_code, sys_role_status, sys_role_in_system, sys_role_sort, sys_role_data_scope, sys_role_remark) VALUES ('运营管理员','OPERATOR',1,0,2,2,'日常运营')");
        safe("INSERT INTO sys_role (sys_role_name, sys_role_code, sys_role_status, sys_role_in_system, sys_role_sort, sys_role_data_scope, sys_role_readonly, sys_role_remark) VALUES ('只读用户','READ_ONLY',1,0,3,1,1,'只读权限')");

        // 用户（admin 口令兜底 + 三个演示用户，盐值需与摘要配对）
        safe("UPDATE sys_user SET sys_user_password=" + adminPwd + ", sys_user_salt='" + DEMO_SALT + "', sys_user_status=1 WHERE sys_user_username='admin'");
        safe("INSERT INTO sys_user (sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_user_status, sys_dept_id, sys_user_email, sys_user_phone) VALUES ('zhangsan'," + adminPwd + ",'" + DEMO_SALT + "','张三',1,2,'zhangsan@example.com','13800000001')");
        safe("INSERT INTO sys_user (sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_user_status, sys_dept_id, sys_user_email, sys_user_phone) VALUES ('lisi'," + adminPwd + ",'" + DEMO_SALT + "','李四',1,2,'lisi@example.com','13800000002')");
        safe("INSERT INTO sys_user (sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_user_status, sys_dept_id, sys_user_email, sys_user_phone) VALUES ('wangwu'," + adminPwd + ",'" + DEMO_SALT + "','王五',1,3,'wangwu@example.com','13800000003')");
        safe("INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES (1,1),(2,2),(3,3),(4,2)");

        // 部门（id=1 已是 Root Dept，补 2/3）
        safe("INSERT INTO sys_dept (sys_dept_name, sys_dept_pid, sys_dept_sort, sys_dept_status, sys_dept_code) VALUES ('技术部',1,1,1,'TECH')");
        safe("INSERT INTO sys_dept (sys_dept_name, sys_dept_pid, sys_dept_sort, sys_dept_status, sys_dept_code) VALUES ('市场部',1,2,1,'MARKET')");

        // 岗位
        safe("INSERT INTO sys_post (sys_post_name, sys_post_code, sys_post_status, sys_post_sort, remark) VALUES ('产品经理','PM',1,1,'产品规划'),('开发工程师','DEV',1,2,'研发'),('测试工程师','QA',1,3,'质量保障')");
        safe("INSERT INTO sys_user_post (sys_user_id, sys_post_id) VALUES (2,2),(3,3),(4,1)");

        // 字典项
        // 字典（列名以建表脚本为准：名称/编码，不是 label/value）
        safe("INSERT INTO sys_dict (sys_dict_id, sys_dict_name, sys_dict_code, sys_dict_status, sys_dict_sort, sys_dict_remark) VALUES (1,'优先级','priority',1,1,'任务优先级')");
        safe("INSERT INTO sys_dict_item (sys_dict_id, sys_dict_item_name, sys_dict_item_code, sys_dict_item_sort, sys_dict_item_status) VALUES (1,'高','high',1,1),(1,'中','medium',2,1),(1,'低','low',3,1)");

        // 公告
        safe("INSERT INTO sys_announcement (sys_announcement_title, sys_announcement_content, sys_announcement_type, sys_announcement_status, sys_announcement_level, sys_announcement_sort, sys_announcement_publish_time, sys_announcement_publisher_name) VALUES ('安全提醒','请妥善保管账号密码。','global',1,2,1,CURRENT_TIMESTAMP,'admin'),('V2.0发布','系统已升级。','global',1,1,2,CURRENT_TIMESTAMP,'admin')");

        // 反馈
        safe("INSERT INTO sys_feedback (sys_feedback_content, sys_feedback_type, sys_feedback_status, sys_feedback_priority, sys_feedback_user_id) VALUES ('列表删除按钮无响应','bug',0,2,1),('大数据量分页较慢','performance',0,1,1)");

        // 消息
        safe("INSERT INTO sys_message (sys_message_title, sys_message_content, sys_message_type, sys_message_sender_id, sys_message_receiver_id, sys_message_status, sys_message_read, sys_message_level, sys_message_send_time, sys_message_sender_name) VALUES ('安全警告','检测到异地登录','warning',1,1,1,0,'high',CURRENT_TIMESTAMP,'admin'),('任务提醒','待处理审批工单','notice',1,1,1,0,'normal',CURRENT_TIMESTAMP,'admin')");

        // 异常日志
        safe("INSERT INTO sys_error_log (sys_error_log_title, sys_error_log_content, sys_error_log_stack, sys_error_log_source, sys_error_log_status, sys_error_key, sys_error_message, sys_error_filename, sys_error_count) VALUES ('支付网关超时','支付接口调用超时3000ms','SocketTimeoutException','rejection',0,'pay-timeout','支付超时','pay.js',3),('数据库连接超时','获取连接失败','SQLTransientConnectionException','js',1,'db-conn','连接超时','db.js',1)");

        // 租户
        safe("INSERT INTO sys_tenant (sys_tenant_name, sys_tenant_code, sys_tenant_status, sys_tenant_contact, sys_tenant_phone, sys_tenant_email, sys_tenant_remark, sys_tenant_username) VALUES ('字节跳动','T001',0,'张管理','13900000001','bd@example.com','总部','bytetech'),('阿里巴巴','T002',1,'李管理','13900000002','ali@example.com','已到期','aligroup')");

        // 系统菜单种子（SUPER_ADMIN 自动可见）。目录 pid=0，子菜单 pid=100。
        // 仅当 sys_menu 为空时插入，避免重复。
        Integer menuCount = queryForInt("SELECT COUNT(*) FROM sys_menu");
        if (menuCount == null || menuCount == 0) {
            safe("INSERT INTO sys_menu (sys_menu_id, sys_menu_name, sys_menu_title, sys_menu_path, sys_menu_component, sys_menu_icon, sys_menu_type, sys_menu_sort, sys_menu_pid, sys_menu_hidden, sys_menu_home, sys_menu_engine, sys_menu_keep_alive, sys_menu_is_tag, sys_menu_show_parent, create_time) VALUES (100,'System','系统管理','/system','Layout','system',0,1,0,0,0,0,0,0,1,CURRENT_TIMESTAMP)");
            String[][] menus = {
                {"101","UserManage","用户管理","/user","users","1","system/user/index"},
                {"102","RoleManage","角色管理","/role","shield","2","system/role/index"},
                {"103","MenuManage","菜单管理","/menu","menu","3","system/menu/index"},
                {"104","DeptManage","部门管理","/dept","dept","4","system/dept/index"},
                {"105","PostManage","岗位管理","/post","post","5","system/post/index"},
                {"106","DictManage","字典管理","/dict","dict","6","system/dict/index"},
                {"107","SettingManage","参数设置","/setting","setting","7","system/setting/index"},
                {"108","AnnouncementManage","公告管理","/announcement","announcement","8","system/announcement/index"},
                {"109","FeedbackManage","反馈管理","/feedback","feedback","9","system/feedback/index"},
                {"110","OnlineManage","在线用户","/online","online","10","system/online/index"},
                {"111","MessageManage","消息管理","/message","message","11","system/message/index"},
                {"112","ErrorLogManage","错误日志","/error-log","error","12","system/errorlog/index"},
                {"113","CacheManage","缓存监控","/cache","cache","13","system/cache/index"},
            };
            for (String[] m : menus) {
                safe("INSERT INTO sys_menu (sys_menu_id, sys_menu_name, sys_menu_title, sys_menu_path, sys_menu_component, sys_menu_icon, sys_menu_type, sys_menu_sort, sys_menu_pid, sys_menu_hidden, sys_menu_home, sys_menu_engine, sys_menu_keep_alive, sys_menu_is_tag, sys_menu_show_parent, create_time) VALUES ("
                    + m[0] + ",'" + m[1] + "','" + m[2] + "','" + m[3] + "','" + m[6] + "','" + m[4] + "',0," + m[5] + ",100,0,0,0,1,0,1,CURRENT_TIMESTAMP)");
            }
            log.info("[DataSeeder] 菜单种子已插入（目录1 + 13个子菜单）");
        }
        // 无论是否首次插入，都把 component 补齐到现有菜单（兼容旧数据）
        String[][] compMap = {
            {"101","system/user/index"},{"102","system/role/index"},{"103","system/menu/index"},
            {"104","system/dept/index"},{"105","system/post/index"},{"106","system/dict/index"},
            {"107","system/setting/index"},{"108","system/announcement/index"},{"109","system/feedback/index"},
            {"110","system/online/index"},{"111","system/message/index"},{"112","system/errorlog/index"},
            {"113","system/cache/index"},
        };
        for (String[] c : compMap) {
            safe("UPDATE sys_menu SET sys_menu_component='" + c[1] + "' WHERE sys_menu_id=" + c[0]);
        }

        log.info("[DataSeeder] 演示数据补充完成");
    }

    /**
     * safe。
     *
     * @param sql SQL，不允许为 null
     */
    private void safe(String sql) {
        try { jdbc.update(sql); }
        catch (Exception e) { log.warn("[DataSeeder] 跳过: {} | {}", sql.substring(0, Math.min(60, sql.length())), e.getMessage()); }
    }

    /**
     * alter。
     *
     * @param sql SQL，不允许为 null
     */
    private void alter(String sql) {
        try { jdbc.execute(sql); }
        catch (Exception e) { log.warn("[DataSeeder] 补列失败: {} | {}", sql.substring(0, Math.min(60, sql.length())), e.getMessage()); }
    }

    /**
     * 查询ForInt。
     *
     * @param sql SQL，不允许为 null
     * @return Integer 对象
     */
    private Integer queryForInt(String sql) {
        try { return jdbc.queryForObject(sql, Integer.class); }
        catch (Exception e) { return null; }
    }
}
