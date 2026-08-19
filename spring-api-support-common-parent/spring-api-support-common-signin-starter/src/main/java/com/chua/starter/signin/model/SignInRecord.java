package com.chua.starter.signin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录实体
 *
 * <p>每个用户每天只能签到一次，记录签到日期、获得积分和连续签到天数。
 * 通过唯一索引 {@code (user_id, sign_date)} 防止重复签到。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Data
@TableName("sign_in_record")
public class SignInRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    /** ID */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到日期（不含时间）
     */
    private LocalDate signDate;

    /**
     * 本次签到获得的总积分
     */
    private Integer points;

    /**
     * 连续签到天数（含本次）
     */
    private Integer streak;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}