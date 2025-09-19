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

/**
 *
 * @author CH
 * @since 2025/9/18 17:25
 */

/**
 * 在线地址线路
 */
@ApiModel(description = "在线地址线路")
@Schema(description = "在线地址线路")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "video_play_address_channel")
public class VideoPlayAddressChannel extends SysBase {
    @TableId(value = "video_play_address_channel_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer videoPlayAddressChannelId;

    /**
     * 线路名称
     */
    @TableField(value = "video_play_address_channel_name")
    @ApiModelProperty(value = "线路名称")
    @Schema(description = "线路名称")
    private String videoPlayAddressChannelName;

    /**
     * 地址ID
     */
    @TableField(value = "video_play_address_id")
    @ApiModelProperty(value = "地址ID")
    @Schema(description = "地址ID")
    private Integer videoPlayAddressId;

    /**
     * 地址
     */
    @TableField(value = "video_play_address_url")
    @ApiModelProperty(value = "地址")
    @Schema(description = "地址")
    private String videoPlayAddressUrl;

    /**
     * 开始索引
     */
    @TableField(value = "video_play_address_start_index")
    @ApiModelProperty(value = "开始索引")
    @Schema(description = "开始索引")
    private Integer videoPlayAddressStartIndex;

    /**
     * 结束索引
     */
    @TableField(value = "video_play_address_end_index")
    @ApiModelProperty(value = "结束索引")
    @Schema(description = "结束索引")
    private Integer videoPlayAddressEndIndex;
}