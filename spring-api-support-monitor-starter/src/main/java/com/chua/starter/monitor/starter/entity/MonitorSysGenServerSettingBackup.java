package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务器配置备份实体类
 *
 * @author CH
 * @since 2024/12/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_setting_backup")
@ApiModel(value = "MonitorSysGenServerSettingBackup对象", description = "服务器配置备份")
@Schema(description = "服务器配置备份")
public class MonitorSysGenServerSettingBackup extends SysBase {

    /**
     * 备份ID
     */
    @TableId(value = "monitor_sys_gen_server_setting_backup_id", type = IdType.AUTO)
    @ApiModelProperty(value = "备份ID")
    @Schema(description = "备份ID")
    private Long monitorSysGenServerSettingBackupId;

    /**
     * 备份名称
     */
    @TableField(value = "backup_name")
    @ApiModelProperty(value = "备份名称")
    @Schema(description = "备份名称")
    @NotBlank(message = "备份名称不能为空")
    @Size(max = 200, message = "备份名称最大长度要小于 200")
    private String backupName;

    /**
     * 备份描述
     */
    @TableField(value = "backup_description")
    @ApiModelProperty(value = "备份描述")
    @Schema(description = "备份描述")
    private String backupDescription;

    /**
     * 备份类型: MANUAL/AUTO/SCHEDULED
     */
    @TableField(value = "backup_type")
    @ApiModelProperty(value = "备份类型")
    @Schema(description = "备份类型: MANUAL/AUTO/SCHEDULED")
    @NotBlank(message = "备份类型不能为空")
    @Size(max = 20, message = "备份类型最大长度要小于 20")
    private String backupType;

    /**
     * 备份的服务器ID列表（JSON格式）
     */
    @TableField(value = "server_ids")
    @ApiModelProperty(value = "备份的服务器ID列表")
    @Schema(description = "备份的服务器ID列表（JSON格式）")
    private String serverIds;

    /**
     * 备份范围: ALL/SELECTED
     */
    @TableField(value = "backup_scope")
    @ApiModelProperty(value = "备份范围")
    @Schema(description = "备份范围: ALL/SELECTED")
    @NotBlank(message = "备份范围不能为空")
    @Size(max = 20, message = "备份范围最大长度要小于 20")
    private String backupScope;

    /**
     * 备份数据（JSON格式）
     */
    @TableField(value = "backup_data")
    @ApiModelProperty(value = "备份数据")
    @Schema(description = "备份数据（JSON格式）")
    @NotBlank(message = "备份数据不能为空")
    private String backupData;

    /**
     * 备份格式: JSON/YAML
     */
    @TableField(value = "backup_format")
    @ApiModelProperty(value = "备份格式")
    @Schema(description = "备份格式: JSON/YAML")
    @Size(max = 10, message = "备份格式最大长度要小于 10")
    private String backupFormat;

    /**
     * 备份的服务器数量
     */
    @TableField(value = "server_count")
    @ApiModelProperty(value = "备份的服务器数量")
    @Schema(description = "备份的服务器数量")
    private Integer serverCount;

    /**
     * 备份数据大小（字节）
     */
    @TableField(value = "backup_size")
    @ApiModelProperty(value = "备份数据大小")
    @Schema(description = "备份数据大小（字节）")
    private Long backupSize;

    /**
     * 备份状态: SUCCESS/FAILED/PARTIAL
     */
    @TableField(value = "backup_status")
    @ApiModelProperty(value = "备份状态")
    @Schema(description = "备份状态: SUCCESS/FAILED/PARTIAL")
    @Size(max = 20, message = "备份状态最大长度要小于 20")
    private String backupStatus;

    /**
     * 备份错误信息
     */
    @TableField(value = "backup_error")
    @ApiModelProperty(value = "备份错误信息")
    @Schema(description = "备份错误信息")
    private String backupError;

    /**
     * 创建用户
     */
    @TableField(value = "create_user")
    @ApiModelProperty(value = "创建用户")
    @Schema(description = "创建用户")
    @Size(max = 100, message = "创建用户最大长度要小于 100")
    private String createUser;

    /**
     * 备份类型枚举
     */
    public enum BackupType {
        MANUAL("MANUAL", "手动备份"),
        AUTO("AUTO", "自动备份"),
        SCHEDULED("SCHEDULED", "定时备份");

        private final String code;
        private final String description;

        BackupType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 备份范围枚举
     */
    public enum BackupScope {
        ALL("ALL", "全部服务器"),
        SELECTED("SELECTED", "选定服务器");

        private final String code;
        private final String description;

        BackupScope(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 备份状态枚举
     */
    public enum BackupStatus {
        SUCCESS("SUCCESS", "备份成功"),
        FAILED("FAILED", "备份失败"),
        PARTIAL("PARTIAL", "部分成功");

        private final String code;
        private final String description;

        BackupStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
