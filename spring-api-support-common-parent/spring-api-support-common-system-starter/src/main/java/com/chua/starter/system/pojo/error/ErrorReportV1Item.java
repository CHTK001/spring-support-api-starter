package com.chua.starter.system.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 前端异常上报单条记录
 *
 * @since 2026/8/8
 * @author CH
 */
@Schema(description = "前端异常上报单条记录")
public record ErrorReportV1Item(
        /**
         * 去重指纹(消息 + 源 + 行列号归一化)
         */
        @Schema(description = "去重指纹")
        /**
         * 密钥
         */
        String key,
        /**
         * 异常来源: js:JS运行时错误 rejection:未处理Promise拒绝 resource:资源加载失败 manual:手动捕获
         */
        @Schema(description = "异常来源")
        /**
         * 来源
         */
        String source,
        /**
         * 异常消息
         */
        @Schema(description = "异常消息")
        /**
         * 消息
         */
        String message,
        /**
         * 异常堆栈
         */
        @Schema(description = "异常堆栈")
        /**
         * 栈
         */
        String stack,
        /**
         * 出错文件URL
         */
        @Schema(description = "出错文件URL")
        /**
         * 文件名
         */
        String filename,
        /**
         * 出错行号
         */
        @Schema(description = "出错行号")
        /**
         * Lineno
         */
        Integer lineno,
        /**
         * 出错列号
         */
        @Schema(description = "出错列号")
        /**
         * Colno
         */
        Integer colno,
        /**
         * 资源加载失败的目标URL(仅资源类异常)
         */
        @Schema(description = "资源加载失败的目标URL")
        /**
         * resourceurl
         */
        String resourceUrl,
        /**
         * 资源元素标签(仅资源类异常, 如IMG/script/链接)
         */
        @Schema(description = "资源元素标签")
        /**
         * 标签名称
         */
        String tagName,
        /**
         * 首次发生时间(毫秒时间戳)
         */
        @Schema(description = "首次发生时间(毫秒时间戳)")
        /**
         * 首个AT
         */
        Long firstAt,
        /**
         * 去重窗口内累计发生次数
         */
        @Schema(description = "去重窗口内累计发生次数")
        /**
         * 数量
         */
        Integer count
) {
}
