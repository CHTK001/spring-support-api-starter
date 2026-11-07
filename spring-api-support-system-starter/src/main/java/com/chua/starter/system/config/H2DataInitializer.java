// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.config;

import com.chua.starter.system.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * H2 数据库初始化器：
 * 1) 清理旧表；2) 执行项目自带完整 DDL data-h2.sql（列与实体对齐）；
 * 3) 补建 data-h2.sql 未覆盖的扩展表；4) 写入演示种子数据。
 *
 * @author CH
 * @since 4.0.0.51
 */
@Profile("h2")
@Component
public class H2DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(H2DataInitializer.class);

    @Autowired
    /**
     * jdbc模板
    */
    private JdbcTemplate jdbcTemplate;

    @Autowired
    /**
     * 数据来源
    */
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("[H2初始化] 开始...");
            // 建表脚本已经负责库结构与种子数据，重建会把它们灌好的数据删光
            boolean scripted = hasBaseSchema();
            if (!scripted) {
                dropAll();
                runSchema();
            }
            createExtensionTables();
            if (!scripted) {
                insertSeedData();
            }
            log.info("[H2初始化] 完成（{}）", scripted ? "沿用建表脚本的结构与数据" : "重建库结构 + 演示数据");
        } catch (Exception e) {
            log.error("[H2初始化] 初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 基础表是否已由建表脚本创建。
     */
    private boolean hasBaseSchema() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES"
                        + " WHERE UPPER(TABLE_NAME) IN ('SYS_USER','SYS_ROLE','SYS_MENU')",
                Integer.class);
        return total != null && total >= 3;
    }

    /**
     * 删除全部。
     */
    private void dropAll() {
        String[] tables = {
            "sys_user_post", "sys_role_menu", "sys_user_role", "sys_user_avatar_frame",
            "sys_avatar_frame", "sys_announcement_read", "sys_announcement", "sys_feedback",
            "sys_message", "sys_error_log", "sys_tenant", "sys_log", "sys_user_log",
            "sys_post", "sys_dict_item_property", "sys_dict_item", "sys_dict",
            "sys_setting", "sys_setting_group", "sys_dept_principal_rel", "sys_dept",
            "sys_role", "sys_mini_menu", "sys_menu", "sys_user"
        };
        for (String t : tables) {
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + t);
            } catch (Exception e) {
                log.warn("[H2] 初始化数据失败，已忽略: {}", e.getMessage());
            }
        }
    }

    /**
     * 执行项目自带完整建表脚本（continueOnError 跳过 H2 不兼容的个别 MySQL 语法）
    */
    private void runSchema() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("data-h2.sql"));
        populator.setContinueOnError(true);
        populator.setIgnoreFailedDrops(true);
        populator.setSeparator(";");
        populator.execute(dataSource);
        log.info("[H2初始化] data-h2.sql 执行完成(容错)");
    }

    /**
     * 补建 data-h2.sql 未覆盖的扩展表
    */
    private void createExtensionTables() {
        // 岗位（备注列名为裸 remark）
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_post (" +
            "sys_post_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_post_name VARCHAR(100), sys_post_code VARCHAR(100)," +
            "sys_post_sort INT DEFAULT 0, sys_post_status INT DEFAULT 0, remark VARCHAR(500)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        // 用户-岗位
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_post (" +
            "sys_user_id INT, sys_post_id INT, PRIMARY KEY(sys_user_id, sys_post_id))");
        // 消息
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_message (" +
            "sys_message_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_message_title VARCHAR(255), sys_message_content TEXT," +
            "sys_message_type VARCHAR(50), sys_message_level VARCHAR(20)," +
            "sys_message_sender_id INT, sys_message_sender_name VARCHAR(100)," +
            "sys_message_receiver_id INT, sys_message_receiver_name VARCHAR(100)," +
            "sys_message_read INT DEFAULT 0, sys_message_read_time datetime," +
            "sys_message_send_time datetime, sys_message_expire_time datetime," +
            "sys_message_biz_type VARCHAR(50), sys_message_biz_id VARCHAR(100)," +
            "sys_message_url VARCHAR(500), sys_message_extra TEXT, sys_message_status INT DEFAULT 1," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        // 异常日志
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_error_log (" +
            "sys_error_log_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_error_key VARCHAR(500), sys_error_source VARCHAR(50)," +
            "sys_error_message VARCHAR(1000), sys_error_stack TEXT," +
            "sys_error_filename VARCHAR(1000), sys_error_lineno INT, sys_error_colno INT," +
            "sys_error_resource_url VARCHAR(1000), sys_error_tag_name VARCHAR(100)," +
            "sys_error_first_at BIGINT, sys_error_count INT DEFAULT 1," +
            "sys_error_report_time BIGINT, sys_error_status INT DEFAULT 0," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        // 租户
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_tenant (" +
            "sys_tenant_id VARCHAR(64) PRIMARY KEY," +
            "sys_tenant_name VARCHAR(200), sys_tenant_username VARCHAR(100)," +
            "sys_tenant_password VARCHAR(255), sys_tenant_sign VARCHAR(500)," +
            "sys_tenant_corporation VARCHAR(200), sys_tenant_delete INT DEFAULT 0," +
            "sys_tenant_status INT DEFAULT 0, sys_tenant_contact VARCHAR(100)," +
            "sys_tenant_phone VARCHAR(50), sys_tenant_address VARCHAR(500)," +
            "sys_tenant_email VARCHAR(200), sys_tenant_code VARCHAR(100)," +
            "sys_tenant_gid VARCHAR(100), sys_tenant_home_url VARCHAR(500)," +
            "sys_tenant_remark VARCHAR(500)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        // 建表脚本按 MySQL 语法写的这几张表在本库解析不过（DEFAULT 必须写在可空约束之前），在此按本库语法补齐
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_setting (" +
            "sys_setting_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_setting_name VARCHAR(255), sys_setting_code VARCHAR(255), sys_setting_value TEXT," +
            "sys_setting_value_type VARCHAR(255), sys_setting_group VARCHAR(255)," +
            "sys_setting_status INT DEFAULT 1, sys_setting_sort INT DEFAULT 0," +
            "sys_setting_collect INT DEFAULT 0, sys_setting_in_system INT DEFAULT 0," +
            "sys_setting_app_inner INT DEFAULT 0, sys_setting_admin INT DEFAULT 0," +
            "sys_setting_version INT DEFAULT 1, sys_setting_config VARCHAR(255)," +
            "sys_setting_remark VARCHAR(255), sys_setting_is_sensitive INT DEFAULT 0," +
            "sys_setting_validate VARCHAR(512)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT," +
            "CONSTRAINT uk_setting_name_group UNIQUE (sys_setting_name, sys_setting_group))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_setting_group (" +
            "sys_setting_group_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_setting_group_name VARCHAR(255), sys_setting_group_code VARCHAR(255)," +
            "sys_setting_group_icon VARCHAR(255), sys_setting_group_enable INT DEFAULT 1," +
            "sys_setting_group_remark VARCHAR(255), sys_setting_group_sort INT DEFAULT 0," +
            "sys_setting_group_use_project_interface INT DEFAULT 0," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT," +
            "CONSTRAINT uk_group_code UNIQUE (sys_setting_group_code))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dict (" +
            "sys_dict_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_dict_name VARCHAR(255), sys_dict_code VARCHAR(255), sys_dict_pid INT DEFAULT 0," +
            "sys_dict_status INT DEFAULT 1, sys_dict_in_system INT DEFAULT 0," +
            "sys_dict_i18n VARCHAR(255), sys_dict_sort INT DEFAULT 0, sys_dict_remark VARCHAR(255)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT," +
            "CONSTRAINT uk_dict_code UNIQUE (sys_dict_code))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dict_item (" +
            "sys_dict_item_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_dict_id INT, sys_dict_item_name VARCHAR(255), sys_dict_item_code VARCHAR(255)," +
            "sys_dict_item_icon VARCHAR(255), sys_dict_item_status INT DEFAULT 1," +
            "sys_dict_item_sort INT DEFAULT 0, sys_dict_item_i18n VARCHAR(255)," +
            "sys_dict_item_delete INT DEFAULT 0, sys_dict_item_property VARCHAR(255)," +
            "sys_dict_item_remark VARCHAR(255)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dict_item_property (" +
            "sys_dict_item_property_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_dict_item_id INT, sys_dict_item_property_group VARCHAR(20)," +
            "sys_dict_item_property_name VARCHAR(255), sys_dict_item_property_value VARCHAR(255)," +
            "sys_dict_item_property_selected_type VARCHAR(255)," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_log (" +
            "sys_log_id INT AUTO_INCREMENT PRIMARY KEY," +
            "sys_log_username VARCHAR(255), sys_log_address VARCHAR(255), sys_log_url VARCHAR(255)," +
            "sys_log_param VARCHAR(255), sys_log_from VARCHAR(255), sys_log_browser VARCHAR(255)," +
            "sys_log_content VARCHAR(2000), sys_log_error TEXT," +
            "sys_log_status INT, sys_log_cost INT," +
            "create_name VARCHAR(255), create_by INT, create_time datetime, update_time datetime," +
            "update_name VARCHAR(255), update_by INT)");
        // 反馈扩展列（data-h2.sql 的 sys_feedback 可能缺这些，补齐）
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_images VARCHAR(1000)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_deal_by INT");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_deal_name VARCHAR(100)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_content TEXT");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_time datetime");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_recover_images VARCHAR(1000)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_user_agent VARCHAR(500)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_platform VARCHAR(100)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_feedback ADD COLUMN IF NOT EXISTS sys_feedback_priority INT DEFAULT 1");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        // sys_user 补列
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS sys_user_lock INT DEFAULT 0");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        // sys_setting 补列
        try {
            jdbcTemplate.execute("ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_config VARCHAR(255)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_collect INT DEFAULT 0");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_is_sensitive INT DEFAULT 0");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_validate VARCHAR(255)");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        // sys_announcement 补列
        try {
            jdbcTemplate.execute("ALTER TABLE sys_announcement ADD COLUMN IF NOT EXISTS sys_announcement_expire_time datetime");
        } catch (Exception e) {
            log.warn("[H2] 执行 ALTER TABLE 补列失败，已忽略: {}", e.getMessage());
        }
        // 趣味模块配置表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS edu_fun_config (" +
            "config_id INT AUTO_INCREMENT PRIMARY KEY," +
            "module_code VARCHAR(50) UNIQUE," +
            "module_name VARCHAR(100)," +
            "module_desc VARCHAR(500)," +
            "module_icon VARCHAR(100)," +
            "module_path VARCHAR(500)," +
            "enabled INT DEFAULT 1," +
            "sort_order INT DEFAULT 0," +
            "config_json TEXT," +
            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        // 成语表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS edu_idiom (" +
            "idiom_id INT AUTO_INCREMENT PRIMARY KEY," +
            "idiom_content VARCHAR(100)," +
            "idiom_pinyin VARCHAR(200)," +
            "idiom_meaning VARCHAR(1000)," +
            "idiom_source VARCHAR(500)," +
            "idiom_example VARCHAR(1000)," +
            "idiom_synonyms VARCHAR(500)," +
            "idiom_antonyms VARCHAR(500)," +
            "idiom_difficulty INT DEFAULT 1," +
            "idiom_tags VARCHAR(200)," +
            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        // 成语学段映射表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS edu_idiom_grade (" +
            "map_id INT AUTO_INCREMENT PRIMARY KEY," +
            "idiom_id INT," +
            "grade_level VARCHAR(50)," +
            "grade VARCHAR(50)," +
            "semester VARCHAR(10)," +
            "textbook_version VARCHAR(100)," +
            "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        log.info("[H2初始化] 扩展表与补列完成");
    }

    /**
     * 插入Seed数据。
     */
    private void insertSeedData() {
        // 摘要算法必须与登录校验端一致，盐值需与摘要配对写入
        String salt = "123456";
        String adminPwd = "'" + PasswordUtils.encrypt("admin123", salt) + "'";

        // 角色（data-h2.sql 可能已建 SUPER_ADMIN，这里补两个业务角色）
        jdbcTemplate.update("DELETE FROM sys_role WHERE sys_role_code IN ('OPERATOR','READ_ONLY')");
        jdbcTemplate.update("INSERT INTO sys_role (sys_role_id, sys_role_name, sys_role_code, sys_role_status, sys_role_in_system, sys_role_" +
            "sort, sys_role_data_scope, sys_role_remark) " +
            "VALUES (2,'运营管理员','OPERATOR',1,0,2,2,'日常运营')");
        jdbcTemplate.update("INSERT INTO sys_role (sys_role_id, sys_role_name, sys_role_code, sys_role_status, sys_role_in_system, sys_role_" +
            "sort, sys_role_data_scope, sys_role_readonly, sys_role_remark) " +
            "VALUES (3,'只读用户','READ_ONLY',1,0,3,1,1,'只读权限')");

        // 用户（覆盖口令，确保 admin123 可登录）
        jdbcTemplate.update("UPDATE sys_user SET sys_user_password=" + adminPwd + ", sys_user_salt='" + salt +
            "', sys_user_status=1 WHERE sys_user_username='admin'");
        jdbcTemplate.update("DELETE FROM sys_user WHERE sys_user_username IN ('zhangsan','lisi','wangwu')");
        jdbcTemplate.update("INSERT INTO sys_user (sys_user_id, sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_" +
            "user_status, sys_dept_id) " +
            "VALUES (2,'zhangsan'," + adminPwd + ",'" + salt + "','张三',1,2)");
        jdbcTemplate.update("INSERT INTO sys_user (sys_user_id, sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_" +
            "user_status, sys_dept_id) " +
            "VALUES (3,'lisi'," + adminPwd + ",'" + salt + "','李四',1,2)");
        jdbcTemplate.update("INSERT INTO sys_user (sys_user_id, sys_user_username, sys_user_password, sys_user_salt, sys_user_nickname, sys_" +
            "user_status, sys_dept_id) " +
            "VALUES (4,'wangwu'," + adminPwd + ",'" + salt + "','王五',1,3)");
        // 用户-角色
        jdbcTemplate.update("DELETE FROM sys_user_role");
        jdbcTemplate.update("INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES (1,1),(2,2),(3,3),(4,2)");

        // 部门（补技术部/市场部）
        jdbcTemplate.update("DELETE FROM sys_dept WHERE sys_dept_code IN ('TECH','MARKET')");
        jdbcTemplate.update("INSERT INTO sys_dept (sys_dept_id, sys_dept_name, sys_dept_pid, sys_dept_sort, sys_dept_status, sys_dept_code) " +
            "VALUES (2,'技术部',1,1,1,'TECH')");
        jdbcTemplate.update("INSERT INTO sys_dept (sys_dept_id, sys_dept_name, sys_dept_pid, sys_dept_sort, sys_dept_status, sys_dept_code) " +
            "VALUES (3,'市场部',1,2,1,'MARKET')");

        // 岗位
        jdbcTemplate.update("DELETE FROM sys_post");
        jdbcTemplate.update("INSERT INTO sys_post (sys_post_id, sys_post_name, sys_post_code, sys_post_status, sys_post_sort, remark) VALUES " +
            "(1,'产品经理','PM',0,1,'产品规划'),(2,'开发工程师','DEV',0,2,'研发'),(3,'测试工程师','QA',0,3,'质量保障')");
        jdbcTemplate.update("DELETE FROM sys_user_post");
        jdbcTemplate.update("INSERT INTO sys_user_post (sys_user_id, sys_post_id) VALUES (2,2),(3,3),(4,1)");

        // 字典（列名以建表脚本为准：名称/编码，不是 label/value）
        jdbcTemplate.update("DELETE FROM sys_dict_item");
        jdbcTemplate.update("DELETE FROM sys_dict");
        jdbcTemplate.update("INSERT INTO sys_dict (sys_dict_id, sys_dict_name, sys_dict_code, sys_dict_status, sys_dict_sort, sys" +
            "_dict_remark) VALUES " +
            "(1,'优先级','priority',1,1,'任务优先级'),(2,'状态','status',1,2,'通用启用状态')");
        jdbcTemplate.update("INSERT INTO sys_dict_item (sys_dict_id, sys_dict_item_name, sys_dict_item_code, sys_dict_item_sort, sys_dict" +
            "_item_status) VALUES " +
            "(1,'高','high',1,1),(1,'中','medium',2,1),(1,'低','low',3,1)," +
            "(2,'启用','enabled',1,1),(2,'禁用','disabled',2,1)");

        // 公告
        jdbcTemplate.update("DELETE FROM sys_announcement");
        jdbcTemplate.update("INSERT INTO sys_announcement (sys_announcement_title, sys_announcement_content, sys_announcement_type, sys_anno" +
            "uncement_status, sys_announcement_level, sys_announcement_position, sys_announcement_sort, sys_announcement_pub" +
            "lish_time, sys_announcement_publisher_name) VALUES " +
            "('安全提醒','请妥善保管账号密码，不要在公共设备登录。','global',1,2,'default',1,CURRENT_TIMESTAMP,'admin')," +
            "('V2.0版本发布','系统已升级至 V2.0，新增岗位、消息、缓存管理等模块。','global',1,1,'fixed-top',2,CURRENT_TIMESTAMP,'admin')," +
            "('系统维护通知','本周日凌晨 2:00-4:00 进行系统维护。','global',0,0,'default',3,CURRENT_TIMESTAMP,'admin')");

        // 反馈
        jdbcTemplate.update("DELETE FROM sys_feedback");
        jdbcTemplate.update("INSERT INTO sys_feedback (sys_feedback_content, sys_feedback_type, sys_feedback_status, sys_feedb" +
            "ack_priority) VALUES " +
            "('用户管理列表删除按钮点击无响应','bug',0,2)," +
            "('大数据量下列表加载较慢，建议优化分页','performance',0,1)," +
            "('希望增加导出为 Excel 的功能','feature',1,1)");

        // 消息
        jdbcTemplate.update("DELETE FROM sys_message");
        jdbcTemplate.update("INSERT INTO sys_message (sys_message_title, sys_message_content, sys_message_type, sys_message_sender_id, sys_m" +
            "essage_receiver_id, sys_message_status, sys_message_read, sys_message_level, sys_message_send_time) VALUES " +
            "('安全警告','检测到您的账号在异地登录','warning',1,1,1,0,'high',CURRENT_TIMESTAMP)," +
            "('任务提醒','您有一份待处理的审批工单','notice',1,1,1,0,'normal',CURRENT_TIMESTAMP)," +
            "('系统通知','系统将于本周日凌晨进行维护','notice',1,1,1,1,'low',CURRENT_TIMESTAMP)");

        // 异常日志
        jdbcTemplate.update("DELETE FROM sys_error_log");
        jdbcTemplate.update("INSERT INTO sys_error_log (sys_error_key, sys_error_source, sys_error_message, sys_error_stack, sys_error_filen" +
            "ame, sys_error_first_at, sys_error_count, sys_error_report_time, sys_error_status) VALUES " +
            "('pay-timeout','rejection','支付网关超时 3000ms','SocketTimeoutException','pay.js',1750000000000,3,1750000000000,0)," +
            "('db-conn','js','数据库连接超时','SQLTransientConnectionException','db.js',1750000000000,1,1750000000000,1)," +
            "('user-null','js','用户ID不能为空','IllegalArgumentException','user.js',1750000000000,5,1750000000000,0)");

        // 租户
        jdbcTemplate.update("DELETE FROM sys_tenant");
        jdbcTemplate.update("INSERT INTO sys_tenant (sys_tenant_id, sys_tenant_name, sys_tenant_username, sys_tenant_status, sys_tenant_cont" +
            "act, sys_tenant_phone, sys_tenant_email, sys_tenant_remark) VALUES " +
            "('T001','字节跳动','bytetech',0,'张管理','13900000001','bd@example.com','总部')," +
            "('T002','阿里巴巴集团','aligroup',1,'李管理','13900000002','ali@example.com','已到期')," +
            "('T003','腾讯科技','tencent',1,'王管理','13900000003','tencent@example.com','已禁用')");

        // 操作日志
        jdbcTemplate.update("DELETE FROM sys_log");
        jdbcTemplate.update("INSERT INTO sys_log (sys_log_username, sys_log_from, sys_log_url, sys_log_status, sys_log_cost, sys_log_address" +
            ", sys_log_content) VALUES " +
            "('admin','系统设置','/v2/setting/update',1,35,'127.0.0.1','更新系统设置')," +
            "('admin','公告','/v2/announcement/delete',1,12,'127.0.0.1','删除公告')," +
            "('admin','角色','/v2/role/update',1,28,'127.0.0.1','更新角色')");

        // 登录日志
        jdbcTemplate.update("DELETE FROM sys_user_log");
        jdbcTemplate.update("INSERT INTO sys_user_log (sys_log_username, sys_log_login_type, sys_log_status, sys_log_message, sys_log_ip, sy" +
            "s_log_address, sys_log_browser, sys_log_system) VALUES " +
            "('admin','WEB',1,'登录成功','127.0.0.1','内网','Chrome','Windows')," +
            "('zhangsan','WEB',1,'登录成功','127.0.0.1','内网','Chrome','Windows')," +
            "('lisi','WEB',1,'登录成功','127.0.0.1','内网','Edge','Windows')," +
            "('wangwu','WEB',0,'密码错误','127.0.0.1','内网','Chrome','Windows')");

        // 趣味模块配置
        jdbcTemplate.update("DELETE FROM edu_fun_config");
        jdbcTemplate.update("INSERT INTO edu_fun_config (module_code, module_name, module_desc, module_icon, module_path, enabled, sort_orde" +
            "r, config_json) VALUES " +
            "('INSECT', '昆虫识图', '交互式3D昆虫图鉴，认识63种昆虫的身体构造', 'Bug', '/edu/fun/insect', 1, 1, '{\"url\":\"https://insect-world.pages.dev\",\"wid" +
                "th\":\"100%\",\"height\":\"800px\"}')," +
            "('IDIOM_GAME', '成语消消乐', '根据学段生成的成语配对游戏', 'Trophy', '/edu/fun/idiom', 1, 2, '{\"defaultCount\":10,\"showPinyin\":true}')," +
            "('IDIOM_BANK', '成语库', '管理成语数据和学段映射', 'Collection', '/edu/fun/idiom-bank', 1, 3, '{}')");

        // 成语数据（按学段）
        jdbcTemplate.update("DELETE FROM edu_idiom");
        jdbcTemplate.update("INSERT INTO edu_idiom (idiom_content, idiom_pinyin, idiom_meaning, idiom_source, idiom_example, idiom_difficult" +
            "y, idiom_tags) VALUES " +
            "('守株待兔', 'shou zhu dai tu', '比喻死守狭隘经验，不知变通', '韩非子·五蠹', '我们不能守株待兔，要主动出击。', 1, '寓言,动物')," +
            "('画蛇添足', 'hua she tian zu', '比喻做多余的事，反而弄巧成拙', '战国策·齐策二', '这样做简直是画蛇添足。', 1, '寓言,动物')," +
            "('亡羊补牢', 'wang yang bu lao', '比喻出了差错后及时补救', '战国策·楚策四', '亡羊补牢，为时未晚。', 1, '寓言,动物')," +
            "('井底之蛙', 'jing di zhi wa', '比喻见识狭隘的人', '庄子·秋水', '不要做井底之蛙，要开阔眼界。', 1, '寓言,动物')," +
            "('狐假虎威', 'hu jia hu wei', '比喻借别人的威势吓唬人', '战国策·楚策一', '他不过是狐假虎威罢了。', 1, '寓言,动物')," +
            "('对牛弹琴', 'dui niu tan qin', '比喻对不懂道理的人讲道理', '理惑论', '跟他说这些简直是对牛弹琴。', 2, '寓言,动物')," +
            "('鹤立鸡群', 'he li ji qun', '比喻一个人的才能或仪表出众', '世说新语·容止', '他在人群中鹤立鸡群。', 2, '动物,外貌')," +
            "('杯弓蛇影', 'bei gong she ying', '比喻疑神疑鬼，自相惊扰', '风俗通义·世间', '不要杯弓蛇影，自己吓自己。', 2, '寓言,动物')," +
            "('马到成功', 'ma dao cheng gong', '比喻事情一开始就取得成功', '元曲选·郑德辉', '祝你马到成功！', 2, '动物,成功')," +
            "('龙飞凤舞', 'long fei feng wu', '形容书法笔势有力，也形容山势蜿蜒', '宋·苏轼', '他的字龙飞凤舞，很有气势。', 2, '动物,艺术')," +
            "('虎头蛇尾', 'hu tou she wei', '比喻做事有始无终', '元·康进之', '做事不能虎头蛇尾。', 3, '动物,做事')," +
            "('鸡飞蛋打', 'ji fei dan da', '比喻一无所得', '民间谚语', '结果鸡飞蛋打，什么都没捞到。', 3, '动物,失败')," +
            "('猴年马月', 'hou nian ma yue', '比喻遥遥无期', '民间谚语', '这事要等到猴年马月啊。', 3, '动物,时间')," +
            "('狗急跳墙', 'gou ji tiao qiang', '比喻坏人在走投无路时豁出去', '敦煌变文集·燕子赋', '小心狗急跳墙。', 3, '动物,处境')," +
            "('螳臂当车', 'tang bi dang che', '比喻不自量力', '庄子·人间世', '他的反对不过是螳臂当车。', 4, '动物,寓言')," +
            "('鱼目混珠', 'yu mu hun zhu', '比喻以假乱真', '玉清经', '市场上鱼目混珠的现象很多。', 4, '动物,真假')," +
            "('鹏程万里', 'peng cheng wan li', '比喻前程远大', '庄子·逍遥游', '祝你鹏程万里！', 4, '动物,前途')," +
            "('塞翁失马', 'sai weng shi ma', '比喻坏事在一定条件下可以变为好事', '淮南子·人间训', '塞翁失马，焉知非福。', 5, '寓言,哲理')," +
            "('叶公好龙', 'ye gong hao long', '比喻口头上说爱好，实际上并不真爱好', '新序·杂事', '他不过是叶公好龙罢了。', 5, '寓言,哲理')," +
            "('掩耳盗铃', 'yan er dao ling', '比喻自己欺骗自己', '吕氏春秋·自知', '这样做简直是掩耳盗铃。', 5, '寓言,哲理')");

        // 成语学段映射
        jdbcTemplate.update("DELETE FROM edu_idiom_grade");
        jdbcTemplate.update("INSERT INTO edu_idiom_grade (idiom_id, grade_level, grade, semester, textbook_version) VALUES " +
            "(1,'小学','一年级','上','人教版'),(2,'小学','一年级','上','人教版'),(3,'小学','一年级','上','人教版')," +
            "(4,'小学','二年级','上','人教版'),(5,'小学','二年级','上','人教版'),(6,'小学','二年级','下','人教版')," +
            "(7,'小学','三年级','上','人教版'),(8,'小学','三年级','下','人教版'),(9,'小学','四年级','上','人教版')," +
            "(10,'小学','四年级','下','人教版'),(11,'小学','五年级','上','人教版'),(12,'小学','五年级','下','人教版')," +
            "(13,'小学','六年级','上','人教版'),(14,'小学','六年级','下','人教版')," +
            "(15,'初中','初一','上','人教版'),(16,'初中','初一','下','人教版'),(17,'初中','初二','上','人教版')," +
            "(18,'初中','初二','下','人教版'),(19,'初中','初三','上','人教版'),(20,'初中','初三','下','人教版')");
    }
}
