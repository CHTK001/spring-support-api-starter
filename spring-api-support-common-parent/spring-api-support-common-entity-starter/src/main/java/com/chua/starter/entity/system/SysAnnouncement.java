package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告表
 *
 * @since 4.0.0.42
 * @author CH
 */
@Schema(description = "系统公告表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_announcement")
public class SysAnnouncement extends SysBase implements Serializable {

    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
      * 公告标识
     */
    @TableId(value = "sys_announcement_id", type = IdType.AUTO)
    @Schema(description = "公告ID")
    @NotNull(message = "公告ID不能为空", groups = {UpdateGroup.class})
    /** sysannouncementid */
    private Integer sysAnnouncementId;

    /**
     * 公告标题
     */
    @TableField(value = "sys_announcement_title")
    @Schema(description = "公告标题")
    @NotBlank(message = "公告标题不能为空", groups = {AddGroup.class})
    @Size(max = 200, message = "公告标题最大长度需小于200")
    /** sysannouncement标题 */
    private String sysAnnouncementTitle;

    /**
     * 公告内容(HTML)
     */
    @TableField(value = "sys_announcement_content")
    @Schema(description = "公告内容(HTML)")
    /** sysannouncement内容 */
    private String sysAnnouncementContent;

    /**
      * 级别: 信息-信息 警告-警告 important-重要 emergency-紧急
     */
    @TableField(value = "sys_announcement_level")
    @Schema(description = "级别: info-信息 warning-警告 important-重要 emergency-紧急")
    @Size(max = 20)
    /** sysannouncement级别 */
    private String sysAnnouncementLevel;

    /**
      * 展示位置: 默认-默认(Tabs下方) fixed-top-固定顶部
     */
    @TableField(value = "sys_announcement_position")
    @Schema(description = "展示位置: default-默认(Tabs下方) fixed-top-固定顶部")
    @Size(max = 20)
    /** sysannouncement位置 */
    private String sysAnnouncementPosition;

    /**
      * 类型: 全局-全局 角色-指定角色
     */
    @TableField(value = "sys_announcement_type")
    @Schema(description = "类型: global-全局 role-指定角色")
    @Size(max = 20)
    /** sysannouncement类型 */
    private String sysAnnouncementType;

    /**
      * 目标角色标识列表(JSON数组)
     */
    @TableField(value = "sys_announcement_target_roles")
    @Schema(description = "目标角色ID列表(JSON数组)")
    @Size(max = 1000)
    /** sysannouncement目标角色 */
    private String sysAnnouncementTargetRoles;

    /**
     * 状态: 0-草稿 1-已发布 2-已下架
     */
    @TableField(value = "sys_announcement_status")
    @Schema(description = "状态: 0-草稿 1-已发布 2-已下架")
    /** sysannouncement状态 */
    private Integer sysAnnouncementStatus;

    /**
     * 排序权重
     */
    @TableField(value = "sys_announcement_sort")
    @Schema(description = "排序权重")
    /** sysannouncementsort */
    private Integer sysAnnouncementSort;

    /**
     * 发布时间
     */
    @TableField(value = "sys_announcement_publish_time")
    @Schema(description = "发布时间")
    /** sysannouncementpublish时间 */
    private LocalDateTime sysAnnouncementPublishTime;

    /**
     * 过期时间(空=永不过期)
     */
    @TableField(value = "sys_announcement_expire_time")
    @Schema(description = "过期时间(空=永不过期)")
    /** sysannouncementexpire时间 */
    private LocalDateTime sysAnnouncementExpireTime;

    /**
     * 跳转链接
     */
    @TableField(value = "sys_announcement_target_url")
    @Schema(description = "跳转链接")
    @Size(max = 500)
    /** sysannouncement目标URL */
    private String sysAnnouncementTargetUrl;

    /**
     * 发布人
     */
    @TableField(value = "sys_announcement_publisher_name")
    @Schema(description = "发布人")
    @Size(max = 100)
    /** sysannouncementpublisher名称 */
    private String sysAnnouncementPublisherName;

    /**
     * 级别：信息
     */
    public static final String LEVEL_INFO = "info";

    /**
     * 级别：警告
     */
    public static final String LEVEL_WARNING = "warning";

    /**
     * 级别：重要
     */
    public static final String LEVEL_IMPORTANT = "important";

    /**
     * 级别：紧急
     */
    public static final String LEVEL_EMERGENCY = "emergency";

    /**
     * 位置：默认（Tabs下方）
     */
    public static final String POSITION_DEFAULT = "default";

    /**
     * 位置：固定顶部
     */
    public static final String POSITION_FIXED_TOP = "fixed-top";

    /**
     * 类型：全局
     */
    public static final String TYPE_GLOBAL = "global";

    /**
     * 类型：指定角色
     */
    public static final String TYPE_ROLE = "role";

    /**
     * 状态：草稿
     */
    public static final int STATUS_DRAFT = 0;

    /**
     * 状态：已发布
     */
    public static final int STATUS_PUBLISHED = 1;

    /**
     * 状态：已下架
     */
    public static final int STATUS_UNPUBLISHED = 2;
}