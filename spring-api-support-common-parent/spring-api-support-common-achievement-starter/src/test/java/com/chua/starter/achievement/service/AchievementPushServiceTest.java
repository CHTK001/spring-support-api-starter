package com.chua.starter.achievement.service;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * AchievementPushService 单元测试
 *
 * <p>覆盖推送服务的核心逻辑：JSON 序列化、SocketSessionTemplate 调用。</p>
 *
 * @since 2026-08-07
 * @author CH
 */
@ExtendWith(MockitoExtension.class)
class AchievementPushServiceTest {

    /**
     * Socket 会话模板模拟
     */
    @Mock
    /** Socket会话模板 */
    private SocketSessionTemplate socketSessionTemplate;

    /**
     * 测试目标推送服务
     */
    private AchievementPushService pushService;

    /**
     * 测试前初始化：构建推送服务及其模拟依赖。
     */
    @BeforeEach
    void setUp() {
        pushService = new AchievementPushService(socketSessionTemplate);
    }

    /**
     * 测试：推送成就时调用 SocketSessionTemplate 的 sendToUser。
     */
    @Test
    void testPushAchievements_calls_socket_session_template() {
        Long userId = 1L;
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("首次登录");
        dto.setType("login");
        dto.setLevel(1);
        List<UnlockedAchievementDTO> achievements = List.of(dto);

        pushService.pushAchievements(userId, achievements);

        verify(socketSessionTemplate).sendToUser(eq("1"), eq("message"), anyString());
    }

    /**
     * 测试：推送空成就列表时不抛异常。
     */
    @Test
    void testPushAchievements_empty_list() {
        Long userId = 1L;

        pushService.pushAchievements(userId, List.of());

        verify(socketSessionTemplate).sendToUser(eq("1"), eq("message"), anyString());
    }

    /**
     * 测试：推送消息体包含 module、event 及成就名称信息。
     */
    @Test
    void testPushAchievements_json_contains_module_event() {
        Long userId = 1L;
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("登录达人");
        dto.setType("login");
        dto.setLevel(2);
        dto.setDescription("累计登录7天");

        pushService.pushAchievements(userId, List.of(dto));

        verify(socketSessionTemplate).sendToUser(eq("1"), eq("message"),
                argThat(msg -> msg.contains("\"module\":\"achievement\"")
                        && msg.contains("\"event\":\"unlock\"")
                        && msg.contains("登录达人")));
    }

    /**
     * 测试：批量推送多个成就均正常发送。
     */
    @Test
    void testPushAchievements_multiple_achievements() {
        Long userId = 1L;
        UnlockedAchievementDTO dto1 = new UnlockedAchievementDTO();
        dto1.setName("首次登录");
        UnlockedAchievementDTO dto2 = new UnlockedAchievementDTO();
        dto2.setName("登录达人");
        UnlockedAchievementDTO dto3 = new UnlockedAchievementDTO();
        dto3.setName("签到新秀");
        List<UnlockedAchievementDTO> achievements = List.of(dto1, dto2, dto3);

        pushService.pushAchievements(userId, achievements);

        verify(socketSessionTemplate).sendToUser(eq("1"), eq("message"), anyString());
    }

    /**
     * 测试：推送消息体包含事件名 achievement_unlocked。
     */
    @Test
    void testPushAchievements_json_contains_event_name() {
        Long userId = 2L;
        UnlockedAchievementDTO dto = new UnlockedAchievementDTO();
        dto.setName("首次登录");
        dto.setType("login");
        dto.setLevel(1);

        pushService.pushAchievements(userId, List.of(dto));

        verify(socketSessionTemplate).sendToUser(eq("2"), eq("message"),
                argThat(msg -> msg.contains("achievement_unlocked")));
    }
}