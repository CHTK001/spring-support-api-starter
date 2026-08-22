package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 密钥
 * @author CH
 */
@Schema(description = "密钥")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_secret")
public class SysSecret extends SysBase {

    /**
     * 密钥主键ID
     */
    @TableId(value = "sys_secret_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysSecretId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 密钥标识
     */
    @TableField(value = "sys_secret_key")
    @Schema(description = "密钥标识")
    private String sysSecretKey;

    /**
     * 密钥值
     */
    @TableField(value = "sys_secret_value")
    @Schema(description = "密钥值")
    private String sysSecretValue;
}
