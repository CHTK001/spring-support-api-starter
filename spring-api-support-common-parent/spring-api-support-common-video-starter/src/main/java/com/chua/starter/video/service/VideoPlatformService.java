// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.service;

import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.common.support.datasearch.video.model.VideoSearch;
import com.chua.common.support.datasearch.video.spi.ResourceProvider;
import com.chua.common.support.datasearch.video.spi.VideoSearchService;
import com.chua.common.support.datasearch.network.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.PageResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.repository.VideoIndex;
import com.chua.starter.video.setting.VideoSettingService;
import com.chua.starter.video.setting.VideoSettingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 视频平台业务服务。
 *
 * <p>统一编排两类数据来源：</p>
 * <ul>
 *   <li><b>多源聚合检索</b>：携带关键词时，委托上游
 *       {@link com.chua.common.support.datasearch.video.spi.VideoSearchService}
 *       并发检索 datasearch 视频 SPI 中的全部数据源；</li>
 *   <li><b>分类浏览</b>：无关键词时，通过 {@link MacCmsCatalogClient}
 *       拉取 MacCMS 兼容资源站的最新资源，并按前端分类码过滤。</li>
 * </ul>
 *
 * <p>聚合结果按 originId 去重后再做年份/地区/标签的后置过滤，并写入有界
 * 详情缓存，供播放页按 id 秒级取回。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class VideoPlatformService {

    /**
     * 上游多源聚合检索服务。
     */
    private final VideoSearchService videoSearchService;

    /**
     * MacCMS 分类浏览客户端。
     */
    private final MacCmsCatalogClient catalogClient;

    /**
     * 视频服务配置。
     */
    private final VideoProperties properties;

    /**
     * 视频配置数据库服务。
     */
    private final VideoSettingService settings;

    /**
     * 详情缓存（按 originId，LRU 逐出，容量可配置）。
     */
    private final Map<String, VideoInfoResult> detailCache;

    /**
     * 本地索引（未引入数据库时为 null，检索退回资源站直查）。
     */
    private final VideoIndex repository;

    /**
     * 构造视频平台业务服务（不接入配置数据库，使用本地配置）。
     *
     * @param properties 视频服务配置
     */
    public VideoPlatformService(VideoProperties properties) {
        this(properties, new VideoSettingService(new VideoSettingStore.Noop()));
    }

    /**
     * 构造视频平台业务服务。
     *
     * @param properties 视频服务配置
     * @param settings   视频配置数据库服务
     */
    public VideoPlatformService(VideoProperties properties, VideoSettingService settings) {
        this(properties, settings, null);
    }

    /**
     * 构造视频平台业务服务（接入本地索引）。
     *
     * @param properties 视频服务配置
     * @param settings   视频配置数据库服务
     * @param repository 本地索引仓储，可为空（为空时退化为资源站直查）
     */
    public VideoPlatformService(VideoProperties properties, VideoSettingService settings,
                                VideoIndex repository) {
        this.repository = repository;
        this.properties = properties;
        this.settings = settings == null ? new VideoSettingService(new VideoSettingStore.Noop()) : settings;
        this.videoSearchService = new VideoSearchService();
        this.catalogClient = new MacCmsCatalogClient(properties,
                () -> this.settings.resolve(VideoSettingService.NAME_CATALOG_URL, properties.getCatalogUrl()));
        int cacheSize = Math.max(16, properties.getDetailCacheSize());
        this.detailCache = Collections.synchronizedMap(new LinkedHashMap<>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, VideoInfoResult> eldest) {
                return size() > cacheSize;
            }
        });
    }

    /**
     * 视频列表（检索或分类浏览）。
     *
     * @param keyword   关键词，可空
     * @param videoType 前端分类码（movie/tv/anime/variety/documentary），可空
     * @param year      年份，可空
     * @param district  地区，可空
     * @param tag       标签，可空
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     * @return 视频条目列表
     */
    public List<VideoInfoResult> list(String keyword, String videoType, String year,
                                      String district, String tag, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 24;
        boolean searching = hasText(keyword);

        // 本地索引优先：同步过就查本地库，库空/未装配时才直连资源站
        List<VideoInfoResult> indexed = queryIndex(keyword, videoType, year, district, tag, safePage, safeSize);
        if (indexed != null) {
            // 列表行只是索引标量，不含线路/网盘，不能写进详情缓存，否则详情页会拿到残缺数据
            return indexed;
        }

        List<VideoInfoResult> raw = searching
                ? aggregate(keyword, videoType, safePage, safeSize)
                : browse(videoType, safePage);

        List<VideoInfoResult> distinct = dedupe(raw);
        List<VideoInfoResult> filtered = distinct.stream()
                .filter(item -> matchType(item, videoType))
                .filter(item -> matchText(item.getVideoYear(), year))
                .filter(item -> matchText(item.getVideoDistrict(), district))
                .filter(item -> matchText(item.getTags(), tag))
                .collect(Collectors.toList());
        // 关键词检索时若数据源未回填分类元数据导致过滤为空，则回退为未过滤结果；
        // 分类浏览时保留过滤结果（分区为空即为空，避免跨分类串数据）。
        if (searching && filtered.isEmpty()) {
            filtered = distinct;
        }

        int maxResults = Math.max(1, properties.getMaxResults());
        if (filtered.size() > maxResults) {
            filtered = new ArrayList<>(filtered.subList(0, maxResults));
        }
        // 资源站直查的结果顺手回写本地索引，下次即可走库
        persist(filtered);
        return filtered;
    }

    /**
     * 视频详情（缓存 → 本地索引 → 资源站 → 聚合检索兜底）。
     *
     * @param id 视频 originId
     * @return 视频详情，未找到返回 null
     */
    public VideoInfoResult detail(String id) {
        if (!hasText(id)) {
            return null;
        }
        VideoInfoResult cached = detailCache.get(id);
        if (cached != null) {
            return cached;
        }
        VideoInfoResult indexed = findIndexed(id);
        if (indexed != null) {
            cache(indexed);
            return indexed;
        }
        VideoInfoResult fromCatalog = catalogClient.detail(id);
        if (fromCatalog != null) {
            cache(fromCatalog);
            List<VideoInfoResult> one = new ArrayList<>(1);
            one.add(fromCatalog);
            persist(one);
            return fromCatalog;
        }
        // 已配置采集站点时以其为详情权威来源，避免再触发全网多源检索；
        // 仅在未配置采集站点时退化为按 id 聚合检索匹配。
        if (catalogClient.available()) {
            return null;
        }
        for (VideoInfoResult item : aggregate(id, null, 1, 24)) {
            if (id.equals(item.getVideoOriginId()) || id.equals(String.valueOf(item.getVideoId()))) {
                cache(item);
                return item;
            }
        }
        return null;
    }

    /**
     * 已注册的视频数据源名称列表。
     *
     * @return 数据源名称
     */
    public List<String> sources() {
        Map<String, ResourceProvider> providers = ServiceProvider.of(ResourceProvider.class).list();
        if (providers == null || providers.isEmpty()) {
            return List.of();
        }
        return providers.keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * 拉取资源站最新目录（同步任务用）。
     *
     * @param page 页码，从 1 开始
     * @return 条目列表
     */
    public List<VideoInfoResult> fetchCatalogPage(int page) {
        List<VideoInfoResult> items = catalogClient.list(null, page);
        return items == null ? List.of() : items;
    }

    /**
     * 拉取资源站详情（同步任务用）。
     *
     * @param id 视频 originId
     * @return 详情，未命中返回 null
     */
    public VideoInfoResult fetchCatalogDetail(String id) {
        return catalogClient.detail(id);
    }

    /**
     * 查本地索引。
     *
     * @return 命中返回结果（可能为空列表）；无法回答时返回 null，由调用方回落资源站
     */
    private List<VideoInfoResult> queryIndex(String keyword, String videoType, String year,
                                             String district, String tag, int page, int pageSize) {
        if (repository == null || !repository.available()) {
            return null;
        }
        try {
            if (repository.countAll() <= 0) {
                return null;
            }
            List<VideoInfoResult> rows = repository.query(keyword, videoType, year, district, tag, page, pageSize);
            if (!rows.isEmpty()) {
                return rows;
            }
            // 纯筛选无结果时如实返回空；关键词检索未命中则回落上游，避免本地库偏小导致搜不到
            return hasText(keyword) ? null : List.of();
        } catch (Exception e) {
            log.warn("[Video] 本地索引查询失败，回落资源站: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 按 id 查本地索引详情。
     *
     * @param id 视频 originId
     * @return 详情；未命中或异常返回 null
     */
    private VideoInfoResult findIndexed(String id) {
        if (repository == null || !repository.available()) {
            return null;
        }
        try {
            return repository.find(id);
        } catch (Exception e) {
            log.warn("[Video] 本地索引详情查询失败 id={}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 回写本地索引（失败只告警，不影响本次返回）。
     *
     * @param items 条目
     */
    private void persist(List<VideoInfoResult> items) {
        if (repository == null || !repository.available() || items == null || items.isEmpty()) {
            return;
        }
        try {
            repository.upsertAll(items);
        } catch (Exception e) {
            log.warn("[Video] 本地索引写入失败: {}", e.getMessage());
        }
    }

    /**
     * 委托上游多源聚合检索。
     *
     * @param keyword   关键词
     * @param videoType 分类码
     * @param page      页码
     * @param pageSize  每页条数
     * @return 聚合条目
     */
    private List<VideoInfoResult> aggregate(String keyword, String videoType, int page, int pageSize) {
        try {
            VideoSearch search = new VideoSearch();
            search.setKeyword(keyword);
            search.setVideoType(videoType);
            search.setPage(page);
            search.setPageSize(pageSize);
            ReturnPageResult<VideoInfoResult> result = videoSearchService.search(search);
            PageResult<VideoInfoResult> pageResult = result == null ? null : result.getData();
            if (pageResult == null || pageResult.getData() == null) {
                return List.of();
            }
            return pageResult.getData();
        } catch (Exception e) {
            log.warn("[Video] 多源聚合检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 分类浏览：优先使用配置的浏览关键词检索，否则拉取资源站最新资源。
     *
     * @param videoType 分类码
     * @param page      页码
     * @return 条目列表
     */
    private List<VideoInfoResult> browse(String videoType, int page) {
        Map<String, String> keywords = settings.resolveMap(
                VideoSettingService.NAME_BROWSE_KEYWORDS, properties.getBrowseKeywords());
        String browseKeyword = videoType != null && keywords != null ? keywords.get(videoType) : null;
        if (browseKeyword != null && !browseKeyword.isBlank()) {
            return aggregate(browseKeyword, videoType, page, properties.getMaxResults());
        }
        List<VideoInfoResult> catalog = catalogClient.list(null, page);
        if (!catalog.isEmpty()) {
            return catalog;
        }
        String fallback = settings.resolve(VideoSettingService.NAME_DEFAULT_KEYWORD, properties.getDefaultKeyword());
        if (hasText(fallback)) {
            return aggregate(fallback, videoType, page, properties.getMaxResults());
        }
        return List.of();
    }

    /**
     * 按 originId（缺失时视频名+年份）去重，保持顺序。
     *
     * @param items 原始条目
     * @return 去重条目
     */
    private List<VideoInfoResult> dedupe(List<VideoInfoResult> items) {
        Map<String, VideoInfoResult> distinct = new LinkedHashMap<>();
        for (VideoInfoResult item : items) {
            if (item == null) {
                continue;
            }
            distinct.putIfAbsent(identity(item), item);
        }
        return new ArrayList<>(distinct.values());
    }

    /**
     * 条目唯一键。
     *
     * @param item 条目
     * @return 唯一键
     */
    private String identity(VideoInfoResult item) {
        if (hasText(item.getVideoOriginId())) {
            return item.getVideoOriginId();
        }
        if (item.getVideoId() != null) {
            return String.valueOf(item.getVideoId());
        }
        return Objects.toString(item.getVideoName(), "") + "#" + Objects.toString(item.getVideoYear(), "");
    }

    /**
     * 写入详情缓存。
     *
     * @param item 条目
     */
    private void cache(VideoInfoResult item) {
        String id = identity(item);
        if (hasText(id)) {
            detailCache.put(id, item);
        }
    }

    /**
     * 分类匹配（校验标准分类码与原始分类文本）。
     *
     * @param item      条目
     * @param videoType 分类码
     * @return 匹配返回 true（未指定分类时恒为 true）
     */
    private boolean matchType(VideoInfoResult item, String videoType) {
        if (!hasText(videoType)) {
            return true;
        }
        String expected = videoType.trim().toLowerCase();
        return equalsIgnoreCase(item.getVideoCategory(), expected)
                || containsIgnoreCase(item.getVideoType(), expected);
    }

    /**
     * 文本包含匹配（未指定查询值或条目字段为空时不过滤）。
     *
     * @param actual   条目字段值
     * @param expected 查询值
     * @return 匹配返回 true
     */
    private boolean matchText(String actual, String expected) {
        if (!hasText(expected)) {
            return true;
        }
        if (actual == null || actual.isBlank()) {
            return true;
        }
        return actual.toLowerCase().contains(expected.trim().toLowerCase());
    }

    /**
     * 文本包含匹配。
     *
     * @param actual   条目字段值
     * @param expected 查询值
     * @return 匹配返回 true
     */
    private boolean matchText(Integer actual, String expected) {
        return matchText(actual == null ? null : String.valueOf(actual), expected);
    }

    /**
     * 忽略大小写相等。
     *
     * @param actual   实际值
     * @param expected 期望值
     * @return 相等返回 true
     */
    private boolean equalsIgnoreCase(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    /**
     * 忽略大小写包含。
     *
     * @param actual   实际值
     * @param expected 期望值
     * @return 包含返回 true
     */
    private boolean containsIgnoreCase(String actual, String expected) {
        return actual != null && actual.toLowerCase().contains(expected);
    }

    /**
     * 文本非空判断。
     *
     * @param text 文本
     * @return 非空返回 true
     */
    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}