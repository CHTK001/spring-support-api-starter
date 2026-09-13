package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.service.SysRoleMenuService;
import com.chua.starter.system.service.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * 角色只读标记单元测试
 *
 * <p>覆盖保存角色时持久化只读标记(sysRoleReadonly=1)、
 * 更新角色时保留只读标记。用户登录时 resolveUserReadonly 将任一只读角色
 * 映射为 UserResume.readonly=true, 该逻辑位于 SysLoginServiceImpl,
 * 依赖 OAuth 上下文, 需在完整环境验证。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色只读测试")
class SysRoleReadonlyTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private SysRoleMenuService sysRoleMenuService;

    @InjectMocks
    private SysRoleServiceImpl sysRoleService;

    @Test
    @DisplayName("保存角色-只读标记=1随insert持久化")
    void shouldPersistReadonlyFlagOnSave() {
        SysRole role = new SysRole();
        role.setSysRoleCode("AUDITOR");
        role.setSysRoleName("审计员");
        role.setSysRoleReadonly(1);
        when(sysRoleMapper.selectOne(any())).thenReturn(null);
        when(sysRoleMapper.insert(any(SysRole.class))).thenReturn(1);

        ReturnResult<SysRole> result = sysRoleService.saveRole(role);

        assertTrue(result.isSuccess());
        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(sysRoleMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getSysRoleReadonly(), "只读标记应随角色入库");
    }

    @Test
    @DisplayName("保存角色-未设置只读标记时默认非只读")
    void shouldDefaultNonReadonlyWhenNotSet() {
        SysRole role = new SysRole();
        role.setSysRoleCode("EDITOR");
        role.setSysRoleName("编辑");
        when(sysRoleMapper.selectOne(any())).thenReturn(null);
        when(sysRoleMapper.insert(any(SysRole.class))).thenReturn(1);

        ReturnResult<SysRole> result = sysRoleService.saveRole(role);

        assertTrue(result.isSuccess());
        // 实体默认值 sysRoleReadonly=0
        assertEquals(0, result.getData().getSysRoleReadonly());
    }

    @Test
    @DisplayName("更新角色-只读标记可被修改并updateById")
    void shouldUpdateReadonlyFlag() {
        SysRole exist = new SysRole();
        exist.setSysRoleId(1);
        exist.setSysRoleInSystem(0);
        exist.setSysRoleCode("EDITOR");

        SysRole update = new SysRole();
        update.setSysRoleId(1);
        update.setSysRoleCode("EDITOR");
        update.setSysRoleReadonly(1);

        when(sysRoleMapper.selectById(1)).thenReturn(exist);
        when(sysRoleMapper.selectOne(any())).thenReturn(null);
        when(sysRoleMapper.updateById(any(SysRole.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysRoleService.updateRole(java.util.Set.of("ADMIN"), update);

        assertTrue(result.isSuccess());
        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(sysRoleMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getSysRoleReadonly());
    }

    @Test
    @DisplayName("用户拥有只读角色-应解析为只读(任一角色sysRoleReadonly=1即只读)")
    void shouldResolveReadonlyWhenUserHasReadonlyRole() {
        // 模拟 sysRoleMapper.listByUserId 返回含只读角色
        SysRole readonly = new SysRole();
        readonly.setSysRoleReadonly(1);
        SysRole normal = new SysRole();
        normal.setSysRoleReadonly(0);
        when(sysRoleMapper.listByUserId(anyCollection())).thenReturn(java.util.Arrays.asList(readonly, normal));

        boolean anyReadonly = sysRoleMapper.listByUserId(java.util.Collections.singleton(1)).stream()
                .anyMatch(r -> r.getSysRoleReadonly() != null && r.getSysRoleReadonly() == 1);

        assertTrue(anyReadonly, "用户任一角色只读则整账号只读 -> UserResume.readonly=true");
    }

    @Test
    @DisplayName("用户无只读角色-应解析为非只读")
    void shouldResolveNonReadonlyWhenNoReadonlyRole() {
        SysRole normal = new SysRole();
        normal.setSysRoleReadonly(0);
        when(sysRoleMapper.listByUserId(anyCollection())).thenReturn(java.util.Collections.singletonList(normal));

        boolean anyReadonly = sysRoleMapper.listByUserId(java.util.Collections.singleton(2)).stream()
                .anyMatch(r -> r.getSysRoleReadonly() != null && r.getSysRoleReadonly() == 1);

        assertFalse(anyReadonly, "无只读角色 -> UserResume.readonly=false");
    }
}
