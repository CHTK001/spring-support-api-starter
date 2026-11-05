// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.service;

import com.chua.common.support.datasearch.video.model.VideoDownload;
import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.common.support.datasearch.video.model.VideoPlayAddress;
import com.chua.starter.video.properties.VideoProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MacCmsCatalogClient} 解析测试。
 *
 * <p>用本地 HTTP 桩返回标准 MacCMS 采集 JSON，校验列表与详情字段映射
 * （封面、评分、年份、分类、播放线路、下载地址）。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class MacCmsCatalogClientTest {

    /**
     * 本地采集接口桩。
     */
    private HttpServer server;

    /**
     * 被测客户端。
     */
    private MacCmsCatalogClient client;

    /**
     * MacCMS 采集返回样例。
     */
    private static final String SAMPLE = """
            {
              "page": 1,
              "total": 1,
              "list": [
                {
                  "vod_id": 100,
                  "vod_name": "测试影片",
                  "vod_sub": "测试别称",
                  "vod_pic": "http://img.example.com/a.jpg",
                  "vod_score": "8.8",
                  "vod_year": "2024",
                  "vod_area": "大陆",
                  "vod_lang": "国语",
                  "vod_director": "导演甲",
                  "vod_actor": "演员乙",
                  "vod_class": "动作",
                  "type_name": "电影",
                  "vod_remarks": "HD",
                  "vod_play_from": "线路一",
                  "vod_play_url": "第01集$http://v.example.com/1.m3u8#第02集$http://v.example.com/2.m3u8",
                  "vod_down_from": "夸克",
                  "vod_down_url": "全集$https://pan.quark.cn/s/abc"
                }
              ]
            }
            """;

    /**
     * 启动本地桩并初始化客户端。
     *
     * @throws Exception 启动失败
     */
    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] body = SAMPLE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        VideoProperties properties = new VideoProperties();
        properties.setCatalogUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api.php/provide/vod/");
        client = new MacCmsCatalogClient(properties);
    }

    /**
     * 停止本地桩。
     */
    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * 列表解析：基础字段 + 分类映射 + 播放/下载地址。
     */
    @Test
    void shouldParseList() {
        List<VideoInfoResult> list = client.list(null, 1);
        assertEquals(1, list.size());

        VideoInfoResult item = list.get(0);
        assertEquals("100", item.getVideoOriginId());
        assertEquals("测试影片", item.getVideoName());
        assertEquals("http://img.example.com/a.jpg", item.getVideoCover());
        assertEquals("movie", item.getVideoCategory());
        assertEquals(2024, item.getVideoYear());
        assertEquals(0, item.getVideoScore().compareTo(new java.math.BigDecimal("8.8")));

        List<?> playAddresses = item.getPlayAddressesList();
        assertNotNull(playAddresses);
        assertFalse(playAddresses.isEmpty());
        VideoPlayAddress line = (VideoPlayAddress) playAddresses.get(0);
        assertEquals("线路一", line.getVideoPlayAddressName());
        assertEquals(2, line.getVideoPlayAddressChannels().size());
        assertEquals("http://v.example.com/1.m3u8", item.getVideoUrl());

        List<?> downloads = item.getDownloadList();
        assertNotNull(downloads);
        assertEquals(1, downloads.size());
        assertEquals("https://pan.quark.cn/s/abc",
                ((VideoDownload) downloads.get(0)).getVideoDownloadUrl());
    }

    /**
     * 详情解析：按 originId 命中单条。
     */
    @Test
    void shouldParseDetail() {
        VideoInfoResult detail = client.detail("100");
        assertNotNull(detail);
        assertEquals("100", detail.getVideoOriginId());
        assertTrue(detail.getVideoUrl().endsWith("1.m3u8"));
    }

    /**
     * 未配置采集地址时不可用。
     */
    @Test
    void shouldBeUnavailableWithoutCatalogUrl() {
        VideoProperties properties = new VideoProperties();
        properties.setCatalogUrl(" ");
        MacCmsCatalogClient disabled = new MacCmsCatalogClient(properties);
        assertFalse(disabled.available());
        assertTrue(disabled.list(null, 1).isEmpty());
    }
}