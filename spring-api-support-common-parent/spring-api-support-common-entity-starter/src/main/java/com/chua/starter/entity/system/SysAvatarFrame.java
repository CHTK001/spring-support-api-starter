package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* 头像框定义表
*
* @since 2024/8/7
* @author CH
 */
@Schema(description = "头像框定义表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_avatar_frame")
public class SysAvatarFrame extends SysBase {

    @TableId(value = "sys_avatar_frame_id", type = IdType.AUTO)
    @Schema(description = "头像框ID")
    /** sysavatarframeid */
    private Integer sysAvatarFrameId;

    @TableField(value = "sys_avatar_frame_name")
    @Schema(description = "头像框名称")
    /** sysavatarframe名称 */
    private String sysAvatarFrameName;

    @TableField(value = "sys_avatar_frame_type")
    @Schema(description = "类型: base-基础 birthday-生日 event-活动")
    /** sysavatarframe类型 */
    private String sysAvatarFrameType;

    @TableField(value = "sys_avatar_frame_image")
    @Schema(description = "头像框图片URL")
    /** sysavatarframe图片 */
    private String sysAvatarFrameImage;

    @TableField(value = "sys_avatar_frame_color")
    @Schema(description = "渐变背景色(css)")
    /** sysavatarframe颜色 */
    private String sysAvatarFrameColor;

    @TableField(value = "sys_avatar_frame_desc")
    @Schema(description = "描述")
    /** sysavatarframedesc */
    private String sysAvatarFrameDesc;

    @TableField(value = "sys_avatar_frame_acquire")
    @Schema(description = "获取途径: default-默认 birthday-生日 achievement-成就")
    /** sysavatarframeacquire */
    private String sysAvatarFrameAcquire;

    @TableField(value = "sys_avatar_frame_sort")
    @Schema(description = "排序")
    /** sysavatarframesort */
    private Integer sysAvatarFrameSort;

    @TableField(value = "sys_avatar_frame_status")
    @Schema(description = "状态: 0-禁用 1-启用")
    /** sysavatarframe状态 */
    private Integer sysAvatarFrameStatus;
}