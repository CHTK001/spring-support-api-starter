package com.chua.starter.oauth.sso.support.properties;

import com.chua.common.support.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * OAuth认证服务器配置属性
 *
 * <p>
 * 定义了OAuth认证服务器的所有配置参数，包括令牌管理、安全设置、协议配置等。
 * </p>
 *
 * <h3>配置前缀：</h3>
 * <p>
 * <code>plugin.oauth.server</code>
 * </p>
 *
 * <h3>主要配置项：</h3>
 * <ul>
 * <li><strong>令牌配置</strong> - token名称、cookie名称、过期时间等</li>
 * <li><strong>安全配置</strong> - 加密方式、服务密钥、AK/SK校验等</li>
 * <li><strong>协议配置</strong> - 支持的认证协议列表</li>
 * <li><strong>管理配置</strong> - 令牌管理器、生成器、日志实现等</li>
 * <li><strong>用户配置</strong> - 用户信息校验、在线模式等</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * 
 * <pre>{@code
 * plugin:
 *   oauth:
 *     server:
 *       token-name: x-oauth-token
 *       expire: 86400
 *       protocols: [http, armeria, rsocket, oauth2]
 *       token-management: redis
 * }</pre> token-management: redis
 * }</pre>
 *
 * @since 2022/7/23 14:51
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @author CH
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ConfigurationProperties(prefix = AuthServerProperties.PRE, ignoreUnknownFields = false)
public class AuthServerProperties {

    /**
     * 配置属性前缀
     */
    public static final String PRE = "plugin.oauth.server";

    /**
     * 是否启用OAuth认证服务
     */
    private boolean enable = true;

    /**
     * 签名验证
     */
    private boolean signVerify = true;
    /**
     * 令牌请求头名称
     * <p>
     * 客户端在HTTP请求头中传递访问令牌时使用的字段名
     * </p>
     * <p>
     * 默认值：x-OAuth-令牌
     * </p>
     */
    private String tokenName = "x-oauth-token";

    /**
     * Cookie名称
     * <p>
     * 存储在浏览器Cookie中的令牌字段名
     * </p>
     * <p>
     * 默认值：x-OAuth-Cookie
     * </p>
     */
    private String cookieName = "x-oauth-cookie";

    /**
     * 令牌加密方式
     * <p>
     * 用于加密存储和传输令牌的算法
     * </p>
     * <p>
     * 支持的加密方式：aes、des、rsa等
     * </p>
     * <p>
     * 默认值：aes
     * </p>
     */
    private String encryption = "aes";

    /**
     * 访问令牌过期时间（秒）
     * <p>
     * 访问令牌的有效期，超过此时间令牌将失效
     * </p>
     * <p>
     * 设置为0或负数表示永不过期（不推荐）
     * </p>
     * <p>
     * 默认值：2592000秒（30天/一个月）
     * </p>
     */
    private long expire = 86_400L * 30;

    /**
     * 刷新令牌过期时间（秒）
     * <p>
     * 刷新令牌的有效期，用于获取新的访问令牌
     * </p>
     * <p>
     * 通常比访问令牌的有效期更长
     * </p>
     * <p>
     * 默认值：3456000秒（40天）
     * </p>
     */
    private long refreshExpire = 86_400L * 40;

    /**
     * 令牌自动续期
     * <p>
     * 是否在每次成功认证后重置令牌的过期时间
     * </p>
     * <p>
     * 启用后，活跃用户的令牌将持续有效
     * </p>
     * <p>
     * 默认值：false
     * </p>
     */
    private boolean renew;

    /**
     * 可信代理地址列表
     *
     * <p>只有当传输层来源地址命中该列表（或为回环地址）时，才信任
     * {@code X-Forwarded-For} 里声明的客户端地址；否则一律以传输层地址为准。
     * 直接采信请求头会让调用方随意伪造来源 IP，从而绕过按 IP 的登录限流、
     * 污染登录审计日志。</p>
     *
     * <p>示例：{@code trusted-proxies: [10.0.0.10, 10.0.0.11]}</p>
     */
    private List<String> trustedProxies = new ArrayList<>();

