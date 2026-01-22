package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.core.constant.CommonConstant;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.*;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, InitializingBean {

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
    @Value("${plugin.socket.path:/socket.io}")
    private String socketPath;

    /**
     * Socket服务默认端口
     */
    @Value("${plugin.socket.room[0].port:29181}")
    private int socketPort;

    /**
     * Socket服务协议类型
     */
    @Value("${plugin.socket.protocol:socketio}")
    private String socketProtocol;
    
    static final String[] APP_KEY = new String[]{"appKey", "clientId"};
    static final String[] APP_SECRET = new String[]{"appSecret", "clientSecret"};
    static final String[] APP_REDIRECT_URI = new String[]{"redirectUri"};

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
        SysSetting sysSetting1 = baseMapper.selectById(sysSetting.getSysSettingId());
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
        version.setSysSettingConfig(UUID.randomUUID().toString());
        List<SysSetting> sysSettings = new LinkedList<>();
        sysSettings.add(sysSetting);
        sysSettings.add(version);
        sysSettingService.updateBatchById(sysSettings);
        // 记录更新历史
        try {
            String operatorId = RequestUtils.getUserId();
            String operatorName = RequestUtils.getUsername();
            sysSettingHistoryService.recordUpdate(sysSetting1, sysSetting, operatorId, operatorName, UUID.randomUUID().toString().replace("-", ""));
        } catch (Exception e) {
            log.warn("记录配置更新历史失败: {}", e.getMessage());
        }

        if(null != sysSetting.getSysSettingStatus() && sysSetting.getSysSettingStatus() == 1) {
            GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            upgradeVersion(version);
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
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysSetting>lambdaQuery()
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingGroup()), SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingName()), SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingRemark()), SysSetting::getSysSettingRemark, sysSetting.getSysSettingRemark())
                .orderByDesc(SysSetting::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysSetting> sysSettings) {
        SysSetting version = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSysSettingName, "version"));
        version.setSysSettingConfig(UUID.randomUUID().toString());
        sysSettings.add(version);
        sysSettingService.updateBatchById(sysSettings);
        for (SysSetting setting : sysSettings) {
            sysSettingService.clearCache(setting);
        }
        register(GlobalSettingFactory.getInstance(), sysSettings);
        return ReturnResult.of(true);
    }

    @Override
    public SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> getAuthRequest(ThirdLoginData loginData) {
        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(SysSetting::getSysSettingGroup, loginData.getLoginType(), "sso")
                .eq(SysSetting::getSysSettingStatus, 1)
        );

        Set<SysSetting> sso = sysSettings.stream().filter(item -> StringUtils.equals(item.getSysSettingGroup(), "sso"))
                .filter(item -> StringUtils.equals(item.getSysSettingName(), loginData.getLoginType()))
                .filter(item -> StringUtils.equals(CommonConstant.TRUE, item.getSysSettingValue())).collect(Collectors.toUnmodifiableSet());

        if(sso.isEmpty()) {
            return null;
        }

        AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(ObjectUtils.firstNonNull(sysSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_KEY, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .clientSecret(ObjectUtils.firstNonNull(sysSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_SECRET, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .redirectUri(ObjectUtils.firstNonNull(sysSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_REDIRECT_URI, item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .build();
        String state = AuthStateUtils.createState();
        authStateCache.cache(state + "_userId", RequestUtils.getUserId());
        authStateCache.cache(state + "_thirdType", loginData.getThirdType());
        authStateCache.cache(state + "_info", loginData.getLoginCode());
        authStateCache.cache(state + "_clientId", authConfig.getClientId());
        authStateCache.cache(state + "_clientSecret", authConfig.getClientSecret());
        authStateCache.cache(state + "_callback", authConfig.getRedirectUri());
        authStateCache.cache(state + "_system_callback", loginData.getCallback());
        return switch (loginData.getLoginType().toLowerCase()) {
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
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:' + #sysSetting.sysSettingGroup", keyGenerator = "customTenantedKeyGenerator")
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
        if(ClassUtils.isPresent("com.chua.report.client.starter.setting.ReportExpireSetting")) {
            globalSettingFactory.register("expire", ClassUtils.forObject("com.chua.report.client.starter.setting.ReportExpireSetting"));
        }
        upgrade();
    }

    private void upgrade() {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        ThreadUtils.newStaticThreadPool().execute(() -> {
            // 重试机制：等待数据库表初始化完成
            int maxRetries = 10;
            int retryDelayMs = 3000;
            
            for (int i = 0; i < maxRetries; i++) {
                try {
                    List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                            .eq(SysSetting::getSysSettingStatus, 1)
                    );
                    register(globalSettingFactory, list);
                    // 同步Socket配置
                    syncSocketConfig();
                    log.info("系统配置加载完成，共 {} 条配置", list.size());
                    return; // 成功后退出
                } catch (Throwable e) {
                    if (i < maxRetries - 1) {
                        log.debug("配置加载失败，{}ms 后重试 ({}/{}):{}", retryDelayMs, i + 1, maxRetries, e.getMessage());
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        log.warn("配置加载失败（已重试{}次）:{}", maxRetries, e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 同步Socket配置到数据库
     * 将YAML中的socket配置同步到sys_setting表，确保前端获取的配置与后端一致
     */
    private void syncSocketConfig() {
        try {
            // 构建Socket URL
            String socketUrl = String.format("http://%s:%d", socketHost, socketPort);
            
            // 同步 SocketUrl
            syncSettingValue("SocketUrl", "config", socketUrl, "socket.io服务器地址");
            
            // 同步 SocketOpen
            syncSettingValue("SocketOpen", "config", String.valueOf(socketEnable), "socket.io是否开启");
            
            // 同步 SocketPath
            syncSettingValue("SocketPath", "config", socketPath, "socket.io路径");
            
            // 同步 SocketProtocol
            syncSettingValue("SocketProtocol", "config", socketProtocol, "socket.io协议类型");
            
            log.info("Socket配置同步完成: url={}, enabled={}, path={}, protocol={}", socketUrl, socketEnable, socketPath, socketProtocol);
        } catch (Exception e) {
            log.warn("Socket配置同步失败: {}", e.getMessage());
        }
    }

    /**
     * 同步单个配置值到数据库
     * @param name 配置名称
     * @param group 配置分组
     * @param value 配置值
     * @param remark 配置备注
     */
    private void syncSettingValue(String name, String group, String value, String remark) {
        SysSetting setting = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingName, name)
                .eq(SysSetting::getSysSettingGroup, group));
        
        if (setting != null) {
            // 配置存在，检查值是否需要更新
            if (!value.equals(setting.getSysSettingValue())) {
                setting.setSysSettingValue(value);
                baseMapper.updateById(setting);
                // 清理缓存
                sysSettingService.clearCache(setting);
                log.debug("配置已更新: {}={}", name, value);
            }
        } else {
            // 配置不存在，创建新配置
            SysSetting newSetting = new SysSetting();
            newSetting.setSysSettingName(name);
            newSetting.setSysSettingGroup(group);
            newSetting.setSysSettingValue(value);
            newSetting.setSysSettingRemark(remark);
            newSetting.setSysSettingStatus(1);
            newSetting.setSysSettingInSystem(1);
            newSetting.setSysSettingSort(0);
            baseMapper.insert(newSetting);
            log.debug("配置已创建: {}={}", name, value);
        }
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
