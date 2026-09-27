package com.chua.starter.entity.system;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * IP 黑白名单传输对象
 *
 * <p><b>本类不再是数据库实体。</b>IP 黑白名单已并入统一策略配置表
 * {@code sys_policy_config}（{@code type='IP'}），本类仅作为策略接口的传输结构，
 * 字段名保持与前端契约一致，由
 * {@code com.chua.starter.system.support.PolicyConfigConverter} 与统一表行互转。</p>
 *
 * <p>注意本类沿用旧 IP 表的取值方向：{@code sysIpType} 0白名单/1黑名单、
 * {@code sysIpStatus} 0启用/1禁用；统一表内部已归一为 1黑/2白、1启用/0禁用，
 * 转换由 Converter 负责，调用方无需自行翻转。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Schema(description = "IP黑白名单")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysIpBlackwhite extends SysBase implements Serializable {
    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识（对应统一表 sys_policy_config_id）
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Integer sysIpId;

    /**
     * 类型: 0白名单 1黑名单
     */
    @Schema(description = "类型: 0白名单 1黑名单")
    @NotNull(message = "类型不能为空", groups = {AddGroup.class})
    private Integer sysIpType;

    /**
     * IP地址或CIDR段
     */
    @Schema(description = "IP地址或CIDR段")
    @Size(max = 255, message = "IP地址最大长度需小于255")
    @NotBlank(message = "IP地址不能为空", groups = {AddGroup.class})
    private String sysIpIp;

    /**
     * 状态: 0启用 1禁用
     */
    @Schema(description = "状态: 0启用 1禁用")
    private Integer sysIpStatus;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    private String sysIpRemark;
}
