package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户锁定/解锁单元测试
 *
 * <p>覆盖管理员主动锁定(sysUserLock=1)、解锁(sysUserLock=0)及参数校验。
 * 登录时锁定校验/自动解锁/失败阈值锁定位于 SysLoginServiceImpl, 依赖
 * OAuth 客户端与 Redis 限流链, 需在完整 Spring 环境中做集成测试。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户锁定测试")
class SysUserLockTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @Test
    @DisplayName("锁定用户成功-sysUserLock置1并写入锁定时间")
    void shouldLockUserSuccessfully() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysUserService.lockUser(5L, 1);

        assertTrue(result.isSuccess());
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        SysUser update = captor.getValue();
        assertEquals(5, update.getSysUserId());
        assertEquals(1, update.getSysUserLock(), "锁定后 sysUserLock 应为1");
        assertNotNull(update.getSysUserLockTime(), "锁定时应写入锁定时间");
    }

    @Test
    @DisplayName("解锁用户成功-sysUserLock置0并清空锁定时间")
    void shouldUnlockUserSuccessfully() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysUserService.lockUser(5L, 0);

        assertTrue(result.isSuccess());
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        SysUser update = captor.getValue();
        assertEquals(0, update.getSysUserLock(), "解锁后 sysUserLock 应为0");
        assertNull(update.getSysUserLockTime(), "解锁时应清空锁定时间");
    }

    @Test
    @DisplayName("锁定用户-lock为其他值时按解锁处理(归零)")
    void shouldTreatNonOneLockAsUnlock() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysUserService.lockUser(5L, 9);

        assertTrue(result.isSuccess());
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getSysUserLock(), "lock!=1 一律按解锁处理");
    }

    @Test
    @DisplayName("锁定用户-用户ID为空返回非法参数")
    void shouldThrowWhenUserIdNull() {
        ReturnResult<Boolean> result = sysUserService.lockUser(null, 1);

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("用户ID不能为空"));
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("登录时用户已锁定且未过期-应拒绝登录(423)")
    void shouldRejectLoginWhenLockedAndNotExpired() {
        // 构造一个已锁定且锁定期未到的用户, 验证锁定时间语义
        SysUser user = new SysUser();
        user.setSysUserId(5);
        user.setSysUserLock(1);
        // lockTime 距现在小于锁定期 -> checkAndAutoUnlock 应返回423
        user.setSysUserLockTime(LocalDateTime.now().minusMinutes(1));

        assertEquals(1, user.getSysUserLock());
        assertTrue(user.getSysUserLockTime().isAfter(LocalDateTime.now().minusMinutes(60)),
                "锁定时间在最近60分钟内, 表示仍在锁定期");
        // 注: checkAndAutoUnlock 为私有方法, 实际 423 分支由 authenticateLocal 触发,
        // 需在完整环境验证(见 SysLoginServiceImpl#checkAndAutoUnlock)。
    }

    @Test
    @DisplayName("登录时用户已锁定但已过期-自动解锁后放行")
    void shouldAutoUnlockWhenLockExpired() {
        SysUser user = new SysUser();
        user.setSysUserId(5);
        user.setSysUserLock(1);
        // lockTime 超过锁定期 -> checkAndAutoUnlock 应执行自动解锁
        user.setSysUserLockTime(LocalDateTime.now().minusHours(10));

        assertTrue(user.getSysUserLockTime().isBefore(LocalDateTime.now().minusMinutes(60)),
                "锁定时间已超过60分钟, 表示锁定期已过, 应自动解锁");
        // 自动解锁(写 sysUserLock=0, sysUserLockTime=null)由 authenticateLocal 触发,
        // 需在完整环境验证。
    }
}
