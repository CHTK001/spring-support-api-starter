package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 脚本管理实体
 *
 * @author CH
 * @since 2024/12/19
 */
@Schema(description = "脚本管理实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_script")
public class MonitorSysGenScript extends SysBase {

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID", example = "1")
    @TableId(value = "monitor_sys_gen_script_id", type = IdType.AUTO)
    private Integer monitorSysGenScriptId;

    /**
     * 脚本名称
     */
    @Schema(description = "脚本名称", example = "系统监控脚本")
    @TableField("monitor_sys_gen_script_name")
    private String monitorSysGenScriptName;

    /**
     * 脚本类型
     */
    @Schema(description = "脚本类型", example = "SHELL", allowableValues = { "SHELL", "PYTHON", "JAVASCRIPT",
            "POWERSHELL" })
    @TableField("monitor_sys_gen_script_type")
    private String monitorSysGenScriptType;

    /**
     * 脚本描述
     */
    @Schema(description = "脚本描述", example = "用于监控系统资源使用情况的脚本")
    @TableField("monitor_sys_gen_script_description")
    private String monitorSysGenScriptDescription;

    /**
     * 脚本内容
     */
    @Schema(description = "脚本内容", example = "#!/bin/bash\necho 'Hello World'")
    @TableField("monitor_sys_gen_script_content")
    private String monitorSysGenScriptContent;

    /**
     * 默认参数(JSON格式)
     */
    @Schema(description = "默认参数(JSON格式)", example = "{\"param1\": \"value1\", \"param2\": \"value2\"}")
    @TableField("monitor_sys_gen_script_parameters")
    private String monitorSysGenScriptParameters;

    /**
     * 执行超时时间(秒)
     */
    @Schema(description = "执行超时时间(秒)", example = "300", minimum = "1", maximum = "3600")
    @TableField("monitor_sys_gen_script_timeout")
    private Integer monitorSysGenScriptTimeout;

    /**
     * 脚本状态
     */
    @Schema(description = "脚本状态", implementation = ScriptStatus.class)
    @TableField("monitor_sys_gen_script_status")
    private ScriptStatus monitorSysGenScriptStatus;

    /**
     * 脚本标签
     */
    @Schema(description = "脚本标签(逗号分隔)", example = "监控,系统,自动化")
    @TableField("monitor_sys_gen_script_tags")
    private String monitorSysGenScriptTags;

    /**
     * 脚本分类
     */
    @Schema(description = "脚本分类", example = "系统监控")
    @TableField("monitor_sys_gen_script_category")
    private String monitorSysGenScriptCategory;

    /**
     * 脚本版本
     */
    @Schema(description = "脚本版本", example = "1.0.0")
    @TableField("monitor_sys_gen_script_version")
    private String monitorSysGenScriptVersion;

    /**
     * 脚本作者
     */
    @Schema(description = "脚本作者", example = "admin")
    @TableField("monitor_sys_gen_script_author")
    private String monitorSysGenScriptAuthor;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间", example = "2024-01-01T12:00:00")
    @TableField("monitor_sys_gen_script_last_execute_time")
    private LocalDateTime monitorSysGenScriptLastExecuteTime;

    /**
     * 执行次数
     */
    @Schema(description = "执行次数", example = "10", minimum = "0")
    @TableField("monitor_sys_gen_script_execute_count")
    private Integer monitorSysGenScriptExecuteCount;

    /**
     * 脚本状态枚举
     */
    @Getter
    @Schema(description = "脚本状态枚举")
    public enum ScriptStatus {
        @Schema(description = "禁用")
        DISABLED(0, "禁用"), @Schema(description = "启用")
        ENABLED(1, "启用");

        private final Integer code;
        private final String desc;

        ScriptStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static ScriptStatus fromCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (ScriptStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 脚本类型枚举
     */
    @Schema(description = "脚本类型枚举")
    public enum ScriptType {
        @Schema(description = "Shell脚本")
        SHELL("shell", "Shell脚本", ".sh"), @Schema(description = "批处理脚本")
        BATCH("batch", "批处理脚本", ".bat"), @Schema(description = "PowerShell脚本")
        POWERSHELL("powershell", "PowerShell脚本", ".ps1"), @Schema(description = "Python脚本")
        PYTHON("python", "Python脚本", ".py"), @Schema(description = "JavaScript脚本")
        JAVASCRIPT("javascript", "JavaScript脚本", ".js"), @Schema(description = "Groovy脚本")
        GROOVY("groovy", "Groovy脚本", ".groovy"), @Schema(description = "Lua脚本")
        LUA("lua", "Lua脚本", ".lua"), @Schema(description = "Perl脚本")
        PERL("perl", "Perl脚本", ".pl"), @Schema(description = "Ruby脚本")
        RUBY("ruby", "Ruby脚本", ".rb"), @Schema(description = "PHP脚本")
        PHP("php", "PHP脚本", ".php");

        private final String code;
        private final String desc;
        private final String extension;

        ScriptType(String code, String desc, String extension) {
            this.code = code;
            this.desc = desc;
            this.extension = extension;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public String getExtension() {
            return extension;
        }

        public static ScriptType fromCode(String code) {
            if (code == null) {
                return null;
            }
            for (ScriptType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 获取脚本类型枚举
     */
    public ScriptType getScriptTypeEnum() {
        return ScriptType.fromCode(monitorSysGenScriptType);
    }

    /**
     * 获取脚本状态枚举
     */
    public ScriptStatus getScriptStatusEnum() {
        return ScriptStatus.fromCode(monitorSysGenScriptStatus != null ? monitorSysGenScriptStatus.getCode() : null);
    }

    /**
     * 检查脚本是否启用
     */
    public boolean isEnabled() {
        return ScriptStatus.ENABLED.equals(monitorSysGenScriptStatus);
    }

    /**
     * 获取格式化的标签列表
     */
    public String[] getTagArray() {
        if (monitorSysGenScriptTags == null || monitorSysGenScriptTags.trim().isEmpty()) {
            return new String[0];
        }
        return monitorSysGenScriptTags.split(",");
    }

    /**
     * 设置标签数组
     */
    public void setTagArray(String[] tags) {
        if (tags == null || tags.length == 0) {
            this.monitorSysGenScriptTags = null;
        } else {
            this.monitorSysGenScriptTags = String.join(",", tags);
        }
    }

    /**
     * 获取默认超时时间
     */
    public int getTimeoutOrDefault() {
        return monitorSysGenScriptTimeout != null ? monitorSysGenScriptTimeout : 300;
    }

    /**
     * 获取执行次数
     */
    public int getExecuteCountOrDefault() {
        return monitorSysGenScriptExecuteCount != null ? monitorSysGenScriptExecuteCount : 0;
    }

    /**
     * 增加执行次数
     */
    public void incrementExecuteCount() {
        this.monitorSysGenScriptExecuteCount = getExecuteCountOrDefault() + 1;
        this.monitorSysGenScriptLastExecuteTime = LocalDateTime.now();
    }
}
