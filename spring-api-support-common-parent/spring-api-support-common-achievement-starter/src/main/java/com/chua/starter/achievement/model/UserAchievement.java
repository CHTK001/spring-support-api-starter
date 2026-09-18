package com.chua.starter.achievement.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户成就记录实体
 *
 * <p>每个用户每个 type 只保留一条记录，记录当前最高解锁等级。</p>
 *
 * @since 2026-08-07
 * @author CH
 */
@Data
@TableName("user_achievement")
public class UserAchievement {

    /**
     * 主键标识
     */
    @TableId(type = IdType.AUTO)
    /** 标识 */
    private Long id;

    /**
    * 用户标识
     */
    private Long userId;

    /**
     * 分组标识，与 achievement_tier.类型 对应
     */
    private String type;

    /**
     * 该组已解锁的最高段位等级
     */
    private Integer maxLevel;

    /**
     * 最近一次解锁时间
     */
    private LocalDateTime unlockedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
