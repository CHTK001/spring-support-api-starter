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
 * 账号黑白名单传输对象
 *
 * <p><b>本类不再是数据库实体。</b>账号黑白名单已并入统一策略配置表
 * {@code sys_policy_config}（{@code type='ACCOUNT'}），本类仅作为策略接口的传输结构，
 * 字段名保持与前端契约一致，由
 * {@code com.chua.starter.system.support.PolicyConfigConverter} 与统一表行互转：
 * {@code account} 落到 {@code bizKey}，{@code type} 落到 {@code listType}，
 * {@code status} 落到 {@code status}（本类取值方向与统一表一致，无需翻转）。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "账号黑白名单")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAccountBlackwhite extends SysBase implements Serializable {
    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识（对应统一表 sys_policy_config_id）
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long sysAccountBwId;

    /**
     * 账号
     */
    @Schema(description = "账号")
    @NotBlank(message = "账号不能为空", groups = {AddGroup.class})
    @Size(max = 100, message = "账号最大长度需小于100")
    private String account;

    /**
     * 类型: 1黑名单 2白名单
     */
    @Schema(description = "类型: 1黑名单 2白名单")
    @NotNull(message = "类型不能为空", groups = {AddGroup.class})
    private Integer type;

    /**
     * 状态: 1启用 0禁用
     */
    @Schema(description = "状态: 1启用 0禁用")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    private String remark;
}
