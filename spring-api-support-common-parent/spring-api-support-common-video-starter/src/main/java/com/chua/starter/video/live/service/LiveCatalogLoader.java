package com.chua.starter.video.live.service;

import com.chua.starter.video.live.model.LiveCatalog;
import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.live.model.LiveGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播目录加载器。
 *
 * <p>支持两类来源：</p>
 * <ul>
 *   <li>内置 JSON（类路径 {@code video/live-channels.json}）；</li>
 *   <li>外部文件：{@code *.json} 目录，或 {@code *.m3u/*.m3u8} 标准 IPTV 播放列表
 *       （按 {@code group-title} 分组、同名频道合并为多候选源）。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class LiveCatalogLoader {

    /**
     * 内置频道目录资源路径。
     */
    public static final String DEFAULT_CATALOG = "video/live-channels.json";

    /**
     * JSON 解析器。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 加载内置目录。
     *
     * @return 目录；缺失或解析失败返回空目录
     */
    public LiveCatalog loadBundled() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_CATALOG)) {
            if (is == null) {
                log.warn("[Video] 内置直播目录缺失: {}", DEFAULT_CATALOG);
                return new LiveCatalog();
            }
            return MAPPER.readValue(is, LiveCatalog.class);
        } catch (Exception e) {
            log.error("[Video] 内置直播目录加载失败: {}", e.getMessage());
            return new LiveCatalog();
        }
    }

    /**
     * 加载外部目录文件。
     *
     * @param file 文件路径（.json / .m3u / .m3u8）
     * @return 目录；不可读或解析失败返回空目录
     */
    public LiveCatalog loadFile(String file) {
        if (file == null || file.isBlank()) {
            return new LiveCatalog();
        }
        try {
            Path path = Path.of(file.trim());
            if (!Files.isReadable(path)) {
                log.warn("[Video] 外部直播目录不可读: {}", file);
                return new LiveCatalog();
            }
            String lower = file.toLowerCase();
            if (lower.endsWith(".m3u") || lower.endsWith(".m3u8")) {
                return parseM3u(Files.readString(path));
            }
            return MAPPER.readValue(Files.readString(path), LiveCatalog.class);
        } catch (Exception e) {
            log.error("[Video] 外部直播目录加载失败({}): {}", file, e.getMessage());
            return new LiveCatalog();
        }
    }

    /**
     * 解析标准 IPTV 播放列表。
     *
     * @param text m3u 文本
     * @return 目录
     */
    public LiveCatalog parseM3u(String text) {
        LiveCatalog catalog = new LiveCatalog();
        List<LiveGroup> groupList = new ArrayList<>();
        List<LiveChannel> channelList = new ArrayList<>();
        Map<String, LiveGroup> groupIndex = new LinkedHashMap<>();
        Map<String, LiveChannel> channelIndex = new LinkedHashMap<>();
        String name = null;
        String groupName = null;
        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#EXTINF")) {
                int comma = line.indexOf(',');
                name = comma >= 0 ? line.substring(comma + 1).trim() : "";
                int gi = line.indexOf("group-title=\"");
                if (gi >= 0) {
                    int end = line.indexOf('"', gi + 13);
                    groupName = end > gi ? line.substring(gi + 13, end) : null;
                } else {
                    groupName = null;
                }
                continue;
            }
            if (line.startsWith("#") || name == null) {
                continue;
            }
            String groupId = groupName == null || groupName.isBlank()
                    ? "default" : "s-" + Integer.toHexString(groupName.hashCode());
            final String label = groupName == null || groupName.isBlank() ? "未分组" : groupName;
            LiveGroup group = groupIndex.computeIfAbsent(groupId, k -> {
                LiveGroup g = new LiveGroup();
                g.setId(k);
                g.setLabel(label);
                g.setCctv(label.contains("央视") || label.toLowerCase().contains("cctv"));
                g.setSort(groupIndex.size() + 1);
                groupList.add(g);
                return g;
            });
            String channelId = "m-" + Integer.toHexString(name.hashCode());
            LiveChannel channel = channelIndex.get(channelId);
            if (channel == null) {
                channel = new LiveChannel();
                channel.setId(channelId);
                channel.setName(name);
                channel.setGroup(group.getId());
                channel.setSort(channelList.size() + 1);
                channelIndex.put(channelId, channel);
                channelList.add(channel);
            }
            channel.getSources().add(line);
            name = null;
        }
        catalog.setGroups(groupList);
        catalog.setChannels(channelList);
        return catalog;
    }
}
