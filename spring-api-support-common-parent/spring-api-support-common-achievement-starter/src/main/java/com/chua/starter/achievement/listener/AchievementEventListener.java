package com.chua.starter.achievement.listener;

import com.chua.starter.achievement.constant.AchievementContextConstants;
import com.chua.starter.achievement.dto.UnlockedAchievementDTO;
import com.chua.starter.achievement.event.UserActionEvent;
import com.chua.starter.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 成就事件监听器
 *
 * <p>监听 {@link UserActionEvent} 事件，异步更新用户统计数据并触发成就判定。
 * 将业务行为（登录、签到、消息等）映射到统计字段名，交由 {@link AchievementService} 处理。</p>
 *
 * <h3>行为类型到统计字段映射</h3>
 * <table>
 * <tr><th>行为类型</th><th>统计字段</th></tr>
 * <tr><td>sign_in</td><td>signInCount</td></tr>
 * <tr><td>login</td><td>loginCount</td></tr>
 * <tr><td>menu_visit</td><td>menuVisitedCount</td></tr>
 * <tr><td>message_sent</td><td>messageSentCount</td></tr>
 * <tr><td>frame_owned</td><td>frameOwnedCount</td></tr>
 * <tr><td>rogue_play</td><td>rogueCounter</td></tr>
 * </table>
 *
 * @author CH
 * @since 2026-08-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventListener {

    /**
     * 成就业务服务
     */
    private final AchievementService achievementService;

    /**
     * 行为类型：签到
     */
    private static final String ACTION_SIGN_IN = "sign_in";

    /**
     * 行为类型：登录
     */
    private static final String ACTION_LOGIN = "login";

    /**
     * 行为类型：菜单访问
     */
    private static final String ACTION_MENU_VISIT = "menu_visit";

    /**
     * 行为类型：消息发送
     */
    private static final String ACTION_MESSAGE_SENT = "message_sent";

    /**
     * 行为类型：获得相框
     */
    private static final String ACTION_FRAME_OWNED = "frame_owned";

    /**
     * 行为类型：rogue 玩法
     */
    private static final String ACTION_ROGUE_PLAY = "rogue_play";

    /**
     * 统计字段：累计登录次数
     */
    private static final String STAT_LOGIN_COUNT = "loginCount";

    /**
     * 统计字段：菜单访问次数
     */
    private static final String STAT_MENU_VISITED_COUNT = "menuVisitedCount";

    /**
     * 统计字段：消息发送次数
     */
    private static final String STAT_MESSAGE_SENT_COUNT = "messageSentCount";

    /**
     * 统计字段：获得相框数量
     */
    private static final String STAT_FRAME_OWNED_COUNT = "frameOwnedCount";

    /**
     * 统计字段：rogue 玩法计数
     */
    private static final String STAT_ROGUE_COUNTER = "rogueCounter";

    /**
     * 监听用户行为事件，异步更新统计并判定成就。
     *
     * @param event 用户行为事件
     */
    @Async
    @EventListener
    public void onUserAction(UserActionEvent event) {
        Long userId = event.getUserId();
        String action = event.getAction();
        long delta = event.getDelta();

        if (userId == null || action == null) {
            return;
        }
        String statKey = mapActionToStatKey(action);
        if (statKey == null) {
            log.warn("未知的行为类型: {}", action);
            return;
        }
        try {
            List<UnlockedAchievementDTO> unlocked = achievementService.onUserAction(userId, statKey, delta);
            if (!unlocked.isEmpty()) {
                log.info("用户 {} 解锁 {} 个成就: {}", userId, unlocked.size(),
                        unlocked.stream().map(UnlockedAchievementDTO::getName).toList());
            }
        } catch (Exception e) {
            log.error("处理用户行为事件失败: userId={}, action={}, error={}",
                    userId, action, e.getMessage(), e);
        }
    }

    /**
     * 将业务行为类型映射为统计字段名。
     *
     * @param action 行为类型标识
     * @return 统计字段名，未匹配时返回 null
     */
    private String mapActionToStatKey(String action) {
        switch (action) {
            case ACTION_SIGN_IN:
                return AchievementContextConstants.STAT_SIGN_IN_COUNT;
            case ACTION_LOGIN:
                return STAT_LOGIN_COUNT;
            case ACTION_MENU_VISIT:
                return STAT_MENU_VISITED_COUNT;
            case ACTION_MESSAGE_SENT:
                return STAT_MESSAGE_SENT_COUNT;
            case ACTION_FRAME_OWNED:
                return STAT_FRAME_OWNED_COUNT;
            case ACTION_ROGUE_PLAY:
                return STAT_ROGUE_COUNTER;
            default:
                return null;
        }
    }
}
