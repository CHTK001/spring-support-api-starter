package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.common.support.utils.NetUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.constant.CommonConstant;
import com.google.common.base.Splitter;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.*;
import com.chua.starter.common.support.api.decode.ApiRequestDecodeSetting;
import com.chua.starter.common.support.api.encode.ApiResponseEncodeConfiguration;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.SysSettingHistoryService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.mapper.SysSettingMapper;
import com.chua.starter.service.setting.GuestDefaultSetting;
import com.chua.starter.service.setting.SmsSetting;
import com.chua.starter.service.setting.SmtpSetting;
import com.chua.starter.service.setting.VersionSetting;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.enums.scope.*;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 *
 * @since 2024/8/7
 * @author CH
 */
@Slf4j
@Service
@Lazy(false)
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, InitializingBean, ApplicationListener<ApplicationReadyEvent> {
    private static final String SETTING_LIST_CACHE_KEY_LEGACY_PREFIX = "sys:setting:";
    private static final String SETTING_LIST_CACHE_KEY_PREFIX = "sys:setting:v2:";

    @Autowired
    @Lazy
    private SysSettingService sysSettingService;
    
    @Autowired
    @Lazy
    private SysSettingHistoryService sysSettingHistoryService;

    /**
     * Socket服务是否启用
     */
    @Value("${plugin.socket.enable:false}")
    private boolean socketEnable;

    /**
     * Socket服务主机地址
     */
    @Value("${plugin.socket.host:localhost}")
    private String socketHost;

    /**
     * Socket服务路径
     */
    @Value("${plugin.socket.room[0].context-path:${plugin.socket.path:/socket.io}}")
    private String socketPath;

    /**
     * Socket服务默认端口
     */
    @Value("${plugin.socket.room[0].port:29181}")
    private int socketPort;

    /**
     * Socket 对外发布主机地址
     */
    @Value("${plugin.socket.publish-host:}")
    private String socketPublishHost;

    /**
     * Socket 对外发布端口
     */
    @Value("${plugin.socket.publish-port:-1}")
    private int socketPublishPort;

    /**
     * Socket服务协议类型
     */
    @Value("${plugin.socket.protocol:socketio}")
    private String socketProtocol;
    
    static final String[] APP_KEY = new String[]{"appKey", "clientId"};
    static final String[] APP_SECRET = new String[]{"appSecret", "clientSecret"};
    static final String[] APP_REDIRECT_URI = new String[]{"redirectUri"};

    /**
     * Socket 配置项名称：SocketUrl
     */
    private static final String SOCKET_SETTING_URL = "SocketUrl";

    /**
     * Socket 配置项名称：SocketOpen
     */
    private static final String SOCKET_SETTING_OPEN = "SocketOpen";

    /**
     * Socket 配置项名称：SocketPath
     */
    private static final String SOCKET_SETTING_PATH = "SocketPath";

    /**
     * Socket 配置项名称：SocketProtocol
     */
    private static final String SOCKET_SETTING_PROTOCOL = "SocketProtocol";

    /**
     * Socket 配置项分组：config
     */
    private static final String SOCKET_SETTING_GROUP = "config";

    /**
     * SocketUrl 配置备注
     */
    private static final String SOCKET_REMARK_URL = "socket.io服务器地址";

    /**
     * SocketOpen 配置备注
     */
    private static final String SOCKET_REMARK_OPEN = "socket.io是否开启";

    /**
     * SocketPath 配置备注
     */
    private static final String SOCKET_REMARK_PATH = "socket.io路径";

    /**
     * SocketProtocol 配置备注
     */
    private static final String SOCKET_REMARK_PROTOCOL = "socket.io协议类型";

    @Override
    public List<SysSetting> list(SysSettingUpdate sysSettingUpdate, Set<String> roles) {
        return baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(StringUtils.isNotBlank(sysSettingUpdate.getSysSettingGroup()), SysSetting::getSysSettingGroup, Splitter.on(",").omitEmptyStrings().trimResults().splitToSet(sysSettingUpdate.getSysSettingGroup()))
                .orderByAsc(SysSetting::getSysSettingSort)
                .orderByDesc(SysSetting::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long settingId) {
        if(null == settingId) {
            return ReturnResult.fail("配置ID不能为空");
        }

        SysSetting sysSetting = baseMapper.selectById(settingId);
        if(null == sysSetting) {
            return ReturnResult.fail("配置不存在, 请刷新后重试!");
        }

        Integer sysRoleInSystem = sysSetting.getSysSettingInSystem();

        if(null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.fail("内置配置不允许删除");
        }

        int b = baseMapper.deleteById(settingId);
        sysSettingService.clearCache(sysSetting);
        // 记录删除历史
        try {
            String operatorId = RequestUtils.getUserId();
            String operatorName = RequestUtils.getUsername();
            sysSettingHistoryService.recordDelete(sysSetting, operatorId, operatorName, UUID.randomUUID().toString().replace("-", ""));
        } catch (Exception e) {
            log.warn("记录配置删除历史失败: {}", e.getMessage());
        }
        return ReturnResult.of(b);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysSetting sysSetting) {
        if (null == sysSetting || null == sysSetting.getSysSettingId()) {
            return ReturnResult.fail("配置ID不能为空");
        }
        SysSetting sysSetting1 = baseMapper.selectById(sysSetting.getSysSettingId());
        if (null == sysSetting1) {
            return ReturnResult.fail("配置不存在, 请刷新后重试!");
        }
        Integer sysRoleInSystem = sysSetting1.getSysSettingInSystem();
        if(!AuthConstant.isSuperAdmin(roles)) {
            if(null != sysRoleInSystem && sysRoleInSystem == 1) {
                return ReturnResult.fail("内置配置不允许修改");
            }
            sysSetting.setSysSettingAppInner(null);
        }

        String sysSettingName = sysSetting.getSysSettingName();
        sysSetting.setSysSettingInSystem(null);
        if(StringUtils.isNotBlank(sysSettingName)) {
            SysSetting sysSetting2 = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingName, sysSettingName)
                    .eq(SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                    .ne(SysSetting::getSysSettingId, sysSetting.getSysSettingId()))
                    ;
            if(null != sysSetting2) {
                return ReturnResult.fail("配置编码已存在");
            }
        }
        SysSetting version = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSysSettingName, "version"));
        List<SysSetting> sysSettings = new LinkedList<>();
        sysSettings.add(sysSetting);
        if (null != version) {
            version.setSysSettingConfig(UUID.randomUUID().toString());
            sysSettings.add(version);
        }
        sysSettingService.updateBatchById(sysSettings);
        // 记录更新历史
        try {
            String operatorId = RequestUtils.getUserId();
            String operatorName = RequestUtils.getUsername();
            sysSettingHistoryService.recordUpdate(sysSetting1, sysSetting, operatorId, operatorName, UUID.randomUUID().toString().replace("-", ""));
        } catch (Exception e) {
            log.warn("记录配置更新历史失败: {}", e.getMessage());
        }

        Integer runtimeStatus = ObjectUtils.defaultIfNull(sysSetting.getSysSettingStatus(), sysSetting1.getSysSettingStatus());
        if (null != runtimeStatus && runtimeStatus == 1) {
            GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            if (null != version) {
                upgradeVersion(version);
            }
        }
        sysSettingService.clearCache(sysSetting);
        return ReturnResult.of(sysSetting);
    }

    @Override
    public ReturnResult<SysSetting> saveForEntity(SysSetting sysSetting, Set<String> roles) {
        sysSetting.setSysSettingInSystem(0);
        if(!AuthConstant.isSuperAdmin(roles)) {
            sysSetting.setSysSettingAppInner(0);
        }

        if(null == sysSetting.getSysSettingStatus()) {
            sysSetting.setSysSettingStatus(1);
        }
        SysSetting setting = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .eq(SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup()));
        if(null != setting) {
            return ReturnResult.fail("配置编码已存在");
        }

        if(baseMapper.insert(sysSetting) > 0) {
            if(sysSetting.getSysSettingStatus() == 1) {
                GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            }
            // 记录新增历史
            try {
                String operatorId = RequestUtils.getUserId();
                String operatorName = RequestUtils.getUsername();
                sysSettingHistoryService.recordCreate(sysSetting, operatorId, operatorName, UUID.randomUUID().toString().replace("-", ""));
            } catch (Exception e) {
                log.warn("记录配置新增历史失败: {}", e.getMessage());
            }
            return ReturnResult.of(sysSettingService.clearCache(sysSetting));
        }
        return ReturnResult.illegal("新增失败");
    }

    @Override
    public IPage<SysSetting> pageForSetting(Query<SysSetting> query, SysSetting sysSetting, Set<String> roles) {
        var queryWrapper = Wrappers.<SysSetting>lambdaQuery()
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingGroup()), SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingName()), SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingRemark()), SysSetting::getSysSettingRemark, sysSetting.getSysSettingRemark())
                .eq(StringUtils.isNotBlank(sysSetting.getSysSettingValueType()), SysSetting::getSysSettingValueType, sysSetting.getSysSettingValueType())
                .eq(null != sysSetting.getSysSettingInSystem(), SysSetting::getSysSettingInSystem, sysSetting.getSysSettingInSystem())
                .orderByDesc(SysSetting::getCreateTime);
        log.debug("设置分页筛选参数: group={}, name={}, remark={}, valueType={}, inSystem={}, sqlSegment={}",
                sysSetting.getSysSettingGroup(),
                sysSetting.getSysSettingName(),
                sysSetting.getSysSettingRemark(),
                sysSetting.getSysSettingValueType(),
                sysSetting.getSysSettingInSystem(),
                queryWrapper.getSqlSegment());
        return baseMapper.selectPage(query.createPage(), queryWrapper);
    }

    @Override
    public ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysSetting> sysSettings) {
        if (null == sysSettings || sysSettings.isEmpty()) {
            return ReturnResult.fail("配置不能为空");
        }
        SysSetting version = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSysSettingName, "version"));
        if (null != version) {
            version.setSysSettingConfig(UUID.randomUUID().toString());
            sysSettings.add(version);
        }
        sysSettingService.updateBatchById(sysSettings);
        for (SysSetting setting : sysSettings) {
            sysSettingService.clearCache(setting);
        }
        register(GlobalSettingFactory.getInstance(), sysSettings);
        return ReturnResult.of(true);
    }

    @Override
    public SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> getAuthRequest(ThirdLoginData loginData) {
        String loginType = StringUtils.trimToEmpty(loginData.getLoginType()).toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(loginType)) {
            log.warn("第三方登录类型为空");
            return null;
        }

        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(SysSetting::getSysSettingGroup, loginType, "sso")
                .eq(SysSetting::getSysSettingStatus, 1)
        );

        Set<SysSetting> sso = sysSettings.stream().filter(item -> StringUtils.equals(item.getSysSettingGroup(), "sso"))
                .filter(item -> StringUtils.equalsIgnoreCase(item.getSysSettingName(), loginType))
                .filter(item -> StringUtils.equals(CommonConstant.TRUE, item.getSysSettingValue())).collect(Collectors.toUnmodifiableSet());

        if(sso.isEmpty()) {
            log.warn("第三方登录开关未开启: loginType={}", loginType);
            return null;
        }

        List<SysSetting> providerSettings = sysSettings.stream()
                .filter(item -> StringUtils.equals(item.getSysSettingGroup(), loginType))
                .toList();

        AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_KEY, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .clientSecret(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_SECRET, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .redirectUri(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_REDIRECT_URI, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .build();
        if (StringUtils.isBlank(authConfig.getClientId())
                || StringUtils.isBlank(authConfig.getClientSecret())
                || StringUtils.isBlank(authConfig.getRedirectUri())) {
            log.warn("第三方登录配置不完整: loginType={}, clientIdPresent={}, clientSecretPresent={}, redirectUriPresent={}",
                    loginType,
                    StringUtils.isNotBlank(authConfig.getClientId()),
                    StringUtils.isNotBlank(authConfig.getClientSecret()),
                    StringUtils.isNotBlank(authConfig.getRedirectUri()));
            return null;
        }

        String state = AuthStateUtils.createState();
        authStateCache.cache(state + "_userId", RequestUtils.getUserId());
        authStateCache.cache(state + "_thirdType", loginData.getThirdType());
        authStateCache.cache(state + "_info", loginData.getLoginCode());
        authStateCache.cache(state + "_clientId", authConfig.getClientId());
        authStateCache.cache(state + "_clientSecret", authConfig.getClientSecret());
        authStateCache.cache(state + "_callback", authConfig.getRedirectUri());
        authStateCache.cache(state + "_system_callback", loginData.getCallback());
        return switch (loginType) {
            case "gitee" -> new SysSettingThirdPartyRequest<>(new AuthGiteeRequest(authConfig, authStateCache), authStateCache, state);
            case "wechat" -> new SysSettingThirdPartyRequest<>(new AuthWeChatOpenRequest(authConfig, authStateCache), authStateCache, state);
            case "wechat_mp" -> new SysSettingThirdPartyRequest<>(new AuthWeChatMpRequest(authConfig, authStateCache), authStateCache, state);
            case "github" -> new SysSettingThirdPartyRequest<>(new AuthGithubRequest(authConfig, authStateCache), authStateCache, state);
            case "qq" -> new SysSettingThirdPartyRequest<>(new AuthQqRequest(authConfig, authStateCache), authStateCache, state);
            case "alipay" -> new SysSettingThirdPartyRequest<>(new AuthAlipayRequest(authConfig, authStateCache), authStateCache, state);
            case "dingtalk" -> new SysSettingThirdPartyRequest<>(new AuthDingTalkRequest(authConfig, authStateCache), authStateCache, state);
            case "wechat_enterprise" -> new SysSettingThirdPartyRequest<>(new AuthWeChatEnterpriseQrcodeRequest(authConfig, authStateCache), authStateCache, state);
            case "weibo" -> new SysSettingThirdPartyRequest<>(new AuthWeiboRequest(authConfig, authStateCache), authStateCache, state);
            case "baidu" -> new SysSettingThirdPartyRequest<>(new AuthBaiduRequest(authConfig, authStateCache), authStateCache, state);
            case "google" -> new SysSettingThirdPartyRequest<>(new AuthGoogleRequest(authConfig, authStateCache), authStateCache, state);
            case "microsoft" -> new SysSettingThirdPartyRequest<>(new AuthMicrosoftRequest(authConfig, authStateCache), authStateCache, state);
            case "taobao" -> new SysSettingThirdPartyRequest<>(new AuthTaobaoRequest(authConfig, authStateCache), authStateCache, state);
            case "douyin" -> new SysSettingThirdPartyRequest<>(new AuthDouyinRequest(authConfig, authStateCache), authStateCache, state);
            case "feishu" -> new SysSettingThirdPartyRequest<>(new AuthFeishuRequest(authConfig, authStateCache), authStateCache, state);
            default -> null;
        };
    }

    private void upgradeVersion(SysSetting version) {
        GlobalSettingFactory.getInstance().set("config", "version", version.getSysSettingVersion());
    }

    /**
     * 清理缓存
     * @param sysSetting 配置
     * @return 是否成功
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'" + SETTING_LIST_CACHE_KEY_LEGACY_PREFIX + "' + #sysSetting.sysSettingGroup", keyGenerator = "customTenantedKeyGenerator"),
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'" + SETTING_LIST_CACHE_KEY_PREFIX + "' + #sysSetting.sysSettingGroup", keyGenerator = "customTenantedKeyGenerator")
    })
    public SysSetting clearCache(SysSetting sysSetting) {
        return sysSetting;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        globalSettingFactory.register("config", new ApiResponseEncodeConfiguration());
        globalSettingFactory.register("config", new ApiRequestDecodeSetting());
        globalSettingFactory.register("config", new VersionSetting());
        globalSettingFactory.register("default", new GuestDefaultSetting());
        globalSettingFactory.register("smtp", new SmtpSetting());
        globalSettingFactory.register("sms", new SmsSetting());
        if (ClassUtils.isPresent("com.chua.report.client.starter.setting.ReportExpireSetting")) {
            globalSettingFactory.register("expire", ClassUtils.forObject("com.chua.report.client.starter.setting.ReportExpireSetting"));
        }
    }

    /**
     * 应用启动完成后加载并注册系统配置。
     *
     * @param event 应用就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        upgrade();
    }

    private void upgrade() {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        try {
            List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingStatus, 1)
            );
            register(globalSettingFactory, list);
            syncSocketConfig();
            log.info("[系统配置][初始化] 系统配置加载完成，共 {} 条配置", list.size());
        } catch (Exception e) {
            log.warn("配置加载失败: {}", e.getMessage());
        }
    }

    /**
     * 同步Socket配置到数据库
     * 将YAML中的socket配置同步到sys_setting表，确保前端获取的配置与后端一致
     */
    private void syncSocketConfig() {
        try {
            // 构建Socket URL
            var publishHost = resolveSocketPublishHost();
            var publishPort = resolveSocketPublishPort();
            var socketUrl = String.format("http://%s:%d", publishHost, publishPort);

            var desired = new LinkedHashMap<String, SysSettingDesiredValue>(4);
            desired.put(SOCKET_SETTING_URL, new SysSettingDesiredValue(socketUrl, SOCKET_REMARK_URL));
            desired.put(SOCKET_SETTING_OPEN, new SysSettingDesiredValue(String.valueOf(socketEnable), SOCKET_REMARK_OPEN));
            desired.put(SOCKET_SETTING_PATH, new SysSettingDesiredValue(socketPath, SOCKET_REMARK_PATH));
            desired.put(SOCKET_SETTING_PROTOCOL, new SysSettingDesiredValue(socketProtocol, SOCKET_REMARK_PROTOCOL));

            // 只查询一次，拿到 Socket 相关配置后统一对比并更新/新增
            var existList = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingGroup, SOCKET_SETTING_GROUP)
                    .in(SysSetting::getSysSettingName, desired.keySet()));

            var existMap = existList.stream().collect(Collectors.toMap(SysSetting::getSysSettingName, it -> it, (a, b) -> a));
            var toUpdate = new ArrayList<SysSetting>(existMap.size());
            var toInsert = new ArrayList<SysSetting>(desired.size());

            for (var entry : desired.entrySet()) {
                var name = entry.getKey();
                var expected = entry.getValue();

                var exist = existMap.get(name);
                if (exist != null) {
                    if (!StringUtils.equals(expected.value(), exist.getSysSettingValue())) {
                        exist.setSysSettingValue(expected.value());
                        toUpdate.add(exist);
                    }
                    continue;
                }

                var newSetting = new SysSetting();
                newSetting.setSysSettingName(name);
                newSetting.setSysSettingGroup(SOCKET_SETTING_GROUP);
                newSetting.setSysSettingValue(expected.value());
                newSetting.setSysSettingRemark(expected.remark());
                newSetting.setSysSettingStatus(1);
                newSetting.setSysSettingInSystem(1);
                newSetting.setSysSettingSort(0);
                toInsert.add(newSetting);
            }

            if (!toUpdate.isEmpty()) {
                updateBatchById(toUpdate);
                for (var item : toUpdate) {
                    sysSettingService.clearCache(item);
                }
            }

            if (!toInsert.isEmpty()) {
                saveBatch(toInsert);
            }

            log.info("[系统配置][Socket同步] Socket配置同步完成: url={}, enabled={}, path={}, protocol={}",
                    socketUrl, socketEnable, socketPath, socketProtocol);
        } catch (Exception e) {
            log.warn("[系统配置][Socket同步] Socket配置同步失败: {}", e.getMessage());
        }
    }

    private String resolveSocketPublishHost() {
        if (StringUtils.isNotBlank(socketPublishHost)) {
            return socketPublishHost.trim();
        }

        if (isSocketLocalPublishHost(socketHost)) {
            var preferredHost = resolvePreferredSocketPublishHost();
            if (StringUtils.isNotBlank(preferredHost)) {
                return preferredHost;
            }

            return NetUtils.getLocalHost();
        }

        return socketHost;
    }

    private boolean isSocketLocalPublishHost(String host) {
        if (StringUtils.isBlank(host)) {
            return true;
        }

        String normalizedHost = host.trim();
        if ("localhost".equalsIgnoreCase(normalizedHost)) {
            return true;
        }

        try {
            return NetUtils.isAnyHost(normalizedHost) || NetUtils.isLocalHost(normalizedHost);
        } catch (Exception e) {
            log.debug("[系统配置][Socket同步] 判断本地Socket地址失败: host={}, error={}", normalizedHost, e.getMessage());
            return false;
        }
    }

    private String resolvePreferredSocketPublishHost() {
        try {
            List<String> candidates = new ArrayList<>();
            List<String> fallbackCandidates = new ArrayList<>();
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            interfaces.sort(Comparator.comparingInt(NetworkInterface::getIndex));

            for (NetworkInterface item : interfaces) {
                if (!item.isUp() || item.isLoopback()) {
                    continue;
                }

                String interfaceName = StringUtils.defaultString(item.getName(), "").toLowerCase(Locale.ROOT);
                String displayName = StringUtils.defaultString(item.getDisplayName(), "").toLowerCase(Locale.ROOT);
                boolean dockerLikeInterface = isDockerLikeInterface(interfaceName, displayName);

                for (var address : Collections.list(item.getInetAddresses())) {
                    if (!(address instanceof Inet4Address) || address.isLoopbackAddress() || address.isLinkLocalAddress()) {
                        continue;
                    }

                    String hostAddress = address.getHostAddress();
                    if (StringUtils.isBlank(hostAddress)) {
                        continue;
                    }

                    if (dockerLikeInterface || isDockerLikeAddress(hostAddress)) {
                        fallbackCandidates.add(hostAddress);
                        continue;
                    }

                    candidates.add(hostAddress);
                }
            }

            if (!candidates.isEmpty()) {
                candidates.sort(this::compareSocketPublishHost);
                return candidates.get(0);
            }

            if (!fallbackCandidates.isEmpty()) {
                fallbackCandidates.sort(this::compareSocketPublishHost);
                return fallbackCandidates.get(0);
            }
        } catch (Exception e) {
            log.debug("[系统配置][Socket同步] 解析对外发布地址失败: {}", e.getMessage());
        }

        return null;
    }

    private int compareSocketPublishHost(String left, String right) {
        return Integer.compare(scoreSocketPublishHost(right), scoreSocketPublishHost(left));
    }

    private int scoreSocketPublishHost(String host) {
        if (StringUtils.isBlank(host)) {
            return Integer.MIN_VALUE;
        }

        if (host.startsWith("192.168.")) {
            return 300;
        }

        if (host.startsWith("10.")) {
            return 250;
        }

        if (isSiteLocal172Address(host)) {
            return 200;
        }

        return 100;
    }

    private boolean isSiteLocal172Address(String host) {
        if (!host.startsWith("172.")) {
            return false;
        }

        String[] parts = host.split("\\.");
        if (parts.length < 2) {
            return false;
        }

        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDockerLikeInterface(String name, String displayName) {
        return name.startsWith("docker")
                || name.startsWith("br-")
                || name.startsWith("veth")
                || displayName.contains("docker")
                || displayName.contains("container")
                || displayName.contains("podman")
                || displayName.contains("virtual");
    }

    private boolean isDockerLikeAddress(String host) {
        return host.startsWith("172.17.")
                || host.startsWith("172.18.")
                || host.startsWith("172.19.")
                || host.startsWith("172.20.")
                || host.startsWith("172.21.")
                || host.startsWith("172.22.")
                || host.startsWith("172.23.")
                || host.startsWith("172.24.")
                || host.startsWith("172.25.")
                || host.startsWith("172.26.")
                || host.startsWith("172.27.")
                || host.startsWith("172.28.")
                || host.startsWith("172.29.")
                || host.startsWith("172.30.")
                || host.startsWith("172.31.");
    }

    private int resolveSocketPublishPort() {
        return socketPublishPort > 0 ? socketPublishPort : socketPort;
    }

    /**
     * Socket 配置期望值
     *
     * @param value  配置值
     * @param remark 配置备注
     */
    private record SysSettingDesiredValue(String value, String remark) {
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param list 配置列表
     */
    private void register(GlobalSettingFactory globalSettingFactory, List<SysSetting> list) {
        Map<String, List<SysSetting>> collect = list.stream().collect(Collectors.groupingBy(SysSetting::getSysSettingGroup));
        for (Map.Entry<String, List<SysSetting>> entry : collect.entrySet()) {
            register(globalSettingFactory, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param key 配置组
     * @param value 配置
     */
    private void register(GlobalSettingFactory globalSettingFactory, String key, List<SysSetting> value) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (SysSetting sysSetting : value) {
            String sysSettingName = sysSetting.getSysSettingName();
            map.put(sysSettingName, ObjectUtils.equalsIgnore("version", sysSettingName) ? sysSetting.getSysSettingVersion() : sysSetting.getSysSettingValue());
        }
        globalSettingFactory.set(key, map);
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param sysSetting 配置
     */
    private void register(GlobalSettingFactory globalSettingFactory, SysSetting sysSetting) {
        if(sysSetting.getSysSettingCollect() == 0) {
            globalSettingFactory.set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            return;
        }

        String sysSettingValue = sysSetting.getSysSettingValue();
        if(StringUtils.isEmpty(sysSettingValue)) {
            return;
        }
        JsonObject jsonObject = Json.getJsonObject(sysSettingValue);
        globalSettingFactory.set(sysSetting.getSysSettingGroup(), jsonObject);
    }

    @Override
    public ReturnResult<String> exportSettings(String group) {
        List<SysSetting> settings;
        if (StringUtils.isNotBlank(group)) {
            settings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingGroup, group)
                    .orderByAsc(SysSetting::getSysSettingSort)
            );
        } else {
            settings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                    .orderByAsc(SysSetting::getSysSettingGroup)
                    .orderByAsc(SysSetting::getSysSettingSort)
            );
        }

        if (settings.isEmpty()) {
            return ReturnResult.fail("没有可导出的配置");
        }

        // 转换为导出格式，去除ID等内部字段
        List<Map<String, Object>> exportList = new ArrayList<>();
        for (SysSetting setting : settings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", setting.getSysSettingName());
            item.put("value", setting.getSysSettingValue());
            item.put("group", setting.getSysSettingGroup());
            item.put("valueType", setting.getSysSettingValueType());
            item.put("remark", setting.getSysSettingRemark());
            item.put("sort", setting.getSysSettingSort());
            item.put("status", setting.getSysSettingStatus());
            item.put("collect", setting.getSysSettingCollect());
            item.put("config", setting.getSysSettingConfig());
            exportList.add(item);
        }

        return ReturnResult.ok(Json.toJson(exportList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<String> importSettings(String json, boolean overwrite, Set<String> roles) {
        if (StringUtils.isBlank(json)) {
            return ReturnResult.fail("导入内容不能为空");
        }

        List<Map<String, Object>> importList;
        try {
            importList = Json.fromJson(json, List.class);
        } catch (Exception e) {
            return ReturnResult.fail("JSON格式错误: " + e.getMessage());
        }

        if (importList == null || importList.isEmpty()) {
            return ReturnResult.fail("导入内容为空");
        }

        String batchNo = UUID.randomUUID().toString().replace("-", "");
        String operatorId = RequestUtils.getUserId();
        String operatorName = RequestUtils.getUsername();
        List<SysSetting> newSettings = new ArrayList<>();
        List<SysSetting> allImportedSettings = new ArrayList<>();

        for (Map<String, Object> item : importList) {
            String name = MapUtils.getString(item, "name");
            String group = MapUtils.getString(item, "group");

            if (StringUtils.isBlank(name) || StringUtils.isBlank(group)) {
                continue;
            }

            // 检查是否已存在
            SysSetting existing = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingName, name)
                    .eq(SysSetting::getSysSettingGroup, group)
            );

            if (existing != null) {
                if (!overwrite) {
                    continue;
                }
                // 覆盖更新，先保存旧值
                String oldValue = existing.getSysSettingValue();
                SysSetting oldSetting = new SysSetting();
                oldSetting.setSysSettingId(existing.getSysSettingId());
                oldSetting.setSysSettingName(existing.getSysSettingName());
                oldSetting.setSysSettingGroup(existing.getSysSettingGroup());
                oldSetting.setSysSettingValue(oldValue);
                
                existing.setSysSettingValue(MapUtils.getString(item, "value"));
                existing.setSysSettingValueType(MapUtils.getString(item, "valueType"));
                existing.setSysSettingRemark(MapUtils.getString(item, "remark"));
                existing.setSysSettingSort(MapUtils.getInteger(item, "sort"));
                existing.setSysSettingStatus(MapUtils.getInteger(item, "status", 1));
                existing.setSysSettingCollect(MapUtils.getInteger(item, "collect", 0));
                existing.setSysSettingConfig(MapUtils.getString(item, "config"));
                baseMapper.updateById(existing);
                sysSettingService.clearCache(existing);
                allImportedSettings.add(existing);
                // 记录覆盖更新历史，包含旧值
                try {
                    sysSettingHistoryService.recordImportUpdate(oldSetting, existing, operatorId, operatorName, batchNo);
                } catch (Exception e) {
                    log.warn("记录配置导入覆盖历史失败: {}", e.getMessage());
                }
            } else {
                // 新增
                SysSetting newSetting = new SysSetting();
                newSetting.setSysSettingName(name);
                newSetting.setSysSettingGroup(group);
                newSetting.setSysSettingValue(MapUtils.getString(item, "value"));
                newSetting.setSysSettingValueType(MapUtils.getString(item, "valueType"));
                newSetting.setSysSettingRemark(MapUtils.getString(item, "remark"));
                newSetting.setSysSettingSort(MapUtils.getInteger(item, "sort"));
                newSetting.setSysSettingStatus(MapUtils.getInteger(item, "status", 1));
                newSetting.setSysSettingCollect(MapUtils.getInteger(item, "collect", 0));
                newSetting.setSysSettingConfig(MapUtils.getString(item, "config"));
                newSetting.setSysSettingInSystem(0);
                newSetting.setSysSettingAppInner(0);
                baseMapper.insert(newSetting);
                sysSettingService.clearCache(newSetting);
                newSettings.add(newSetting);
                allImportedSettings.add(newSetting);
            }
        }

        // 记录新增配置的导入历史
        if (!newSettings.isEmpty()) {
            try {
                sysSettingHistoryService.recordImport(newSettings, operatorId, operatorName, batchNo);
            } catch (Exception e) {
                log.warn("记录配置导入历史失败: {}", e.getMessage());
            }
        }
        
        // 注册到全局配置
        if (!allImportedSettings.isEmpty()) {
            register(GlobalSettingFactory.getInstance(), allImportedSettings);
        }

        log.info("配置导入完成: batchNo={}, count={}", batchNo, allImportedSettings.size());
        return ReturnResult.ok(batchNo);
    }
}
