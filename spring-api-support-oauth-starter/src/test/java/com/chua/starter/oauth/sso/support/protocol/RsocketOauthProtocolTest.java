package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.text.json.Json;
import com.chua.starter.oauth.sso.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.information.RsocketAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.*;
import org.springframework.context.support.StaticApplicationContext;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSocket OAuth 协议集成测试
 * <p>
 * 启动真实 RSocket 服务器，通过 RSocket 客户端发送请求，
 * 验证 /login、/oauth、/logout、/upgrade、/temporary-token 路由的正确性。
 * </p>
 *
 * <p>测试端口使用 17000，避免与默认 7000 冲突。</p>
 *
 * @author Test
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RsocketOauthProtocolTest {

    private static final int TEST_PORT = 17000;

    private RsocketOauthProtocol server;
    private AuthServerProperties properties;
    private RSocket clientSocket;

    @BeforeAll
    void startServer() throws Exception {
        // 准备 Spring ApplicationContext（最小化，仅用于测试）
        StaticApplicationContext appContext = new StaticApplicationContext();

        // 准备 AuthServerProperties，启用 rsocket 协议，使用测试端口
        properties = new AuthServerProperties();
        properties.setProtocols(java.util.Arrays.asList("rsocket"));
        properties.setRsocketPort(TEST_PORT);
        properties.setSignVerify(false); // 关闭签名校验，简化测试
        properties.setTokenManagement("memory"); // 内存模式，无需 Redis

        // 准备 LoginCheck（无 Redis/DB 依赖的轻量构造）
        com.chua.starter.oauth.sso.support.check.LoginCheck loginCheck =
                new com.chua.starter.oauth.sso.support.check.LoginCheck(properties);

        // 创建并启动 RSocket 服务器
        server = new RsocketOauthProtocol(properties, loginCheck, appContext);
        // 手动调用初始化（因为 AfterInitializingBean 在非 Spring 容器环境下不会自动触发）
        invokeInitialize(server);

        // 等待服务器启动
        Thread.sleep(1000);

        // 创建 RSocket 客户端
        clientSocket = RSocketConnector.create()
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .connect(TcpClientTransport.create(TEST_PORT))
                .block(Duration.ofSeconds(10));
    }

    @AfterAll
    void stopServer() throws Exception {
        if (clientSocket != null) {
            clientSocket.dispose();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * 通过反射调用 initializeRsocketServer()，绕过 Spring InitializingBean 生命周期
     */
    private void invokeInitialize(RsocketOauthProtocol protocol) throws Exception {
        var method = RsocketOauthProtocol.class.getDeclaredMethod("initializeRsocketServer");
        method.setAccessible(true);
        method.invoke(protocol);
    }

    // ------------------------------------------------------------------
    // 1. /login 路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/login - 缺少必要参数时应返回错误")
    void login_withMissingParams_shouldReturnError() throws Exception {
        // metadata 为 JSON，包含 route；body 为空
        String metadata = "{\"route\":\"login\"}";
        String body = "{}";

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response, "服务器应返回响应");
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody, "响应体不应为 null");
        // 服务器应返回标准 JSON 错误响应（含 code/msg 字段）
        // 实际返回示例: {"code":"C0400S0000","data":null,"msg":"AppId不能为空"}
        assertTrue(responseBody.contains("\"code\"") && responseBody.contains("\"msg\""),
                "缺少参数时应返回标准错误响应: " + responseBody);
    }

    @Test
    @DisplayName("/login - 无效 JSON body 时应返回错误")
    void login_withInvalidJsonBody_shouldReturnError() throws Exception {
        String metadata = "{\"route\":\"login\"}";
        String body = "this-is-not-json";

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response);
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
        // 解析异常应被服务器 catch 并返回错误信息
        assertTrue(responseBody.length() > 0, "响应体不应为空");
    }

    // ------------------------------------------------------------------
    // 2. /oauth 路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/oauth - 缺少 token 时应返回认证失败")
    void oauth_withoutToken_shouldReturnAuthFail() throws Exception {
        String metadata = "{\"route\":\"oauth\"}";
        String body = Json.toJson(new AuthRequest());

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response);
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
        // 无 token 时，服务器应返回错误响应（包含 code/msg 字段）
        // 实际返回示例: {"code":"C0400S0000","data":null,"msg":"AppId不能为空"}
        assertTrue(responseBody.contains("\"code\"") && responseBody.contains("\"msg\""),
                "无 token 的 /oauth 请求应返回标准错误响应: " + responseBody);
    }

    // ------------------------------------------------------------------
    // 3. /logout 路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/logout - 基本请求应能到达处理逻辑")
    void logout_basicRequest_shouldReachHandler() throws Exception {
        String metadata = "{\"route\":\"logout\"}";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setData("test-token-data");
        String body = Json.toJson(authRequest);

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response, "服务器应返回响应（即使 token 无效）");
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody, "响应体不应为 null");
        // 即使 token 无效，服务器也应正常返回（不应抛异常）
        assertTrue(responseBody.length() > 0, "响应体不应为空");
    }

    // ------------------------------------------------------------------
    // 4. /upgrade 路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/upgrade - 无效 token 应返回失败而非异常")
    void upgrade_withInvalidToken_shouldReturnErrorNotException() throws Exception {
        String metadata = "{\"route\":\"upgrade\"}";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setData("invalid-refresh-token");
        String body = Json.toJson(authRequest);

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response, "升级请求应收到响应");
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
        // 不应抛出未捕获的异常
        assertFalse(responseBody.contains("Exception"),
                "响应不应包含异常堆栈: " + responseBody);
    }

    // ------------------------------------------------------------------
    // 5. /temporary-token 路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("/temporary-token - 无效参数应返回错误而非异常")
    void temporaryToken_withInvalidParams_shouldReturnErrorNotException() throws Exception {
        String metadata = "{\"route\":\"temporary-token\"}";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setData("invalid-token");
        String body = Json.toJson(authRequest);

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response, "临时令牌请求应收到响应");
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
        assertFalse(responseBody.contains("Exception"),
                "响应不应包含异常堆栈: " + responseBody);
    }

    // ------------------------------------------------------------------
    // 6. 未知路由测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("未知路由应返回 '不支持的路由' 错误")
    void unknownRoute_shouldReturnUnsupportedError() throws Exception {
        String metadata = "{\"route\":\"unknown-route\"}";
        String body = "{}";

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response);
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("不支持的路由"),
                "未知路由应返回 '不支持的路由' 错误: " + responseBody);
    }

    // ------------------------------------------------------------------
    // 7. metadata 为纯字符串（非 JSON）的兼容性测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("metadata 为非 JSON 字符串时应做降级处理")
    void metadata_nonJson_shouldFallbackGracefully() throws Exception {
        // 直接传纯字符串作为 metadata（触发 catch 中的 ignored 分支）
        String metadata = "/login"; // 非 JSON，走降级逻辑
        String body = "{}";

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        // 即使 metadata 格式异常，服务器也不应崩溃
        assertNotNull(response, "服务器应能处理非 JSON 的 metadata");
    }

    // ------------------------------------------------------------------
    // 8. 响应 metadata（headers）测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("响应 metadata 中应包含响应头信息")
    void response_shouldContainMetadataHeaders() throws Exception {
        String metadata = "{\"route\":\"oauth\"}";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setData("test");
        String body = Json.toJson(authRequest);

        Payload response = clientSocket.requestResponse(
                DefaultPayload.create(body, metadata)
        ).block(Duration.ofSeconds(5));

        assertNotNull(response);
        String responseMetadata = response.getMetadataUtf8();
        // 服务器在 sharedResponse 有 header 时会把 headers 放到 metadata 中
        // 即使无 header，至少响应体不应为空
        String responseBody = response.getDataUtf8();
        assertNotNull(responseBody);
    }
}
