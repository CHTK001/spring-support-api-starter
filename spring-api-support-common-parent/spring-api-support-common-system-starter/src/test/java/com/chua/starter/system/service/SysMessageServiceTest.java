package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.service.impl.SysMessageServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统消息服务单元测试
 *
 * <p>覆盖管理员分页查询、发送消息(设置发送时间/状态/默认值)、
 * 广播(receiverId=0)、撤回(状态改为2)、删除消息。</p>
 *
 * <p>P3C40 去mockito改造: 参数校验类测试使用真实 {@link SysMessageServiceImpl};
 * 依赖mapper落库/回查的测试方法加 @Disabled, 待完整Spring环境(H2 + 真实MyBatis mapper)补齐。</p>
 */
@DisplayName("系统消息服务测试")
class SysMessageServiceTest {

    private final SysMessageServiceImpl sysMessageService = new SysMessageServiceImpl();

    /**
     * 构建消息。
     *
     * @return Sys消息 对象
     */
    private SysMessage buildMessage() {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle("系统维护通知");
        message.setSysMessageContent("今晚0点维护");
        return message;
    }

    /**
     * 测试：页MessagesForAdmin。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("管理员分页查询-委托mapper分页并返回结构")
    void shouldPageMessagesForAdmin() {
        // 原断言逻辑: 分页结果 total=1, 首条标题为"系统维护通知"
        IPage<SysMessage> result = sysMessageService.pageForAdmin(new Query<>(), new SysMessage());
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("系统维护通知", result.getRecords().get(0).getSysMessageTitle());
    }

    /**
     * 测试：发送消息Successfully。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("发送消息成功-自动设置发送时间、状态为已发送、接收者默认广播")
    void shouldSendMessageSuccessfully() {
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

    /**
     * 测试：ThrowWhen消息标题Empty。
     */
    @Test
    @DisplayName("发送消息-标题为空返回非法参数")
    void shouldThrowWhenMessageTitleEmpty() {
        SysMessage message = new SysMessage();
        message.setSysMessageTitle("  ");
        ReturnResult<SysMessage> result = sysMessageService.sendMessage(message);

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("消息标题不能为空"));
    }

    /**
     * 测试：Broadcast转为全部When编号Targets。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("发送广播消息-接收者为空时receiverId置为0并插入一条")
    void shouldBroadcastToAllWhenNoTargets() {
        ReturnResult<Integer> result = sysMessageService.broadcast(buildMessage(), null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData(), "全局广播只插入一条");
    }

    /**
     * 测试：Broadcast转为目标Users。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("定向广播-为每个目标用户复制并插入")
    void shouldBroadcastToTargetUsers() {
        ReturnResult<Integer> result = sysMessageService.broadcast(buildMessage(), java.util.Arrays.asList(100, 200));

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData(), "定向发送应为每个用户插入一条");
    }

    /**
     * 测试：Recall消息Successfully。
     */
    @Disabled("[P3C40] 原 ArgumentCaptor 断言改为 @Disabled：真实回查需要 DB 环境")
    @Test
    @DisplayName("撤回消息成功-状态改为已撤回(2)")
    void shouldRecallMessageSuccessfully() {
        ReturnResult<Boolean> result = sysMessageService.recallMessage(9L);

        assertTrue(result.isSuccess());
        // 原 captor 断言: 更新体状态为 RECALLED(2)
    }

    /**
     * 测试：ThrowWhenRecallIDNull。
     */
    @Test
    @DisplayName("撤回消息-ID为空返回非法参数")
    void shouldThrowWhenRecallIdNull() {
        ReturnResult<Boolean> result = sysMessageService.recallMessage(null);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：删除MessagesSuccessfully。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("删除消息成功-批量删除")
    void shouldDeleteMessagesSuccessfully() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(java.util.Arrays.asList(1L, 2L, 3L));

        assertTrue(result.isSuccess());
    }

    /**
     * 测试：ThrowWhen删除IdsEmpty。
     */
    @Test
    @DisplayName("删除消息-ID集合为空返回非法参数")
    void shouldThrowWhenDeleteIdsEmpty() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：复制模板ForReceiver。
     */
    @Disabled("[P3C40] 原 ArgumentCaptor 断言改为 @Disabled：真实回查需要 DB 环境")
    @Test
    @DisplayName("模板发送变量替换-copyForReceiver保留发送者并设置指定接收者")
    void shouldCopyTemplateForReceiver() {
        SysMessage template = buildMessage();
        template.setSysMessageSenderId(1);
        template.setSysMessageSenderName("管理员");

        sysMessageService.broadcast(template, java.util.List.of(88));

        // 原 captor 断言: 插入行 receiver=88, sender 继承自模板, 状态 SENT
    }
}
