package com.chua.starter.system.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前端异常上报单条记录
 *
 * @author CH
 * @since 2026/8/8
 */
@Data
@Schema(description = "前端异常上报单条记录")
public class ErrorReportV1Item {

    /**
     * 去重指纹(message + source + 行列号归一化)
     */
    @Schema(description = "去重指纹")
    private String key;

    /**
     * 异常来源: js:JS运行时错误 rejection:未处理Promise拒绝 resource:资源加载失败 manual:手动捕获
     */
    @Schema(description = "异常来源")
    private String source;

    /**
     * 异常消息
     */
    @Schema(description = "异常消息")
    private String message;

    /**
     * 异常堆栈
     */
    @Schema(description = "异常堆栈")
    private String stack;

    /**
     * 出错文件URL
     */
    @Schema(description = "出错文件URL")
    private String filename;

    /**
     * 出错行号
     */
    @Schema(description = "出错行号")
    private Integer lineno;

    /**
     * 出错列号
     */
    @Schema(description = "出错列号")
    private Integer colno;

    /**
     * 资源加载失败的目标URL(仅资源类异常)
     */
    @Schema(description = "资源加载失败的目标URL")
    private String resourceUrl;

    /**
     * 资源元素标签(仅资源类异常, 如IMG/SCRIPT/LINK)
     */
    @Schema(description = "资源元素标签")
    private String tagName;

    /**
     * 首次发生时间(毫秒时间戳)
     */
    @Schema(description = "首次发生时间(毫秒时间戳)")
    private Long firstAt;

    /**
     * 去重窗口内累计发生次数
     */
    @Schema(description = "去重窗口内累计发生次数")
    private Integer count;
}
