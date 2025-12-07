package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.common.support.validator.group.UploadGroup;
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
 * 缁勪欢琛?
 *
 * @author CH
 * @since 2024/10/24
 */
@ApiModel(description = "缁勪欢琛")
@Schema(description = "缁勪欢琛")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sfc")
public class SysSfc extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_sfc_id", type = IdType.AUTO)
    @ApiModelProperty(value = "缁勪欢ID")
    @Schema(description = "缁勪欢ID")
    @NotNull(message = "涓嶈兘涓簄ull", groups = {UpdateGroup.class, UploadGroup.class})
    private Integer sysSfcId;

    /**
     * 缁勪欢鍚嶇О
     */
    @TableField(value = "sys_sfc_name")
    @ApiModelProperty(value = "缁勪欢鍚嶇О")
    @Schema(description = "缁勪欢鍚嶇О")
    @Size(max = 255, message = "缁勪欢鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255", groups = {AddGroup.class})
    private String sysSfcName;

    /**
     * 缁勪欢涓枃鍚嶇О
     */
    @TableField(value = "sys_sfc_chinese_name")
    @ApiModelProperty(value = "缁勪欢涓枃鍚嶇О")
    @Schema(description = "缁勪欢涓枃鍚嶇О")
    @Size(max = 255, message = "缁勪欢涓枃鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255", groups = {AddGroup.class})
    private String sysSfcChineseName;

    /**
     * 缂撳瓨妯″紡
     */
    @TableField(value = "sys_sfc_model_cache")
    @ApiModelProperty(value = "缂撳瓨妯″紡")
    @Schema(description = "缂撳瓨妯″紡")
    private String sysSfcModelCache;

    /**
     * 缁勪欢鍔熻兘, 澶氫釜閫楀彿鍒嗛殧
     */
    @TableField(value = "sys_sfc_function")
    @ApiModelProperty(value = "缁勪欢鍔熻兘, 澶氫釜閫楀彿鍒嗛殧")
    @Schema(description = "缁勪欢鍔熻兘, 澶氫釜閫楀彿鍒嗛殧")
    @Size(max = 255, message = "缁勪欢鍔熻兘, 澶氫釜閫楀彿鍒嗛殧鏈€澶ч暱搴﹁灏忎簬 255", groups = {AddGroup.class})
    private String sysSfcFunction;

    /**
     * 缁勪欢鍥炬爣
     */
    @TableField(value = "sys_sfc_icon")
    @ApiModelProperty(value = "缁勪欢鍥炬爣")
    @Schema(description = "缁勪欢鍥炬爣")
    @Size(max = 255, message = "缁勪欢鍥炬爣鏈€澶ч暱搴﹁灏忎簬 255", groups = {AddGroup.class})
    private String sysSfcIcon;

    /**
     * 鍒嗙被; HOME
     */
    @TableField(value = "sys_sfc_category")
    @ApiModelProperty(value = "鍒嗙被; HOME")
    @Schema(description = "鍒嗙被; HOME")
    @Size(max = 255, message = "鍒嗙被; HOME鏈€澶ч暱搴﹁灏忎簬 255", groups = {AddGroup.class})
    private String sysSfcCategory;

    /**
     * 缁勪欢瀛樻斁璺緞
     */
    @TableField(value = "sys_sfc_path")
    @ApiModelProperty(value = "缁勪欢瀛樻斁璺緞")
    @Schema(description = "缁勪欢瀛樻斁璺緞")
    @Size(max = 255, message = "缁勪欢瀛樻斁璺緞鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysSfcPath;

    /**
     * 缁勪欢浠ｇ悊鍦板潃
     */
    @TableField(value = "sys_sfc_proxy")
    @ApiModelProperty(value = "缁勪欢浠ｇ悊鍦板潃")
    @Schema(description = "缁勪欢浠ｇ悊鍦板潃")
    @Size(max = 255, message = "缁勪欢浠ｇ悊鍦板潃鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysSfcProxy;

    /**
     * 缁勪欢绫诲瀷; 0:鏂囦欢; 1:鏂囨湰
     */
    @TableField(value = "sys_sfc_type")
    @ApiModelProperty(value = "缁勪欢绫诲瀷; 0:鏂囦欢; 1:鏂囨湰")
    @Schema(description = "缁勪欢绫诲瀷; 0:鏂囦欢; 1:鏂囨湰")
    private Integer sysSfcType;

    /**
     * 缁勪欢鐗堟湰
     */
    @TableField(value = "sys_sfc_version")
    @ApiModelProperty(value = "缁勪欢鐗堟湰")
    @Schema(description = "缁勪欢鐗堟湰")
    private String sysSfcVersion;

    /**
     * 缁勪欢鍐呭
     */
    @TableField(value = "sys_sfc_content")
    @ApiModelProperty(value = "缁勪欢鍐呭")
    @Schema(description = "缁勪欢鍐呭")
    private String sysSfcContent;

    /**
     * 缁勪欢鐘舵€; 0:鏈惎鐢
     */
    @TableField(value = "sys_sfc_status")
    @ApiModelProperty(value = "缁勪欢鐘舵€; 0:鏈惎鐢")
    @Schema(description = "缁勪欢鐘舵€; 0:鏈惎鐢")
    private Integer sysSfcStatus;

    /**
     * 寤惰繜鏃堕棿; 2000
     */
    @TableField(value = "sys_sfc_delay")
    @ApiModelProperty(value = "寤惰繜鏃堕棿; 2000")
    @Schema(description = "寤惰繜鏃堕棿; 2000")
    private Integer sysSfcDelay;

    /**
     * 瓒呮椂鏃堕棿; 3000
     */
    @TableField(value = "sys_sfc_timeout")
    @ApiModelProperty(value = "瓒呮椂鏃堕棿; 3000")
    @Schema(description = "瓒呮椂鏃堕棿; 3000")
    private Integer sysSfcTimeout;

    /**
     * 缁勪欢鎻忚堪
     */
    @TableField(value = "sys_sfc_desc")
    @ApiModelProperty(value = "缁勪欢鎻忚堪")
    @Schema(description = "缁勪欢鎻忚堪")
    @Size(max = 255, message = "缁勪欢鎻忚堪鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysSfcDesc;

    /**
     * 鏄惁宸插畨瑁"
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "鏄惁宸插畨瑁")
    @Schema(description = "鏄惁宸插畨瑁")
    private Integer sysSfcInstall;
}