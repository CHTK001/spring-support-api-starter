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
import java.math.BigDecimal;

/**
 * 鐢ㄦ埛鏃ュ織琛? *
 * @author CH
 * @since 2024/8/8
 */
@ApiModel(description = "鐢ㄦ埛鏃ュ織琛")
@Schema(description = "鐢ㄦ埛鏃ュ織琛")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_log")
public class SysUserLog extends SysBase implements Serializable {

    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "鏃ュ織ID")
    @Schema(description = "鏃ュ織ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysLogId;

    /**
     * 璐﹀彿
     */
    @TableField(value = "sys_log_username")
    @ApiModelProperty(value = "璐﹀彿")
    @Schema(description = "璐﹀彿")
    @Size(max = 255, message = "璐﹀彿鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogUsername;

    /**
     * 鐧诲綍鏂瑰紡
     */
    @TableField(value = "sys_log_login_type")
    @ApiModelProperty(value = "鐧诲綍鏂瑰紡")
    @Schema(description = "鐧诲綍鏂瑰紡")
    @Size(max = 255, message = "鐧诲綍鏂瑰紡鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogLoginType;

    /**
     * 娴忚鍣║A
     */
    @TableField(value = "sys_log_ua")
    @ApiModelProperty(value = "娴忚鍣╱a")
    @Schema(description = "娴忚鍣╱a")
    @Size(max = 255, message = "娴忚鍣╱a鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogUa;

    /**
     * 娴忚鍣ㄦ寚绾?     */
    @TableField(value = "sys_log_fingerprint")
    @ApiModelProperty(value = "娴忚鍣ㄦ寚绾")
    @Schema(description = "娴忚鍣ㄦ寚绾")
    private String sysLogFingerprint;

    /**
     * 娴忚鍣?     */
    @TableField(value = "sys_log_browser")
    @ApiModelProperty(value = "娴忚鍣")
    @Schema(description = "娴忚鍣")
    @Size(max = 255, message = "娴忚鍣ㄦ渶澶ч暱搴﹁灏忎簬 255")
    private String sysLogBrowser;

    /**
     * 绯荤粺
     */
    @TableField(value = "sys_log_system")
    @ApiModelProperty(value = "绯荤粺")
    @Schema(description = "绯荤粺")
    @Size(max = 255, message = "绯荤粺鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogSystem;

    /**
     * 璇锋眰IP
     */
    @TableField(value = "sys_log_ip")
    @ApiModelProperty(value = "璇锋眰IP")
    @Schema(description = "璇锋眰IP")
    @Size(max = 255, message = "璇锋眰IP鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogIp;

    /**
     * 璇锋眰鍦板潃
     */
    @TableField(value = "sys_log_address")
    @ApiModelProperty(value = "璇锋眰鍦板潃")
    @Schema(description = "璇锋眰鍦板潃")
    @Size(max = 255, message = "璇锋眰鍦板潃鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogAddress;

    /**
     * 杩愯惀鍟?     */
    @TableField(value = "sys_log_isp")
    @ApiModelProperty(value = "杩愯惀鍟")
    @Schema(description = "杩愯惀鍟")
    @Size(max = 255, message = "杩愯惀鍟嗘渶澶ч暱搴﹁灏忎簬 255")
    private String sysLogIsp;

    /**
     * 璇锋眰URL
     */
    @TableField(value = "sys_log_url")
    @ApiModelProperty(value = "璇锋眰URL")
    @Schema(description = "璇锋眰URL")
    @Size(max = 255, message = "璇锋眰URL鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogUrl;

    /**
     * 搴旂敤鍚嶇О
     */
    @TableField(value = "sys_log_app_name")
    @ApiModelProperty(value = "搴旂敤鍚嶇О")
    @Schema(description = "搴旂敤鍚嶇О")
    private String sysLogAppName;

    /**
     * 璇锋眰鍙傛暟
     */
    @TableField(value = "sys_log_param")
    @ApiModelProperty(value = "璇锋眰鍙傛暟")
    @Schema(description = "璇锋眰鍙傛暟")
    @Size(max = 255, message = "璇锋眰鍙傛暟鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogParam;

    /**
     * 璇锋眰妯″潡
     */
    @TableField(value = "sys_log_from")
    @ApiModelProperty(value = "璇锋眰妯″潡")
    @Schema(description = "璇锋眰妯″潡")
    @Size(max = 255, message = "璇锋眰妯″潡鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysLogFrom;

    /**
     * 璇锋眰鐘舵€; 0: 澶辫触; 1: 鎴愬姛
     */
    @TableField(value = "sys_log_status")
    @ApiModelProperty(value = "璇锋眰鐘舵€; 0: 澶辫触; 1: 鎴愬姛")
    @Schema(description = "璇锋眰鐘舵€; 0: 澶辫触; 1: 鎴愬姛")
    private Integer sysLogStatus;

    /**
     * 璇锋眰娑堟伅
     */
    @TableField(value = "sys_log_message")
    @ApiModelProperty(value = "璇锋眰娑堟伅")
    @Schema(description = "璇锋眰娑堟伅")
    private String sysLogMessage;

    /**
     * 鑰楁椂
     */
    @TableField(value = "sys_log_cost")
    @ApiModelProperty(value = "鑰楁椂")
    @Schema(description = "鑰楁椂")
    private BigDecimal sysLogCost;

    /**
     * 閮ㄩ棬ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "閮ㄩ棬ID")
    @Schema(description = "閮ㄩ棬ID")
    private Integer sysDeptId;

    /**
     * 鏈烘瀯鍚嶇О
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "鏈烘瀯鍚嶇О")
    @Schema(description = "鏈烘瀯鍚嶇О")
    private String sysDeptName;
}