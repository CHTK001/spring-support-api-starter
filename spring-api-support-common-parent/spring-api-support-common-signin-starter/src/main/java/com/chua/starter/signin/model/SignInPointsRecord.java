package com.chua.starter.signin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到积分流水实体
 *
 * <p>记录每次签到产生的积分明细，每条记录对应一条签到规则。
 * 一次签到可能产生多条积分流水（如基础积分 + 连续奖励）。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@TableName("sign_in_points_record")
public class SignInPointsRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联签到记录ID
     */
    private Long signInRecordId;

    /**
     * 关联积分规则ID
     */
    private Long ruleId;

    /**
     * 本次积分值
     */
    private Integer points;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}