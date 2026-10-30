package com.chua.starter.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 租户表
 *
 * @since 2024/12/06
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "租户表")
@Schema(description = "租户表")
@Data
@TableName(value = "sys_tenant")
public class SysTenant extends SysBase {

    /**
     * 租户标识（同时作为租户编码使用）
     */
    @TableId(value = "sys_tenant_id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "租户ID/租户编码")
    @Schema(description = "租户ID/租户编码")
    @NotBlank(message = "租户ID不能为空", groups = {UpdateGroup.class})
    /**
     * systenantid
    */
    private String sysTenantId;

    /**
     * 名称
     */
    @TableField(value = "sys_tenant_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @NotBlank(message = "租户名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    /**
     * systenant名称
    */
    private String sysTenantName;

    /**
     * 账号
     */
    @TableField(value = "sys_tenant_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    /**
     * systenantusername
    */
    private String sysTenantUsername;

    /**
     * 租户端登录口令
     *
     * <p>{@code WRITE_ONLY}：请求体里能读进来（save/update 要用），但<b>永远不会出现在响应里</b>。
     * 库中存的是 {@code TenantPasswordCipher} 加密的密文，租户同步走
     * {@code TenantServerSyncPublisher} 自己的 Map 转换（不经 Jackson），所以
     * 这里屏蔽序列化不影响同步下发。</p>
     *
     * <p>不加这个注解时，{@code /v2/tenant/list}、{@code page}、{@code get} 会把口令原样回显，
     * 哪怕值已是密文也不该外泄（密文可被拿去离线爆破，且暴露了密钥存在性）。</p>
     */
    @TableField(value = "sys_tenant_password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    private String sysTenantPassword;

    /**
     * 签名
     */
    @TableField(value = "sys_tenant_sign")
    @ApiModelProperty(value = "签名")
    @Schema(description = "签名")
    /**
     * systenantsign
    */
    private String sysTenantSign;

    /**
     * 公司
     */
    @TableField(value = "sys_tenant_corporation")
    @ApiModelProperty(value = "公司")
    @Schema(description = "公司")
    /**
     * systenantcorporation
    */
    private String sysTenantCorporation;

    /**
     * 是否删除; 0:正常
     */
    @TableField(value = "sys_tenant_delete")
    @ApiModelProperty(value = "是否删除; 0:正常")
    @Schema(description = "是否删除; 0:正常")
    @TableLogic(value = "0", delval = "1")
    /**
     * systenantdelete
    */
    private Integer sysTenantDelete;

    /**
     * 是否禁用; 0:正常
     */
    @TableField(value = "sys_tenant_status")
    @ApiModelProperty(value = "是否禁用; 0:正常")
    @Schema(description = "是否禁用; 0:正常")
    /**
     * systenant状态
    */
    private Integer sysTenantStatus;

    /**
     * 联系人
     */
    @TableField(value = "sys_tenant_contact")
    @ApiModelProperty(value = "联系人")
    @Schema(description = "联系人")
    /**
     * systenantcontact
    */
    private String sysTenantContact;

    /**
     * 手机号
     */
    @TableField(value = "sys_tenant_phone")
    @ApiModelProperty(value = "手机号")
    @Schema(description = "手机号")
    /**
     * systenantphone
    */
    private String sysTenantPhone;

    /**
     * 地址
     */
    @TableField(value = "sys_tenant_address")
    @ApiModelProperty(value = "地址")
    @Schema(description = "地址")
    /**
     * systenant地址
    */
    private String sysTenantAddress;

    /**
     * 邮件
     */
    @TableField(value = "sys_tenant_email")
    @ApiModelProperty(value = "邮件")
    @Schema(description = "邮件")
    /**
     * systenantemail
    */
    private String sysTenantEmail;

    /**
     * 唯一编码（已废弃，使用 systenantid 代替）
     */
    @Deprecated
    @TableField(value = "sys_tenant_code")
    @ApiModelProperty(value = "唯一编码（已废弃）")
    @Schema(description = "唯一编码（已废弃，使用 sysTenantId 代替）")
    /**
     * systenant代码
    */
    private String sysTenantCode;

    /**
     * 租户对于配置唯一标识
     */
    @TableField(value = "sys_tenant_gid")
    @ApiModelProperty(value = "租户对于配置唯一ID")
    @Schema(description = "租户对于配置唯一ID")
    /**
     * systenantgid
    */
    private String sysTenantGid;

    /**
     * 租户访问地址
     */
    @TableField(value = "sys_tenant_home_url")
    @ApiModelProperty(value = "租户访问地址")
    @Schema(description = "租户访问地址")
    /**
     * systenanthomeurl
    */
    private String sysTenantHomeUrl;

    /**
     * 备注
     */
    @TableField(value = "sys_tenant_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    /**
     * systenantremark
    */
    private String sysTenantRemark;

    /**
     * 主库地址（租户专属数据库实例地址）
     */
    @TableField(value = "sys_tenant_host")
    @ApiModelProperty(value = "主库地址")
    @Schema(description = "主库地址")
    /**
     * systenanthost
     */
    private String sysTenantHost;

    /**
     * 主库端口（租户专属数据库实例端口）
     */
    @TableField(value = "sys_tenant_port")
    @ApiModelProperty(value = "主库端口")
    @Schema(description = "主库端口")
    /**
     * systenantport
     */
    private Integer sysTenantPort;

    /**
     * 主库账号（租户专属数据库登录账号）
     */
    @TableField(value = "sys_tenant_username_sql")
    @ApiModelProperty(value = "数据库账号")
    @Schema(description = "数据库账号")
    /**
     * systenantsqlusername
     */
    private String sysTenantSqlUsername;

    /**
     * 主库口令（租户专属数据库登录口令，密文落库）
     */
    @TableField(value = "sys_tenant_password_sql")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ApiModelProperty(value = "数据库口令")
    @Schema(description = "数据库口令")
    /**
     * systenantsqlpassword
     */
    private String sysTenantSqlPassword;

    /**
     * 数据库名（租户专属数据库名）
     */
    @TableField(value = "sys_tenant_dbname")
    @ApiModelProperty(value = "数据库名")
    @Schema(description = "数据库名")
    /**
     * systenantdbname
     */
    private String sysTenantDbname;

    /**
     * 主库IP（租户专属数据库实例 IP）
     */
    @TableField(value = "sys_tenant_ip")
    @ApiModelProperty(value = "主库IP")
    @Schema(description = "主库IP")
    /**
     * systenantip
     */
    private String sysTenantIp;

    /**
     * 租户 - 服务标识列表
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "租户 - 服务ID列表")
    @Schema(description = "租户 - 服务ID列表")
    /**
     * 服务标识
    */
    private List<Integer> serviceIds;

    /**
     * 租户 - 服务列表
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "租户 - 服务列表")
    @Schema(description = "租户 - 服务列表")
    /**
     * systenant服务
    */
    private List<SysTenantService> sysTenantService;
}
