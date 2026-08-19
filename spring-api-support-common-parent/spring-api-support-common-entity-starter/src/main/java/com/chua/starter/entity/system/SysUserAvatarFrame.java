package com.chua.starter.entity.system;

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
 * 用户头像框关联表
 *
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "用户头像框关联表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_avatar_frame")
public class SysUserAvatarFrame extends SysBase {

    @TableId(value = "sys_user_avatar_frame_id", type = IdType.AUTO)
    @Schema(description = "用户头像框ID")
    /** SYS用户avatarframeID */
    private Integer sysUserAvatarFrameId;

    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    /** SYS用户ID */
    private Integer sysUserId;

    @TableField(value = "sys_avatar_frame_id")
    @Schema(description = "头像框ID")
    /** SYSavatarframeID */
    private Integer sysAvatarFrameId;

    @TableField(value = "acquire_time")
    @Schema(description = "获取时间")
    /** Acquire时间 */
    private LocalDateTime acquireTime;

    @TableField(value = "expire_time")
    @Schema(description = "过期时间(NULL=永久)")
    /** Expire时间 */
    private LocalDateTime expireTime;
}