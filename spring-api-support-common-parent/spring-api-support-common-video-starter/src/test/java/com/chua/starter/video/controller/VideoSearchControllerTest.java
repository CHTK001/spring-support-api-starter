// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.controller;

import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.service.VideoPlatformService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link VideoSearchController} 接口测试。
 *
 * <p>以本地 MacCMS 桩驱动真实 {@link VideoPlatformService}，通过 MockMvc 校验
 * 列表、分类过滤、详情与未命中的统一信封。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class VideoSearchControllerTest {

    /**
     * 本地采集接口桩。
     */
    private HttpServer server;

    /**
     * MockMvc 请求入口。
     */
    private MockMvc mockMvc;

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
                  "vod_pic": "http://img.example.com/a.jpg",
                  "vod_score": "8.8",
                  "vod_year": "2024",
                  "vod_area": "大陆",
                  "vod_class": "动作",
                  "type_name": "电影",
                  "vod_play_from": "线路一",
                  "vod_play_url": "第01集$http://v.example.com/1.m3u8"
                }
              ]
            }
            """;

    /**
     * 启动本地桩并装配 MockMvc。
     *
     * @throws Exception 启动失败
     */
    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String body = query != null && query.contains("ids=999") ? "{\"list\":[]}" : SAMPLE;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        VideoProperties properties = new VideoProperties();
        properties.setCatalogUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api.php/provide/vod/");
        VideoPlatformService service = new VideoPlatformService(properties);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VideoSearchController(service))
                .build();
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
     * 列表接口：无关键词走分类浏览，返回统一信封。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldListByCatalog() throws Exception {
        mockMvc.perform(get("/video/api/video/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0].videoName").value("测试影片"))
                .andExpect(jsonPath("$.data[0].videoCategory").value("movie"))
                .andExpect(jsonPath("$.data[0].videoUrl").value("http://v.example.com/1.m3u8"));
    }

    /**
     * 列表接口：分类过滤生效（样例为电影，请求动漫应为空）。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldFilterByVideoType() throws Exception {
        mockMvc.perform(get("/video/api/video/list").param("videoType", "anime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data").value(empty()));
    }

    /**
     * 详情接口：命中返回条目。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldReturnDetail() throws Exception {
        mockMvc.perform(get("/video/api/video/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.videoOriginId").value("100"));
    }

    /**
     * 详情接口：未命中返回错误信封。
     *
     * @throws Exception 请求失败
     */
    @Test
    void shouldReturnErrorWhenDetailMissing() throws Exception {
        mockMvc.perform(get("/video/api/video/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not("00000")));
    }
}