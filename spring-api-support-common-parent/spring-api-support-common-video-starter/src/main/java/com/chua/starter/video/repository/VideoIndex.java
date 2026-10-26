package com.chua.starter.video.repository;

import com.chua.common.support.datasearch.video.model.VideoInfoResult;

import java.util.Collection;
import java.util.List;

/**
 * 视频本地索引接口。
 *
 * <p>刻意不引用 MyBatis-Plus 类型：宿主未引入数据库时视频模块仍能正常装配，
 * 只是拿不到实现，检索自动回落资源站直查。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface VideoIndex {

    /**
     * 索引是否可用。
     *
     * @return 可用返回 true
     */
    boolean available();

    /**
     * 已入库条目数。
     *
     * @return 条目数
     */
    long countAll();

    /**
     * 按条件分页查询。
     *
     * @param keyword   关键词，可空
     * @param videoType 分类码，可空
     * @param year      年份，可空
     * @param district  地区，可空
     * @param tag       标签，可空
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     * @return 条目列表
     */
    List<VideoInfoResult> query(String keyword, String videoType, String year,
                                String district, String tag, int page, int pageSize);

    /**
     * 按 originId 查详情。
     *
     * @param originId 资源站条目 ID
     * @return 详情，未命中返回 null
     */
    VideoInfoResult find(String originId);

    /**
     * 批量写入（存在则更新）。
     *
     * @param items 条目
     * @return 写入条数
     */
    int upsertAll(Collection<VideoInfoResult> items);

    /**
     * 待刷新详情的连载条目 ID。
     *
     * @param limit 本次最多条数
     * @return 条目 ID 列表
     */
    List<String> pendingSeriesIds(int limit);
}
