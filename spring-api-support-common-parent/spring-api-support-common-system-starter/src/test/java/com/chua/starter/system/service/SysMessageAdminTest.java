package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysMessageMapper;
import com.chua.starter.system.service.impl.SysMessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 站内消息管理端服务单元测试
 *
 * <p>覆盖全局广播/定向广播、消息统计、管理端分页。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息管理端服务测试")
class SysMessageAdminTest {

    @Mock
    private SysMessageMapper sysMessageMapper;

    @InjectMocks
    private SysMessageServiceImpl sysMessageService;

    private SysMessage message(String title) {
        SysMessage m = new SysMessage();
        m.setSysMessageTitle(title);
        m.setSysMessageContent("内容");
        return m;
    }

    @Test
    @DisplayName("广播-消息为空返回非法参数")
    void testBroadcastNullMessage() {
        ReturnResult<Integer> result = sysMessageService.broadcast(null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("标题"));
    }

    @Test
    @DisplayName("广播-标题为空返回非法参数")
    void testBroadcastBlankTitle() {
        ReturnResult<Integer> result = sysMessageService.broadcast(message(""), Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("广播-目标用户为空时全局广播(receiverId=0)")
    void testBroadcastGlobal() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);

        ReturnResult<Integer> result = sysMessageService.broadcast(message("系统通知"), null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData());
        verify(sysMessageMapper, times(1)).insert(any(SysMessage.class));
    }

    @Test
    @DisplayName("广播-指定用户逐个创建定向消息")
    void testBroadcastTargeted() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);

        ReturnResult<Integer> result = sysMessageService.broadcast(
                message("定向通知"), Arrays.asList(10, 20, 30));

        assertTrue(result.isSuccess());
        assertEquals(3, result.getData());
        verify(sysMessageMapper, times(3)).insert(any(SysMessage.class));
    }

    @Test
    @DisplayName("广播-目标列表全为null时发送失败")
    void testBroadcastAllNullUserIds() {
        ReturnResult<Integer> result = sysMessageService.broadcast(
                message("定向通知"), Arrays.asList(null, null));
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("统计-返回总数/已读/未读")
    void testStatistic() {
        when(sysMessageMapper.selectCount(any())).thenReturn(100L, 40L, 60L);
        Map<String, Object> stat = sysMessageService.statistic();
        assertEquals(100L, stat.get("total"));
        assertEquals(40L, stat.get("read"));
        assertEquals(60L, stat.get("unread"));
    }

    @Test
    @DisplayName("管理端分页-调用分页查询")
    void testPageForAdmin() {
        Query<SysMessage> query = new Query<>();
        SysMessage condition = new SysMessage();
        condition.setSysMessageTitle("通知");
        when(sysMessageMapper.selectPage(any(), any()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        IPage<SysMessage> page = sysMessageService.pageForAdmin(query, condition);
        assertNotNull(page);
    }

    @Test
    @DisplayName("删除消息-空ID集合返回非法参数")
    void testDeleteEmpty() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("删除消息-合法ID集合调用批量删除")
    void testDeleteValid() {
        when(sysMessageMapper.deleteBatchIds(anyCollection())).thenReturn(2);
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Arrays.asList(1L, 2L));
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("发送消息-标题为空返回非法参数")
    void testSendBlankTitle() {
        ReturnResult<SysMessage> result = sysMessageService.sendMessage(message(" "));
        assertFalse(result.isSuccess());
    }
}
