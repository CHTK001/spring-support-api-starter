package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.message.MessageBroadcastRequest;
import com.chua.starter.system.service.SysMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 站内消息管理端接口单元测试
 *
 * <p>纯 Mockito 验证分页、发送消息(构造消息体并指定发送方)、删除与统计委托。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息管理端接口测试")
class SysMessageAdminControllerTest {

    @Mock
    private SysMessageService sysMessageService;

    @InjectMocks
    private SysMessageAdminController sysMessageAdminController;

    @Test
    @DisplayName("分页-委托service")
    void testPage() {
        when(sysMessageService.pageForAdmin(any(Query.class), any(SysMessage.class)))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        ReturnPageResult<SysMessage> result =
                sysMessageAdminController.page(new Query<>(), new SysMessage());
        assertNotNull(result);
    }

    @Test
    @DisplayName("发送-构造消息体并默认系统发送方, 委托广播")
    void testSave() {
        MessageBroadcastRequest request = new MessageBroadcastRequest();
        request.setTitle("标题");
        request.setContent("内容");
        request.setType("notice");
        request.setTargetUserIds(Collections.singletonList(10));
        when(sysMessageService.broadcast(any(SysMessage.class), anyList()))
                .thenReturn(ReturnResult.ok(1));

        ReturnResult<Integer> result = sysMessageAdminController.save(request);
        assertTrue(result.isSuccess());

        ArgumentCaptor<SysMessage> captor = ArgumentCaptor.forClass(SysMessage.class);
        verify(sysMessageService).broadcast(captor.capture(), eq(request.getTargetUserIds()));
        SysMessage sent = captor.getValue();
        assertEquals("标题", sent.getSysMessageTitle());
        assertEquals(0, sent.getSysMessageSenderId());
        assertEquals("系统", sent.getSysMessageSenderName());
    }

    @Test
    @DisplayName("删除-空id返回非法参数")
    void testDeleteNull() {
        ReturnResult<Boolean> result = sysMessageAdminController.delete(null);
        assertFalse(result.isSuccess());
        verify(sysMessageService, never()).deleteMessage(anyList());
    }

    @Test
    @DisplayName("删除-合法id包装为单元素列表委托")
    void testDeleteOk() {
        when(sysMessageService.deleteMessage(anyList())).thenReturn(ReturnResult.ok(true));
        ReturnResult<Boolean> result = sysMessageAdminController.delete(8);
        assertTrue(result.isSuccess());
        verify(sysMessageService).deleteMessage(Collections.singletonList(8L));
    }

    @Test
    @DisplayName("统计-委托service并返回map")
    void testStatistic() {
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", 10L);
        when(sysMessageService.statistic()).thenReturn(stat);
        ReturnResult<Map<String, Object>> result = sysMessageAdminController.statistic();
        assertTrue(result.isSuccess());
        assertEquals(10L, result.getData().get("total"));
    }
}
