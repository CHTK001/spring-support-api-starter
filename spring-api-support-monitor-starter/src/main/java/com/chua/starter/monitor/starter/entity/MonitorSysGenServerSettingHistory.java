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

import java.time.LocalDateTime;

/**
 * 服务器配置变更历史实体类
 *
 * @author CH
 * @since 2024/12/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_setting_history")
@ApiModel(value = "MonitorSysGenServerSettingHistory对象", description = "服务器配置变更历史")
@Schema(description = "服务器配置变更历史")
public class MonitorSysGenServerSettingHistory extends SysBase {

    /**
     * 历史记录ID
     */
    @TableId(value = "monitor_sys_gen_server_setting_history_id", type = IdType.AUTO)
    @ApiModelProperty(value = "历史记录ID")
    @Schema(description = "历史记录ID")
    private Long monitorSysGenServerSettingHistoryId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 配置ID
     */
    @TableField(value = "monitor_sys_gen_server_setting_id")
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    private Long monitorSysGenServerSettingId;

    /**
     * 变更类型: CREATE/UPDATE/DELETE
     */
    @TableField(value = "change_type")
    @ApiModelProperty(value = "变更类型")
    @Schema(description = "变更类型: CREATE/UPDATE/DELETE")
    @NotNull(message = "变更类型不能为空")
    @Size(max = 20, message = "变更类型最大长度要小于 20")
    private String changeType;

    /**
     * 变更描述
     */
    @TableField(value = "change_description")
    @ApiModelProperty(value = "变更描述")
    @Schema(description = "变更描述")
    private String changeDescription;

    /**
     * 变更用户
     */
    @TableField(value = "change_user")
    @ApiModelProperty(value = "变更用户")
    @Schema(description = "变更用户")
    @Size(max = 100, message = "变更用户最大长度要小于 100")
    private String changeUser;

    /**
     * 变更时间
     */
    @TableField(value = "change_time")
    @ApiModelProperty(value = "变更时间")
    @Schema(description = "变更时间")
    @NotNull(message = "变更时间不能为空")
    private LocalDateTime changeTime;

    /**
     * 配置快照（JSON格式）
     */
    @TableField(value = "setting_snapshot")
    @ApiModelProperty(value = "配置快照")
    @Schema(description = "配置快照（JSON格式）")
    private String settingSnapshot;

    /**
     * 变更字段列表（JSON格式）
     */
    @TableField(value = "changed_fields")
    @ApiModelProperty(value = "变更字段列表")
    @Schema(description = "变更字段列表（JSON格式）")
    private String changedFields;

    /**
     * 变更前的值（JSON格式）
     */
    @TableField(value = "old_values")
    @ApiModelProperty(value = "变更前的值")
    @Schema(description = "变更前的值（JSON格式）")
    private String oldValues;

    /**
     * 变更后的值（JSON格式）
     */
    @TableField(value = "new_values")
    @ApiModelProperty(value = "变更后的值")
    @Schema(description = "变更后的值（JSON格式）")
    private String newValues;

    /**
     * 创建历史记录
     */
    public static MonitorSysGenServerSettingHistory createHistory(
            Integer serverId,
            Long settingId,
            ChangeType changeType,
            String description,
            String user) {
        MonitorSysGenServerSettingHistory history = new MonitorSysGenServerSettingHistory();
        history.setMonitorSysGenServerId(serverId);
        history.setMonitorSysGenServerSettingId(settingId);
        history.setChangeType(changeType.getCode());
        history.setChangeDescription(description);
        history.setChangeUser(user);
        history.setChangeTime(LocalDateTime.now());
        return history;
    }

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        CREATE("CREATE", "创建"),
        UPDATE("UPDATE", "更新"),
        DELETE("DELETE", "删除"),
        COPY("COPY", "复制"),
        RESET("RESET", "重置"),
        IMPORT("IMPORT", "导入"),
        TEMPLATE("TEMPLATE", "应用模板");

        private final String code;
        private final String description;

        ChangeType(String code, String description) {
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
