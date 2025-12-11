package com.chua.starter.monitor.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Hotspot Agent 上报数据
 * 用于接收 Hotspot Agent 上报的监控数据
 *
 * @author CH
 * @since 2024/12/11
 * @version 4.0.0.34
 */
@Data
public class HotspotReportData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上报类型
     * TRACE-链路追踪, LOG-日志, SQL-SQL监控, SERVER-服务监控,
     * EXCEPTION-异常, PERFORMANCE-性能, CACHE-缓存, MQ-消息队列
     */
    private String reportType;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用端口
     */
    private Integer appPort;

    /**
     * 应用主机
     */
    private String appHost;

    /**
     * 上报时间戳
     */
    private Long timestamp;

    /**
     * 上报数据
     */
    private Object reportData;

    /**
     * 链路追踪数据
     */
    @Data
    public static class TraceData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 链路ID
         */
        private String linkId;

        /**
         * 父级ID
         */
        private String parentId;

        /**
         * Span ID
         */
        private String spanId;

        /**
         * 线程名称
         */
        private String threadName;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 方法名称
         */
        private String method;

        /**
         * 描述
         */
        private String description;

        /**
         * 协议
         */
        private String protocol;

        /**
         * 地址
         */
        private String address;

        /**
         * 状态码
         */
        private String code;

        /**
         * 进入时间
         */
        private Long enterTime;

        /**
         * 退出时间
         */
        private Long exitTime;

        /**
         * 耗时（毫秒）
         */
        private Long costTime;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 请求参数
         */
        private List<String> params;

        /**
         * 请求头
         */
        private List<String> headers;

        /**
         * 子 Span 列表
         */
        private List<TraceData> children;

        /**
         * 服务实例列表
         */
        private List<Map<String, Object>> instances;
    }

    /**
     * SQL 监控数据
     */
    @Data
    public static class SqlData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 链路ID
         */
        private String linkId;

        /**
         * 数据库名称
         */
        private String database;

        /**
         * SQL 语句
         */
        private String sql;

        /**
         * 执行时间（毫秒）
         */
        private Long executeTime;

        /**
         * 影响行数
         */
        private Integer affectedRows;

        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 错误信息
         */
        private String error;
    }

    /**
     * 日志监控数据
     */
    @Data
    public static class LogData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 链路ID
         */
        private String linkId;

        /**
         * 日志级别
         */
        private String level;

        /**
         * 日志内容
         */
        private String message;

        /**
         * 日志时间
         */
        private Long timestamp;

        /**
         * 线程名称
         */
        private String threadName;

        /**
         * 类名
         */
        private String className;

        /**
         * 方法名
         */
        private String methodName;

        /**
         * 行号
         */
        private Integer lineNumber;
    }

    /**
     * 异常监控数据
     */
    @Data
    public static class ExceptionData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 链路ID
         */
        private String linkId;

        /**
         * 异常类型
         */
        private String exceptionType;

        /**
         * 异常消息
         */
        private String message;

        /**
         * 堆栈信息
         */
        private List<String> stackTrace;

        /**
         * 发生时间
         */
        private Long timestamp;

        /**
         * 类名
         */
        private String className;

        /**
         * 方法名
         */
        private String methodName;
    }

    /**
     * 服务实例数据
     */
    @Data
    public static class ServerData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 服务名称
         */
        private String name;

        /**
         * 源主机
         */
        private String sourceHost;

        /**
         * 源端口
         */
        private Integer sourcePort;

        /**
         * 目标主机
         */
        private String targetHost;

        /**
         * 目标端口
         */
        private Integer targetPort;

        /**
         * 协议
         */
        private String protocol;

        /**
         * 调用次数
         */
        private Integer count;
    }
}
