package com.chua.starter.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2025/9/18 9:26
 */

/**
 * 在线地址
 */
@ApiModel(description = "在线地址")
@Schema(description = "在线地址")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "video_play_address")
public class VideoPlayAddress extends SysBase {
    @TableId(value = "video_play_address_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer videoPlayAddressId;

    /**
     * 地址名称
     */
    @TableField(value = "video_play_address_name")
    @ApiModelProperty(value = "地址名称")
    @Schema(description = "地址名称")
    private String videoPlayAddressName;

    /**
     * 地址编号
     */
    @TableField(value = "video_play_address_code")
    @ApiModelProperty(value = "地址编号")
    @Schema(description = "地址编号")
    private String videoPlayAddressCode;

    /**
     * 地址唯一编码
     */
    @TableField(value = "video_play_address_unique")
    @ApiModelProperty(value = "地址唯一编码")
    @Schema(description = "地址唯一编码")
    private String videoPlayAddressUnique;

    /**
     * 视频ID
     */
    @TableField(value = "video_id")
    @ApiModelProperty(value = "视频ID")
    @Schema(description = "视频ID")
    private Integer videoId;
    /**
     * 地址通道
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "地址通道")
    private List<VideoPlayAddressChannel> videoPlayAddressChannels;
}