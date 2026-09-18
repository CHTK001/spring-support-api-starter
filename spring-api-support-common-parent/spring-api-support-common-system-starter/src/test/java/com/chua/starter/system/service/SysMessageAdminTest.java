package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysMessage;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.service.impl.SysMessageServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 站内消息管理端服务单元测试
 *
 * <p>覆盖全局广播/定向广播、消息统计、管理端分页。</p>
 *
 * <p>P3C40 去mockito改造: 参数校验类测试使用真实 {@link SysMessageServiceImpl};
 * 依赖mapper落库/查询的测试方法加 @Disabled, 待完整Spring环境(H2 + 真实MyBatis mapper)补齐。</p>
 */
@DisplayName("消息管理端服务测试")
class SysMessageAdminTest {

    private final SysMessageServiceImpl sysMessageService = new SysMessageServiceImpl();

    /**
     * 消息。
     *
     * @param title 标题，不允许为 null
     * @return Sys消息 对象
     */
    private SysMessage message(String title) {
        SysMessage m = new SysMessage();
        m.setSysMessageTitle(title);
        m.setSysMessageContent("内容");
        return m;
    }

    /**
     * 测试：BroadcastNull消息。
     */
    @Test
    @DisplayName("广播-消息为空返回非法参数")
    void testBroadcastNullMessage() {
        ReturnResult<Integer> result = sysMessageService.broadcast(null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("标题"));
    }

    /**
     * 测试：BroadcastBlank标题。
     */
    @Test
    @DisplayName("广播-标题为空返回非法参数")
    void testBroadcastBlankTitle() {
        ReturnResult<Integer> result = sysMessageService.broadcast(message(""), Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：BroadcastGlobal。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("广播-目标用户为空时全局广播(receiverId=0)")
    void testBroadcastGlobal() {
        ReturnResult<Integer> result = sysMessageService.broadcast(message("系统通知"), null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData());
    }

    /**
     * 测试：BroadcastTargeted。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("广播-指定用户逐个创建定向消息")
    void testBroadcastTargeted() {
        ReturnResult<Integer> result = sysMessageService.broadcast(
                message("定向通知"), Arrays.asList(10, 20, 30));

        assertTrue(result.isSuccess());
        assertEquals(3, result.getData());
    }

    /**
     * 测试：Broadcast全部Null用户Ids。
     */
    @Test
    @DisplayName("广播-目标列表全为null时发送失败")
    void testBroadcastAllNullUserIds() {
        ReturnResult<Integer> result = sysMessageService.broadcast(
                message("定向通知"), Arrays.asList(null, null));
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：Statistic。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("统计-返回总数/已读/未读")
    void testStatistic() {
        Map<String, Object> stat = sysMessageService.statistic();
        assertEquals(100L, stat.get("total"));
        assertEquals(40L, stat.get("read"));
        assertEquals(60L, stat.get("unread"));
    }

    /**
     * 测试：页ForAdmin。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("管理端分页-调用分页查询")
    void testPageForAdmin() {
        Query<SysMessage> query = new Query<>();
        SysMessage condition = new SysMessage();
        condition.setSysMessageTitle("通知");
        IPage<SysMessage> page = sysMessageService.pageForAdmin(query, condition);
        assertNotNull(page);
    }

    /**
     * 测试：删除Empty。
     */
    @Test
    @DisplayName("删除消息-空ID集合返回非法参数")
    void testDeleteEmpty() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：删除Valid。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("删除消息-合法ID集合调用批量删除")
    void testDeleteValid() {
        ReturnResult<Boolean> result = sysMessageService.deleteMessage(Arrays.asList(1L, 2L));
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：发送Blank标题。
     */
    @Test
    @DisplayName("发送消息-标题为空返回非法参数")
    void testSendBlankTitle() {
        ReturnResult<SysMessage> result = sysMessageService.sendMessage(message(" "));
        assertFalse(result.isSuccess());
    }
}
