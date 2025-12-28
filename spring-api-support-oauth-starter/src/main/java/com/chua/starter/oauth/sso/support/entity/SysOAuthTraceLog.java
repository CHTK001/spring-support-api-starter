package com.chua.starter.oauth.sso.support.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * OAuth追踪日志实体类
 * 记录认证请求的完整链路追踪信息
 *
 * @author CH
 * @since 2025/01/01
 */
@Data
@TableName("sys_oauth_trace_log")
@Schema(description = "OAuth追踪日志")
public class SysOAuthTraceLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long sysTraceLogId;

    /**
     * 追踪ID - 贯穿客户端和服务端的唯一标识
     */
    @Schema(description = "追踪ID")
    private String sysTraceId;

    /**
     * 操作类型 - AUTH:认证, LOGIN:登录, LOGOUT:登出, REFRESH:刷新令牌, UPGRADE:升级令牌
     */
    @Schema(description = "操作类型")
    private String sysOperationType;

    /**
     * 客户端应用名
     */
    @Schema(description = "客户端应用名")
    private String sysClientAppName;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP")
    private String sysClientIp;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String sysUserId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String sysUsername;

    /**
     * 请求参数 - JSON格式，敏感字段已脱敏
     */
    @Schema(description = "请求参数(已脱敏)")
    private String sysRequestParams;

    /**
     * 响应码
     */
    @Schema(description = "响应码")
    private Integer sysResponseCode;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean sysSuccess;

    /**
     * 总耗时(毫秒)
     */
    @Schema(description = "总耗时(毫秒)")
    private Long sysTotalCostMs;

    /**
     * Span详情 - JSON数组
     */
    @Schema(description = "Span详情")
    private String sysSpansDetail;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String sysErrorMessage;

    /**
     * 请求URI
     */
    @Schema(description = "请求URI")
    private String sysRequestUri;

    /**
     * AccessKey(已脱敏)
     */
    @Schema(description = "AccessKey(已脱敏)")
    private String sysAccessKey;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
}
