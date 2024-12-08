package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @since 2024/12/6
 * @author CH    
 */

/**
 * 银行BIN对照表
 */
@ApiModel(description = "银行BIN对照表")
@Schema(description = "银行BIN对照表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "kuntu.sys_bank_bins")
public class SysBankBins extends SysBase {
    @TableId(value = "sys_blank_bin_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysBlankBinId;

    @TableField(value = "sys_blank_bin_bank_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 20, message = "最大长度要小于 20")
    @NotBlank(message = "不能为空")
    private String sysBlankBinBankName;

    @TableField(value = "sys_blank_bin_bank_name_en")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 20, message = "最大长度要小于 20")
    private String sysBlankBinBankNameEn;

    @TableField(value = "sys_blank_bin_card_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 20, message = "最大长度要小于 20")
    private String sysBlankBinCardName;

    @TableField(value = "sys_blank_bin_card_type")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 20, message = "最大长度要小于 20")
    private String sysBlankBinCardType;

    @TableField(value = "sys_blank_bin_bin")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 20, message = "最大长度要小于 20")
    @NotBlank(message = "不能为空")
    private String sysBlankBinBin;

    @TableField(value = "sys_blank_bin_n_length")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Byte sysBlankBinNLength;

    @TableField(value = "sys_blank_bin_bin_length")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Boolean sysBlankBinBinLength;

    @TableField(value = "sys_blank_bin_issue_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysBlankBinIssueId;
}