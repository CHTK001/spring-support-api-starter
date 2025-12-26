package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫任务实体
 *
 * @author CH
 * @since 2024/12/25
 */
@Data
@TableName("monitor_spider_task")
@ApiModel(description = "爬虫任务实体")
public class MonitorSpiderTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("任务ID")
    private Integer spiderTaskId;

    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String spiderTaskName;

    /**
     * 任务描述
     */
    @ApiModelProperty("任务描述")
    private String spiderTaskDesc;

    /**
     * 目标URL
     */
    @ApiModelProperty("目标URL")
    private String spiderTaskUrl;

    /**
     * URL模式（正则表达式，用于自动发现URL）
     */
    @ApiModelProperty("URL模式")
    private String spiderTaskUrlPattern;

    /**
     * 数据提取规则（JSON格式，包含XPath/CSS选择器等）
     */
    @ApiModelProperty("数据提取规则")
    private String spiderTaskExtractRules;

    /**
     * 选择器类型: XPATH, CSS, REGEX, JSON_PATH
     */
    @ApiModelProperty("选择器类型")
    private String spiderTaskSelectorType;

    /**
     * 任务状态: 0-停用, 1-启用, 2-运行中
     */
    @ApiModelProperty("任务状态")
    private Integer spiderTaskStatus;

    /**
     * 调度类型: NONE-一次性, CRON-定时
     */
    @ApiModelProperty("调度类型")
    private String spiderTaskScheduleType;

    /**
     * CRON表达式
     */
    @ApiModelProperty("CRON表达式")
    private String spiderTaskCron;

    /**
     * 线程数
     */
    @ApiModelProperty("线程数")
    private Integer spiderTaskThreadNum;

    /**
     * 爬取间隔（毫秒）
     */
    @ApiModelProperty("爬取间隔")
    private Integer spiderTaskSleepTime;

    /**
     * 重试次数
     */
    @ApiModelProperty("重试次数")
    private Integer spiderTaskRetryTimes;

    /**
     * 超时时间（秒）
     */
    @ApiModelProperty("超时时间")
    private Integer spiderTaskTimeout;

    /**
     * 请求头配置（JSON格式）
     */
    @ApiModelProperty("请求头配置")
    private String spiderTaskHeaders;

    /**
     * Cookie配置
     */
    @ApiModelProperty("Cookie配置")
    private String spiderTaskCookies;

    /**
     * User-Agent配置
     */
    @ApiModelProperty("User-Agent配置")
    private String spiderTaskUserAgent;

    /**
     * 代理配置（JSON格式）
     */
    @ApiModelProperty("代理配置")
    private String spiderTaskProxy;

    /**
     * 最大爬取深度（0表示不限制）
     */
    @ApiModelProperty("最大爬取深度")
    private Integer spiderTaskMaxDepth;

    /**
     * 是否启用URL自动发现
     */
    @ApiModelProperty("是否启用URL自动发现")
    private Boolean spiderTaskEnableUrlDiscover;

    /**
     * 分页URL模式（支持{page}占位符）
     */
    @ApiModelProperty("分页URL模式")
    private String spiderTaskPagePattern;

    /**
     * 最大分页数（0表示不限制）
     */
    @ApiModelProperty("最大分页数")
    private Integer spiderTaskMaxPages;

    /**
     * 增量爬取模式: NONE-全量, TIME-按时间, HASH-按哈希
     */
    @ApiModelProperty("增量爬取模式")
    private String spiderTaskIncrementalMode;

    /**
     * 速率限制（每分钟最大请求数，0表示不限制）
     */
    @ApiModelProperty("速率限制")
    private Integer spiderTaskRateLimit;

    /**
     * 数据存储类型: CONSOLE, FILE, JSON, DATABASE
     */
    @ApiModelProperty("数据存储类型")
    private String spiderTaskPipelineType;

    /**
     * 数据存储配置（JSON格式）
     */
    @ApiModelProperty("数据存储配置")
    private String spiderTaskPipelineConfig;

    /**
     * 最后执行时间
     */
    @ApiModelProperty("最后执行时间")
    private LocalDateTime spiderTaskLastRunTime;

    /**
     * 下次执行时间
     */
    @ApiModelProperty("下次执行时间")
    private LocalDateTime spiderTaskNextRunTime;

    /**
     * 运行状态: IDLE/RUNNING/STOPPED/ERROR
     */
    @ApiModelProperty("运行状态")
    private String spiderTaskRunStatus;

    /**
     * 成功数量
     */
    @ApiModelProperty("成功数量")
    private Long spiderTaskTotalSuccess;

    /**
     * 失败数量
     */
    @ApiModelProperty("失败数量")
    private Long spiderTaskTotalFail;

    /**
     * 关联的Job ID
     */
    @ApiModelProperty("关联的Job ID")
    private Integer spiderTaskJobId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        DISABLED(0, "停用"),
        ENABLED(1, "启用"),
        RUNNING(2, "运行中");

        private final int code;
        private final String desc;

        TaskStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 调度类型枚举
     */
    public enum ScheduleType {
        NONE("NONE", "一次性"),
        CRON("CRON", "定时任务");

        private final String code;
        private final String desc;

        ScheduleType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 选择器类型枚举
     */
    public enum SelectorType {
        XPATH("XPATH", "XPath选择器"),
        CSS("CSS", "CSS选择器"),
        REGEX("REGEX", "正则表达式"),
        JSON_PATH("JSON_PATH", "JsonPath选择器");

        private final String code;
        private final String desc;

        SelectorType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * Pipeline类型枚举
     */
    public enum PipelineType {
        CONSOLE("CONSOLE", "控制台输出"),
        FILE("FILE", "文件存储"),
        JSON("JSON", "JSON文件"),
        DATABASE("DATABASE", "数据库存储");

        private final String code;
        private final String desc;

        PipelineType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 增量爬取模式枚举
     */
    public enum IncrementalMode {
        NONE("NONE", "全量爬取"),
        TIME("TIME", "按时间增量"),
        HASH("HASH", "按哈希去重");

        private final String code;
        private final String desc;

        IncrementalMode(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
