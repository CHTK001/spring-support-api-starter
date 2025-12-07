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
 * 鐢ㄦ埛甯冨眬
 *
 * @author CH
 * @since 2024/8/30
 */
@ApiModel(description = "鐢ㄦ埛甯冨眬")
@Schema(description = "鐢ㄦ埛甯冨眬")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_layout")
public class SysUserLayout extends SysBase implements Serializable {

    @TableId(value = "sys_layout_id", type = IdType.AUTO)
    @ApiModelProperty(value = "甯冨眬ID")
    @Schema(description = "甯冨眬ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysLayoutId;

    /**
     * 甯冨眬
     */
    @TableField(value = "sys_layout_layout")
    @ApiModelProperty(value = "甯冨眬")
    @Schema(description = "甯冨眬")
    @Size(max = 255, message = "甯冨眬鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLayoutLayout;

    /**
     * 甯冨眬缃戞牸
     */
    @TableField(value = "sys_layout_grid")
    @ApiModelProperty(value = "甯冨眬缃戞牸")
    @Schema(description = "甯冨眬缃戞牸")
    @Size(max = 255, message = "甯冨眬缃戞牸鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLayoutGrid;

    /**
     * 缁勪欢
     */
    @TableField(value = "sys_layout_component")
    @ApiModelProperty(value = "缁勪欢")
    @Schema(description = "缁勪欢")
    @Size(max = 255, message = "缁勪欢鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLayoutComponent;

    /**
     * 鎵€灞炵敤鎴"     */
    @TableField(value = "sys_layout_user_id")
    @ApiModelProperty(value = "鎵€灞炵敤鎴")
    @Schema(description = "鎵€灞炵敤鎴")
    private Integer sysLayoutUserId;
}