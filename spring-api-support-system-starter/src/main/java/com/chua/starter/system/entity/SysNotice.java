package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
/**
 * 通知记录
 */
@ApiModel(description="通知记录")
@Schema(description="通知记录")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_notice")
public class SysNotice extends SysBase implements Serializable {
    @TableId(value = "sys_notice_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysNoticeId;

    /**
     * 通知目标
     */
    @TableField(value = "sys_notice_target")
    @ApiModelProperty(value="通知目标")
    @Schema(description="通知目标")
    @Size(max = 255,message = "通知目标最大长度要小于 255")
    private String sysNoticeTarget;

    /**
     * 通知内容
     */
    @TableField(value = "sys_notice_content")
    @ApiModelProperty(value="通知内容")
    @Schema(description="通知内容")
    @Size(max = 255,message = "通知内容最大长度要小于 255")
    private String sysNoticeContent;

    /**
     * 通知方式
     */
    @TableField(value = "sys_notice_type")
    @ApiModelProperty(value="通知方式")
    @Schema(description="通知方式")
    @Size(max = 255,message = "通知方式最大长度要小于 255")
    private String sysNoticeType;

    /**
     * 通知来源
     */
    @TableField(value = "sys_notice_origin")
    @ApiModelProperty(value="通知来源")
    @Schema(description="通知来源")
    @Size(max = 255,message = "通知来源最大长度要小于 255")
    private String sysNoticeOrigin;

    /**
     * 是否成功；0：失败
     */
    @TableField(value = "sys_notice_status")
    @ApiModelProperty(value="是否成功；0：失败")
    @Schema(description="是否成功；0：失败")
    private Integer sysNoticeStatus;

    /**
     * 失败消息
     */
    @TableField(value = "sys_notice_error")
    @ApiModelProperty(value="失败消息")
    @Schema(description="失败消息")
    @Size(max = 255,message = "失败消息最大长度要小于 255")
    private String sysNoticeError;
}