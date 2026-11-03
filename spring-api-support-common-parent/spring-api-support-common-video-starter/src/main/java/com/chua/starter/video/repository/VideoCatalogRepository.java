// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.starter.video.repository.mapper.VideoCatalogMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 视频本地索引仓储。
 *
 * <p>负责 {@code video_info} 的读写：同步任务 upsert，检索走标量列，
 * 详情直接取 {@code video_payload} 快照。所有方法在库不可用时抛出或返回空，
 * 由上层决定是否回落到资源站直查，绝不让本地库故障放大成全站不可用。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class VideoCatalogRepository implements VideoIndex {

    /**
     * 索引状态：上架。
     */
    private static final int STATUS_ONLINE = 1;

    /**
     * 单次 IN 查询的分片大小。
     */
    private static final int IN_CHUNK = 200;

    /**
     * 详情快照序列化器（需要 JSR-310 支持 LocalDateTime）。
     */
    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 索引 Mapper。
     */
    private final VideoCatalogMapper mapper;

    /**
     * 本地索引是否可用（宿主引入了 MyBatis-Plus 且注入了 Mapper）。
     *
     * @return 可用返回 true
     */
    @Override
    public boolean available() {
        return mapper != null;
    }

    /**
     * 已入库条目数。
     *
     * @return 条目数；库不可用时返回 0
     */
    @Override
    public long countAll() {
        if (mapper == null) {
            return 0;
        }
        try {
            Number count = mapper.selectCount(Wrappers.<VideoInfoEntity>lambdaQuery()
                    .eq(VideoInfoEntity::getVideoStatus, STATUS_ONLINE));
            return count == null ? 0 : count.longValue();
        } catch (Exception e) {
            log.warn("[Video] 本地索引计数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 按条件查询列表（替代资源站直查）。
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
    @Override
    public List<VideoInfoResult> query(String keyword, String videoType, String year,
                                       String district, String tag, int page, int pageSize) {
        if (mapper == null) {
            return List.of();
        }
        List<VideoInfoEntity> rows = mapper.selectList(buildQuery(keyword, videoType, year, district, tag, page, pageSize));
        List<VideoInfoResult> results = new ArrayList<>(rows.size());
        for (VideoInfoEntity row : rows) {
            results.add(toInfo(row));
        }
        return results;
    }

    /**
     * 按 originId 查询详情（优先返回 payload 快照）。
     *
     * @param originId 资源站条目 ID
     * @return 详情，未命中返回 null
     */
    @Override
    public VideoInfoResult find(String originId) {
        if (mapper == null || originId == null || originId.isBlank()) {
            return null;
        }
        VideoInfoEntity row = mapper.selectOne(Wrappers.<VideoInfoEntity>lambdaQuery()
                .eq(VideoInfoEntity::getVideoOriginId, originId)
                .last("LIMIT 1"));
        if (row == null) {
            return null;
        }
        if (row.getVideoPayload() != null && !row.getVideoPayload().isBlank()) {
            try {
                return JSON.readValue(row.getVideoPayload(), VideoInfoResult.class);
            } catch (Exception e) {
                log.warn("[Video] 详情快照解析失败 id={}: {}", originId, e.getMessage());
            }
        }
        return toInfo(row);
    }

    /**
     * 批量落库（存在则更新，不存在则插入）。
     *
     * @param items 资源站条目
     * @return 写入条数
     */
    @Override
    public int upsertAll(Collection<VideoInfoResult> items) {
        if (mapper == null || items == null || items.isEmpty()) {
            return 0;
        }
        Map<String, VideoInfoEntity> existing = loadExisting(items);
        LocalDateTime now = LocalDateTime.now();
        int changed = 0;
        for (VideoInfoResult item : items) {
            String originId = originId(item);
            if (originId == null) {
                continue;
            }
            VideoInfoEntity entity = toEntity(item, originId);
            entity.setVideoSyncTime(now);
            entity.setUpdateTime(now);
            VideoInfoEntity old = existing.get(originId);
            if (old == null) {
                entity.setCreateTime(now);
                mapper.insert(entity);
            } else {
                entity.setVideoInfoId(old.getVideoInfoId());
                mapper.updateById(entity);
            }
            changed++;
        }
        return changed;
    }

    /**
     * 待刷新的连载条目 ID（更新状态以「更新至」开头，按同步时间升序）。
     *
     * @param limit 本次最多刷新条数
     * @return 条目 ID 列表
     */
    @Override
    public List<String> pendingSeriesIds(int limit) {
        if (mapper == null || limit <= 0) {
            return List.of();
        }
        List<VideoInfoEntity> rows = mapper.selectList(Wrappers.<VideoInfoEntity>lambdaQuery()
                .select(VideoInfoEntity::getVideoOriginId)
                .eq(VideoInfoEntity::getVideoStatus, STATUS_ONLINE)
                .likeRight(VideoInfoEntity::getVideoRemark, "更新至")
                .orderByAsc(VideoInfoEntity::getVideoSyncTime)
                .last("LIMIT " + limit));
        List<String> ids = new ArrayList<>(rows.size());
        for (VideoInfoEntity row : rows) {
            if (row.getVideoOriginId() != null) {
                ids.add(row.getVideoOriginId());
            }
        }
        return ids;
    }

    /**
     * 本地索引概况（运维/接口排查用）。
     *
     * @return 条目数、最近同步时间、分类分布
     */
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("available", available());
        result.put("total", countAll());
        if (mapper == null) {
            return result;
        }
        try {
            VideoInfoEntity latest = mapper.selectOne(Wrappers.<VideoInfoEntity>lambdaQuery()
                    .select(VideoInfoEntity::getVideoSyncTime)
                    .isNotNull(VideoInfoEntity::getVideoSyncTime)
                    .orderByDesc(VideoInfoEntity::getVideoSyncTime)
                    .last("LIMIT 1"));
            result.put("lastSyncTime", latest == null ? null : latest.getVideoSyncTime());
            Map<String, Long> categories = new HashMap<>();
            for (VideoInfoEntity row : mapper.selectList(Wrappers.<VideoInfoEntity>lambdaQuery()
                    .select(VideoInfoEntity::getVideoCategory)
                    .eq(VideoInfoEntity::getVideoStatus, STATUS_ONLINE))) {
                categories.merge(row.getVideoCategory() == null ? "unknown" : row.getVideoCategory(), 1L, Long::sum);
            }
            result.put("categories", categories);
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 构建查询条件：标量列过滤 + 排序 + 分页。
     */
    private LambdaQueryWrapper<VideoInfoEntity> buildQuery(
            String keyword, String videoType, String year, String district, String tag, int page, int pageSize) {
        LambdaQueryWrapper<VideoInfoEntity> wrapper = Wrappers.<VideoInfoEntity>lambdaQuery()
                .eq(VideoInfoEntity::getVideoStatus, STATUS_ONLINE);
        if (hasText(videoType)) {
            wrapper.eq(VideoInfoEntity::getVideoCategory, videoType);
        }
        if (hasText(district)) {
            wrapper.eq(VideoInfoEntity::getVideoDistrict, district);
        }
        Integer parsedYear = parseYear(year);
        if (parsedYear != null) {
            wrapper.eq(VideoInfoEntity::getVideoYear, parsedYear);
        }
        if (hasText(tag)) {
            wrapper.like(VideoInfoEntity::getVideoTags, tag);
        }
        boolean searching = hasText(keyword);
        if (searching) {
            String kw = keyword.trim();
            wrapper.and(q -> q.like(VideoInfoEntity::getVideoName, kw)
                    .or().like(VideoInfoEntity::getVideoAliasName, kw)
                    .or().like(VideoInfoEntity::getVideoActor, kw)
                    .or().like(VideoInfoEntity::getVideoTags, kw));
        }
        if (searching) {
            // 关键词检索优先按评分，再按更新时间兜底
            wrapper.orderByDesc(VideoInfoEntity::getVideoScore)
                    .orderByDesc(VideoInfoEntity::getVideoPublishTime);
        } else {
            // 分类浏览按资源站更新时间
            wrapper.orderByDesc(VideoInfoEntity::getVideoPublishTime)
                    .orderByDesc(VideoInfoEntity::getVideoScore);
        }
        int size = Math.max(1, pageSize);
        int offset = (Math.max(1, page) - 1) * size;
        return wrapper.last("LIMIT " + size + " OFFSET " + offset);
    }

    /**
     * 批量查已存在的行（按 originId）。
     */
    private Map<String, VideoInfoEntity> loadExisting(Collection<VideoInfoResult> items) {
        Set<String> ids = new LinkedHashSet<>();
        for (VideoInfoResult item : items) {
            String id = originId(item);
            if (id != null) {
                ids.add(id);
            }
        }
        Map<String, VideoInfoEntity> existing = new HashMap<>();
        List<String> all = new ArrayList<>(ids);
        for (int start = 0; start < all.size(); start += IN_CHUNK) {
            List<String> chunk = all.subList(start, Math.min(all.size(), start + IN_CHUNK));
            for (VideoInfoEntity row : mapper.selectList(Wrappers.<VideoInfoEntity>lambdaQuery()
                    .in(VideoInfoEntity::getVideoOriginId, chunk))) {
                existing.put(row.getVideoOriginId(), row);
            }
        }
        return existing;
    }

    /**
     * 条目唯一键（优先 originId，缺失时退回 videoId）。
     */
    private static String originId(VideoInfoResult item) {
        if (item == null) {
            return null;
        }
        String id = item.getVideoOriginId();
        if (id == null || id.isBlank()) {
            id = item.getVideoId() == null ? null : String.valueOf(item.getVideoId());
        }
        return id == null || id.isBlank() ? null : id.trim();
    }

    /**
     * 结果条目 → 索引行。
     */
    private VideoInfoEntity toEntity(VideoInfoResult item, String originId) {
        VideoInfoEntity entity = new VideoInfoEntity();
        entity.setVideoOriginId(originId);
        entity.setVideoSource("maccms");
        entity.setVideoName(firstText(item.getVideoName(), item.getVideoTitle()));
        entity.setVideoAliasName(item.getVideoAliasName());
        entity.setVideoCategory(item.getVideoCategory());
        entity.setVideoTypeName(item.getVideoType());
        entity.setVideoYear(item.getVideoYear());
        entity.setVideoDistrict(item.getVideoDistrict());
        entity.setVideoLanguage(item.getVideoLanguage());
        entity.setVideoTags(item.getTags());
        entity.setVideoScore(item.getVideoScore());
        entity.setVideoViews(item.getVideoViews());
        entity.setVideoCover(firstText(item.getVideoCover(), item.getVideoThumbnail()));
        entity.setVideoActor(item.getVideoActor());
        entity.setVideoDirector(item.getVideoDirector());
        entity.setVideoDescription(item.getVideoDescription());
        entity.setVideoRemark(item.getVideoRemark());
        entity.setVideoQuality(item.getVideoQuality());
        entity.setVideoPublishTime(item.getVideoPublishDate());
        entity.setVideoStatus(STATUS_ONLINE);
        try {
            entity.setVideoPayload(JSON.writeValueAsString(item));
        } catch (Exception e) {
            log.warn("[Video] 详情快照序列化失败 id={}: {}", originId, e.getMessage());
        }
        return entity;
    }

    /**
     * 索引行 → 结果条目（列表用，不再解析 payload）。
     */
    private VideoInfoResult toInfo(VideoInfoEntity entity) {
        VideoInfoResult info = new VideoInfoResult();
        info.setVideoId(entity.getVideoInfoId() == null ? null : entity.getVideoInfoId().intValue());
        info.setVideoOriginId(entity.getVideoOriginId());
        info.setVideoName(entity.getVideoName());
        info.setVideoTitle(entity.getVideoName());
        info.setVideoAliasName(entity.getVideoAliasName());
        info.setVideoCategory(entity.getVideoCategory());
        info.setVideoType(entity.getVideoTypeName());
        info.setVideoYear(entity.getVideoYear());
        info.setVideoDistrict(entity.getVideoDistrict());
        info.setVideoLanguage(entity.getVideoLanguage());
        info.setTags(entity.getVideoTags());
        info.setVideoScore(entity.getVideoScore());
        info.setVideoViews(entity.getVideoViews());
        info.setVideoCover(entity.getVideoCover());
        info.setVideoActor(entity.getVideoActor());
        info.setVideoDirector(entity.getVideoDirector());
        info.setVideoDescription(entity.getVideoDescription());
        info.setVideoRemark(entity.getVideoRemark());
        info.setVideoQuality(entity.getVideoQuality());
        info.setVideoPublishDate(entity.getVideoPublishTime());
        info.setVideoStatus(entity.getVideoStatus());
        return info;
    }

    /**
     * 年份参数解析：容忍「2023」「2023年」，非数字返回 null。
     */
    private static Integer parseYear(String year) {
        if (!hasText(year)) {
            return null;
        }
        String digits = year.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 4) {
            return null;
        }
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 非空文本判断。
     */
    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    /**
     * 取第一个非空文本。
     */
    private static String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }
}
