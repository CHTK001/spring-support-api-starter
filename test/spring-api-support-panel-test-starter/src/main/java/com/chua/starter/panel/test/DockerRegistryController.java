package com.chua.starter.panel.test;

import com.chua.common.support.lang.code.ReturnResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试环境镜像仓库 CRUD 控制器。
 * <p>前端 SoftwareInstallWizard 通过 /docker/registries 加载仓库列表，
 * 供安装向导中的仓库选择器使用。直接使用 JdbcTemplate 查询 system_soft_registry 表，
 * 避免引入 monitor-starter 导致的 bean 名称冲突。</p>
 */
@RestController
@RequestMapping("/docker/registries")
@RequiredArgsConstructor
public class DockerRegistryController {

    private final JdbcTemplate jdbcTemplate;

    private static final String TABLE = "system_soft_registry";
    private static final String SELECT_COLS =
            "system_soft_registry_id, system_soft_registry_name, system_soft_registry_type, " +
            "system_soft_registry_url, system_soft_registry_username, system_soft_registry_password, " +
            "system_soft_registry_email, system_soft_registry_active, system_soft_registry_ssl_enabled, " +
            "system_soft_registry_support_sync, system_soft_registry_timeout, " +
            "system_soft_registry_description, system_soft_registry_config, " +
            "system_soft_registry_last_connect_time, system_soft_registry_connect_status, " +
            "system_soft_registry_error_message, system_soft_registry_sort, " +
            "system_soft_registry_status";