    /**
     * 同一来源 IP 每分钟允许的登录失败次数上限
     *
     * <p>达到上限后该 IP 的登录请求会被直接拒绝，用于阻断跨账号的密码喷洒
     * （按账号锁定挡不住这种攻击）。{@code <=0} 表示关闭该维度。</p>
     *
     * <p><b>默认关闭（0）</b>：如果 oauth 前面有反向代理而没配置
     * {@link #trustedProxies}，所有用户会共享代理 IP 的计数桶，攻击者刷满阈值
     * 就能把整个出口封掉。确认来源地址可信（直连，或代理地址已写入
     * {@code trusted-proxies}）后再开启。</p>
     *
     * <p>只对「可信任来源地址」生效：HTTP 直连或可信代理之后的真实 IP；
     * TCP/KCP/RSocket 拿不到传输层客户端 IP，不会启用该维度。</p>
     */
    private int loginIpFailureMaxPerMinute = 0;

    /**
     * 协议服务器下发的登录 Cookie 是否带 {@code Secure}
     *
     * <p>默认 {@code false} 以兼容内网 HTTP 调试；对外/HTTPS 部署必须置为
     * {@code true}，否则浏览器可能通过明文 HTTP 回传会话 Cookie。</p>
     *
     * <p>无论该值如何，Cookie 始终带 {@code HttpOnly} 与 {@code SameSite=Lax}。</p>
     */
    private boolean cookieSecure = false;

    /**
     * 刷新令牌轮换
     * <p>
     * 使用 refresh令牌 换取新 access令牌 时，是否同时生成新的 refresh令牌（旧的失效）
     * </p>
     * <p>
     * false（默认）：refresh令牌 不轮换，旧 refresh令牌 继续有效，直到自然过期
     * </p>
     * <p>
     * true：每次刷新都生成新的 refresh令牌，旧的立即失效（更安全，但客户端需保存新值）
     * </p>
     * <p>
     * 默认值：false
     * </p>
     */
    private boolean refreshTokenRotation = false;

    /**
     * 服务密钥
     * <p>
     * 用于令牌签名和加密的主密钥
     * </p>
     * <p>
     * 【安全警告】本项<b>刻意不提供默认值</b>。此前出厂值 {@code 1234567890oOlIi0}
     * 写死在源码里，而源码进仓库即等于公开 —— 任何拿到这份代码的人都能用它签出
     * 合法令牌。留空时 {@code AuthServerConfigValidator} 会在启动阶段直接抛
     * IllegalStateException，使应用起不来；这是有意为之：宁可起不来，
     * 也不要用一个「人人都知道」的主密钥跑起来。
     * </p>
     * <p>
     * 部署时注入：{@code plugin.oauth.server.service-key=<随机串>}，或走环境变量
     * {@code OAUTH_SERVICE_KEY}（dev profile 已按此约定占位）。
     * </p>
     */
    private String serviceKey;

    /**
     * 支持的认证协议列表
     * <p>
     * 定义服务器支持的OAuth认证协议类型
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li><strong>tcp</strong> - TCP 帧协议，默认。帧格式 topic:body（如 login:{...}）</li>
     * <li><strong>http</strong> - HTTP协议，基于RESTful API，兼容性好</li>
     * <li><strong>oauth2</strong> - OAuth 2.0标准协议，符合RFC 6749规范</li>
     * <li><strong>ipc</strong> - IPC协议，基于Unix Domain Socket本地进程间通信，单机场景性能极高</li>
     * <li><strong>udp</strong> - UDP协议，无连接，极致性能</li>
     * <li><strong>custom</strong> - 自定义协议</li>
     * <li><strong>jwt</strong> - JWT令牌协议</li>
     * <li><strong>jwt-rsa</strong> - JWT RSA签名协议</li>
     * </ul>
     * <p>
     * <p>
     * 默认值：[tcp]。真正决定监听哪些协议的是 {@code plugin.oauth.protocol-server.protocols}
     * （由 {@code OauthProtocolServerManager} 按 {@code type} + {@code protocol} 取 Server），
     * 本列表只被各 {@code *OauthProtocol} 的 {@code contains()} 判断读取。
     * armeria 已于 2026-09-29 整体移除（客户端 ArmeriaProtocol 与
     * 服务端 ArmeriaOauthProtocol / ArmeriaAuthInformation 一并退役）。
     * </p>
     */
    private List<String> protocols = Lists.newArrayList("tcp");

