package com.chua.starter.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统安全配置属性
 * <p>
 * 用于配置登录安全相关参数，包括账户锁定策略、密码策略、导入导出限制等
 * </p>
 *
 * @since 2024/12/28
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = SystemSecurityProperties.PRE)
public class SystemSecurityProperties {

    /**
     * PRE
    */
    public static final String PRE = "plugin.system.security";

    /**
     * 登录失败最大尝试次数
     * <p>超过此次数后账户将被锁定</p>
     */
    private int maxLoginAttempts = 6;

    /**
     * 账户锁定时间（分钟）
     * <p>账户被锁定后，需等待此时间后才能再次尝试登录</p>
     */
    private int lockoutDurationMinutes = 5;

    /**
     * 登录错误计数过期时间（小时）
     * <p>在此时间内未达到锁定阈值，错误计数会自动清除</p>
     */
    private int loginErrorExpireHours = 24;

    /**
     * 密码盐值长度
     */
    private int saltLength = 14;

    /**
     * 用户导入时的默认密码，由配置文件 plugin.系统.安全性.默认-导入-密码 覆盖。
     * <p>建议部署时显式配置，不在此处硬编码。</p>
     */
    private String defaultImportPassword = "";

    /**
     * 用户导出最大数量限制
     */
    private int maxExportCount = 10000;

    /**
     * 第三方登录回调加密密钥
     * <p>用于加密第三方登录回调时传递的登录信息，密钥长度必须为16位</p>
     */
    private String thirdPartyCallbackKey = "1234567890Oil#@1";

    /**
     * 第三方登录「系统回调地址」白名单（{@code scheme://host[:port]} 前缀）
     *
     * <p>守护 {@code callback} 参数的开放重定向缺陷：回调地址为绝对地址时，
     * 其 {@code scheme://host[:port]} 前缀必须命中本列表，否则拒绝重定向
     * （见 {@code com.chua.starter.service.utils.SystemCallbackPolicy}）。</p>
     *
     * <p><b>默认空 = fail-closed</b>：绝对地址一律拒绝，仅允许站内相对路径
     * （以单个 {@code /} 开头）。前端发送的是「绝对 origin + pathname」，
     * 因此生产与开发环境<b>都必须</b>显式配置，否则三方登录回跳会失败，
     * 例如：</p>
     * <pre>
     * plugin:
     *   system:
     *     security:
     *       allowed-system-callback-hosts:
     *         - http://localhost:8096
     *         - http://192.168.50.198:8096
     * </pre>
     *
     * @see com.chua.starter.service.utils.SystemCallbackPolicy
     */
    private List<String> allowedSystemCallbackHosts = new ArrayList<>();

    /**
     * 租户口令落库加密密钥（AES/CBC/PKCS5Padding，长度必须为 16/24/32 字节）
     *
     * <p>租户口令与用户口令不同：用户口令存 {@code md5(明文+盐)}，不可逆；租户口令
     * 需要下发给租户节点（{@code TenantServerSyncPublisher} → {@code TenantClientSyncHandler}），
     * 所以不能哈希，只能落库时加密、读取时解密。为此这里用<b>可逆</b>加密而非摘要，
     * 以保持同步线格式不变、租户端零改动。</p>
     *
     * <p><b>生产环境必须覆盖</b>：不同环境用不同密钥，且与 {@code thirdPartyCallbackKey} 区分开。
     * 换密钥后旧密文无法解密，需同步重置租户口令。</p>
     *
     * <p>默认值刻意取 <b>32 字节</b>（AES-256）。{@code SecretKeySpec} 只接受 16/24/32 字节，
     * 长度非法会在第一次加密时抛 {@code Invalid AES key length}，而不是启动时报错 ——
     * 那意味着所有租户写接口 500 却查不出根因，故这里必须配成合法长度。</p>
     */
    private String tenantPasswordCipherKey = "ChuaTenantPwdCipherKey2026AbCdEf";

    /**
     * 账号状态守卫的进程内缓存有效期（秒）
     *
     * <p>对应 {@code com.chua.starter.system.support.UserStateCache} 的 L1 缓存。
     * 越小则禁用越及时，但 Redis 访问越多；设为 0 表示关闭 L1、每次请求都读 Redis。
     * 禁用/启用操作会主动失效本实例的 L1，因此该值主要影响<b>其它</b>实例的生效延迟。</p>
     */
    private int userStateLocalTtlSeconds = 5;

    /**
     * 账号状态守卫的 Redis 缓存有效期（秒）
     *
     * <p>多实例共享的状态缓存 TTL。禁用/启用操作会直接删除该键，正常情况下不会等到过期；
     * 这里只是兜底，避免异常路径下脏状态长期滞留。</p>
     */
    private int userStateRedisTtlSeconds = 30;
}
