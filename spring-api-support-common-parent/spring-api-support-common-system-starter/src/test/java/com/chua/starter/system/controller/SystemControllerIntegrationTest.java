package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.model.cache.SysCacheInfo;
import com.chua.starter.system.pojo.message.MessageBroadcastRequest;
import com.chua.starter.system.service.SysAnnouncementService;
import com.chua.starter.system.service.SysCacheService;
import com.chua.starter.system.service.SysErrorLogService;
import com.chua.starter.system.service.SysMessageService;
import com.chua.starter.system.service.SysPostService;
import com.chua.starter.system.service.SysUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 系统模块接口集成测试(纯 Mockito 风格)
 *
 * <p>项目现有 Controller 测试均不启动 Web 上下文, 此处沿用同样的方式:
 * 直接实例化 Controller 并 mock Service, 验证接口对 Service 的委托、
 * 参数缺失兜底与返回结构。对应接口:
 * POST /v2/post/save、GET /v2/post/page、GET /v2/cache/list、
 * DELETE /v2/cache/refresh、POST /v2/message/admin/save、
 * GET /v2/log/error-statistic、PUT /v2/user/lock、
 * GET /v2/announcement/read-stat。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("系统接口集成测试")
class SystemControllerIntegrationTest {

    @Mock
    private SysPostService sysPostService;
    @Mock
    private SysCacheService sysCacheService;
    @Mock
    private SysMessageService sysMessageService;
    @Mock
    private SysErrorLogService sysErrorLogService;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysAnnouncementService sysAnnouncementService;

    @InjectMocks
    private SysPostController sysPostController;
    @InjectMocks
    private SysCacheController sysCacheController;
    @InjectMocks
    private SysMessageAdminController sysMessageAdminController;
    @InjectMocks
    private SysErrorLogController sysErrorLogController;
    @InjectMocks
    private SysUserController sysUserController;
    @InjectMocks
    private SysAnnouncementController sysAnnouncementController;

    // ===== POST /v2/post/save =====
    @Test
    @DisplayName("[岗位] 新增岗位-校验通过委托service")
    void shouldSavePostThroughController() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        SysPost saved = new SysPost();
        when(sysPostService.savePost(any(SysPost.class))).thenReturn(ReturnResult.ok(saved));

        ReturnResult<SysPost> result = sysPostController.save(new SysPost(), br);

        assertTrue(result.isSuccess());
        verify(sysPostService).savePost(any(SysPost.class));
    }

    @Test
    @DisplayName("[岗位] 新增岗位-参数校验失败不调用service")
    void shouldRejectPostWhenValidationFails() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);
        when(br.getAllErrors()).thenReturn(
                Collections.singletonList(new org.springframework.validation.ObjectError("post", "岗位名称不能为空")));

        ReturnResult<SysPost> result = sysPostController.save(new SysPost(), br);

        assertFalse(result.isSuccess());
        verify(sysPostService, never()).savePost(any());
    }

    // ===== GET /v2/post/page =====
    @Test
    @DisplayName("[岗位] 分页查询-委托service并返回分页结构")
    void shouldPagePostsThroughController() {
        when(sysPostService.page(any(Query.class), any(SysPost.class)))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());

        ReturnPageResult<SysPost> result = sysPostController.page(new Query<>(), new SysPost());

        assertNotNull(result);
        verify(sysPostService).page(any(Query.class), any(SysPost.class));
    }

    // ===== GET /v2/cache/list =====
    @Test
    @DisplayName("[缓存] 列表-委托service返回6项")
    void shouldListCachesThroughController() {
        when(sysCacheService.list()).thenReturn(
                Collections.nCopies(6, new SysCacheInfo("role", "cache:role", "角色缓存")));

        ReturnResult<List<SysCacheInfo>> result = sysCacheController.list();

        assertTrue(result.isSuccess());
        assertEquals(6, result.getData().size());
    }

    // ===== DELETE /v2/cache/refresh =====
    @Test
    @DisplayName("[缓存] 刷新-委托service")
    void shouldRefreshCacheThroughController() {
        when(sysCacheService.refresh("dict")).thenReturn(ReturnResult.ok(true));

        ReturnResult<Boolean> result = sysCacheController.refresh("dict");

        assertTrue(result.isSuccess());
        verify(sysCacheService).refresh("dict");
    }

    // ===== POST /v2/message/admin/save =====
    @Test
    @DisplayName("[消息] 发送消息-组装消息体并委托broadcast")
    void shouldSendMessageThroughController() {
        MessageBroadcastRequest req = new MessageBroadcastRequest();
        req.setTitle("通知");
        req.setContent("内容");
        when(sysMessageService.broadcast(any(SysMessage.class), any())).thenReturn(ReturnResult.ok(1));

        ReturnResult<Integer> result = sysMessageAdminController.save(req);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData());
        verify(sysMessageService).broadcast(any(SysMessage.class), eq(req.getTargetUserIds()));
    }

    // ===== GET /v2/log/error-statistic =====
    @Test
    @DisplayName("[异常日志] 统计-委托service并返回统计map")
    void shouldStatisticErrorLogThroughController() {
        when(sysErrorLogService.statistic()).thenReturn(Map.of("total", 100, "handleRate", 50.0));

        ReturnResult<Map<String, Object>> result = sysErrorLogController.statistic();

        assertTrue(result.isSuccess());
        assertEquals(100, result.getData().get("total"));
    }

    @Test
    @DisplayName("[异常日志] 更新状态-委托service")
    void shouldUpdateErrorStatusThroughController() {
        when(sysErrorLogService.updateStatus(1L, 1)).thenReturn(ReturnResult.ok(true));

        ReturnResult<Boolean> result = sysErrorLogController.updateStatus(1L, 1);

        assertTrue(result.isSuccess());
    }

    // ===== PUT /v2/user/lock =====
    @Test
    @DisplayName("[用户] 锁定-委托service")
    void shouldLockUserThroughController() {
        when(sysUserService.lockUser(5L, 1)).thenReturn(ReturnResult.ok(true));

        ReturnResult<Boolean> result = sysUserController.lock(5L, 1);

        assertTrue(result.isSuccess());
        verify(sysUserService).lockUser(5L, 1);
    }

    @Test
    @DisplayName("[用户] 锁定-用户ID为空返回非法参数")
    void shouldRejectLockWhenUserIdNull() {
        ReturnResult<Boolean> result = sysUserController.lock(null, 1);

        assertFalse(result.isSuccess());
        verify(sysUserService, never()).lockUser(any(), any());
    }

    // ===== GET /v2/announcement/read-stat =====
    @Test
    @DisplayName("[公告] 已读统计-公告ID为空返回非法参数")
    void shouldRejectReadStatWhenIdNull() {
        // readStat 内部直接读 mapper, ID 为空时应在入口提前返回, 不触碰任何依赖
        ReturnResult<Map<String, Object>> result = sysAnnouncementController.readStat(null);

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("公告ID不能为空"));
    }
}
