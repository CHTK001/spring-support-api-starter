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
 * 采集数据记录实体。
 *
 * <p>对应表 {@code spider_data_record}，保存爬虫实际采集到的数据载荷，
 * 其中 {@code spider_depth} 记录该条数据的爬取深度（0=列表/N=第N层详情）。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@TableName("spider_data_record")
@Schema(description = "采集数据记录")
public class SpiderDataRecord {

    /**
     * 主键
     */
    @TableId(value = "spider_id", type = IdType.AUTO)
    private Long spiderId;

    /**
     * 爬虫编码
     */
    @TableField("spider_definition_code")
    private String spiderDefinitionCode;

    /**
     * 执行批次号
     */
    @TableField("spider_execution_no")
    private String spiderExecutionNo;

    /**
     * 目标 URL
     */
    @TableField("spider_target_url")
    private String spiderTargetUrl;

    /**
     * 数据 schema 编码
     */
    @TableField("spider_data_code")
    private String spiderDataCode;

    /**
     * 数据项名称
     */
    @TableField("spider_data_name")
    private String spiderDataName;

    /**
     * JSON 载荷
     */
    @TableField("spider_data_payload")
    private String spiderDataPayload;

    /**
     * 结果状态
     */
    @TableField("spider_data_status")
    private String spiderDataStatus;

    /**
     * 爬取深度 0=列表/N=第N层详情
     */
    @TableField("spider_depth")
    private Integer spiderDepth;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
