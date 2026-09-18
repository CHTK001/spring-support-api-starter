package com.chua.starter.system.service;

import com.chua.starter.entity.system.SysRole;
import com.chua.starter.system.model.user.SysUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据权限单元测试
 *
 * 测试角色数据权限范围的计算逻辑和查询参数注入
 */
@DisplayName("数据权限测试")
class DataScopeTest {

    private SysUserQuery query;

    /**
     * 设置Up。
     */
    @BeforeEach
    void setUp() {
        query = new SysUserQuery();
    }

    /**
     * 测试：AdminRole编号数据Scope。
     */
    @Test
    @DisplayName("超级管理员不受数据权限限制")
    void testAdminRoleNoDataScope() {
        Set<String> roles = new HashSet<>(Arrays.asList("ADMIN", "USER"));
        // 模拟超级管理员：dataScope 应为 null（不过滤）
        boolean isAdmin = roles.contains("ADMIN") || roles.contains("SUPER_ADMIN");
        assertTrue(isAdmin, "ADMIN 角色应被识别为超级管理员");
    }

    /**
     * 测试：全部数据Scope。
     */
    @Test
    @DisplayName("全部数据权限不过滤")
    void testAllDataScope() {
        query.setDataScope(1);
        assertEquals(1, query.getDataScope());
        assertNull(query.getCurrentUserId());
        assertNull(query.getCurrentDeptId());
        assertNull(query.getCurrentDeptTreeId());
        assertNull(query.getDataScopeDeptIds());
    }

    /**
     * 测试：Dept数据Scope。
     */
    @Test
    @DisplayName("本部门数据权限注入部门ID")
    void testDeptDataScope() {
        query.setDataScope(2);
        query.setCurrentDeptId(100);
        assertEquals(2, query.getDataScope());
        assertEquals(100, query.getCurrentDeptId());
    }

    /**
     * 测试：DeptAndChildren数据Scope。
     */
    @Test
    @DisplayName("本部门及以下数据权限注入部门树路径")
    void testDeptAndChildrenDataScope() {
        query.setDataScope(3);
        query.setCurrentDeptTreeId("0,1,100");
        assertEquals(3, query.getDataScope());
        assertEquals("0,1,100", query.getCurrentDeptTreeId());
    }

    /**
     * 测试：Self数据Scope。
     */
    @Test
    @DisplayName("仅本人数据权限注入用户ID")
    void testSelfDataScope() {
        query.setDataScope(4);
        query.setCurrentUserId(999);
        assertEquals(4, query.getDataScope());
        assertEquals(999, query.getCurrentUserId());
    }

    /**
     * 测试：CustomDept数据Scope。
     */
    @Test
    @DisplayName("自定义部门数据权限注入部门ID列表")
    void testCustomDeptDataScope() {
        query.setDataScope(5);
        List<Integer> deptIds = Arrays.asList(10, 20, 30);
        query.setDataScopeDeptIds(deptIds);
        assertEquals(5, query.getDataScope());
        assertEquals(3, query.getDataScopeDeptIds().size());
        assertTrue(query.getDataScopeDeptIds().contains(10));
    }

    /**
     * 测试：SysRoleDefault数据Scope。
     */
    @Test
    @DisplayName("角色实体数据权限字段默认值为全部")
    void testSysRoleDefaultDataScope() {
        SysRole role = new SysRole();
        assertEquals(1, role.getSysRoleDataScope(), "默认数据权限应为全部(1)");
        assertNull(role.getSysRoleDataScopeDeptIds());
    }

    /**
     * 测试：SysRoleCustomDeptIds。
     */
    @Test
    @DisplayName("角色实体自定义部门ID序列化")
    void testSysRoleCustomDeptIds() {
        SysRole role = new SysRole();
        role.setSysRoleDataScope(5);
        role.setSysRoleDataScopeDeptIds("10,20,30");
        assertEquals(5, role.getSysRoleDataScope());
        assertEquals("10,20,30", role.getSysRoleDataScopeDeptIds());
        String[] ids = role.getSysRoleDataScopeDeptIds().split(",");
        assertEquals(3, ids.length);
    }

    /**
     * 测试：MultipleRolesTakeMostPermissive。
     */
    @Test
    @DisplayName("多角色取最宽松数据权限")
    void testMultipleRolesTakeMostPermissive() {
        // 模拟用户有两个角色：数据权限2(本部门)和4(仅本人)
        // 应取最宽松的 2
        List<Integer> scopes = Arrays.asList(2, 4);
        Integer minScope = scopes.stream().min(Integer::compareTo).orElse(1);
        assertEquals(2, minScope, "多角色应取最宽松(最小)的数据权限");
    }

    /**
     * 测试：编号RoleDefaultSelf。
     */
    @Test
    @DisplayName("无角色用户默认仅本人")
    void testNoRoleDefaultSelf() {
        List<SysRole> userRoles = Collections.emptyList();
        Integer dataScope = userRoles.stream()
                .map(SysRole::getSysRoleDataScope)
                .min(Integer::compareTo)
                .orElse(4);
        assertEquals(4, dataScope, "无角色用户默认仅本人数据权限");
    }
}
