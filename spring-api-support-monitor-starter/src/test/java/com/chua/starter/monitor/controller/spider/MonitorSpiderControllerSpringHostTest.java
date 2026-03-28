package com.chua.starter.monitor.controller.spider;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.spider.SelectorTestRequest;
import com.chua.starter.monitor.pojo.spider.SpiderDataSourceOption;
import com.chua.starter.monitor.pojo.spider.XPathGenerateRequest;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.monitor.service.data.SystemDataSettingService;
import com.chua.starter.monitor.service.spider.MonitorSpiderConnectionService;
import com.chua.starter.monitor.service.spider.MonitorSpiderDataService;
import com.chua.starter.monitor.service.spider.MonitorSpiderLogService;
import com.chua.starter.monitor.service.spider.MonitorSpiderNodeService;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderOutputTableService;
import com.chua.starter.monitor.service.spider.SpiderSchemaService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = MonitorSpiderControllerSpringHostTest.TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitorSpiderControllerSpringHostTest {

    private static final String REMOTE_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Spider Demo</title>
            </head>
            <body>
              <header id="top">Header Area</header>
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
              <footer data-role="foot">Footer Area</footer>
            </body>
            </html>
            """;

    private static final String INLINE_HTML = """
            <html>
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
    private MonitorSpiderController controller;

    @Autowired
    private MonitorSpiderTaskService taskService;

    @Autowired
    private MonitorSpiderNodeService nodeService;

    @Autowired
    private MonitorSpiderConnectionService connectionService;

    @Autowired
    private MonitorSpiderDataService dataService;

    @Autowired
    private MonitorSpiderLogService logService;

    @Autowired
    private SpiderSchemaService spiderSchemaService;

    @Autowired
    private SpiderOutputTableService spiderOutputTableService;

    @Autowired
    private SystemDataSettingService systemDataSettingService;

    private HttpServer server;
    private String baseUrl;

    @BeforeAll
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/sample", exchange -> {
            byte[] payload = REMOTE_HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(payload);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    void stopServer() {
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
    void previewLoadsRemoteDocumentFromMinimalSpringHost() {
        ReturnResult<Map<String, Object>> result = controller.preview(baseUrl + "/sample");

        assertThat(result.getData()).isNotNull();
        assertThat(result.getData())
                .containsEntry("title", "Spider Demo")
                .containsEntry("url", baseUrl + "/sample");
        assertThat(String.valueOf(result.getData().get("bodyHtml"))).contains("Header Area", "Read now");
        assertThat(String.valueOf(result.getData().get("baseUrl"))).isEqualTo(baseUrl + "/sample");
    }

    @Test
    void parseHtmlBuildsVisualTreeAndAppliesChildLimit() {
        ReturnResult<Map<String, Object>> result = controller.parseHtml(baseUrl + "/sample", 2, 2);

        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsEntry("title", "Spider Demo");
        assertThat(result.getData()).containsEntry("maxDepth", 2);
        assertThat(result.getData()).containsEntry("maxChildren", 2);
        assertThat(result.getData()).containsEntry("nodeCount", 4);

        @SuppressWarnings("unchecked")
        Map<String, Object> tree = (Map<String, Object>) result.getData().get("tree");
        assertThat(tree)
                .containsEntry("tag", "body")
                .containsEntry("xpath", "/html/body[1]")
                .containsEntry("childCount", 3)
                .containsEntry("childrenTruncated", true)
                .containsEntry("truncatedCount", 1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) tree.get("children");
        assertThat(children).hasSize(2);
        assertThat(children.getFirst()).containsEntry("displayName", "header#top");
        assertThat(children.get(1)).containsEntry("displayName", "main#content");
    }

    @Test
    void selectorApisSupportXpathAndCssWithInlineHtml() {
        SelectorTestRequest xpathRequest = new SelectorTestRequest();
        xpathRequest.setHtml(INLINE_HTML);
        xpathRequest.setSelector("//*[@id='content']//li");
        xpathRequest.setType("XPATH");

        ReturnResult<List<String>> selectorResult = controller.testSelector(xpathRequest);
        assertThat(selectorResult.getData()).containsExactly("One", "Two", "Three");

        SelectorTestRequest cssRequest = new SelectorTestRequest();
        cssRequest.setHtml(INLINE_HTML);
        cssRequest.setSelector("a.nav.item");
        cssRequest.setType("CSS");

        ReturnResult<List<Map<String, Object>>> inspectResult = controller.inspectSelector(cssRequest);
        assertThat(inspectResult.getData()).hasSize(1);
        assertThat(inspectResult.getData().getFirst())
                .containsEntry("tag", "a")
                .containsEntry("xpath", "/html/body[1]/main[1]/article[1]/a[1]")
                .containsEntry("cssSelector", "main#content > article.card.featured:nth-of-type(1) > a.nav.item:nth-of-type(1)");

        @SuppressWarnings("unchecked")
        Map<String, String> attributes = (Map<String, String>) inspectResult.getData().getFirst().get("attributes");
        assertThat(attributes).containsEntry("href", "/detail");
    }

    @Test
    void generateXpathReturnsExistingXpathAndCanResolveCssSelectionAgainstRemotePage() {
        XPathGenerateRequest existingXpath = new XPathGenerateRequest();
        existingXpath.setElementPath("/html/body[1]/main[1]/article[1]/h1[1]");

        ReturnResult<String> existingResult = controller.generateXpath(existingXpath);
        assertThat(existingResult.getData()).isEqualTo("/html/body[1]/main[1]/article[1]/h1[1]");

        XPathGenerateRequest cssPath = new XPathGenerateRequest();
        cssPath.setUrl(baseUrl + "/sample");
        cssPath.setElementPath("main#content > article.card.featured > h1");

        ReturnResult<String> cssResult = controller.generateXpath(cssPath);
        assertThat(cssResult.getData()).isEqualTo("/html/body[1]/main[1]/article[1]/h1[1]");
    }

    @Test
    void dataSourceOptionsKeepBuiltinDatabaseAndFilterNonJdbcSources() {
        SystemDataSetting jdbc = new SystemDataSetting();
        jdbc.setSystemDataSettingId(11);
        jdbc.setSystemDataSettingName("业务库");
        jdbc.setSystemDataSettingType("JDBC");
        jdbc.setSystemDataSettingServer("jdbc:mysql://127.0.0.1:3306/app");
        jdbc.setSystemDataSettingDatabase("app");
        jdbc.setSystemDataSettingEnabled(true);

        SystemDataSetting jdbcLike = new SystemDataSetting();
        jdbcLike.setSystemDataSettingId(12);
        jdbcLike.setSystemDataSettingName("审计库");
        jdbcLike.setSystemDataSettingType("CUSTOM");
        jdbcLike.setSystemDataSettingServer("jdbc:postgresql://127.0.0.1:5432/audit");
        jdbcLike.setSystemDataSettingDatabase("audit");
        jdbcLike.setSystemDataSettingEnabled(false);

        SystemDataSetting redis = new SystemDataSetting();
        redis.setSystemDataSettingId(13);
        redis.setSystemDataSettingName("缓存");
        redis.setSystemDataSettingType("REDIS");
        redis.setSystemDataSettingServer("redis://127.0.0.1:6379");

        Mockito.when(systemDataSettingService.list()).thenReturn(List.of(jdbc, jdbcLike, redis));

        ReturnResult<List<SpiderDataSourceOption>> result = controller.getDataSourceOptions();

        assertThat(result.getData()).hasSize(3);
        SpiderDataSourceOption builtin = result.getData().getFirst();
        assertThat(builtin.getBuiltin()).isTrue();
        assertThat(builtin.getName()).isEqualTo("当前监控库");
        assertThat(result.getData().stream().map(SpiderDataSourceOption::getName))
                .containsExactly("当前监控库", "业务库", "审计库");
        assertThat(result.getData().stream().map(SpiderDataSourceOption::getType))
                .containsExactly("JDBC", "JDBC", "CUSTOM");
    }

    @Test
    void dataSourceOptionsStillReturnBuiltinWhenDataSettingServiceIsUnavailable() {
        MonitorSpiderController controllerWithoutSettingService = new MonitorSpiderController(
                taskService,
                nodeService,
                connectionService,
                dataService,
                logService,
                new SpiderProperties(),
                spiderSchemaService,
                spiderOutputTableService,
                null
        );

        ReturnResult<List<SpiderDataSourceOption>> result = controllerWithoutSettingService.getDataSourceOptions();

        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getBuiltin()).isTrue();
        assertThat(result.getData().getFirst().getName()).isEqualTo("当前监控库");
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        SpiderProperties spiderProperties() {
            SpiderProperties properties = new SpiderProperties();
            properties.setRequestTimeoutSeconds(5);
            properties.setPreviewMaxDepth(4);
            properties.setPreviewMaxChildren(4);
            properties.setPreviewTextLimit(200);
            return properties;
        }

        @Bean
        MonitorSpiderTaskService taskService() {
            return Mockito.mock(MonitorSpiderTaskService.class);
        }

        @Bean
        MonitorSpiderNodeService nodeService() {
            return Mockito.mock(MonitorSpiderNodeService.class);
        }

        @Bean
        MonitorSpiderConnectionService connectionService() {
            return Mockito.mock(MonitorSpiderConnectionService.class);
        }

        @Bean
        MonitorSpiderDataService dataService() {
            return Mockito.mock(MonitorSpiderDataService.class);
        }

        @Bean
        MonitorSpiderLogService logService() {
            return Mockito.mock(MonitorSpiderLogService.class);
        }

        @Bean
        SpiderSchemaService spiderSchemaService() {
            return Mockito.mock(SpiderSchemaService.class);
        }

        @Bean
        SpiderOutputTableService spiderOutputTableService() {
            return Mockito.mock(SpiderOutputTableService.class);
        }

        @Bean
        SystemDataSettingService systemDataSettingService() {
            return Mockito.mock(SystemDataSettingService.class);
        }

        @Bean
        MonitorSpiderController monitorSpiderController(
                MonitorSpiderTaskService taskService,
                MonitorSpiderNodeService nodeService,
                MonitorSpiderConnectionService connectionService,
                MonitorSpiderDataService dataService,
                MonitorSpiderLogService logService,
                SpiderProperties spiderProperties,
                SpiderSchemaService spiderSchemaService,
                SpiderOutputTableService spiderOutputTableService,
                SystemDataSettingService systemDataSettingService) {
            return new MonitorSpiderController(taskService, nodeService, connectionService, dataService, logService,
                    spiderProperties, spiderSchemaService, spiderOutputTableService, systemDataSettingService);
        }
    }
}