    /**
     * 启动时自动插入默认镜像仓库种子数据。
     */
    @PostConstruct
    public void initSeedData() {
        // 确保表存在（H2 等空库场景）
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "system_soft_registry_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "system_soft_registry_name VARCHAR(255), " +
                "system_soft_registry_type VARCHAR(100), " +
                "system_soft_registry_url VARCHAR(500), " +
                "system_soft_registry_username VARCHAR(255), " +
                "system_soft_registry_password VARCHAR(255), " +
                "system_soft_registry_email VARCHAR(255), " +
                "system_soft_registry_active INT DEFAULT 1, " +
                "system_soft_registry_ssl_enabled INT DEFAULT 1, " +
                "system_soft_registry_support_sync INT DEFAULT 0, " +
                "system_soft_registry_timeout INT DEFAULT 30, " +
                "system_soft_registry_description VARCHAR(500), " +
                "system_soft_registry_config TEXT, " +
                "system_soft_registry_last_connect_time VARCHAR(50), " +
                "system_soft_registry_connect_status INT DEFAULT 0, " +
                "system_soft_registry_error_message VARCHAR(500), " +
                "system_soft_registry_sort INT DEFAULT 0, " +
                "system_soft_registry_status INT DEFAULT 1" +
                ")");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE, Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO " + TABLE + " (system_soft_registry_name, system_soft_registry_type, " +
                    "system_soft_registry_url, system_soft_registry_ssl_enabled, system_soft_registry_support_sync, " +
                    "system_soft_registry_timeout, system_soft_registry_sort, system_soft_registry_status, " +
                    "system_soft_registry_active, system_soft_registry_description) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, ?)",
                    "阿里云容器镜像服务", "aliyun", "https://registry.cn-hangzhou.aliyuncs.com",
                    1, 1, 30, 1, "阿里云容器镜像服务（中国区），加速国内 Docker 镜像拉取");
            jdbcTemplate.update("INSERT INTO " + TABLE + " (system_soft_registry_name, system_soft_registry_type, " +
                    "system_soft_registry_url, system_soft_registry_ssl_enabled, system_soft_registry_support_sync, " +
                    "system_soft_registry_timeout, system_soft_registry_sort, system_soft_registry_status, " +
                    "system_soft_registry_active, system_soft_registry_description) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, ?)",
                    "DaoCloud 镜像加速", "docker_hub", "https://docker.m.daocloud.io/",
                    1, 0, 30, 2, "DaoCloud Docker Hub 镜像代理（国内加速）");
            jdbcTemplate.update("INSERT INTO " + TABLE + " (system_soft_registry_name, system_soft_registry_type, " +
                    "system_soft_registry_url, system_soft_registry_ssl_enabled, system_soft_registry_support_sync, " +
                    "system_soft_registry_timeout, system_soft_registry_sort, system_soft_registry_status, " +
                    "system_soft_registry_active, system_soft_registry_description) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, ?)",
                    "网易 163 镜像加速", "docker_hub", "https://hub-mirror.c.163.com/",
                    1, 0, 30, 3, "网易 Docker Hub 镜像代理（国内加速）");
            jdbcTemplate.update("INSERT INTO " + TABLE + " (system_soft_registry_name, system_soft_registry_type, " +
                    "system_soft_registry_url, system_soft_registry_ssl_enabled, system_soft_registry_support_sync, " +
                    "system_soft_registry_timeout, system_soft_registry_sort, system_soft_registry_status, " +
                    "system_soft_registry_active, system_soft_registry_description) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, ?)",
                    "百度镜像加速", "docker_hub", "https://mirror.baidubce.com/",
                    1, 0, 30, 4, "百度 Docker Hub 镜像代理（国内加速）");
        }
    }

    private RowMapper<Map<String, Object>> registryMapper = (rs, rowNum) -> {
        Map<String, Object> m = new LinkedHashMap<>();
        putInt(m, rs, "id", "system_soft_registry_id");
        putInt(m, rs, "systemSoftRegistryId", "system_soft_registry_id");
        putStr(m, rs, "registryName", "system_soft_registry_name");
        putStr(m, rs, "systemSoftRegistryName", "system_soft_registry_name");
        putStr(m, rs, "registryType", "system_soft_registry_type");
        putStr(m, rs, "systemSoftRegistryType", "system_soft_registry_type");
        putStr(m, rs, "registryUrl", "system_soft_registry_url");
        putStr(m, rs, "systemSoftRegistryUrl", "system_soft_registry_url");
        putStr(m, rs, "registryUsername", "system_soft_registry_username");
        putStr(m, rs, "systemSoftRegistryUsername", "system_soft_registry_username");
        putStr(m, rs, "registryEmail", "system_soft_registry_email");
        putStr(m, rs, "systemSoftRegistryEmail", "system_soft_registry_email");
        putInt(m, rs, "registryActive", "system_soft_registry_active");
        putInt(m, rs, "systemSoftRegistryActive", "system_soft_registry_active");
        putInt(m, rs, "registrySslEnabled", "system_soft_registry_ssl_enabled");
        putInt(m, rs, "systemSoftRegistrySslEnabled", "system_soft_registry_ssl_enabled");
        putInt(m, rs, "registrySupportSync", "system_soft_registry_support_sync");
        putInt(m, rs, "systemSoftRegistrySupportSync", "system_soft_registry_support_sync");
        putInt(m, rs, "registryTimeout", "system_soft_registry_timeout");
        putInt(m, rs, "systemSoftRegistryTimeout", "system_soft_registry_timeout");
        putStr(m, rs, "registryDescription", "system_soft_registry_description");
        putStr(m, rs, "systemSoftRegistryDescription", "system_soft_registry_description");
        putStr(m, rs, "registryConfig", "system_soft_registry_config");
        putStr(m, rs, "systemSoftRegistryConfig", "system_soft_registry_config");
        putStr(m, rs, "registryLastConnectTime", "system_soft_registry_last_connect_time");
        putStr(m, rs, "systemSoftRegistryLastConnectTime", "system_soft_registry_last_connect_time");
        putInt(m, rs, "registryConnectStatus", "system_soft_registry_connect_status");
        putInt(m, rs, "systemSoftRegistryConnectStatus", "system_soft_registry_connect_status");
        putStr(m, rs, "registryErrorMessage", "system_soft_registry_error_message");
        putStr(m, rs, "systemSoftRegistryErrorMessage", "system_soft_registry_error_message");
        putInt(m, rs, "registrySort", "system_soft_registry_sort");
        putInt(m, rs, "systemSoftRegistrySort", "system_soft_registry_sort");
        putInt(m, rs, "enabled", "system_soft_registry_status");
        putInt(m, rs, "systemSoftRegistryStatus", "system_soft_registry_status");
        return m;
    };

    @GetMapping
    public ReturnResult<List<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled) {
        StringBuilder sql = new StringBuilder("SELECT ").append(SELECT_COLS).append(" FROM ").append(TABLE).append(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (Boolean.TRUE.equals(enabled)) {
            sql.append(" AND system_soft_registry_status = 1");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND system_soft_registry_name LIKE ?");
            args.add("%" + keyword + "%");
        }
        sql.append(" ORDER BY system_soft_registry_sort");
        List<Map<String, Object>> result = jdbcTemplate.query(sql.toString(), registryMapper, args.toArray());
        return ReturnResult.ok(result);
    }

    @GetMapping("/{id}")
    public ReturnResult<Map<String, Object>> detail(@PathVariable Integer id) {
        String sql = "SELECT " + SELECT_COLS + " FROM " + TABLE + " WHERE system_soft_registry_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, registryMapper, id);
        if (rows.isEmpty()) {
            return ReturnResult.error("仓库不存在");
        }
        return ReturnResult.ok(rows.get(0));
    }

    @PostMapping
    public ReturnResult<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String sql = "INSERT INTO " + TABLE + " (system_soft_registry_name, system_soft_registry_type, system_soft_registry_url, " +
                "system_soft_registry_username, system_soft_registry_password, system_soft_registry_email, " +
                "system_soft_registry_active, system_soft_registry_ssl_enabled, system_soft_registry_support_sync, " +
                "system_soft_registry_timeout, system_soft_registry_description, system_soft_registry_config, " +
                "system_soft_registry_sort, system_soft_registry_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                str(body, "registryName", "systemSoftRegistryName"),
                str(body, "registryType", "systemSoftRegistryType"),
                str(body, "registryUrl", "systemSoftRegistryUrl"),
                str(body, "registryUsername", "systemSoftRegistryUsername"),
                str(body, "registryPassword", "systemSoftRegistryPassword"),
                str(body, "registryEmail", "systemSoftRegistryEmail"),
                intObj(body, "registryActive", "systemSoftRegistryActive"),
                intObj(body, "registrySslEnabled", "systemSoftRegistrySslEnabled"),
                intObj(body, "registrySupportSync", "systemSoftRegistrySupportSync"),
                intObj(body, "registryTimeout", "systemSoftRegistryTimeout"),
                str(body, "registryDescription", "systemSoftRegistryDescription"),
                str(body, "registryConfig", "systemSoftRegistryConfig"),
                intObj(body, "registrySort", "systemSoftRegistrySort"),
                intObj(body, "enabled", "systemSoftRegistryStatus", 1));
        // 查询新插入的记录
        Integer newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return detail(newId);
    }

    @PutMapping("/{id}")
    public ReturnResult<Map<String, Object>> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        String sql = "UPDATE " + TABLE + " SET system_soft_registry_name=?, system_soft_registry_type=?, system_soft_registry_url=?, " +
                "system_soft_registry_username=?, system_soft_registry_password=?, system_soft_registry_email=?, " +
                "system_soft_registry_active=?, system_soft_registry_ssl_enabled=?, system_soft_registry_support_sync=?, " +
                "system_soft_registry_timeout=?, system_soft_registry_description=?, system_soft_registry_config=?, " +
                "system_soft_registry_sort=?, system_soft_registry_status=? " +
                "WHERE system_soft_registry_id=?";
        jdbcTemplate.update(sql,
                str(body, "registryName", "systemSoftRegistryName"),
                str(body, "registryType", "systemSoftRegistryType"),
                str(body, "registryUrl", "systemSoftRegistryUrl"),
                str(body, "registryUsername", "systemSoftRegistryUsername"),
                str(body, "registryPassword", "systemSoftRegistryPassword"),
                str(body, "registryEmail", "systemSoftRegistryEmail"),
                intObj(body, "registryActive", "systemSoftRegistryActive"),
                intObj(body, "registrySslEnabled", "systemSoftRegistrySslEnabled"),
                intObj(body, "registrySupportSync", "systemSoftRegistrySupportSync"),
                intObj(body, "registryTimeout", "systemSoftRegistryTimeout"),
                str(body, "registryDescription", "systemSoftRegistryDescription"),
                str(body, "registryConfig", "systemSoftRegistryConfig"),
                intObj(body, "registrySort", "systemSoftRegistrySort"),
                intObj(body, "enabled", "systemSoftRegistryStatus"),
                id);
        return detail(id);
    }

    @DeleteMapping("/{id}")
    public ReturnResult<Boolean> delete(@PathVariable Integer id) {
        int affected = jdbcTemplate.update("DELETE FROM " + TABLE + " WHERE system_soft_registry_id = ?", id);
        return ReturnResult.ok(affected > 0);
    }

    // ─── helper methods ───────────────────────────────────────────

    private static void putStr(Map<String, Object> m, ResultSet rs, String key, String col) throws SQLException {
        String val = rs.getString(col);
        if (val != null) m.put(key, val);
    }

    private static void putInt(Map<String, Object> m, ResultSet rs, String key, String col) throws SQLException {
        int val = rs.getInt(col);
        if (!rs.wasNull()) m.put(key, val);
    }

    private static String str(Map<String, Object> body, String... keys) {
        for (String k : keys) {
            Object v = body.get(k);
            if (v instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private static Integer intObj(Map<String, Object> body, String... keys) {
        for (String k : keys) {
            Object v = body.get(k);
            if (v instanceof Number n) return n.intValue();
        }
        return null;
    }

    private static Integer intObj(Map<String, Object> body, String key1, String key2, int def) {
        Integer v = intObj(body, key1, key2);
        return v != null ? v : def;
    }
}
