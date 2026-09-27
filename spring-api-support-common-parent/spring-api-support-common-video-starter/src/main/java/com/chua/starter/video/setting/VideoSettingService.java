package com.chua.starter.video.setting;

import com.chua.starter.video.live.model.LiveCatalog;
import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.live.model.LiveGroup;
import com.chua.starter.video.properties.VideoProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 视频配置数据库服务。
 *
 * <p>把视频/直播配置存入通用配置表 {@code sys_setting}（group={@value #GROUP}），
 * 复用 {@code /v2/setting} 管理端、变更历史与导入导出。存储布局：</p>
 * <ul>
 *   <li>标量配置：{@code catalogUrl} / {@code defaultKeyword} / {@code browseKeywords}；</li>
 *   <li>直播目录：{@code liveCatalog} → 整段 JSON（分组 + 频道 + 候选源）。</li>
 * </ul>
 *
 * <p>直播目录以整段 JSON 存于 {@code sys_setting_value}（已放宽为 {@code TEXT}），
 * 编辑更直观。兼容早期按行存储的 {@code liveGroup.* / liveChannel.* / liveSource.*}：
 * 读取时优先整段 JSON，缺失则回退逐行组装。</p>
 *
 * <p>首次启动用内置 JSON 播种，之后以数据库为准；存储不可用时回退
 * {@link VideoProperties} 与内置 JSON。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class VideoSettingService {

    /**
     * 配置分组。
     */
    public static final String GROUP = "video";

    /**
     * 标量：资源站采集接口地址。
     */
    public static final String NAME_CATALOG_URL = "catalogUrl";

    /**
     * 标量：兜底检索关键词。
     */
    public static final String NAME_DEFAULT_KEYWORD = "defaultKeyword";

    /**
     * 标量：分类浏览关键词映射（JSON）。
     */
    public static final String NAME_BROWSE_KEYWORDS = "browseKeywords";

    /**
     * 直播目录（整段 JSON）。
     */
    public static final String NAME_LIVE_CATALOG = "liveCatalog";

    /**
     * 旧版直播分组行前缀（兼容读取）。
     */
    public static final String PREFIX_GROUP = "liveGroup.";

    /**
     * 旧版直播频道行前缀（兼容读取）。
     */
    public static final String PREFIX_CHANNEL = "liveChannel.";

    /**
     * 旧版候选源行前缀（兼容读取）。
     */
    public static final String PREFIX_SOURCE = "liveSource.";

    /**
     * JSON 解析器（忽略未知字段，兼容目录快照里多出的派生字段）。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 配置存储。
     */
    private final VideoSettingStore store;

    /**
     * 名称 → 值 快照。
     */
    private volatile Map<String, String> values = Map.of();

    /**
     * 直播目录快照。
     */
    private volatile LiveCatalog catalog = new LiveCatalog();

    /**
     * 构造视频配置服务。
     *
     * @param store 配置存储
     */
    public VideoSettingService(VideoSettingStore store) {
        this.store = store == null ? new VideoSettingStore.Noop() : store;
    }

    /**
     * 存储是否可用。
     *
     * @return 可用返回 true
     */
    public boolean available() {
        return store.available();
    }

    /**
     * 初始化：按需播种默认配置并加载快照。
     *
     * @param properties 视频配置（播种默认值）
     * @param defaults   默认直播目录（通常来自内置 JSON 或外部文件）
     */
    public synchronized void initialize(VideoProperties properties, LiveCatalog defaults) {
        if (!available()) {
            return;
        }
        try {
            seedIfAbsent(properties, defaults);
            load();
        } catch (Exception e) {
            log.warn("[Video] 视频配置初始化失败，回退内置配置: {}", e.getMessage());
        }
    }

    /**
     * 重新加载配置快照。
     */
    public synchronized void reload() {
        if (!available()) {
            return;
        }
        try {
            load();
        } catch (Exception e) {
            log.warn("[Video] 视频配置重新加载失败: {}", e.getMessage());
        }
    }

    /**
     * 解析标量配置。
     *
     * @param name     配置名称
     * @param fallback 缺省值
     * @return 配置值
     */
    public String resolve(String name, String fallback) {
        String value = values.get(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * 解析布尔配置。
     *
     * @param name     配置名称
     * @param fallback 缺省值
     * @return 布尔值
     */
    public boolean resolveBool(String name, boolean fallback) {
        String value = values.get(name);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value.trim());
    }

    /**
     * 解析 JSON 映射配置。
     *
     * @param name     配置名称
     * @param fallback 缺省值
     * @return 映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> resolveMap(String name, Map<String, String> fallback) {
        String value = values.get(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            Map<String, String> map = MAPPER.readValue(value, LinkedHashMap.class);
            return map == null || map.isEmpty() ? fallback : map;
        } catch (Exception e) {
            log.warn("[Video] 配置 {} 解析失败: {}", name, e.getMessage());
            return fallback;
        }
    }

    /**
     * 当前直播目录快照。
     *
     * @return 目录（非 null）
     */
    public LiveCatalog catalog() {
        return catalog;
    }

    /**
     * 覆盖写入直播目录（整段 JSON），并清理旧版逐行数据。
     *
     * @param target 目标目录
     */
    public synchronized void saveCatalog(LiveCatalog target) {
        if (!available() || target == null) {
            return;
        }
        store.upsert(GROUP, NAME_LIVE_CATALOG, toJson(target), "JSON", "直播频道目录", 10);
        store.deleteByPrefix(GROUP, PREFIX_SOURCE);
        store.deleteByPrefix(GROUP, PREFIX_CHANNEL);
        store.deleteByPrefix(GROUP, PREFIX_GROUP);
        load();
    }

    /**
     * 导出视频配置快照（标量 + 整段直播目录）。
     *
     * @return 配置快照
     */
    public VideoConfigSnapshot exportConfig() {
        return new VideoConfigSnapshot(
                resolve(NAME_CATALOG_URL, null),
                resolve(NAME_DEFAULT_KEYWORD, null),
                resolveMap(NAME_BROWSE_KEYWORDS, Map.of()),
                catalog());
    }

    /**
     * 导入视频配置快照。
     *
     * @param snapshot  配置快照
     * @param overwrite 是否覆盖已存在的配置项
     * @return 是否导入成功
     */
    public synchronized boolean importConfig(VideoConfigSnapshot snapshot, boolean overwrite) {
        if (!available() || snapshot == null) {
            return false;
        }
        upsertIfAllowed(NAME_CATALOG_URL, snapshot.catalogUrl(),
                "String", "视频资源站采集接口地址", 1, overwrite);
        upsertIfAllowed(NAME_DEFAULT_KEYWORD, snapshot.defaultKeyword(),
                "String", "兜底检索关键词", 2, overwrite);
        if (snapshot.browseKeywords() != null) {
            upsertIfAllowed(NAME_BROWSE_KEYWORDS, toJson(snapshot.browseKeywords()),
                    "JSON", "分类浏览关键词映射", 3, overwrite);
        }
        if (snapshot.liveCatalog() != null && !snapshot.liveCatalog().isEmpty()) {
            upsertIfAllowed(NAME_LIVE_CATALOG, toJson(snapshot.liveCatalog()),
                    "JSON", "直播频道目录", 10, overwrite);
        }
        load();
        log.info("[Video] 视频配置导入完成: overwrite={}", overwrite);
        return true;
    }

    /**
     * 按覆盖策略写入配置项。
     *
     * @param name      名称
     * @param value     值
     * @param valueType 值类型
     * @param remark    备注
     * @param sort      排序号
     * @param overwrite 是否覆盖已存在项
     */
    private void upsertIfAllowed(String name, String value, String valueType,
                                 String remark, int sort, boolean overwrite) {
        if (value == null) {
            return;
        }
        if (!overwrite && values.containsKey(name)) {
            return;
        }
        store.upsert(GROUP, name, value, valueType, remark, sort);
    }

    /**
     * 播种默认配置（仅缺失时写入）。
     *
     * @param properties 视频配置
     * @param defaults   默认直播目录
     */
    private void seedIfAbsent(VideoProperties properties, LiveCatalog defaults) {
        List<VideoSettingStore.Row> rows = store.list(GROUP);
        java.util.Set<String> existing = new java.util.HashSet<>();
        for (VideoSettingStore.Row row : rows) {
            if (row.name() != null) {
                existing.add(row.name());
            }
        }
        boolean hasCatalog = existing.contains(NAME_LIVE_CATALOG)
                || existing.stream().anyMatch(name -> name.startsWith(PREFIX_CHANNEL));
        if (!hasCatalog && defaults != null && !defaults.isEmpty()) {
            store.upsert(GROUP, NAME_LIVE_CATALOG, toJson(defaults), "JSON", "直播频道目录", 10);
        }
        seedScalar(existing, NAME_CATALOG_URL, properties.getCatalogUrl(),
                "String", "视频资源站采集接口地址", 1);
        seedScalar(existing, NAME_DEFAULT_KEYWORD, properties.getDefaultKeyword(),
                "String", "兜底检索关键词", 2);
        try {
            seedScalar(existing, NAME_BROWSE_KEYWORDS, MAPPER.writeValueAsString(properties.getBrowseKeywords()),
                    "JSON", "分类浏览关键词映射", 3);
        } catch (Exception e) {
            log.warn("[Video] 分类关键词播种失败: {}", e.getMessage());
        }
    }

    /**
     * 播种单个标量配置。
     *
     * @param existing  已存在的配置名称
     * @param name      名称
     * @param value     值
     * @param valueType 值类型
     * @param remark    备注
     * @param sort      排序号
     */
    private void seedScalar(java.util.Set<String> existing, String name, String value,
                            String valueType, String remark, int sort) {
        if (!existing.contains(name)) {
            store.upsert(GROUP, name, value == null ? "" : value, valueType, remark, sort);
        }
    }

    /**
     * 从存储加载快照。
     */
    private void load() {
        List<VideoSettingStore.Row> rows = store.list(GROUP);
        Map<String, String> next = new LinkedHashMap<>();
        for (VideoSettingStore.Row row : rows) {
            if (row.name() != null && row.value() != null) {
                next.put(row.name(), row.value());
            }
        }
        this.values = next;
        String json = next.get(NAME_LIVE_CATALOG);
        this.catalog = json == null || json.isBlank()
                ? buildCatalog(rows)
                : parseCatalog(json);
    }

    /**
     * 解析整段目录 JSON。
     *
     * @param json JSON 文本
     * @return 目录
     */
    private LiveCatalog parseCatalog(String json) {
        try {
            LiveCatalog parsed = MAPPER.readValue(json, LiveCatalog.class);
            if (parsed == null) {
                return new LiveCatalog();
            }
            normalize(parsed);
            return parsed;
        } catch (Exception e) {
            log.warn("[Video] 直播目录 JSON 解析失败: {}", e.getMessage());
            return new LiveCatalog();
        }
    }

    /**
     * 由旧版逐行配置组装目录（兼容回退）。
     *
     * @param rows 配置行
     * @return 目录
     */
    private LiveCatalog buildCatalog(List<VideoSettingStore.Row> rows) {
        Map<String, LiveGroup> groups = new LinkedHashMap<>();
        Map<String, LiveChannel> channels = new LinkedHashMap<>();
        Map<String, TreeMap<Integer, String>> sources = new LinkedHashMap<>();
        List<VideoSettingStore.Row> ordered = new ArrayList<>(rows);
        ordered.sort(Comparator.comparingInt(VideoSettingStore.Row::sort));
        for (VideoSettingStore.Row row : ordered) {
            String name = row.name();
            if (name == null) {
                continue;
            }
            if (name.startsWith(PREFIX_GROUP)) {
                String id = name.substring(PREFIX_GROUP.length());
                JsonNode node = readJson(row.value());
                LiveGroup group = groups.computeIfAbsent(id, k -> {
                    LiveGroup g = new LiveGroup();
                    g.setId(k);
                    return g;
                });
                group.setLabel(node.path("label").asText(id));
                group.setCctv(node.path("cctv").asBoolean(false));
                group.setSort(node.path("sort").asInt(row.sort()));
            } else if (name.startsWith(PREFIX_CHANNEL)) {
                String id = name.substring(PREFIX_CHANNEL.length());
                JsonNode node = readJson(row.value());
                LiveChannel channel = channels.computeIfAbsent(id, k -> {
                    LiveChannel c = new LiveChannel();
                    c.setId(k);
                    return c;
                });
                channel.setName(node.path("name").asText(id));
                channel.setGroup(node.path("group").asText(""));
                channel.setSort(node.path("sort").asInt(row.sort()));
            } else if (name.startsWith(PREFIX_SOURCE)) {
                parseSource(name, row, sources);
            }
        }
        for (Map.Entry<String, TreeMap<Integer, String>> entry : sources.entrySet()) {
            LiveChannel channel = channels.get(entry.getKey());
            if (channel != null) {
                channel.setSources(new ArrayList<>(entry.getValue().values()));
            }
        }
        LiveCatalog result = new LiveCatalog();
        result.setGroups(new ArrayList<>(groups.values()));
        result.setChannels(new ArrayList<>(channels.values()));
        normalize(result);
        return result;
    }

    /**
     * 归一化目录：回填分组名称 / 央视标记 / 频道数，并按 sort 排序。
     *
     * @param target 目录
     */
    private void normalize(LiveCatalog target) {
        Map<String, LiveGroup> groups = new LinkedHashMap<>();
        if (target.getGroups() != null) {
            for (LiveGroup group : target.getGroups()) {
                if (group != null && group.getId() != null) {
                    groups.put(group.getId(), group);
                }
            }
        }
        List<LiveChannel> channels = target.getChannels() == null
                ? new ArrayList<>() : new ArrayList<>(target.getChannels());
        channels.removeIf(channel -> channel == null || channel.getId() == null);
        for (LiveChannel channel : channels) {
            LiveGroup group = groups.get(channel.getGroup());
            channel.setGroupLabel(group == null ? channel.getGroup() : group.getLabel());
            channel.setCctv(group != null && group.isCctv());
        }
        for (LiveGroup group : groups.values()) {
            group.setCount((int) channels.stream()
                    .filter(c -> group.getId().equals(c.getGroup()))
                    .count());
        }
        channels.sort(Comparator.comparingInt(LiveChannel::getSort));
        target.setGroups(new ArrayList<>(groups.values()));
        target.setChannels(channels);
    }

    /**
     * 解析旧版候选源行（{@code liveSource.<channelId>.<index>}）。
     *
     * @param name 行名
     * @param row  行
     * @param out  结果累积
     */
    private void parseSource(String name, VideoSettingStore.Row row,
                             Map<String, TreeMap<Integer, String>> out) {
        String rest = name.substring(PREFIX_SOURCE.length());
        int dot = rest.lastIndexOf('.');
        if (dot <= 0 || dot == rest.length() - 1) {
            return;
        }
        String channelId = rest.substring(0, dot);
        int index;
        try {
            index = Integer.parseInt(rest.substring(dot + 1));
        } catch (NumberFormatException e) {
            index = row.sort();
        }
        if (row.value() != null && !row.value().isBlank()) {
            out.computeIfAbsent(channelId, k -> new TreeMap<>()).put(index, row.value());
        }
    }

    /**
     * 解析 JSON 文本，失败返回空节点。
     *
     * @param text JSON 文本
     * @return 节点
     */
    private JsonNode readJson(String text) {
        try {
            JsonNode node = MAPPER.readTree(text == null ? "{}" : text);
            return node == null ? MAPPER.createObjectNode() : node;
        } catch (Exception e) {
            return MAPPER.createObjectNode();
        }
    }

    /**
     * 序列化 JSON。
     *
     * @param value 对象
     * @return JSON 文本
     */
    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
