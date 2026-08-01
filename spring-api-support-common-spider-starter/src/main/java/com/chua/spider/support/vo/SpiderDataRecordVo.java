package com.chua.spider.support.vo;

import com.chua.spider.support.entity.SpiderDataRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集数据视图。
 *
 * <p>由 {@link SpiderDataRecord} 转换而来，供前端展示采集数据，
 * 其中 {@code spiderDepth} 标识该条数据所属的爬取深度。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "采集数据视图")
public class SpiderDataRecordVo {

    /**
     * 主键
     */
    private Long spiderId;

    /**
     * 爬虫编码
     */
    private String spiderDefinitionCode;

    /**
     * 执行批次号
     */
    private String spiderExecutionNo;

    /**
     * 目标 URL
     */
    private String spiderTargetUrl;

    /**
     * 数据 schema 编码
     */
    private String spiderDataCode;

    /**
     * 数据项名称
     */
    private String spiderDataName;

    /**
     * JSON 载荷
     */
    private String spiderDataPayload;

    /**
     * 结果状态
     */
    private String spiderDataStatus;

    /**
     * 爬取深度 0=列表/N=第N层详情
     */
    private Integer spiderDepth;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 将实体转换为视图对象。
     *
     * @param e 采集数据实体
     * @return 视图对象，实体为 null 时返回 null
     */
    public static SpiderDataRecordVo from(SpiderDataRecord e) {
        if (e == null) {
            return null;
        }
        SpiderDataRecordVo v = new SpiderDataRecordVo();
        v.spiderId = e.getSpiderId();
        v.spiderDefinitionCode = e.getSpiderDefinitionCode();
        v.spiderExecutionNo = e.getSpiderExecutionNo();
        v.spiderTargetUrl = e.getSpiderTargetUrl();
        v.spiderDataCode = e.getSpiderDataCode();
        v.spiderDataName = e.getSpiderDataName();
        v.spiderDataPayload = e.getSpiderDataPayload();
        v.spiderDataStatus = e.getSpiderDataStatus();
        v.spiderDepth = e.getSpiderDepth();
        v.createTime = e.getCreateTime();
        return v;
    }
}