    /**
     * 是否启用AK/SK密钥校验
     * <p>
     * 启用后，客户端需要提供Access 键和Secret 键进行身份验证
     * </p>
     * <p>
     * 适用于API接口的安全访问控制
     * </p>
     * <p>
     * <b>修复</b>：默认值此前为 {@code false}，即「未配置即不校验」。
     * 由于本服务的上下文路径把所有协议入口都收在同一 HTTP 端口上，
     * 关闭 AK/SK 等于让任何匿名请求都能直接调用 /oauth/login、/oauth/verify 等接口。
     * 现改为默认 {@code true}，需要放宽的部署必须显式写出 {@code open-check-ak-sk: false}。
     * </p>
     */
    private boolean openCheckAkSk = true;

    /**
     * 是否启用响应加密
     * <p>
     * 启用后，服务器响应数据将使用AK/SK进行加密
     * </p>
     * <p>
     * 提高数据传输的安全性
     * </p>
     * <p>
     * 默认值：true
     * </p>
     */
    private boolean openReturnCheckAkSk = true;

    /**
     * 是否启用IP白名单认证
     * <p>
     * 启用后，只有在IP白名单中的客户端才能访问
     * </p>
     * <p>
     * 需要配合打开检查aksk使用
     * </p>
     * <p>
     * 默认值：false
     * </p>
     */
    private boolean enableIpWhitelist = false;

    /**
     * IP白名单配置
     * <p>
     * 允许访问的客户端IP地址列表
     * </p>
     * <p>
     * 支持CIDR格式，如：192.168.1.0/24
     * </p>
     * <p>
     * 支持单个IP，如：192.168.1.100
     * </p>
     */
    private List<String> ipWhitelist = new ArrayList<>();

    /**
     * 令牌管理器类型
     * <p>
     * 指定用于存储和管理令牌的实现方式
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li>redis - 使用Redis存储令牌（推荐，支持分布式）</li>
     * <li>memory - 使用内存存储令牌（单机模式）</li>
     * <li>database - 使用数据库存储令牌</li>
     * </ul>
     * <p>
     * 默认值：Redis
     * </p>
     */
    private String tokenManagement = "redis";

    /**
     * 令牌生成器类型
     * <p>
     * 指定用于生成访问令牌和密钥对的算法实现
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li>simple - 简单令牌生成器</li>
     * <li>uuid - 基于UUID的令牌生成器</li>
     * <li>jwt - JWT令牌生成器</li>
     * </ul>
     * <p>
     * 默认值：简单
     * </p>
     */
    private String tokenGeneration = "simple";

    /**
     * 日志记录器类型
     * <p>
     * 指定用于记录认证和授权日志的实现方式
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li>simple - 简单日志记录器</li>
     * <li>database - 数据库日志记录器</li>
     * <li>elasticsearch - ES日志记录器</li>
     * </ul>
     * <p>
     * 默认值：简单
     * </p>
     */
    private String log = "simple";

    /**
     * AK/SK认证管理器
     * <p>
     * 指定用于管理Access 键和Secret 键的实现方式
     * </p>
     * <p>
     * 空字符串表示不启用AK/SK认证
     * </p>
     * <p>
     * 默认值：空字符串
     * </p>
     */
    private String authenticate = "redis";


    /**
     * 用户在线模式
     * <p>
     * 控制用户的登录会话管理策略
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li>SINGLE - 单点登录，同一用户只能在一个地方登录</li>
     * <li>MULTIPLE - 多点登录，同一用户可以在多个地方同时登录（无限制）</li>
     * <li>LIMIT - 限制登录，同一用户最多在指定数量的设备上登录</li>
     * </ul>
     * <p>
     * 默认值：多个
     * </p>
     */
    private Online online = Online.MULTIPLE;

    /**
     * 最大在线设备数量
     * <p>
     * 仅当 online = 限制 时生效
     * </p>
     * <p>
     * 默认值：3
     * </p>
     */
    private int maxOnlineCount = 3;

