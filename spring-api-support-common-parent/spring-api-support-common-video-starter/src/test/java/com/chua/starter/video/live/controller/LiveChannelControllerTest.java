package com.chua.starter.video.live.controller;

import com.chua.starter.video.live.service.HlsProxyService;
import com.chua.starter.video.live.service.LiveCatalogLoader;
import com.chua.starter.video.live.service.LiveChannelService;
import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.setting.VideoSettingService;
import com.chua.starter.video.setting.VideoSettingStore;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 直播目录与 HLS 代理接口测试。
 *
 * <p>用本地 HLS 桩（主清单 → 子清单 → 分片/密钥）验证：频道目录接口、
 * 播放列表重写、子清单与分片代理，以及越权地址拦截。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class LiveChannelControllerTest {

    /**
     * 本地 HLS 桩。
     */
    private HttpServer server;

    /**
     * MockMvc 请求入口。
     */
    private MockMvc mockMvc;

    /**
     * 使用临时目录承载外部频道目录文件。
     */
    @TempDir
    Path tempDir;

    /**
     * 启动 HLS 桩与目录，装配 MockMvc。
     *
     * @throws Exception 启动失败
     */
    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/live/index.m3u8", exchange -> respond(exchange, "application/vnd.apple.mpegurl",
                "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1000000\nmedia.m3u8\n"));
        server.createContext("/live/media.m3u8", exchange -> respond(exchange, "application/vnd.apple.mpegurl",
                "#EXTM3U\n#EXT-X-KEY:METHOD=AES-128,URI=\"key.bin\"\n#EXTINF:10,\nseg1.ts\n"));
        server.createContext("/live/seg1.ts", exchange -> respond(exchange, "video/mp2t", "SEGMENTDATA"));
        server.createContext("/live/key.bin", exchange -> respond(exchange, "application/octet-stream", "0123456789abcdef"));
        server.start();

        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        Path catalog = tempDir.resolve("live.json");
        Files.writeString(catalog, """
                {
                  "groups": [{ "id": "cctv", "label": "央视", "cctv": true, "sort": 1 }],
                  "channels": [{
                    "id": "t1", "name": "测试台", "group": "cctv", "sort": 1,
                    "sources": ["%s/live/index.m3u8"]
                  }]
                }
                """.formatted(base), StandardCharsets.UTF_8);

        VideoProperties properties = new VideoProperties();
        properties.setLiveChannelFile(catalog.toString());
        LiveChannelService channelService = new LiveChannelService(properties, new LiveCatalogLoader(),
                new VideoSettingService(new VideoSettingStore.Noop()));
        channelService.init();
        HlsProxyService proxyService = new HlsProxyService(properties, channelService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LiveChannelController(channelService, proxyService))
                .build();
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
     * 频道目录接口返回分组与频道。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldListChannels() throws Exception {
        mockMvc.perform(get("/video/api/live/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0].id").value("t1"))
                .andExpect(jsonPath("$.data[0].cctv").value(true));
        mockMvc.perform(get("/video/api/live/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].count").value(1));
    }

    /**
     * 主清单被重写为代理地址。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldRewritePlaylist() throws Exception {
        mockMvc.perform(get("/video/api/live/t1/play.m3u8"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(HlsProxyService.M3U8_CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("resource?src=0&u=")));
    }

    /**
     * 子清单与分片经代理返回。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldProxyNestedPlaylistAndSegment() throws Exception {
        String master = mockMvc.perform(get("/video/api/live/t1/play.m3u8"))
                .andReturn().getResponse().getContentAsString();
        String variant = firstResource(master);

        MvcResult media = mockMvc.perform(get("/video/api/live/t1/resource").param("src", "0").param("u", variant))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("#EXT-X-KEY:METHOD=AES-128,URI=\"resource?src=0&u=")))
                .andReturn();
        List<String> resources = allResources(media.getResponse().getContentAsString());
        // 首个为密钥、末个为分片，均已重写为代理地址
        mockMvc.perform(get("/video/api/live/t1/resource").param("src", "0").param("u", resources.get(0)))
                .andExpect(status().isOk())
                .andExpect(content().string("0123456789abcdef"));
        mockMvc.perform(get("/video/api/live/t1/resource").param("src", "0").param("u", resources.get(resources.size() - 1)))
                .andExpect(status().isOk())
                .andExpect(content().string("SEGMENTDATA"));
    }

    /**
     * 越权地址被拒绝（SSRF 防护）。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldRejectForeignHost() throws Exception {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("http://example.com/x.ts".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/video/api/live/t1/resource").param("src", "0").param("u", encoded))
                .andExpect(status().isForbidden());
    }

    /**
     * 从重写清单中提取首个资源参数。
     *
     * @param playlist 清单文本
     * @return u 参数值
     */
    private String firstResource(String playlist) {
        Matcher matcher = Pattern.compile("u=([A-Za-z0-9_-]+)").matcher(playlist);
        assertTrue(matcher.find(), "清单中应包含代理资源地址");
        return matcher.group(1);
    }

    /**
     * 从重写清单中提取全部资源参数。
     *
     * @param playlist 清单文本
     * @return u 参数值列表
     */
    private List<String> allResources(String playlist) {
        Matcher matcher = Pattern.compile("u=([A-Za-z0-9_-]+)").matcher(playlist);
        List<String> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        assertTrue(values.size() >= 2, "清单中应包含密钥与分片两条代理地址");
        return values;
    }

    /**
     * 发送文本响应。
     *
     * @param exchange  交换
     * @param type      内容类型
     * @param body      内容
     * @throws java.io.IOException 写出失败
     */
    private void respond(com.sun.net.httpserver.HttpExchange exchange, String type, String body)
            throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", type);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
