package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 前端异常上报日志表
 *
 * <p>承接前端全局异常监控（monitor worker）批量上报的运行时错误记录，
 * 覆盖 JS 运行时错误、未处理 Promise 拒绝、资源加载失败等场景。</p>
 *
 * @since 2026/8/8
 * @author CH
 */
@Schema(description = "前端异常上报日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_error_log")
public class SysErrorLog extends SysBase {

    /**
     * 异常日志ID
     */
    @TableId(value = "sys_error_log_id", type = IdType.AUTO)
    @Schema(description = "异常日志ID")
    /** SYS错误日志ID */
    private Long sysErrorLogId;

    /**
     * 去重指纹(message + source + 行列号归一化)
     */
    @TableField(value = "sys_error_key")
    @Schema(description = "去重指纹")
    /** SYS错误密钥 */
    private String sysErrorKey;

    /**
     * 异常来源: js:JS运行时错误 rejection:未处理Promise拒绝 resource:资源加载失败 manual:手动捕获
     */
    @TableField(value = "sys_error_source")
    @Schema(description = "异常来源")
    /** SYS错误来源 */
    private String sysErrorSource;

    /**
     * 异常消息
     */
    @TableField(value = "sys_error_message")
    @Schema(description = "异常消息")
    /** SYS错误消息 */
    private String sysErrorMessage;

    /**
     * 异常堆栈
     */
    @TableField(value = "sys_error_stack")
    @Schema(description = "异常堆栈")
    /** SYS错误栈 */
    private String sysErrorStack;

    /**
     * 出错文件URL
     */
    @TableField(value = "sys_error_filename")
    @Schema(description = "出错文件URL")
    /** SYS错误filename */
    private String sysErrorFilename;

    /**
     * 出错行号
     */
    @TableField(value = "sys_error_lineno")
    @Schema(description = "出错行号")
    /** SYS错误lineno */
    private Integer sysErrorLineno;

    /**
     * 出错列号
     */
    @TableField(value = "sys_error_colno")
    @Schema(description = "出错列号")
    /** SYS错误colno */
    private Integer sysErrorColno;

    /**
     * 资源加载失败的目标URL(仅资源类异常)
     */
    @TableField(value = "sys_error_resource_url")
    @Schema(description = "资源加载失败的目标URL")
    /** SYS错误resourceURL */
    private String sysErrorResourceUrl;

    /**
     * 资源元素标签(仅资源类异常, 如IMG/SCRIPT/LINK)
     */
    @TableField(value = "sys_error_tag_name")
    @Schema(description = "资源元素标签")
    /** SYS错误标签名称 */
    private String sysErrorTagName;

    /**
     * 首次发生时间(毫秒时间戳)
     */
    @TableField(value = "sys_error_first_at")
    @Schema(description = "首次发生时间(毫秒时间戳)")
    /** SYS错误首个AT */
    private Long sysErrorFirstAt;

    /**
     * 去重窗口内累计发生次数
     */
    @TableField(value = "sys_error_count")
    @Schema(description = "去重窗口内累计发生次数")
    /** SYS错误数量 */
    private Integer sysErrorCount;

    /**
     * 客户端上报时间(毫秒时间戳)
     */
    @TableField(value = "sys_error_report_time")
    @Schema(description = "客户端上报时间(毫秒时间戳)")
    /** SYS错误report时间 */
    private Long sysErrorReportTime;
}
