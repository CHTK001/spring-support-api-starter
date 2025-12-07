package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 绯荤粺鍙嶉
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "绯荤粺鍙嶉")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_feedback")
public class SysFeedback extends SysBase {

    @TableId(value = "sys_feedback_id", type = IdType.AUTO)
    @Schema(description = "鍙嶉ID")
    private Integer sysFeedbackId;

    /**
     * 鍙嶉绫诲瀷
     */
    @TableField(value = "sys_feedback_type")
    @Schema(description = "鍙嶉绫诲瀷")
    private String sysFeedbackType;

    /**
     * 鍙嶉鍥剧墖
     */
    @TableField(value = "sys_feedback_images")
    @Schema(description = "鍙嶉鍥剧墖")
    private String sysFeedbackImages;

    /**
     * 鍙嶉鍐呭
     */
    @TableField(value = "sys_feedback_content")
    @Schema(description = "鍙嶉鍐呭")
    private String sysFeedbackContent;

    /**
     * 鍙嶉鐘舵€?0:鏈鐞 1:宸插鐞     */
    @TableField(value = "sys_feedback_status")
    @Schema(description = "鍙嶉鐘舵€?0:鏈鐞 1:宸插鐞")
    private Integer sysFeedbackStatus;

    /**
     * 澶勭悊浜篒D
     */
    @TableField(value = "sys_feedback_deal_by")
    @Schema(description = "澶勭悊浜篒D")
    private Integer sysFeedbackDealBy;

    /**
     * 澶勭悊浜哄悕绉?     */
    @TableField(value = "sys_feedback_deal_name")
    @Schema(description = "澶勭悊浜哄悕绉")
    private String sysFeedbackDealName;

    /**
     * 鍥炲鍐呭
     */
    @TableField(value = "sys_feedback_recover_content")
    @Schema(description = "鍥炲鍐呭")
    private String sysFeedbackRecoverContent;

    /**
     * 鍥炲鏃堕棿
     */
    @TableField(value = "sys_feedback_recover_time")
    @Schema(description = "鍥炲鏃堕棿")
    private LocalDateTime sysFeedbackRecoverTime;

    /**
     * 鍥炲鐨勫浘鐗"     */
    @TableField(value = "sys_feedback_recover_images")
    @Schema(description = "鍥炲鐨勫浘鐗")
    private String sysFeedbackRecoverImages;
}