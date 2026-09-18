package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户-岗位关系表
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "用户-岗位关系表")
@Data
@TableName(value = "sys_user_post")
public class SysUserPost implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
    * 主键标识
     */
    @TableId(value = "sys_user_post_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    /** SYS用户岗位id */
    private Long sysUserPostId;

    /**
    * 用户标识
     */
    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    /** SYS用户标识 */
    private Integer sysUserId;

    /**
    * 岗位标识
     */
    @TableField(value = "sys_post_id")
    @Schema(description = "岗位ID")
    /** syspostid */
    private Long sysPostId;
}
