package com.chua.spider.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 爬虫定义保存请求。
 *
 * <p>用于新增或更新爬虫定义，必填字段包括业务编码、名称与入口 URL。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "爬虫定义保存请求")
public class SpiderDefinitionSaveRequest {

    /**
     * 爬虫业务编码
     */
    @NotBlank(message = "爬虫编码不能为空")
    private String spiderCode;

    /**
     * 爬虫名称
     */
    @NotBlank(message = "爬虫名称不能为空")
    private String spiderName;

    /**
     * 入口 URL
     */
    @NotBlank(message = "入口 URL 不能为空")
    private String spiderEntryUrl;

    /**
     * 站点主机
     */
    private String spiderSiteHost;

    /**
     * 调度类型
     */
    private String spiderExecutorType = "ONCE";

    /**
     * 调度轮次
     */
    private Integer spiderExecutorRounds = 1;

    /**
     * 是否深度采集 0/1
     */
    private Integer spiderCollectDeepUrl = 1;

    /**
     * 最大爬取深度
     */
    private Integer spiderMaxDepth = 1;

    /**
     * 最大页数
     */
    private Integer spiderMaxPages = 50;

    /**
     * 去重策略
     */
    private String spiderDedupStrategy = "URL_FINGERPRINT";

    /**
     * URL 存储类型
     */
    private String spiderUrlStorageType = "MEMORY";

    /**
     * 启停状态 0/1
     */
    private Integer spiderStatus = 1;

    /**
     * 备注
     */
    private String spiderRemark;
}
