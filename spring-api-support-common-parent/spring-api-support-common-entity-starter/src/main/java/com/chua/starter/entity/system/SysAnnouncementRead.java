package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读记录表
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "公告已读记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_announcement_read")
public class SysAnnouncementRead extends SysBase implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
    * 主键标识
     */
    @TableId(value = "sys_announcement_read_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    /** sysannouncementreadid */
    private Long sysAnnouncementReadId;

    /**
    * 公告标识
     */
    @TableField(value = "sys_announcement_id")
    @Schema(description = "公告ID")
    /** sysannouncementid */
    private Integer sysAnnouncementId;

    /**
    * 用户标识
     */
    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    /** SYS用户标识 */
    private Integer sysUserId;

    /**
    * 阅读时间
     */
    @TableField(value = "read_time")
    @Schema(description = "阅读时间")
    /** read时间 */
    private LocalDateTime readTime;
}
