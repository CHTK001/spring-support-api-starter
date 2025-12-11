package com.chua.starter.oauth.sso.support.properties;

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
 *       protocols: [http, grpc, jwt, jwt-rsa]
 *       token-management: redis
 * }</pre>
 *
 * @author CH
 * @since 2022/7/23 14:51
 * @see org.springframework.boot.context.properties.ConfigurationProperties
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
    private boolean enable = false;

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
     * 默认值：x-oauth-token
     * </p>
     */
    private String tokenName = "x-oauth-token";

    /**
     * Cookie名称
     * <p>
     * 存储在浏览器Cookie中的令牌字段名
     * </p>
     * <p>
     * 默认值：x-oauth-cookie
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
     * 默认值：259200秒（3天）
     * </p>
     */
    private long expire = 86_400L * 3;

    /**
     * 刷新令牌过期时间（秒）
     * <p>
     * 刷新令牌的有效期，用于获取新的访问令牌
     * </p>
     * <p>
     * 通常比访问令牌的有效期更长
     * </p>
     * <p>
     * 默认值：345600秒（4天）
     * </p>
     */
    private long refreshExpire = 86_400L * 4;

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
     * 服务密钥
     * <p>
     * 用于令牌签名和加密的主密钥
     * </p>
     * <p>
     * 【安全警告】生产环境必须配置此值！建议通过环境变量或配置中心注入
     * </p>
     */
    private String serviceKey = "1234567890oOlIi0";

    /**
     * 支持的认证协议列表
     * <p>
     * 定义服务器支持的OAuth认证协议类型
     * </p>
     * <p>
     * 可选值：
     * </p>
     * <ul>
     * <li><strong>armeria</strong> - Armeria协议，基于Netty的高性能异步框架，支持HTTP/2和gRPC，【推荐】</li>
     * <li><strong>http</strong> - HTTP协议，基于RESTful API，兼容性好</li>
     * <li><strong>grpc</strong> - gRPC协议，基于HTTP/2，支持流式通信，性能优异</li>
     * <li><strong>tcp</strong> - TCP协议，二进制传输，高性能</li>
     * <li><strong>udp</strong> - UDP协议，无连接，极致性能</li>
     * <li><strong>custom</strong> - 自定义协议</li>
     * <li><strong>jwt</strong> - JWT令牌协议</li>
     * <li><strong>jwt-rsa</strong> - JWT RSA签名协议</li>
     * </ul>
     * <p>
     * <p>
     * 默认值：[armeria, http] - 默认启用Armeria高性能协议，同时保留HTTP兼容
     * </p>
     */
    private List<String> protocols = Lists.newArrayList("armeria", "http");

    /**
     * 是否启用AK/SK密钥校验
     * <p>
     * 启用后，客户端需要提供Access Key和Secret Key进行身份验证
     * </p>
     * <p>
     * 适用于API接口的安全访问控制
     * </p>
     * <p>
     * 默认值：false
     * </p>
     */
    private boolean openCheckAkSk;

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
     * 需要配合openCheckAkSk使用
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
     * 默认值：redis
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
     * 默认值：simple
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
     * 默认值：simple
     * </p>
     */
    private String log = "simple";

    /**
     * AK/SK认证管理器
     * <p>
     * 指定用于管理Access Key和Secret Key的实现方式
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
     * 服务上下文路径
     * <p>
     * OAuth认证服务的URL路径前缀
     * </p>
     * <p>
     * 例如：/oauth、/auth等
     * </p>
     * <p>
     * 默认值：null（使用根路径）
     * </p>
     */
    private String contextPath;

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
     * 默认值：MULTIPLE
     * </p>
     */
    private Online online = Online.MULTIPLE;

    /**
     * 最大在线设备数量
     * <p>
     * 仅当 online = LIMIT 时生效
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
     * gRPC服务器端口
     * <p>
     * gRPC协议服务器监听端口
     * </p>
     * <p>
     * 默认值：9090
     * </p>
     */
    private Integer grpcPort = 9090;

    /**
     * Armeria服务器端口
     * <p>
     * Armeria协议服务器监听端口
     * </p>
     * <p>
     * 默认值：9091
     * </p>
     */
    private Integer armeriaPort = 9091;

    /**
     * RSocket服务器端口
     * <p>
     * RSocket协议服务器监听端口
     * </p>
     * <p>
     * 默认值：7000
     * </p>
     */
    private Integer rsocketPort = 7000;

    /**
     * 是否启用gRPC TLS加密
     * <p>
     * 生产环境建议启用TLS加密以确保通信安全
     * </p>
     * <p>
     * 默认值：false
     * </p>
     */
    private boolean grpcEnableTls = false;

    /**
     * gRPC TLS证书链文件路径
     * <p>
     * 启用TLS时需要配置证书文件路径
     * </p>
     */
    private String grpcCertChainFile;

    /**
     * gRPC TLS私钥文件路径
     * <p>
     * 启用TLS时需要配置私钥文件路径
     * </p>
     */
    private String grpcPrivateKeyFile;

    /**
     * gRPC最大入站消息大小（字节）
     * <p>
     * 限制客户端发送的单个消息的最大大小
     * </p>
     * <p>
     * 默认值：4MB
     * </p>
     */
    private int grpcMaxInboundMessageSize = 4 * 1024 * 1024;

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

}
