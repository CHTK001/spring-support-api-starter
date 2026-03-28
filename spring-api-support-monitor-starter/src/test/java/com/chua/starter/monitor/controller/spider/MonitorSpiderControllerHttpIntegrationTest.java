package com.chua.starter.monitor.controller.spider;

import com.chua.starter.monitor.arthas.config.ArthasProxyProperties;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.spider.SpiderOutputTableRequest;
import com.chua.starter.monitor.pojo.spider.SpiderTableStatus;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.monitor.service.data.SystemDataSettingService;
import com.chua.starter.monitor.service.spider.MonitorSpiderConnectionService;
import com.chua.starter.monitor.service.spider.MonitorSpiderDataService;
import com.chua.starter.monitor.service.spider.MonitorSpiderLogService;
import com.chua.starter.monitor.service.spider.MonitorSpiderNodeService;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderOutputTableService;
import com.chua.starter.monitor.service.spider.SpiderSchemaService;
import com.chua.starter.monitor.support.SpiderConsoleSessionSupport;
import com.sun.net.httpserver.HttpServer;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MonitorSpiderController.class)
@Import(MonitorSpiderControllerHttpIntegrationTest.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitorSpiderControllerHttpIntegrationTest {

    private static final String REMOTE_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Spider Demo Http</title>
            </head>
            <body>
              <main id="content">
                <article class="card featured">
                  <h1>Headline</h1>
                  <ul>
                    <li>One</li>
                    <li>Two</li>
                    <li>Three</li>
                  </ul>
                  <a class="nav item" href="/detail">Read now</a>
                </article>
              </main>
            </body>
            </html>
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonitorSpiderTaskService taskService;

    @MockitoBean
    private MonitorSpiderNodeService nodeService;

    @MockitoBean
    private MonitorSpiderConnectionService connectionService;

    @MockitoBean
    private MonitorSpiderDataService dataService;

    @MockitoBean
    private MonitorSpiderLogService logService;

    @MockitoBean
    private SpiderSchemaService spiderSchemaService;

    @MockitoBean
    private SpiderOutputTableService spiderOutputTableService;

    @MockitoBean
    private SystemDataSettingService systemDataSettingService;

    @MockitoBean
    private ArthasProxyProperties arthasProxyProperties;

    private HttpServer server;
    private String pageUrl;

    @BeforeAll
    void startHtmlServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/page", exchange -> {
            byte[] payload = REMOTE_HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(payload);
            }
        });
        server.start();
        pageUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/page";
    }

    @AfterAll
    void stopHtmlServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(taskService, nodeService, connectionService, dataService, logService,
                spiderSchemaService, spiderOutputTableService, systemDataSettingService);
        Mockito.when(systemDataSettingService.list()).thenReturn(List.of());
    }

    @Test
    void anonymousRequestRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/v1/spider/preview").queryParam("url", pageUrl))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("未登录"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void springMvcHostExposesPreviewParseAndXpathEndpoints() throws Exception {
        MockHttpSession session = authenticatedSession();

        mockMvc.perform(get("/v1/spider/preview").queryParam("url", pageUrl).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spider Demo Http"))
                .andExpect(jsonPath("$.data.bodyHtml").value(org.hamcrest.Matchers.containsString("Read now")));

        mockMvc.perform(get("/v1/spider/parse-html")
                        .queryParam("url", pageUrl)
                        .queryParam("maxDepth", "3")
                        .queryParam("maxChildren", "3")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spider Demo Http"))
                .andExpect(jsonPath("$.data.nodeCount").value(6))
                .andExpect(jsonPath("$.data.tree.xpath").value("/html/body[1]"))
                .andExpect(jsonPath("$.data.tree.children[0].xpath").value("/html/body[1]/main[1]"));

        mockMvc.perform(post("/v1/spider/inspect-selector")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "%s",
                                  "selector": "main#content > article.card.featured > a",
                                  "type": "CSS"
                                }
                                """.formatted(pageUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].xpath").value("/html/body[1]/main[1]/article[1]/a[1]"))
                .andExpect(jsonPath("$.data[0].cssSelector").value("main#content > article.card.featured:nth-of-type(1) > a.nav.item:nth-of-type(1)"));

        mockMvc.perform(post("/v1/spider/generate-xpath")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "%s",
                                  "elementPath": "main#content > article.card.featured > a"
                                }
                                """.formatted(pageUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("/html/body[1]/main[1]/article[1]/a[1]"));
    }

    @Test
    void springMvcHostExposesSelectorAndDatasourceEndpoints() throws Exception {
        MockHttpSession session = authenticatedSession();
        SystemDataSetting jdbc = new SystemDataSetting();
        jdbc.setSystemDataSettingId(1);
        jdbc.setSystemDataSettingName("业务库");
        jdbc.setSystemDataSettingType("JDBC");
        jdbc.setSystemDataSettingServer("jdbc:mysql://127.0.0.1:3306/app");
        jdbc.setSystemDataSettingDatabase("app");
        jdbc.setSystemDataSettingEnabled(true);
        Mockito.when(systemDataSettingService.list()).thenReturn(List.of(jdbc));

        mockMvc.perform(get("/v1/spider/data-source/options").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].builtin").value(true))
                .andExpect(jsonPath("$.data[0].name").value("当前监控库"))
                .andExpect(jsonPath("$.data[1].name").value("业务库"));

        mockMvc.perform(post("/v1/spider/test-selector")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "%s",
                                  "selector": "//*[@id='content']//li",
                                  "type": "XPATH"
                                }
                                """.formatted(pageUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("One"))
                .andExpect(jsonPath("$.data[1]").value("Two"))
                .andExpect(jsonPath("$.data[2]").value("Three"));
    }

    @Test
    void springMvcHostExposesSchemaAndOutputTableEndpoints() throws Exception {
        MockHttpSession session = authenticatedSession();
        SpiderTableStatus status = new SpiderTableStatus();
        status.setReady(false);
        status.setTables(Map.of("monitor_spider_task", true, "monitor_spider_data", false));
        status.setMissingTables(List.of("monitor_spider_data"));
        status.setMissingColumns(Map.of("monitor_spider_data", List.of("spider_data_hash")));
        Mockito.when(spiderSchemaService.checkTableStatus()).thenReturn(status);

        String ddl = """
                CREATE TABLE `spider_result` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                  PRIMARY KEY (`id`)
                );
                """.trim();
        Mockito.when(spiderOutputTableService.previewCreateTableSql(
                eq("spider_result"),
                any(SpiderOutputTableRequest.class),
                eq("mysql")))
                .thenReturn(ddl);

        mockMvc.perform(get("/v1/spider/table/status").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ready").value(false))
                .andExpect(jsonPath("$.data.missingTables[0]").value("monitor_spider_data"));

        mockMvc.perform(post("/v1/spider/output/table/preview-sql")
                        .session(session)
                        .queryParam("tableName", "spider_result")
                        .queryParam("dbType", "mysql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tableComment": "抓取结果表",
                                  "columns": [
                                    {
                                      "name": "id",
                                      "type": "BIGINT",
                                      "nullable": false,
                                      "primaryKey": true,
                                      "autoIncrement": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ddl));
    }

    private MockHttpSession authenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        SpiderConsoleSessionSupport.markAuthenticated(session, "admin");
        return session;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        SpiderProperties spiderProperties() {
            SpiderProperties properties = new SpiderProperties();
            properties.setRequestTimeoutSeconds(5);
            properties.setPreviewMaxDepth(5);
            properties.setPreviewMaxChildren(5);
            properties.setPreviewTextLimit(200);
            properties.getWebAuth().setMode("embedded");
            return properties;
        }

        @Bean
        DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
            return new SqlSessionFactoryBuilder().build(configuration);
        }
    }
}
