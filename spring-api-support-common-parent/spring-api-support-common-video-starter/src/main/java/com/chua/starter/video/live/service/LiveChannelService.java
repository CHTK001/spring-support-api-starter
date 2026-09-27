package com.chua.starter.video.live.service;

import com.chua.starter.video.live.model.LiveCatalog;
import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.live.model.LiveGroup;
import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.setting.VideoSettingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播频道目录服务。
 *
 * <p>目录来源优先级：配置数据库（{@code sys_setting}，group={@code video}）
 * &gt; 外部文件 {@code spring.video.live-channel-file}
 * &gt; 内置 {@code video/live-channels.json}。</p>
 *
 * <p>首次启动由 {@link VideoSettingService} 以内置/外部目录播种数据库，之后以数据库为准；
 * 通过 {@link #reload()} 可在配置变更后热加载。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class LiveChannelService implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 视频配置。
     */
    private final VideoProperties properties;

    /**
     * 目录加载器。
     */
    private final LiveCatalogLoader loader;

    /**
     * 视频配置数据库服务。
     */
    private final VideoSettingService settings;

    /**
     * 分组（保持顺序）。
     */
    private final Map<String, LiveGroup> groups = new LinkedHashMap<>();

    /**
     * 频道（保持顺序）。
     */
    private final Map<String, LiveChannel> channels = new LinkedHashMap<>();

    /**
     * 构造直播频道目录服务。
     *
     * @param properties 视频配置
     * @param loader     目录加载器
     * @param settings   视频配置数据库服务
     */
    public LiveChannelService(VideoProperties properties, LiveCatalogLoader loader, VideoSettingService settings) {
        this.properties = properties;
        this.loader = loader;
        this.settings = settings;
    }

    /**
     * 启动加载（配置数据库不可用时回退文件/内置）。
     */
    @PostConstruct
    public void init() {
        try {
            if (settings.available()) {
                settings.initialize(properties, resolveDefaults());
            }
            apply(resolveTarget(resolveDefaults()));
        } catch (Exception e) {
            log.warn("[Video] 直播目录初始化失败，回退内置目录: {}", e.getMessage());
            apply(resolveDefaults());
        }
        log.info("[Video] 直播频道目录加载完成: 分组={}, 频道={}, 存储={}",
                groups.size(), channels.size(), settings.available() ? "db" : "json");
    }

    /**
     * 应用就绪后再加载一次：确保数据库表结构就绪（配置服务比本模块更晚初始化时），
     * 完成默认配置播种与目录热加载。
     *
     * @param event 应用就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (settings.available()) {
            reload();
        }
    }

    /**
     * 热加载目录（配置变更后调用）。
     */
    public synchronized void reload() {
        LiveCatalog defaults = resolveDefaults();
        if (settings.available()) {
            settings.initialize(properties, defaults);
        }
        apply(resolveTarget(defaults));
        log.info("[Video] 直播频道目录已热加载: 分组={}, 频道={}", groups.size(), channels.size());
    }

    /**
     * 分组列表。
     *
     * @return 分组（带频道数量）
     */
    public List<LiveGroup> groups() {
        return new ArrayList<>(groups.values());
    }

    /**
     * 频道列表。
     *
     * @param group 分组标识，可空
     * @param cctv  仅央视分组；为 null 表示不过滤
     * @return 频道列表
     */
    public List<LiveChannel> channels(String group, Boolean cctv) {
        return channels.values().stream()
                .filter(c -> group == null || group.isBlank() || group.equals(c.getGroup()))
                .filter(c -> cctv == null || cctv == c.isCctv())
                .sorted(Comparator.comparingInt(LiveChannel::getSort))
                .toList();
    }

    /**
     * 按标识取频道。
     *
     * @param id 频道标识
     * @return 频道，未找到返回 null
     */
    public LiveChannel get(String id) {
        return id == null ? null : channels.get(id);
    }

    /**
     * 取频道指定下标的候选源。
     *
     * @param id    频道标识
     * @param index 候选下标
     * @return 流地址，越界返回 null
     */
    public String source(String id, int index) {
        LiveChannel channel = get(id);
        if (channel == null || channel.getSources() == null
                || index < 0 || index >= channel.getSources().size()) {
            return null;
        }
        return channel.getSources().get(index);
    }

    /**
     * 解析默认目录（外部文件优先，其次内置）。
     *
     * @return 目录
     */
    private LiveCatalog resolveDefaults() {
        String file = properties.getLiveChannelFile();
        if (file != null && !file.isBlank()) {
            LiveCatalog external = loader.loadFile(file);
            if (!external.isEmpty()) {
                return external;
            }
        }
        return loader.loadBundled();
    }

    /**
     * 解析生效目录（数据库优先，其次默认）。
     *
     * @param defaults 默认目录
     * @return 目录
     */
    private LiveCatalog resolveTarget(LiveCatalog defaults) {
        if (settings.available()) {
            LiveCatalog fromDb = settings.catalog();
            if (fromDb != null && !fromDb.isEmpty()) {
                return fromDb;
            }
        }
        return defaults;
    }

    /**
     * 应用目录到内存索引。
     *
     * @param catalog 目录
     */
    private void apply(LiveCatalog catalog) {
        groups.clear();
        channels.clear();
        if (catalog == null) {
            return;
        }
        if (catalog.getGroups() != null) {
            for (LiveGroup group : catalog.getGroups()) {
                if (group != null && group.getId() != null) {
                    groups.put(group.getId(), group);
                }
            }
        }
        if (catalog.getChannels() != null) {
            for (LiveChannel channel : catalog.getChannels()) {
                if (channel == null || channel.getId() == null
                        || channel.getSources() == null || channel.getSources().isEmpty()) {
                    continue;
                }
                LiveGroup group = groups.get(channel.getGroup());
                channel.setGroupLabel(group == null ? channel.getGroup() : group.getLabel());
                channel.setCctv(group != null && group.isCctv());
                channels.put(channel.getId(), channel);
            }
        }
        for (LiveGroup group : groups.values()) {
            group.setCount((int) channels.values().stream()
                    .filter(c -> group.getId().equals(c.getGroup()))
                    .count());
        }
    }
}
