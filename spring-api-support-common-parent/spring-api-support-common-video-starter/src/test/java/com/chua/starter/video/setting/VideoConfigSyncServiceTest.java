package com.chua.starter.video.setting;

import com.chua.starter.video.live.service.LiveCatalogLoader;
import com.chua.starter.video.properties.VideoProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VideoConfigSyncService} 测试。
 *
 * <p>本地 HTTP 桩提供配置快照，验证：拉取并导入远程快照、热加载生效，
 * 以及未配置远程地址时仅做本地重载。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class VideoConfigSyncServiceTest {

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
     * 快照桩。
     */
    private HttpServer server;

    /**
     * 启动快照桩。
     *
     * @throws Exception 启动失败
     */
    @BeforeEach
    void setUp() throws Exception {
        String snapshot = """
                {"catalogUrl":"http://synced.example.com/vod/",
                 "defaultKeyword":"同步关键词",
                 "browseKeywords":{"movie":"电影"}}
                """;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/snapshot", exchange -> {
            byte[] body = snapshot.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
    }

    /**
     * 停止桩。
     */
    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * 配置远程地址时：拉取快照并导入，热加载后生效。
     */
    @Test
    void shouldPullRemoteSnapshot() {
        VideoProperties properties = new VideoProperties();
        properties.setConfigSyncUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/snapshot");

        VideoSettingService settings = new VideoSettingService(new InMemoryStore());
        settings.initialize(properties, new LiveCatalogLoader().loadBundled());
        assertEquals("https://cj.lziapi.com/api.php/provide/vod/",
                settings.resolve(VideoSettingService.NAME_CATALOG_URL, null));

        VideoConfigSyncService syncService = new VideoConfigSyncService(settings, properties, null);
        Map<String, Object> result = syncService.sync();

        assertEquals(Boolean.TRUE, result.get("pulled"));
        assertEquals("http://synced.example.com/vod/",
                settings.resolve(VideoSettingService.NAME_CATALOG_URL, null));
        assertEquals("同步关键词",
                settings.resolve(VideoSettingService.NAME_DEFAULT_KEYWORD, null));
        assertEquals("电影",
                settings.resolveMap(VideoSettingService.NAME_BROWSE_KEYWORDS, Map.of()).get("movie"));
    }

    /**
     * 未配置远程地址：仅做本地重载，不拉取。
     */
    @Test
    void shouldReloadOnlyWithoutUrl() {
        VideoProperties properties = new VideoProperties();
        VideoSettingService settings = new VideoSettingService(new InMemoryStore());
        settings.initialize(properties, new LiveCatalogLoader().loadBundled());
        settings.saveCatalog(settings.catalog());

        VideoConfigSyncService syncService = new VideoConfigSyncService(settings, properties, null);
        Map<String, Object> result = syncService.sync();

        assertFalse((Boolean) result.get("pulled"));
        assertEquals("db", result.get("storage"));
        assertTrue(((Number) result.get("liveChannelCount")).intValue() == 0);
    }
}
