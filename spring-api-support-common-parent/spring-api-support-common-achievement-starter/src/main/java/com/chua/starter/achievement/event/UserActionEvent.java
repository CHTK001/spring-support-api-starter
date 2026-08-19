package com.chua.starter.achievement.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户行为事件
 *
 * <p>其他模块在用户行为成功后发布此事件，成就模块监听并判定是否满足解锁条件。
 * 事件携带用户ID、行为类型（用于统计字段映射）和增量值。</p>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // 签到模块
 * @Autowired
 * private ApplicationEventPublisher eventPublisher;
 *
 * public void signIn(Long userId) {
 *     signInMapper.insert(...);
 *     eventPublisher.publishEvent(new UserActionEvent(this, userId, "sign_in"));
 * }
 * }</pre>
 *
 * @since 2026-08-07
 * @author CH
 */
@Getter
public class UserActionEvent extends ApplicationEvent {

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 行为类型，如 sign_in / login / menu_visit / message_sent / frame_owned
     */
    private final String action;

    /**
     * 增量值，默认 1
     */
    private final long delta;

    /**
     * 构造用户行为事件，使用默认增量 1。
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param action 行为类型
     */
    public UserActionEvent(Object source, Long userId, String action) {
        this(source, userId, action, 1L);
    }

    /**
     * 构造用户行为事件，带自定义增量。
     *
     * @param source 事件源
     * @param userId 用户ID
     * @param action 行为类型
     * @param delta  增量值
     */
    public UserActionEvent(Object source, Long userId, String action, long delta) {
        super(source);
        this.userId = userId;
        this.action = action;
        this.delta = delta;
    }
}
