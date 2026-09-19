package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 银行BIN对照表
 *
 * @since 2024/12/6
 * @author CH
 */
@Schema(description = "银行BIN对照表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_bank_bins")
public class SysBankBins extends SysBase {

    /**
     * 主键标识
     */
    @TableId(value = "sys_blank_bin_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    /** sysblankbinid */
    private Integer sysBlankBinId;

    /**
     * 银行名称
     */
    @TableField(value = "sys_blank_bin_bank_name")
    @Schema(description = "银行名称")
    @Size(max = 20, message = "银行名称最大长度需小于20")
    @NotBlank(message = "银行名称不能为空")
    /** sysblankbinbank名称 */
    private String sysBlankBinBankName;

    /**
     * 银行英文名称
     */
    @TableField(value = "sys_blank_bin_bank_name_en")
    @Schema(description = "银行英文名称")
    @Size(max = 20, message = "银行英文名称最大长度需小于20")
    /** sysblankbinbank名称EN */
    private String sysBlankBinBankNameEn;

    /**
     * 卡名称
     */
    @TableField(value = "sys_blank_bin_card_name")
    @Schema(description = "卡名称")
    @Size(max = 20, message = "卡名称最大长度需小于20")
    /** sysblankbincard名称 */
    private String sysBlankBinCardName;

    /**
     * 卡类型
     */
    @TableField(value = "sys_blank_bin_card_type")
    @Schema(description = "卡类型")
    @Size(max = 20, message = "卡类型最大长度需小于20")
    /** sysblankbincard类型 */
    private String sysBlankBinCardType;

    /**
     * BIN码
     */
    @TableField(value = "sys_blank_bin_bin")
    @Schema(description = "BIN码")
    @Size(max = 20, message = "BIN码最大长度需小于20")
    @NotBlank(message = "BIN码不能为空")
    /** sysblankbinbin */
    private String sysBlankBinBin;

    /**
     * 卡号长度
     */
    @TableField(value = "sys_blank_bin_n_length")
    @Schema(description = "卡号长度")
    /** sysblankbinnlength */
    private Byte sysBlankBinNLength;

    /**
     * BIN长度
     */
    @TableField(value = "sys_blank_bin_bin_length")
    @Schema(description = "BIN长度")
    /** sysblankbinbin长度 */
    private Boolean sysBlankBinBinLength;

    /**
     * 发卡机构标识
     */
    @TableField(value = "sys_blank_bin_issue_id")
    @Schema(description = "发卡机构ID")
    /** sysblankbinissueid */
    private Integer sysBlankBinIssueId;
}
