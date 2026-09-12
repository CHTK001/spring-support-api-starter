package com.chua.starter.achievement.constant;

/**
* 成就上下文常量，统一约定 kvengine 中的键前缀与统计字段名，
* 供成就模块与签到模块共享，避免各模块重复定义键名导致约定漂移。
*
* @since 4.0.0.42
* @author CH
 */
public final class AchievementContextConstants {

    /**
    * kvengine 中成就数据的键前缀，完整键格式：achievement:{用户id}:{stat键}
     */
    public static final String KEY_PREFIX = "achievement:";

    /**
    * 统计字段：累计签到次数
     */
    public static final String STAT_SIGN_IN_COUNT = "signInCount";

    /**
    * 统计字段：当前连续签到天数
     */
    public static final String STAT_SIGN_IN_STREAK = "signInStreak";

    /**
    * 统计字段：累计签到积分
     */
    public static final String STAT_SIGN_IN_TOTAL_POINTS = "signInTotalPoints";

    /**
    * 统计字段：历史最高连续签到天数
     */
    public static final String STAT_SIGN_IN_MAX_STREAK = "signInMaxStreak";

    /**
    * 私有构造方法，禁止实例化
     */
    private AchievementContextConstants() {
    }
}