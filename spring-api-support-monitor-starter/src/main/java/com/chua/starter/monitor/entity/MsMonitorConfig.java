package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局配置实体，记录监控模块的键值对配置项。
 *
 * @since 2026-08-11
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ms_monitor_config")
@ApiModel(description = "全局配置")
@Schema(description = "全局配置")
public class MsMonitorConfig extends SysBase {

    /**
      * 配置主键标识，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    /** 标识 */
    private Long id;

    /**
      * 配置键，例如 监控.扫描.间隔
     */
    @TableField("config_key")
    @ApiModelProperty("配置键")
    @Schema(description = "配置键")
    /** 配置密钥 */
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    @ApiModelProperty("配置值")
    @Schema(description = "配置值")
    /** 配置值 */
    private String configValue;

    /**
      * 配置类型，例如 字符串/数字/布尔值
     */
    @TableField("config_type")
    @ApiModelProperty("配置类型")
    @Schema(description = "配置类型")
    /** 配置类型 */
    private String configType;

    /**
      * 配置分组，例如 扫描/ALERT/NETWORK
     */
    @TableField("config_group")
    @ApiModelProperty("配置分组")
    @Schema(description = "配置分组")
    /** 配置分组 */
    private String configGroup;

    /**
     * 逻辑删除标记，0=未删除，1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @ApiModelProperty("逻辑删除")
    @Schema(description = "逻辑删除")
    /** 删除标记 */
    private Integer deleted;
}
