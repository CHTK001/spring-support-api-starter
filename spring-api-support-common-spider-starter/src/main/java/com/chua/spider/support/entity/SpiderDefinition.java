package com.chua.spider.support.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫定义实体。
 *
 * <p>对应表 {@code spider_definition}，保存爬虫的业务编码、入口 URL、
 * 采集深度、调度类型等信息。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@TableName("spider_definition")
@Schema(description = "爬虫定义")
public class SpiderDefinition {

    /**
     * 主键
     */
    @TableId(value = "spider_id", type = IdType.AUTO)
    private Long spiderId;

    /**
     * 爬虫业务编码
     */
    @TableField("spider_code")
    private String spiderCode;

    /**
     * 爬虫名称
     */
    @TableField("spider_name")
    private String spiderName;

    /**
     * 入口 URL
     */
    @TableField("spider_entry_url")
    private String spiderEntryUrl;

    /**
     * 站点主机
     */
    @TableField("spider_site_host")
    private String spiderSiteHost;

    /**
     * 调度类型
     */
    @TableField("spider_executor_type")
    private String spiderExecutorType;

    /**
     * 调度轮次
     */
    @TableField("spider_executor_rounds")
    private Integer spiderExecutorRounds;

    /**
     * 是否深度采集 0/1
     */
    @TableField("spider_collect_deep_url")
    private Integer spiderCollectDeepUrl;

    /**
     * 最大爬取深度
     */
    @TableField("spider_max_depth")
    private Integer spiderMaxDepth;

    /**
     * 最大页数
     */
    @TableField("spider_max_pages")
    private Integer spiderMaxPages;

    /**
     * 去重策略
     */
    @TableField("spider_dedup_strategy")
    private String spiderDedupStrategy;

    /**
     * URL 存储类型
     */
    @TableField("spider_url_storage_type")
    private String spiderUrlStorageType;

    /**
     * 启停状态 0/1
     */
    @TableField("spider_status")
    private Integer spiderStatus;

    /**
     * 备注
     */
    @TableField("spider_remark")
    private String spiderRemark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
