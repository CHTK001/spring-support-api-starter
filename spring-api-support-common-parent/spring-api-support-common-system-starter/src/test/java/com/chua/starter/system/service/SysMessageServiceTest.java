package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * 系统消息服务单元测试
 *
 * <p>覆盖管理员分页查询、发送消息(设置发送时间/状态/默认值)、
 * 广播(receiverId=0)、撤回(状态改为2)、删除消息。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("系统消息服务测试")
class SysMessageServiceTest {

    @Mock
    private SysMessageMapper sysMessageMapper;

    @InjectMocks
    private SysMessageServiceImpl sysMessageService;

    private SysMessage buildMessage() {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle("系统维护通知");
        message.setSysMessageContent("今晚0点维护");
        return message;
    }

    @Test
    @DisplayName("管理员分页查询-委托mapper分页并返回结构")
    void shouldPageMessagesForAdmin() {
        Query<SysMessage> query = new Query<>();
        SysMessage condition = new SysMessage();
        condition.setSysMessageTitle("通知");
        Page<SysMessage> page = new Page<>();
        page.setRecords(Collections.singletonList(buildMessage()));
        page.setTotal(1);
        when(sysMessageMapper.selectPage(any(Page.class), any())).thenReturn(page);

        IPage<SysMessage> result = sysMessageService.pageForAdmin(query, condition);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("系统维护通知", result.getRecords().get(0).getSysMessageTitle());
    }

    @Test
    @DisplayName("发送消息成功-自动设置发送时间、状态为已发送、接收者默认广播")
    void shouldSendMessageSuccessfully() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);

        SysMessage input = buildMessage();
        ReturnResult<SysMessage> result = sysMessageService.sendMessage(input);

        assertTrue(result.isSuccess());
        SysMessage saved = result.getData();
        assertNotNull(saved.getSysMessageSendTime(), "应自动填充发送时间");
        assertEquals(SysMessage.Status.SENT, saved.getSysMessageStatus(), "状态应为已发送(1)");
        assertEquals(0, saved.getSysMessageReceiverId(), "接收者为空时默认广播(0)");
        assertEquals(0, saved.getSysMessageRead(), "已读状态默认未读(0)");
        assertNull(saved.getSysMessageId(), "发送前应清空主键");
    }

    @Test
    @DisplayName("发送消息-标题为空返回非法参数")
    void shouldThrowWhenMessageTitleEmpty() {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle("  ");
        ReturnResult<SysMessage> result = sysMessageService.sendMessage(message);

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("消息标题不能为空"));
        verify(sysMessageMapper, never()).insert(any(SysMessage.class));
    }

    @Test
    @DisplayName("发送广播消息-接收者为空时receiverId置为0并插入一条")
    void shouldBroadcastToAllWhenNoTargets() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);

        ReturnResult<Integer> result = sysMessageService.broadcast(buildMessage(), null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData(), "全局广播只插入一条");
        verify(sysMessageMapper, times(1)).insert(any(SysMessage.class));
    }

    @Test
    @DisplayName("定向广播-为每个目标用户复制并插入")
    void shouldBroadcastToTargetUsers() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);

        ReturnResult<Integer> result = sysMessageService.broadcast(buildMessage(), Arrays.asList(100, 200));

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData(), "定向发送应为每个用户插入一条");
        verify(sysMessageMapper, times(2)).insert(any(SysMessage.class));
    }

    @Test
    @DisplayName("撤回消息成功-状态改为已撤回(2)")
    void shouldRecallMessageSuccessfully() {
        when(sysMessageMapper.updateById(any(SysMessage.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysMessageService.recallMessage(9L);

        assertTrue(result.isSuccess());
        // 捕获更新体, 验证状态为 RECALLED(2)
        org.mockito.ArgumentCaptor<SysMessage> captor = org.mockito.ArgumentCaptor.forClass(SysMessage.class);
        verify(sysMessageMapper).updateById(captor.capture());
        assertEquals(SysMessage.Status.RECALLED, captor.getValue().getSysMessageStatus(), "撤回后状态应为2");
    }

    @Test
    @DisplayName("撤回消息-ID为空返回非法参数")
    void shouldThrowWhenRecallIdNull() {
        ReturnResult<Boolean> result = sysMessageService.recallMessage(null);
        assertFalse(result.isSuccess());
        verify(sysMessageMapper, never()).updateById(any(SysMessage.class));
    }

    @Test
    @DisplayName("删除消息成功-批量删除")
    void shouldDeleteMessagesSuccessfully() {
        when(sysMessageMapper.deleteBatchIds(anyCollection())).thenReturn(3);

        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Arrays.asList(1L, 2L, 3L));

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("删除消息-ID集合为空返回非法参数")
    void shouldThrowWhenDeleteIdsEmpty() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Collections.emptyList());
        assertFalse(result.isSuccess());
        verify(sysMessageMapper, never()).deleteBatchIds(anyCollection());
    }

    @Test
    @DisplayName("模板发送变量替换-copyForReceiver保留发送者并设置指定接收者")
    void shouldCopyTemplateForReceiver() {
        when(sysMessageMapper.insert(any(SysMessage.class))).thenReturn(1);
        SysMessage template = buildMessage();
        template.setSysMessageSenderId(1);
        template.setSysMessageSenderName("管理员");

        sysMessageService.broadcast(template, List.of(88));

        org.mockito.ArgumentCaptor<SysMessage> captor = org.mockito.ArgumentCaptor.forClass(SysMessage.class);
        verify(sysMessageMapper).insert(captor.capture());
        SysMessage row = captor.getValue();
        assertEquals(88, row.getSysMessageReceiverId(), "接收者应为目标用户");
        assertEquals(1, row.getSysMessageSenderId(), "发送者应从模板继承");
        assertEquals("管理员", row.getSysMessageSenderName());
        assertEquals(SysMessage.Status.SENT, row.getSysMessageStatus());
    }
}
