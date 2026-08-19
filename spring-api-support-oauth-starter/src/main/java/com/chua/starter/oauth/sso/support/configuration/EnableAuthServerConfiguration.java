/**
 * OAuth认证服务器自动配置类
 *
 * <p>这是OAuth认证服务器的核心配置类，负责初始化和配置认证服务器的各个组件。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>自动注册认证相关的Bean定义</li>
 *   <li>配置OAuth协议处理器</li>
 *   <li>初始化第三方登录提供者（如Gitee）</li>
 *   <li>设置用户信息解析器</li>
 *   <li>配置响应处理器和消息转换器</li>
 * </ul>
 *
 * <h3>支持的协议：</h3>
 * <ul>
 *   <li>HTTP协议 - 标准HTTP认证</li>
 *   <li>Custom协议 - 自定义认证协议</li>
 *   <li>JWT协议 - JSON Web Token认证</li>
 *   <li>JWT-RSA协议 - 基于RSA签名的JWT认证</li>
 * </ul>
 *
 * <h3>集成的第三方登录：</h3>
 * <ul>
 *   <li>Gitee登录和账号绑定</li>
 *   <li>支持扩展其他第三方登录提供者</li>
 * </ul>
 *
 * <p>该配置类实现了多个Spring接口，确保在应用启动的不同阶段正确初始化各个组件。</p>
 *
 * @author CH
 * @since 2022/7/23 8:51
 * @see AuthServerProperties 认证服务器配置属性
 * @see LoginCheck 登录检查服务
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({
        AuthServerProperties.class,
        ThirdPartyBinderProperties.class,
        CasProperties.class,
        ThirdPartyLoginProperties.class,
        OauthSecurityProperties.class,
        OauthProtocolServersProperties.class
})
public class EnableAuthServerConfiguration