// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.setting;

import com.chua.starter.video.live.model.LiveCatalog;
import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.live.service.LiveCatalogLoader;
import com.chua.starter.video.properties.VideoProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VideoSettingService} 测试。
 *
 * <p>用内存存储验证：首次用内置目录播种（整段 JSON）、标量解析、目录组装、热加载，
 * 以及存储不可用时的降级。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class VideoSettingServiceTest {

    /**
     * 内存配置存储。
     */
    static class InMemoryStore implements VideoSettingStore {

        /**
         * 分组 → 名称 → 行。
         */
        private final Map<String, Map<String, Row>> data = new LinkedHashMap<>();

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public List<Row> list(String group) {
            return new ArrayList<>(data.getOrDefault(group, Map.of()).values());
        }

        @Override
        public void upsert(String group, String name, String value, String valueType, String remark, int sort) {
            data.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(name, new Row(name, value, sort));
        }

        @Override
        public int deleteByPrefix(String group, String namePrefix) {
            Map<String, Row> rows = data.get(group);
            if (rows == null) {
                return 0;
            }
            int removed = 0;
            for (String key : new ArrayList<>(rows.keySet())) {
                if (key.startsWith(namePrefix)) {
                    rows.remove(key);
                    removed++;
                }
            }
            return removed;
        }
    }

    /**
     * 播种 + 标量解析 + 目录组装 + 255 约束。
     */
    @Test
    void shouldSeedAndLoad() {
        LiveCatalog bundled = new LiveCatalogLoader().loadBundled();
        assertFalse(bundled.isEmpty(), "内置直播目录应可加载");

        VideoProperties properties = new VideoProperties();
        properties.setCatalogUrl("http://catalog.example.com/api.php/provide/vod/");
        properties.setDefaultKeyword("繁花");
        properties.getBrowseKeywords().put("movie", "电影");

        InMemoryStore store = new InMemoryStore();
        VideoSettingService service = new VideoSettingService(store);
        service.initialize(properties, bundled);

        assertTrue(service.available());
        assertEquals("http://catalog.example.com/api.php/provide/vod/",
                service.resolve(VideoSettingService.NAME_CATALOG_URL, null));
        assertEquals("繁花", service.resolve(VideoSettingService.NAME_DEFAULT_KEYWORD, null));
        assertEquals("电影", service.resolveMap(VideoSettingService.NAME_BROWSE_KEYWORDS, Map.of()).get("movie"));

        LiveCatalog catalog = service.catalog();
        assertEquals(bundled.getChannels().size(), catalog.getChannels().size());
        LiveChannel channel = catalog.getChannels().stream()
                .filter(c -> "cctv1".equals(c.getId())).findFirst().orElseThrow();
        assertEquals("央视", channel.getGroupLabel());
        assertTrue(channel.isCctv());
        assertFalse(channel.getSources().isEmpty());

        // 目录以整段 JSON 存储于 sys_setting_value（TEXT），长度远超 255
        String catalogJson = store.list(VideoSettingService.GROUP).stream()
                .filter(r -> VideoSettingService.NAME_LIVE_CATALOG.equals(r.name()))
                .map(VideoSettingStore.Row::value)
                .findFirst().orElseThrow();
        assertTrue(catalogJson.length() > 255, "目录 JSON 应超过 255 字符以验证 TEXT 放宽");
    }

    /**
     * 数据库中修改后热加载生效。
     */
    @Test
    void shouldReloadAfterChange() {
        LiveCatalog bundled = new LiveCatalogLoader().loadBundled();
        VideoProperties properties = new VideoProperties();
        InMemoryStore store = new InMemoryStore();
        VideoSettingService service = new VideoSettingService(store);
        service.initialize(properties, bundled);

        // 改掉整段目录 JSON 中首个频道名称，重新加载应生效
        String catalogJson = store.list(VideoSettingService.GROUP).stream()
                .filter(r -> VideoSettingService.NAME_LIVE_CATALOG.equals(r.name()))
                .map(VideoSettingStore.Row::value)
                .findFirst().orElseThrow();
        String renamed = catalogJson.replaceFirst("\"name\":\"[^\"]*\"", "\"name\":\"改名频道\"");
        store.upsert(VideoSettingService.GROUP, VideoSettingService.NAME_LIVE_CATALOG,
                renamed, "JSON", "", 10);

        service.reload();
        assertTrue(service.catalog().getChannels().stream()
                .anyMatch(c -> "改名频道".equals(c.getName())));
    }

    /**
     * 存储不可用时降级：不解析、目录为空。
     */
    @Test
    void shouldDegradeWhenUnavailable() {
        VideoSettingService service = new VideoSettingService(new VideoSettingStore.Noop());
        assertFalse(service.available());
        assertEquals("fallback", service.resolve("catalogUrl", "fallback"));
        assertTrue(service.catalog().isEmpty());
        assertNull(service.resolve("catalogUrl", null));
    }

    /**
     * 导出快照 → 导入全新环境；overwrite 语义正确。
     */
    @Test
    void shouldExportAndImport() {
        LiveCatalog bundled = new LiveCatalogLoader().loadBundled();
        VideoProperties properties = new VideoProperties();
        properties.setCatalogUrl("http://a.example.com/api.php/provide/vod/");

        InMemoryStore source = new InMemoryStore();
        VideoSettingService src = new VideoSettingService(source);
        src.initialize(properties, bundled);

        VideoConfigSnapshot snapshot = src.exportConfig();
        assertEquals("http://a.example.com/api.php/provide/vod/", snapshot.catalogUrl());
        assertFalse(snapshot.liveCatalog().isEmpty());

        // 导入全新环境
        VideoSettingService target = new VideoSettingService(new InMemoryStore());
        assertTrue(target.importConfig(snapshot, true));
        assertEquals(snapshot.catalogUrl(), target.resolve(VideoSettingService.NAME_CATALOG_URL, null));
        assertEquals(snapshot.liveCatalog().getChannels().size(), target.catalog().getChannels().size());

        // overwrite=false 不覆盖既有项
        target.importConfig(new VideoConfigSnapshot("http://b.example.com/vod/", null, null, null), false);
        assertEquals("http://a.example.com/api.php/provide/vod/",
                target.resolve(VideoSettingService.NAME_CATALOG_URL, null));

        // overwrite=true 覆盖
        target.importConfig(new VideoConfigSnapshot("http://b.example.com/vod/", null, null, null), true);
        assertEquals("http://b.example.com/vod/",
                target.resolve(VideoSettingService.NAME_CATALOG_URL, null));
    }
}