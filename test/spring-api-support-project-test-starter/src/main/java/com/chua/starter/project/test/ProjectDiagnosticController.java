package com.chua.starter.project.test;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.mapper.SysProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理诊断 Controller
 */
@RestController
@RequestMapping("/v2/dbg")
public class ProjectDiagnosticController {

    @Autowired
    private SysProjectMapper sysProjectMapper;

    @Autowired(required = false)
    private AuthService authService;

    @Autowired
    private DataSource dataSource;

    @GetMapping("/project-raw")
    public ReturnResult<Object> directQuery() throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        long count = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Test count
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_project")) {
                if (rs.next()) count = rs.getLong(1);
            }
            // Test data query
            try (ResultSet rs = stmt.executeQuery(
                "SELECT sys_project_id, sys_project_name, sys_project_delete, create_by FROM sys_project LIMIT 10")) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sys_project_id", rs.getLong("sys_project_id"));
                    row.put("sys_project_name", rs.getString("sys_project_name"));
                    row.put("sys_project_delete", rs.getInt("sys_project_delete"));
                    row.put("create_by", rs.getString("create_by"));
                    rows.add(row);
                }
            }
        }
        return ReturnResult.ok(Map.of("count", count, "rows", rows));
    }

    @GetMapping("/project-page-direct")
    public ReturnResult<Object> pageDirect() throws Exception {
        // 1. Raw JDBC - check tables
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            while (rs.next()) tables.add(rs.getString(1));
        }
        // 2. Raw JDBC count
        long rawCount = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_project")) {
            if (rs.next()) rawCount = rs.getLong(1);
        }
        // 3. MyBatis selectList
        List<SysProject> mybatisData = sysProjectMapper.selectList(null);
        // 4. MyBatis selectCount
        long mybatisCount = sysProjectMapper.selectCount(null);
        // 5. MyBatis selectPageFor with searchCount disabled
        Page<SysProject> page = new Page<>(1, 10);
        page.setSearchCount(false);
        IPage<SysProject> pageResult = sysProjectMapper.selectPageFor(page, new SysProject());

        return ReturnResult.ok(Map.of(
            "tables", tables,
            "raw_jdbc_count", rawCount,
            "mybatis_selectList_count", mybatisData.size(),
            "mybatis_selectCount", mybatisCount,
            "mybatis_selectPageFor_records",
                pageResult.getRecords() == null ? "null" : pageResult.getRecords().size(),
            "mybatis_selectPageFor_total", pageResult.getTotal()
        ));
    }

    @GetMapping("/project-page-mapper")
    public ReturnPageResult<SysProject> pageMapper() {
        Page<SysProject> page = new Page<>(1, 10);
        SysProject param = new SysProject();
        return PageResultUtils.ok(sysProjectMapper.selectPageFor(page, param));
    }

    @GetMapping("/project-page-mapper-raw")
    public ReturnResult<Object> pageMapperRaw() {
        Page<SysProject> page = new Page<>(1, 10);
        SysProject param = new SysProject();
        IPage<SysProject> result = sysProjectMapper.selectPageFor(page, param);
        return ReturnResult.ok(Map.of(
            "total", result.getTotal(),
            "current", result.getCurrent(),
            "size", result.getSize(),
            "records", result.getRecords() == null ? "null" : result.getRecords().size()
        ));
    }

    @GetMapping("/project-page-no-count")
    public ReturnResult<Object> pageNoCount() {
        Page<SysProject> page = new Page<>(1, 10);
        page.setSearchCount(false);
        SysProject param = new SysProject();
        IPage<SysProject> result = sysProjectMapper.selectPageFor(page, param);
        return ReturnResult.ok(Map.of(
            "total", result.getTotal(),
            "current", result.getCurrent(),
            "size", result.getSize(),
            "records", result.getRecords() == null ? "null" : result.getRecords().size()
        ));
    }

    @GetMapping("/auth-user")
    public ReturnResult<Object> currentAuthUser() {
        if (authService == null) {
            return ReturnResult.ok("authService is null");
        }
        CurrentUser user = authService.getCurrentUser();
        if (user == null) {
            return ReturnResult.ok("currentUser is null");
        }
        return ReturnResult.ok(Map.of(
            "userId", user.getUserId(),
            "username", user.getUsername(),
            "roles", user.getRoles(),
            "dataPermission", user.getDataPermission()
        ));
    }

    @GetMapping("/seed")
    public ReturnResult<Object> seedData() {
        List<SysProject> projects = new ArrayList<>();

        String[][] seedData = {
            {"AI 智能客服平台", "AI", "ai-cs", "APP001", "AI客服系统", "1", "0"},
            {"电商数据分析系统", "BI", "ecommerce-bi", "APP002", "电商BI", "1", "0"},
            {"物联网设备管理中心", "IOT", "iot-mgmt", "APP003", "IoT管理", "1", "0"},
            {"企业资源计划系统", "ERP", "erp-system", "APP004", "ERP系统", "1", "0"},
            {"客户关系管理系统", "CRM", "crm-system", "APP005", "CRM系统", "1", "0"},
            {"DevOps 持续集成平台", "DEVOPS", "devops-ci", "APP006", "DevOps", "1", "1"},
            {"在线教育直播平台", "EDU", "edu-live", "APP007", "在线教育", "1", "1"},
            {"智慧医疗数据中心", "MED", "med-data", "APP008", "智慧医疗", "1", "1"},
            {"金融风控分析平台", "FIN", "fin-risks", "APP009", "金融风控", "1", "1"},
            {"供应链协同管理系统", "SCM", "scm-system", "APP010", "供应链管理", "1", "1"},
        };

        for (String[] row : seedData) {
            SysProject p = new SysProject();
            p.setSysProjectName(row[0]);
            p.setSysProjectGroup(row[1]);
            p.setSysProjectCode(row[2]);
            p.setSysProjectAppId(row[3]);
            p.setSysProjectRemark(row[4]);
            p.setSysProjectStatus(Integer.parseInt(row[5]));
            p.setSysProjectSort(Integer.parseInt(row[6]));
            p.setSysProjectDelete(0);
            projects.add(p);
        }

        int inserted = 0;
        for (SysProject p : projects) {
            try {
                sysProjectMapper.insert(p);
                inserted++;
            } catch (Exception ignored) {
            }
        }

        long total = sysProjectMapper.selectCount(null);
        return ReturnResult.ok(Map.of("inserted", inserted, "total", total));
    }

    @GetMapping("/seed-clean")
    public ReturnResult<Object> cleanSeedData() {
        int deleted = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            deleted = stmt.executeUpdate("DELETE FROM sys_project WHERE sys_project_app_id LIKE 'APP%'");
        } catch (Exception ignored) {
        }
        long total = sysProjectMapper.selectCount(null);
        return ReturnResult.ok(Map.of("deleted", deleted, "total", total));
    }
}
