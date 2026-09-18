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
 * @since 2024/12/20
 * @author CH
 */
@Schema(description = "系统配置变更历史表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting_history")
public class SysSettingHistory extends SysBase implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
    * 历史记录标识
     */
    @TableId(value = "sys_setting_history_id", type = IdType.AUTO)
    @Schema(description = "历史记录ID")
    /** SYS设置历史id */
    private Long sysSettingHistoryId;

    /**
    * 关联的配置标识
     */
    @TableField(value = "sys_setting_id")
    @Schema(description = "关联的配置ID")
    /** SYS设置标识 */
    private Integer sysSettingId;

    /**
    * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @Schema(description = "配置名称")
    /** SYS设置名称 */
    private String sysSettingName;

    /**
    * 配置所属分组
     */
    @TableField(value = "sys_setting_group")
    @Schema(description = "配置所属分组")
    /** SYS设置分组 */
    private String sysSettingGroup;

    /**
    * 变更前的值
     */
    @TableField(value = "sys_setting_old_value")
    @Schema(description = "变更前的值")
    /** SYS设置旧值 */
    private String sysSettingOldValue;

    /**
    * 变更后的值
     */
    @TableField(value = "sys_setting_new_value")
    @Schema(description = "变更后的值")
    /** SYS设置新值 */
    private String sysSettingNewValue;

    /**
    * 操作类型: 创建-新增 更新-更新 删除-删除 导入-导入 ROLLBACK-回滚
     */
    @TableField(value = "sys_setting_operation")
    @Schema(description = "操作类型: CREATE-新增 UPDATE-更新 DELETE-删除 IMPORT-导入 ROLLBACK-回滚")
    /** SYS设置operation */
    private String sysSettingOperation;

    /**
    * 操作人标识
     */
    @TableField(value = "sys_setting_operator_id")
    @Schema(description = "操作人ID")
    /** SYS设置操作符id */
    private String sysSettingOperatorId;

    /**
    * 操作人名称
     */
    @TableField(value = "sys_setting_operator_name")
    @Schema(description = "操作人名称")
    /** SYS设置操作符名称 */
    private String sysSettingOperatorName;

    /**
    * 变更备注
     */
    @TableField(value = "sys_setting_remark")
    @Schema(description = "变更备注")
    /** SYS设置评论 */
    private String sysSettingRemark;

    /**
    * 批次号（用于批量操作）
     */
    @TableField(value = "sys_setting_batch_no")
    @Schema(description = "批次号（用于批量操作和回滚）")
    /** SYS设置batchno */
    private String sysSettingBatchNo;
}
