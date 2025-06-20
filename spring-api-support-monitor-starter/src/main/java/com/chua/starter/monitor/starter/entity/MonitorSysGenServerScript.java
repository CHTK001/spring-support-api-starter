package com.chua.starter.monitor.starter.entity;

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
import java.time.LocalDateTime;

/**
 * 服务器脚本实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器脚本")
@Schema(description = "服务器脚本")
@Data
@TableName(value = "monitor_sys_gen_server_scripts")
public class MonitorSysGenServerScript extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_script_id", type = IdType.AUTO)
    @ApiModelProperty(value = "脚本ID")
    @Schema(description = "脚本ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerScriptId;

    /**
     * 脚本名称
     */
    @TableField(value = "monitor_sys_gen_server_script_name")
    @ApiModelProperty(value = "脚本名称")
    @Schema(description = "脚本名称")
    @NotNull(message = "脚本名称不能为空")
    @Size(max = 255, message = "脚本名称最大长度要小于 255")
    private String monitorSysGenServerScriptName;

    /**
     * 脚本描述
     */
    @TableField(value = "monitor_sys_gen_server_script_description")
    @ApiModelProperty(value = "脚本描述")
    @Schema(description = "脚本描述")
    private String monitorSysGenServerScriptDescription;

    /**
     * 脚本内容
     */
    @TableField(value = "monitor_sys_gen_server_script_content")
    @ApiModelProperty(value = "脚本内容")
    @Schema(description = "脚本内容")
    @NotNull(message = "脚本内容不能为空")
    private String monitorSysGenServerScriptContent;

    /**
     * 脚本类型
     */
    @TableField(value = "monitor_sys_gen_server_script_type")
    @ApiModelProperty(value = "脚本类型")
    @Schema(description = "脚本类型(SHELL:Shell脚本 PYTHON:Python脚本 POWERSHELL:PowerShell脚本 BATCH:批处理脚本)")
    @Size(max = 50, message = "脚本类型最大长度要小于 50")
    private String monitorSysGenServerScriptType;

    /**
     * 脚本语言
     */
    @TableField(value = "monitor_sys_gen_server_script_language")
    @ApiModelProperty(value = "脚本语言")
    @Schema(description = "脚本语言")
    @Size(max = 50, message = "脚本语言最大长度要小于 50")
    private String monitorSysGenServerScriptLanguage;

    /**
     * 脚本版本
     */
    @TableField(value = "monitor_sys_gen_server_script_version")
    @ApiModelProperty(value = "脚本版本")
    @Schema(description = "脚本版本")
    @Size(max = 50, message = "脚本版本最大长度要小于 50")
    private String monitorSysGenServerScriptVersion;

    /**
     * 脚本分类
     */
    @TableField(value = "monitor_sys_gen_server_script_category")
    @ApiModelProperty(value = "脚本分类")
    @Schema(description = "脚本分类")
    @Size(max = 100, message = "脚本分类最大长度要小于 100")
    private String monitorSysGenServerScriptCategory;

    /**
     * 脚本标签(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_tags")
    @ApiModelProperty(value = "脚本标签")
    @Schema(description = "脚本标签(JSON格式)")
    private String monitorSysGenServerScriptTags;

    /**
     * 脚本参数定义(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_parameters")
    @ApiModelProperty(value = "脚本参数定义")
    @Schema(description = "脚本参数定义(JSON格式)")
    private String monitorSysGenServerScriptParameters;

    /**
     * 执行超时时间(秒)
     */
    @TableField(value = "monitor_sys_gen_server_script_timeout_seconds")
    @ApiModelProperty(value = "执行超时时间(秒)")
    @Schema(description = "执行超时时间(秒)")
    private Integer monitorSysGenServerScriptTimeoutSeconds;

    /**
     * 重试次数
     */
    @TableField(value = "monitor_sys_gen_server_script_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer monitorSysGenServerScriptRetryCount;

    /**
     * 是否为模板
     */
    @TableField(value = "monitor_sys_gen_server_script_is_template")
    @ApiModelProperty(value = "是否为模板")
    @Schema(description = "是否为模板(0:否 1:是)")
    private Integer monitorSysGenServerScriptIsTemplate;

    /**
     * 是否公开
     */
    @TableField(value = "monitor_sys_gen_server_script_is_public")
    @ApiModelProperty(value = "是否公开")
    @Schema(description = "是否公开(0:否 1:是)")
    private Integer monitorSysGenServerScriptIsPublic;

    /**
     * 执行模式
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_mode")
    @ApiModelProperty(value = "执行模式")
    @Schema(description = "执行模式(SYNC:同步 ASYNC:异步)")
    @Size(max = 20, message = "执行模式最大长度要小于 20")
    private String monitorSysGenServerScriptExecutionMode;

    /**
     * 工作目录
     */
    @TableField(value = "monitor_sys_gen_server_script_working_directory")
    @ApiModelProperty(value = "工作目录")
    @Schema(description = "工作目录")
    @Size(max = 500, message = "工作目录最大长度要小于 500")
    private String monitorSysGenServerScriptWorkingDirectory;

    /**
     * 环境变量(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_environment_vars")
    @ApiModelProperty(value = "环境变量")
    @Schema(description = "环境变量(JSON格式)")
    private String monitorSysGenServerScriptEnvironmentVars;

    /**
     * 依赖项(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_dependencies")
    @ApiModelProperty(value = "依赖项")
    @Schema(description = "依赖项(JSON格式)")
    private String monitorSysGenServerScriptDependencies;

    /**
     * 验证规则(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_validation_rules")
    @ApiModelProperty(value = "验证规则")
    @Schema(description = "验证规则(JSON格式)")
    private String monitorSysGenServerScriptValidationRules;

    /**
     * 创建用户
     */
    @TableField(value = "monitor_sys_gen_server_script_create_user")
    @ApiModelProperty(value = "创建用户")
    @Schema(description = "创建用户")
    @Size(max = 100, message = "创建用户最大长度要小于 100")
    private String monitorSysGenServerScriptCreateUser;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_script_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerScriptCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_script_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerScriptUpdateTime;

    /**
     * 最后执行时间
     */
    @TableField(value = "monitor_sys_gen_server_script_last_execution_time")
    @ApiModelProperty(value = "最后执行时间")
    @Schema(description = "最后执行时间")
    private LocalDateTime monitorSysGenServerScriptLastExecutionTime;

    /**
     * 执行次数
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_count")
    @ApiModelProperty(value = "执行次数")
    @Schema(description = "执行次数")
    private Integer monitorSysGenServerScriptExecutionCount;

    /**
     * 成功次数
     */
    @TableField(value = "monitor_sys_gen_server_script_success_count")
    @ApiModelProperty(value = "成功次数")
    @Schema(description = "成功次数")
    private Integer monitorSysGenServerScriptSuccessCount;

    /**
     * 脚本状态
     */
    @TableField(value = "monitor_sys_gen_server_script_status")
    @ApiModelProperty(value = "脚本状态")
    @Schema(description = "脚本状态(0:禁用 1:启用)")
    private Integer monitorSysGenServerScriptStatus;

    /**
     * 脚本类型枚举
     */
    public enum ScriptType {
        SHELL("SHELL", "Shell脚本"),
        PYTHON("PYTHON", "Python脚本"),
        POWERSHELL("POWERSHELL", "PowerShell脚本"),
        BATCH("BATCH", "批处理脚本");

        private final String code;
        private final String desc;

        ScriptType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ScriptType fromCode(String code) {
            for (ScriptType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return SHELL;
        }
    }

    /**
     * 执行模式枚举
     */
    public enum ExecutionMode {
        SYNC("SYNC", "同步执行"),
        ASYNC("ASYNC", "异步执行");

        private final String code;
        private final String desc;

        ExecutionMode(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ExecutionMode fromCode(String code) {
            for (ExecutionMode mode : values()) {
                if (mode.code.equals(code)) {
                    return mode;
                }
            }
            return SYNC;
        }
    }

    /**
     * 获取脚本类型描述
     */
    public String getScriptTypeDesc() {
        return ScriptType.fromCode(this.monitorSysGenServerScriptType).getDesc();
    }

    /**
     * 获取执行模式描述
     */
    public String getExecutionModeDesc() {
        return ExecutionMode.fromCode(this.monitorSysGenServerScriptExecutionMode).getDesc();
    }

    /**
     * 是否为模板
     */
    public boolean isTemplate() {
        return this.monitorSysGenServerScriptIsTemplate != null && this.monitorSysGenServerScriptIsTemplate == 1;
    }

    /**
     * 是否公开
     */
    public boolean isPublic() {
        return this.monitorSysGenServerScriptIsPublic != null && this.monitorSysGenServerScriptIsPublic == 1;
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this.monitorSysGenServerScriptStatus != null && this.monitorSysGenServerScriptStatus == 1;
    }

    /**
     * 是否同步执行
     */
    public boolean isSyncExecution() {
        return ExecutionMode.SYNC.getCode().equals(this.monitorSysGenServerScriptExecutionMode);
    }

    /**
     * 是否异步执行
     */
    public boolean isAsyncExecution() {
        return ExecutionMode.ASYNC.getCode().equals(this.monitorSysGenServerScriptExecutionMode);
    }

    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (this.monitorSysGenServerScriptExecutionCount == null || this.monitorSysGenServerScriptExecutionCount == 0) {
            return 0.0;
        }
        if (this.monitorSysGenServerScriptSuccessCount == null) {
            return 0.0;
        }
        return (this.monitorSysGenServerScriptSuccessCount * 100.0) / this.monitorSysGenServerScriptExecutionCount;
    }

    /**
     * 获取失败次数
     */
    public Integer getFailureCount() {
        if (this.monitorSysGenServerScriptExecutionCount == null || this.monitorSysGenServerScriptSuccessCount == null) {
            return 0;
        }
        return this.monitorSysGenServerScriptExecutionCount - this.monitorSysGenServerScriptSuccessCount;
    }

    /**
     * 设置默认值
     */
    public void setDefaultValues() {
        if (this.monitorSysGenServerScriptType == null) {
            this.monitorSysGenServerScriptType = ScriptType.SHELL.getCode();
        }
        if (this.monitorSysGenServerScriptLanguage == null) {
            this.monitorSysGenServerScriptLanguage = "bash";
        }
        if (this.monitorSysGenServerScriptVersion == null) {
            this.monitorSysGenServerScriptVersion = "1.0.0";
        }
        if (this.monitorSysGenServerScriptExecutionMode == null) {
            this.monitorSysGenServerScriptExecutionMode = ExecutionMode.SYNC.getCode();
        }
        if (this.monitorSysGenServerScriptTimeoutSeconds == null) {
            this.monitorSysGenServerScriptTimeoutSeconds = 300;
        }
        if (this.monitorSysGenServerScriptRetryCount == null) {
            this.monitorSysGenServerScriptRetryCount = 0;
        }
        if (this.monitorSysGenServerScriptIsTemplate == null) {
            this.monitorSysGenServerScriptIsTemplate = 0;
        }
        if (this.monitorSysGenServerScriptIsPublic == null) {
            this.monitorSysGenServerScriptIsPublic = 0;
        }
        if (this.monitorSysGenServerScriptExecutionCount == null) {
            this.monitorSysGenServerScriptExecutionCount = 0;
        }
        if (this.monitorSysGenServerScriptSuccessCount == null) {
            this.monitorSysGenServerScriptSuccessCount = 0;
        }
        if (this.monitorSysGenServerScriptStatus == null) {
            this.monitorSysGenServerScriptStatus = 1;
        }
    }
}
