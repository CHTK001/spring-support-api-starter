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
 * 限流规则传输对象
 *
 * <p><b>本类不再是数据库实体。</b>限流规则已并入统一策略配置表
 * {@code sys_firewall_config}（{@code type='RATE_LIMIT'}），本类仅作为策略接口的传输结构，
 * 字段名保持与前端契约一致，由
 * {@code com.chua.starter.system.support.PolicyConfigConverter} 与统一表行互转：
 * {@code dimension} 落到同名列，{@code target} 落到 {@code bizKey}，
 * {@code enabled} 落到 {@code status}。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "限流规则")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRateLimitRule extends SysBase implements Serializable {
    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识（对应统一表 sys_firewall_config_id）
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long sysRateLimitRuleId;

    /**
     * 限流维度: interface/ip/account
     */
    @Schema(description = "限流维度: interface/ip/account")
    @NotBlank(message = "限流维度不能为空", groups = {AddGroup.class})
    @Size(max = 20, message = "限流维度最大长度需小于20")
    private String dimension;

    /**
     * 限流目标: 接口路径/IP/账号
     */
    @Schema(description = "限流目标: 接口路径/IP/账号")
    @NotBlank(message = "限流目标不能为空", groups = {AddGroup.class})
    @Size(max = 500, message = "限流目标最大长度需小于500")
    private String target;

    /**
     * QPS
     */
    @Schema(description = "QPS")
    private Integer qps;

    /**
     * 突发容量
     */
    @Schema(description = "突发容量")
    private Integer burstSize;

    /**
     * 是否启用: 1启用 0禁用
     */
    @Schema(description = "是否启用: 1启用 0禁用")
    private Integer enabled;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    private String remark;
}
