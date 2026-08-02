package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统配置变更历史表
 *
 * @author CH
 * @since 2024/12/20
 */
@Schema(description = "系统配置变更历史表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting_history")
public class SysSettingHistory extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 历史记录ID
     */
    @TableId(value = "sys_setting_history_id", type = IdType.AUTO)
    @Schema(description = "历史记录ID")
    private Long sysSettingHistoryId;

    /**
     * 关联的配置ID
     */
    @TableField(value = "sys_setting_id")
    @Schema(description = "关联的配置ID")
    private Integer sysSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @Schema(description = "配置名称")
    private String sysSettingName;

    /**
     * 配置所属分组
     */
    @TableField(value = "sys_setting_group")
    @Schema(description = "配置所属分组")
    private String sysSettingGroup;

    /**
     * 变更前的值
     */
    @TableField(value = "sys_setting_old_value")
    @Schema(description = "变更前的值")
    private String sysSettingOldValue;

    /**
     * 变更后的值
     */
    @TableField(value = "sys_setting_new_value")
    @Schema(description = "变更后的值")
    private String sysSettingNewValue;

    /**
     * 操作类型: CREATE-新增 UPDATE-更新 DELETE-删除 IMPORT-导入 ROLLBACK-回滚
     */
    @TableField(value = "sys_setting_operation")
    @Schema(description = "操作类型: CREATE-新增 UPDATE-更新 DELETE-删除 IMPORT-导入 ROLLBACK-回滚")
    private String sysSettingOperation;

    /**
     * 操作人ID
     */
    @TableField(value = "sys_setting_operator_id")
    @Schema(description = "操作人ID")
    private String sysSettingOperatorId;

    /**
     * 操作人名称
     */
    @TableField(value = "sys_setting_operator_name")
    @Schema(description = "操作人名称")
    private String sysSettingOperatorName;

    /**
     * 变更备注
     */
    @TableField(value = "sys_setting_remark")
    @Schema(description = "变更备注")
    private String sysSettingRemark;

    /**
     * 批次号（用于批量操作）
     */
    @TableField(value = "sys_setting_batch_no")
    @Schema(description = "批次号（用于批量操作和回滚）")
    private String sysSettingBatchNo;
}
