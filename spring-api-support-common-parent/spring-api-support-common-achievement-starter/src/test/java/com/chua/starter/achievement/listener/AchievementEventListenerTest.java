package com.chua.starter.achievement.listener;

import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.event.UserActionEvent;
import com.chua.starter.achievement.service.AchievementService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AchievementEventListener 单元测试
 *
 * <p>覆盖事件监听器的核心逻辑：行为类型映射、服务调用、异常处理。</p>
 *
 * @author CH
 * @since 2026-08-07
 */
@ExtendWith(MockitoExtension.class)
class AchievementEventListenerTest {

    /**
     * 成就业务服务模拟
     */
    @Mock
    private AchievementService achievementService;

    /**
     * 测试目标监听器
     */
    private AchievementEventListener listener;

    /**
     * 测试前初始化：构建监听器及其模拟依赖。
     */
    @BeforeEach
    void setUp() {
        listener = new AchievementEventListener(achievementService);
    }

    /**
     * 测试：sign_in 行为映射到 signInCount 统计字段。
     */
    @Test
    void testOnUserAction_sign_in() {
        UserActionEvent event = new UserActionEvent(this, 1L, "sign_in", 1L);
        when(achievementService.onUserAction(1L, "signInCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "signInCount", 1L);
    }

    /**
     * 测试：login 行为映射到 loginCount 统计字段。
     */
    @Test
    void testOnUserAction_login() {
        UserActionEvent event = new UserActionEvent(this, 1L, "login", 1L);
        when(achievementService.onUserAction(1L, "loginCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "loginCount", 1L);
    }

    /**
     * 测试：menu_visit 行为映射到 menuVisitedCount 统计字段。
     */
    @Test
    void testOnUserAction_menu_visit() {
        UserActionEvent event = new UserActionEvent(this, 1L, "menu_visit", 1L);
        when(achievementService.onUserAction(1L, "menuVisitedCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "menuVisitedCount", 1L);
    }

    /**
     * 测试：message_sent 行为映射到 messageSentCount 统计字段。
     */
    @Test
    void testOnUserAction_message_sent() {
        UserActionEvent event = new UserActionEvent(this, 1L, "message_sent", 1L);
        when(achievementService.onUserAction(1L, "messageSentCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "messageSentCount", 1L);
    }

    /**
     * 测试：frame_owned 行为映射到 frameOwnedCount 统计字段。
     */
    @Test
    void testOnUserAction_frame_owned() {
        UserActionEvent event = new UserActionEvent(this, 1L, "frame_owned", 1L);
        when(achievementService.onUserAction(1L, "frameOwnedCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "frameOwnedCount", 1L);
    }

    /**
     * 测试：rogue_play 行为映射到 rogueCounter 统计字段。
     */
    @Test
    void testOnUserAction_rogue_play() {
        UserActionEvent event = new UserActionEvent(this, 1L, "rogue_play", 1L);
        when(achievementService.onUserAction(1L, "rogueCounter", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "rogueCounter", 1L);
    }

    /**
     * 测试：未知行为类型不调用成就服务。
     */
    @Test
    void testOnUserAction_unknown_action() {
        UserActionEvent event = new UserActionEvent(this, 1L, "unknown_action", 1L);

        listener.onUserAction(event);

        verify(achievementService, never()).onUserAction(anyLong(), anyString(), anyLong());
    }

    /**
     * 测试：用户ID为 null 时不调用成就服务。
     */
    @Test
    void testOnUserAction_null_userId() {
        UserActionEvent event = new UserActionEvent(this, null, "login", 1L);

        listener.onUserAction(event);

        verify(achievementService, never()).onUserAction(anyLong(), anyString(), anyLong());
    }

    /**
     * 测试：行为类型为 null 时不调用成就服务。
     */
    @Test
    void testOnUserAction_null_action() {
        UserActionEvent event = new UserActionEvent(this, 1L, null, 1L);

        listener.onUserAction(event);

        verify(achievementService, never()).onUserAction(anyLong(), anyString(), anyLong());
    }

    /**
     * 测试：处理后有新解锁成就时正常调用服务。
     */
    @Test
    void testOnUserAction_with_unlocked_achievements() {
        UserActionEvent event = new UserActionEvent(this, 1L, "login", 1L);
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("首次登录");
        when(achievementService.onUserAction(1L, "loginCount", 1L))
                .thenReturn(List.of(dto));

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "loginCount", 1L);
    }

    /**
     * 测试：服务抛出异常时监听器不被中断并记录错误日志。
     */
    @Test
    void testOnUserAction_service_exception() {
        UserActionEvent event = new UserActionEvent(this, 1L, "login", 1L);
        when(achievementService.onUserAction(1L, "loginCount", 1L))
                .thenThrow(new RuntimeException("service error"));

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "loginCount", 1L);
    }

    /**
     * 测试：使用默认增量值 1 的事件构造。
     */
    @Test
    void testOnUserAction_default_delta() {
        UserActionEvent event = new UserActionEvent(this, 1L, "login");
        when(achievementService.onUserAction(1L, "loginCount", 1L))
                .thenReturn(List.of());

        listener.onUserAction(event);

        verify(achievementService).onUserAction(1L, "loginCount", 1L);
    }
}
