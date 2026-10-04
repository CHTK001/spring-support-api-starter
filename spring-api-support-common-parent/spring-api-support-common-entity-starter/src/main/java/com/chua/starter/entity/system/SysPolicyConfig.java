package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 统一策略配置表
 *
 * <p>一行 = 一条策略配置，取代原先分散的 IP 黑白名单、限流规则、账号黑白名单三张表。
 * 通过 {@code type} 判别配置种类，配合通用宽列与 {@code config} JSON 兜底承载各类型专有字段。</p>
 *
 * <p>各类型对列的用法：</p>
 * <ul>
 *   <li>{@code IP} / {@code ACCOUNT}：{@code bizKey} 为 IP 或账号，{@code listType} 区分黑白名单</li>
 *   <li>{@code RATE_LIMIT}：{@code dimension} 为限流维度，{@code bizKey} 为匹配目标，
 *       {@code qps} / {@code burstSize} 生效</li>
 *   <li>{@code GRAY}：{@code bizKey} 为灰度键，{@code weight} 为流量权重，
 *       {@code config} 存灰度命中策略</li>
 *   <li>{@code FEATURE}：{@code bizKey} 为功能开关键，{@code status} 即开关状态</li>
 * </ul>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "统一策略配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_firewall_config")
public class SysPolicyConfig extends SysBase implements Serializable {
    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 策略类型: IP/RATE_LIMIT/ACCOUNT/GRAY/FEATURE
     */
    public static final String TYPE_IP = "IP";
    /**
     * 限流规则类型
     */
    public static final String TYPE_RATE_LIMIT = "RATE_LIMIT";
    /**
     * 账号名单类型
     */
    public static final String TYPE_ACCOUNT = "ACCOUNT";
    /**
     * 灰度规则类型
     */
    public static final String TYPE_GRAY = "GRAY";
    /**
     * 功能开关类型
     */
    public static final String TYPE_FEATURE = "FEATURE";

    /**
     * 接口停用 / 转发
     */
    public static final String TYPE_API_ROUTE = "API_ROUTE";

    /**
     * 消息头拦截（爬虫 / 自动化客户端）
     *
     * <p>{@code bizKey} = 消息头名（主要是 {@code User-Agent}），
     * {@code config} = 规则 JSON（matchType/patterns/action/status/message）。</p>
     */
    public static final String TYPE_HEADER = "HEADER";
    /**
     * 黑名单
     */
    public static final int LIST_BLACK = 1;
    /**
     * 白名单
     */
    public static final int LIST_WHITE = 2;
    /**
     * 非名单类配置占位值
     */
    public static final int LIST_NONE = 0;
    /**
     * 启用
     */
    public static final int STATUS_ENABLED = 1;
    /**
     * 禁用
     */
    public static final int STATUS_DISABLED = 0;

    /**
     * 主键标识
     */
    @TableId(value = "sys_firewall_config_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long sysPolicyConfigId;

    /**
     * 策略类型
     */
    @TableField(value = "type")
    @Schema(description = "策略类型: IP/RATE_LIMIT/ACCOUNT/GRAY/FEATURE")
    @NotBlank(message = "策略类型不能为空", groups = {AddGroup.class})
    @Size(max = 32, message = "策略类型最大长度需小于32")
    private String type;

    /**
     * 限流维度: interface/ip/account; 其它类型填空串
     */
    @TableField(value = "dimension")
    @Schema(description = "限流维度: interface/ip/account; 其它类型填空串")
    @Size(max = 20, message = "限流维度最大长度需小于20")
    private String dimension;

    /**
     * 名单方向: 1黑名单 2白名单; 其它类型填0
     */
    @TableField(value = "list_type")
    @Schema(description = "名单方向: 1黑名单 2白名单; 其它类型填0")
    private Integer listType;

    /**
     * 业务键: IP值/账号/匹配目标/灰度键/功能键
     */
    @TableField(value = "biz_key")
    @Schema(description = "业务键: IP值/账号/匹配目标/灰度键/功能键")
    @NotBlank(message = "业务键不能为空", groups = {AddGroup.class})
    @Size(max = 255, message = "业务键最大长度需小于255")
    private String bizKey;

    /**
     * 宽列值: 灰度目标/命中对象等
     */
    @TableField(value = "biz_value")
    @Schema(description = "宽列值: 灰度目标/命中对象等")
    @Size(max = 1000, message = "宽列值最大长度需小于1000")
    private String bizValue;

    /**
     * 状态: 1启用 0禁用
     */
    @TableField(value = "status")
    @Schema(description = "状态: 1启用 0禁用")
    private Integer status;

    /**
     * QPS: 仅 RATE_LIMIT 使用
     */
    @TableField(value = "qps")
    @Schema(description = "QPS: 仅 RATE_LIMIT 使用")
    private Integer qps;

    /**
     * 突发容量: 仅 RATE_LIMIT 使用
     */
    @TableField(value = "burst_size")
    @Schema(description = "突发容量: 仅 RATE_LIMIT 使用")
    private Integer burstSize;

    /**
     * 权重/百分比: 仅 GRAY 使用
     */
    @TableField(value = "weight")
    @Schema(description = "权重/百分比: 仅 GRAY 使用")
    private Integer weight;

    /**
     * 排序值, 越小越靠前
     */
    @TableField(value = "sort")
    @Schema(description = "排序值, 越小越靠前")
    private Integer sort;

    /**
     * 类型特有配置(JSON)
     */
    @TableField(value = "config")
    @Schema(description = "类型特有配置(JSON)")
    private String config;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description = "备注")
    @Size(max = 1000, message = "备注最大长度需小于1000")
    private String remark;
}