    /**
     * 用户信息校验模式集合
     * <p>
     * 定义需要进行用户信息校验的场景
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li>login - 登录时校验</li>
     * <li>refresh - 刷新令牌时校验</li>
     * <li>access - 访问资源时校验</li>
     * </ul>
     * <p>
     * 默认值：空集合（不进行额外校验）
     * </p>
     */
    private Set<String> user = new HashSet<>();

    /**
     * Armeria服务器端口
     * <p>
     * Armeria协议服务器监听端口。Armeria 已于 2026-09-29 移除，此字段仅保留占位以兼容
     * 存量配置文件里的同名键；实际不再有协议读取它。
     * </p>
     * <p>
     * 注意：历史上 HTTP 与 OAuth2 协议都误用了这个字段当自己的监听端口，
     * 现已改用 {@link #httpPort}。配置里若仍写 armeria-port，不会再影响任何协议。
     * </p>
     * <p>
     * 默认值：19180
     * </p>
     */
    @Deprecated
    private Integer armeriaPort = 19180;

    /**
     * HTTP / OAuth2 协议服务器监听端口
     * <p>
     * 原本 HTTP 与 OAuth2 协议读取的是 {@link #armeriaPort}（字段名与实际用途不符，
     * 属于历史遗留）。Armeria 移除后改为本字段，语义与名字一致。
     * </p>
     * <p>
     * 默认值：8080
     * </p>
     */
    private Integer httpPort = 8080;

    /**
     * r套接字服务器端口
     * <p>
     * r套接字协议服务器监听端口
     * </p>
     * <p>
     * 默认值：7000
     * </p>
     */
    private Integer rsocketPort = 7000;

    /**
     * IPC 协议 套接字 文件路径
     * <p>
     * 仅当 协议 包含 "ipc" 时生效。IPC 协议使用 Unix Domain 套接字 进行本地进程间通信，
     * 避免 TCP/IP 协议栈开销，适用于客户端与服务端在同一台机器上的场景。
     * </p>
     * <p>
     * 默认值：${Java.io.tmpdir}/OAuth-ipc.sock
     * </p>
     * <p>
     * Linux 示例：/var/运行/OAuth-ipc.sock
     * 窗口 示例：C:/用户/app数据/本地/Temp/OAuth-ipc.sock
     * </p>
     */
    private String ipcSocketPath;

    /**
     * JWT 配置
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * JWT 配置类
     *
     * @since 4.0.0.42
     * @author CH
     */
    @Data
    public static class JwtConfig {
        /**
         * 是否启用 JWT 认证
         */
        private boolean enabled = true;  // 默认启用

        /**
         * JWT 签名算法
         * <p>
         * 支持的算法：
         * </p>
         * <ul>
         * <li>HS256 - HMAC-SHA256（对称加密，默认）</li>
         * <li>HS384 - HMAC-SHA384</li>
         * <li>HS512 - HMAC-SHA512</li>
         * <li>RS256 - RSA-SHA256（非对称加密）</li>
         * <li>RS384 - RSA-SHA384</li>
         * <li>RS512 - RSA-SHA512</li>
         * <li>ES256 - ECDSA-SHA256</li>
         * <li>ES384 - ECDSA-SHA384</li>
         * <li>ES512 - ECDSA-SHA512</li>
         * </ul>
         */
        private String algorithm = "HS256";

        /**
         * JWT 密钥（HS256/HS384/HS512 算法使用）
         * <p>
         * 【安全警告】本项<b>刻意不提供默认值</b>。此前出厂值
         * {@code your-256-bit-secret-key-here-32b} 写死在源码里，攻击者拿到它即可
         * 用 HS256 离线伪造任意用户的合法 JWT —— 这是整个令牌体系最致命的一处硬编码。
         * 留空或使用出厂值时 {@code AuthServerConfigValidator} 会阻止应用启动。
         * </p>
         * <p>
         * 生成方式：{@code openssl rand -base64 48}
         * </p>
         */
        private String secret;

        /**
         * RSA/ECDSA 公钥文件路径 (R/ES 算法使用)
         * <p>
         * 支持 类路径: 前缀, 如: 类路径:JWT/公共.pem
         * </p>
         */
        private String publicKeyPath;

        /**
         * RSA/ECDSA 私钥文件路径 (R/ES 算法使用)
         * <p>
         * 支持 类路径: 前缀, 如: 类路径:JWT/私募.pem
         * </p>
         */
        private String privateKeyPath;

        /**
         * JWT 签发者 (iss claim)
         */
        private String issuer = "oauth-server";

        /**
         * JWT 受众 (aud claim)
         */
        private String audience;

        /**
         * JWT 过期时间（秒）
         * <p>
         * 默认 3600 秒（1小时）
         * </p>
         */
        private long expireSeconds = 3600L;

        /**
         * Refresh 令牌 过期时间（秒）
         * <p>
         * 默认 604800 秒（7天）
         * </p>
         */
        private long refreshExpireSeconds = 604800L;

        /**
         * 是否启用 JWKS 端点
         * <p>
         * 启用后可通过 /.well-known/jwks.json 获取公钥
         * </p>
         */
        private boolean jwksEnabled = false;

        /**
         * 是否将 JWT 存入 Redis（支持主动撤销）
         * <p>
         * 启用后 JWT 可以被主动撤销，但会增加 Redis 访问
         * </p>
         */
        private boolean storeInRedis = false;
    }

    /**
     * 用户在线模式枚举。
     *
     * @since 4.0.0.42
     * @author CH
     */
    public enum Online {
        /**
         * 只允许一个在线
         * <p>新登录会踢掉所有旧设备</p>
         */
        SINGLE,
        /**
         * 允许多个在线（不限制）
         */
        MULTIPLE,
        /**
         * 限制在线数量
         * <p>超过限制时踢掉最早登录的设备</p>
         */
        LIMIT
    }

    /**
     * 实际启用的认证协议（只读派生值，不参与配置绑定）。
     *
     * <p><b>为什么需要它</b>：真正决定 oauth 服务端监听什么的是
     * {@code plugin.oauth.protocol-server.protocols[]}（由 {@code OauthProtocolServerManager}
     * 按 {@code type} + {@code protocol} 取 {@code network.Server} 实现）。
     * 而 {@link #protocols} 这个字段只被 {@code *OauthProtocol} 的 {@code contains()} 判断读取，
     * 而那些类依赖的 {@code com.chua.protocol...Protocol} SPI 全仓零实现、已随本次改造删除，
     * 因此它不再影响任何监听行为。管理端状态、配置校验与健康检查都改为读本方法，
     * 避免对外显示一个与实际监听不一致的协议列表。</p>
     *
     * @return 实际启用的协议名列表；未配置 {@code protocol-server} 时返回 {@link #protocols}
     */
    public List<String> resolveEnabledProtocols() {
        var serverProperties = OauthProtocolServersPropertiesHolder.get();
        if (serverProperties == null || serverProperties.getProtocols() == null
                || serverProperties.getProtocols().isEmpty()) {
            return protocols;
        }
        var enabled = new ArrayList<String>();
        for (OauthProtocolServersProperties.ProtocolServer each : serverProperties.getProtocols()) {
            if (each != null && each.isEnable()) {
                enabled.add(StringUtils.isNotBlank(each.getProtocol())
                        ? each.getProtocol() : each.getType());
            }
        }
        return enabled.isEmpty() ? protocols : enabled;
    }

    /**
     * {@link OauthProtocolServersProperties} 的静态持有者。
     *
     * <p>{@code AuthServerProperties} 是纯配置类，不该为了读另一个配置而注入 Bean；
     * 而 {@code OauthProtocolServersProperties} 是 {@code @Component}，由 Spring 管理。
     * 这里用一个静态持有者把两者接起来，避免给配置类引入 Spring 依赖。</p>
     */
    public static final class OauthProtocolServersPropertiesHolder {

        /**
         * 实际持有的配置实例；由 Spring 在容器就绪后写入
         */
        private static volatile OauthProtocolServersProperties instance;

        private OauthProtocolServersPropertiesHolder() {
        }

        /**
         * 读取当前实例。
         *
         * @return 配置实例；Spring 尚未就绪时返回 {@code null}
         */
        public static OauthProtocolServersProperties get() {
            return instance;
        }

        /**
         * 写入当前实例。
         *
         * @param value {@code OauthProtocolServersProperties} Bean
         */
        public static void set(OauthProtocolServersProperties value) {
            instance = value;
        }
    }

}
