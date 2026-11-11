// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ArrayUtils;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.constant.CommonConstant;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.*;
import com.chua.common.support.network.net.NetUtils;
import com.chua.common.support.task.retry.RetryProvider;
import com.chua.common.support.task.retry.RetryFlow;
import com.chua.starter.common.support.api.decode.ApiRequestDecodeSetting;
import com.chua.starter.common.support.api.encode.ApiResponseEncodeConfiguration;
import com.chua.common.support.application.GlobalSettingFactory;
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
import com.chua.starter.service.dto.setting.AuthOidcRequest;
import com.chua.starter.service.dto.setting.AuthRequestFactory;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.event.HotReloadChangedEvent;
import com.chua.starter.service.hotreload.HotReloadConfigService;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import com.chua.starter.service.utils.SystemCallbackPolicy;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, InitializingBean,
    ApplicationListener<ApplicationReadyEvent> {
    private static final Set<String> HOT_RELOAD_VALUE_TYPES = Set.of("string", "number", "boolean", "json");

    @Autowired
    @Lazy
    private SysSettingService sysSettingService; // syssetting服务
    
    @Autowired
    @Lazy
    private SysSettingHistoryService sysSettingHistoryService; // syssetting历史服务

    @Autowired
    @Lazy
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 热重载配置缓存服务。
     *
     * <p>删除热更新配置后必须清掉它的值缓存 —— 否则 Redis 里仍留着已删除的旧值，
     * 运行时消费者的 {@code get(code)} 会继续命中缓存、读到一条数据库里已经不存在的配置。
     * 用 {@code @Lazy}：{@code HotReloadConfigService} 反过来依赖本接口，直接注入会循环。</p>
     */
    @Autowired
    @Lazy
    private HotReloadConfigService hotReloadConfigService;

    /**
     * 套接字服务是否启用
     */
    @Value("${plugin.socket.enable:false}")
    private boolean socketEnable; // 套接字enable

    /**
     * 套接字服务主机地址
     */
    @Value("${plugin.socket.host:localhost}")
    private String socketHost; // 套接字主机

    /**
     * 套接字服务路径
     */
    @Value("${plugin.socket.room[0].context-path:${plugin.socket.path:/socket.io}}")
    private String socketPath; // 套接字路径

    /**
     * 套接字服务默认端口
     */
    @Value("${plugin.socket.room[0].port:29181}")
    private int socketPort; // 套接字端口

    /**
     * 套接字 对外发布主机地址
     */
    @Value("${plugin.socket.publish-host:}")
    private String socketPublishHost; // 套接字发布主机

    /**
     * 套接字 对外发布端口
     */
    @Value("${plugin.socket.publish-port:-1}")
    private int socketPublishPort; // 套接字发布端口

    /**
     * 套接字对外发布地址（完整 origin，可选）
     *
     * <p>HTTPS 反向代理 / NAT 场景下 publish-host + publish-port 拼不出正确地址，
     * 需显式配置，例如 {@code https://example.com}；留空保持旧行为。</p>
     */
    @Value("${plugin.socket.public-url:}")
    private String socketPublicUrl; // 套接字发布URL

    /**
     * 套接字服务协议类型
     */
    @Value("${plugin.socket.protocol:socketio}")
    private String socketProtocol; // 套接字协议
    
    static final String[] APP_KEY = new String[]{"appKey", "clientId"}; // app键
    static final String[] APP_SECRET = new String[]{"appSecret", "clientSecret"}; // APP_SECRET
    static final String[] APP_REDIRECT_URI = new String[]{"redirectUri"}; // APP_REDIRECT_URI
    static final String[] APP_ISSUER_URI = new String[]{"issuerUri", "issuer"}; // OIDC 签发者地址
    static final String[] APP_SCOPE = new String[]{"scope"}; // OIDC 请求范围

    /**
     * 套接字 配置项名称：套接字url
     */
    private static final String SOCKET_SETTING_URL = "SocketUrl";

    /**
     * 套接字 配置项名称：套接字打开
     */
    private static final String SOCKET_SETTING_OPEN = "SocketOpen";

    /**
     * 套接字 配置项名称：套接字路径
     */
    private static final String SOCKET_SETTING_PATH = "SocketPath";

    /**
     * 套接字 配置项名称：套接字协议
     */
    private static final String SOCKET_SETTING_PROTOCOL = "SocketProtocol";

    /**
     * 套接字 配置项分组：配置
     */
    private static final String SOCKET_SETTING_GROUP = "config";

    /**
     * 设置名 → 前端配置点号编码。
     *
     * <p>前端 {@code flattenSettingToConfig} 只认 {@code sysSettingCode}；
     * 历史数据里这一列是空的，会让 SocketUrl/SocketOpen 等配置被静默丢弃。</p>
     */
    private static String socketSettingCode(String name) {
        switch (name) {
            case SOCKET_SETTING_URL:
                return "socket.urls";
            case SOCKET_SETTING_OPEN:
                return "socket.enable";
            case SOCKET_SETTING_PATH:
                return "socket.context";
            case SOCKET_SETTING_PROTOCOL:
                return "socket.protocol";
            default:
                return null;
        }
    }

    /**
     * 套接字url 配置备注
     */
    private static final String SOCKET_REMARK_URL = "socket.io服务器地址";

    /**
     * 套接字打开 配置备注
     */
    private static final String SOCKET_REMARK_OPEN = "socket.io是否开启";

    /**
     * 套接字路径 配置备注
     */
    private static final String SOCKET_REMARK_PATH = "socket.io路径";

    /**
     * 套接字协议 配置备注
     */
    private static final String SOCKET_REMARK_PROTOCOL = "socket.io协议类型";

    /**
     * 版本号配置项名称：与初始化种子中的行名一致
     */
    private static final String VERSION_SETTING_NAME = "Version";

    /**
     * 版本号配置项分组：config
     */
    private static final String VERSION_SETTING_GROUP = "config";

    @Override
    public List<SysSetting> list(SysSettingUpdate sysSettingUpdate, Set<String> roles) {
        String group = (sysSettingUpdate != null) ? sysSettingUpdate.getSysSettingGroup() : null;
        Set<String> groupSet = null;
        if (StringUtils.isNotBlank(group)) {
            groupSet = Arrays.stream(group.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        }
        return baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(groupSet != null && !groupSet.isEmpty(), SysSetting::getSysSettingGroup, groupSet)
                .orderByAsc(SysSetting::getSysSettingSort)
                .orderByDesc(SysSetting::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long settingId) {
        if(null == settingId) {
            return ReturnResult.error("配置ID不能为空");
        }

        SysSetting sysSetting = baseMapper.selectById(settingId);
        if(null == sysSetting) {
            return ReturnResult.error("配置不存在, 请刷新后重试!");
        }

        Integer sysRoleInSystem = sysSetting.getSysSettingInSystem();

        if(null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.error("内置配置不允许删除");
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
            return ReturnResult.error("配置ID不能为空");
        }
        SysSetting sysSetting1 = baseMapper.selectById(sysSetting.getSysSettingId());
        if (null == sysSetting1) {
            return ReturnResult.error("配置不存在, 请刷新后重试!");
        }
        Integer sysRoleInSystem = sysSetting1.getSysSettingInSystem();
        if(!AuthConstant.isSuperAdmin(roles)) {
            if(null != sysRoleInSystem && sysRoleInSystem == 1) {
                return ReturnResult.error("内置配置不允许修改");
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
                return ReturnResult.error("配置编码已存在");
            }
        }
        // 校验配置值是否满足正则规则（掩码值视为未修改，跳过）
        String validateRule = StringUtils.isNotBlank(sysSetting.getSysSettingValidate())
                ? sysSetting.getSysSettingValidate() : sysSetting1.getSysSettingValidate();
        String validateError = validateSettingValue(sysSetting.getSysSettingValue(), validateRule);
        if (null != validateError) {
            return ReturnResult.error(validateError);
        }
        SysSetting version = findVersionSetting();
        List<SysSetting> sysSettings = new LinkedList<>();
        sysSettings.add(sysSetting);
        if (null != version) {
            version.setSysSettingConfig(UUID.randomUUID().toString());
            sysSettings.add(version);
        }
        boolean updated = sysSettingService.updateBatchById(sysSettings);
        if (!updated) {
            // 乐观锁版本号不匹配（配置已被他人/其它会话修改），需明确报错而非静默成功
            return ReturnResult.error("配置已被修改，请刷新后重试");
        }
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
            // 逐键 set 只改散表，不刷新启动时注册的强类型实例（SmtpSetting / SmsSetting …）：
            // get(group, Class) 拿到的仍是旧字段值，表现为「系统设置里改完邮件账号，
            // 任务失败告警仍报未配置」，必须整组重绑一次。
            refreshGroupBinding(sysSetting.getSysSettingGroup());
            if (null != version) {
                upgradeVersion(version);
            }
        }
        sysSettingService.clearCache(sysSetting);
        return ReturnResult.of(sysSetting);
    }

    /**
     * 重新绑定某个分组的强类型配置对象。
     *
     * <p>系统配置启动时用 {@code register(group, new SmtpSetting())} 把分组绑成强类型 Bean，
     * 之后在设置页改单个键走的是 {@code set(group, name, value)}——它只更新散表，
     * 不会把新值写回那个已注册的 Bean。于是 {@code get("smtp", SmtpSetting.class)}
     * 仍返回旧字段值：改了邮件账号，任务失败告警却继续报「未配置」。
     * 保存后按分组重读一遍并重新注册，强类型 Bean 才会拿到新值，改配置立即生效、无需重启。</p>
     *
     * @param group 配置分组名
     */
    private void refreshGroupBinding(String group) {
        if (StringUtils.isBlank(group)) {
            return;
        }
        try {
            List<SysSetting> rows = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingGroup, group)
                    .eq(SysSetting::getSysSettingStatus, 1));
            register(GlobalSettingFactory.getInstance(), group, rows);
            log.info("[系统配置] 分组 {} 已重新绑定强类型配置（{} 项）", group, rows.size());
        } catch (Exception e) {
            log.warn("[系统配置] 分组 {} 重新绑定失败: {}", group, e.getMessage());
        }
    }

    @Override
    public ReturnResult<SysSetting> saveForEntity(SysSetting sysSetting, Set<String> roles, String operatorId, String operatorName) {
        sysSetting.setSysSettingInSystem(0);
        if(!AuthConstant.isSuperAdmin(roles)) {
            sysSetting.setSysSettingAppInner(0);
        }

        if(null == sysSetting.getSysSettingStatus()) {
            sysSetting.setSysSettingStatus(1);
        }
        // 校验配置值是否满足正则规则
        String validateError = validateSettingValue(sysSetting.getSysSettingValue(), sysSetting.getSysSettingValidate());
        if (null != validateError) {
            return ReturnResult.error(validateError);
        }
        SysSetting setting = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .eq(SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup()));
        if(null != setting) {
            return ReturnResult.error("配置编码已存在");
        }

        // 新建配置：当前运行值为空时以出厂默认值初始化
        if (StringUtils.isEmpty(sysSetting.getSysSettingValue()) && !StringUtils.isEmpty(sysSetting.getSysSettingDefaultValue())) {
            sysSetting.setSysSettingValue(sysSetting.getSysSettingDefaultValue());
        }
        if(baseMapper.insert(sysSetting) > 0) {
            if(sysSetting.getSysSettingStatus() == 1) {
                GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            }
            // 记录新增历史
            try {
                sysSettingHistoryService.recordCreate(sysSetting, operatorId, operatorName, UUID.randomUUID().toString().replace("-", ""));
            } catch (Exception e) {
                log.warn("记录配置新增历史失败: {}", e.getMessage());
            }
            return ReturnResult.of(sysSettingService.clearCache(sysSetting));
        }
        return ReturnResult.illegal("新增失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> saveHotReload(SysSetting sysSetting) {
        return saveHotReload(sysSetting, Set.of());
    }

    /**
     * 删除热更新配置。
     *
     * <p>与通用 {@code delete(settingId)} 的区别有两处，都是必要的：
     * <ol>
     *   <li><b>限分组</b>：只允许删 {@code hot_reload} 组的配置。通用删除对任何组都生效，
     *       前端若把 settingId 传错，会删掉一条与策略无关的普通系统配置。</li>
     *   <li><b>清值缓存</b>：删除后调 {@code hotReloadConfigService.evict(code)}，
     *       否则 {@code sys:hot-reload:config} 里那条已删配置的旧值仍在，
     *       运行时会继续按它生效（表现为「删了不生效」）。</li>
     * </ol>
     * 内置配置（{@code sysSettingInSystem=1}）沿用通用删除的保护，不允许删。</p>
     *
     * @param settingId 配置主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteHotReload(Long settingId) {
        if (null == settingId) {
            return ReturnResult.error("配置ID不能为空");
        }
        SysSetting existing = baseMapper.selectById(settingId);
        if (null == existing) {
            return ReturnResult.error("配置不存在, 请刷新后重试!");
        }
        if (!HOT_RELOAD_GROUP.equals(existing.getSysSettingGroup())) {
            return ReturnResult.error("该配置不属于热更新分组, 不能在此删除");
        }
        Integer inSystem = existing.getSysSettingInSystem();
        if (null != inSystem && inSystem == 1) {
            return ReturnResult.error("内置配置不允许删除");
        }
        if (baseMapper.deleteById(settingId) <= 0) {
            return ReturnResult.error("删除失败");
        }
        // 先清值缓存（按配置编码），再清列表缓存
        if (null != hotReloadConfigService && StringUtils.hasText(existing.getSysSettingCode())) {
            hotReloadConfigService.evict(existing.getSysSettingCode().trim());
        }
        clearHotReloadCache();
        // 记录删除历史（与通用删除一致）
        try {
            String operatorId = RequestUtils.getUserId();
            String operatorName = RequestUtils.getUsername();
            sysSettingHistoryService.recordDelete(existing, operatorId, operatorName,
                    UUID.randomUUID().toString().replace("-", ""));
        } catch (Exception e) {
            log.warn("记录热更新配置删除历史失败: {}", e.getMessage());
        }
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> saveHotReload(SysSetting sysSetting, Set<String> runtimeConsumers) {
        String validationError = normalizeAndValidateHotReloadSetting(sysSetting);
        if (null != validationError) {
            return ReturnResult.error(validationError);
        }
        boolean saved = saveOrUpdate(sysSetting);
        if (saved) {
            publishAfterCommit(new HotReloadChangedEvent(this, sysSetting.getSysSettingId(),
                    sysSetting.getSysSettingName(), sysSetting.getSysSettingCode(), sysSetting.getSysSettingValue(),
                    sysSetting.getSysSettingValueType(), sysSetting.getSysSettingStatus(), runtimeConsumers));
        }
        return ReturnResult.of(saved);
    }

    static String normalizeAndValidateHotReloadSetting(SysSetting setting) {
        if (null == setting) {
            return "配置不能为空";
        }
        setting.setSysSettingGroup(SysSettingService.HOT_RELOAD_GROUP);
        if (null == setting.getSysSettingStatus()) {
            setting.setSysSettingStatus(1);
        }
        if (StringUtils.isBlank(setting.getSysSettingName())) {
            return "配置名称不能为空";
        }
        if (StringUtils.isBlank(setting.getSysSettingCode())) {
            return "配置编码不能为空";
        }
        if (StringUtils.isBlank(setting.getSysSettingValueType())) {
            return "配置值类型不能为空";
        }
        String valueType = setting.getSysSettingValueType().trim().toLowerCase(Locale.ROOT);
        if (!HOT_RELOAD_VALUE_TYPES.contains(valueType)) {
            return "配置值类型仅支持 string/number/boolean/json";
        }
        setting.setSysSettingName(setting.getSysSettingName().trim());
        setting.setSysSettingCode(setting.getSysSettingCode().trim());
        setting.setSysSettingValueType(valueType);
        String value = setting.getSysSettingValue();
        if ("string".equals(valueType)) {
            return null;
        }
        if (StringUtils.isBlank(value)) {
            return "配置值不能为空";
        }
        try {
            switch (valueType) {
                case "number" -> new java.math.BigDecimal(value.trim());
                case "boolean" -> {
                    if (!"true".equalsIgnoreCase(value.trim()) && !"false".equalsIgnoreCase(value.trim())) {
                        return "配置值必须是 boolean 类型的 true 或 false";
                    }
                }
                case "json" -> Json.fromJson(value, Object.class);
                default -> throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            return "配置值不符合 " + valueType + " 类型";
        }
        return null;
    }

    /** publishAfterCommit 方法 */
    private void publishAfterCommit(HotReloadChangedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationEventPublisher.publishEvent(event);
                }
            });
            return;
        }
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public IPage<SysSetting> pageForSetting(Query<SysSetting> query, SysSetting sysSetting, String scope, Set<String> roles) {
        boolean userScope = "user".equalsIgnoreCase(scope);
        boolean systemScope = "system".equalsIgnoreCase(scope);
        var queryWrapper = Wrappers.<SysSetting>lambdaQuery()
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingGroup()), SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingName()), SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingRemark()), SysSetting::getSysSettingRemark, sysSetting.getSysSettingRemark())
                .eq(StringUtils.isNotBlank(sysSetting.getSysSettingValueType()), SysSetting::getSysSettingValueType, sysSetting.getSysSettingValueType())
                .eq(null != sysSetting.getSysSettingInSystem(), SysSetting::getSysSettingInSystem, sysSetting.getSysSettingInSystem())
                .in(userScope, SysSetting::getSysSettingGroup, USER_FACING_GROUPS)
                .notIn(systemScope, SysSetting::getSysSettingGroup, USER_FACING_GROUPS)
                .orderByDesc(SysSetting::getCreateTime);
        log.debug("设置分页筛选参数: scope={}, group={}, name={}, remark={}, valueType={}, inSystem={}, sqlSegment={}",
                scope,
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
            return ReturnResult.error("配置不能为空");
        }
        // 更新前批量查询旧值，用于记录变更历史
        List<Integer> updateIds = sysSettings.stream()
                .map(SysSetting::getSysSettingId)
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, SysSetting> oldValueMap = updateIds.isEmpty()
                ? Collections.emptyMap()
                : baseMapper.selectBatchIds(updateIds).stream()
                        .collect(Collectors.toMap(SysSetting::getSysSettingId, it -> it, (a, b) -> a));

        SysSetting version = findVersionSetting();
        if (null != version) {
            version.setSysSettingConfig(UUID.randomUUID().toString());
            sysSettings.add(version);
        }
        boolean batchUpdated = sysSettingService.updateBatchById(sysSettings);
        if (!batchUpdated) {
            return ReturnResult.error("部分配置已被修改，请刷新后重试");
        }
        for (SysSetting setting : sysSettings) {
            sysSettingService.clearCache(setting);
        }
        register(GlobalSettingFactory.getInstance(), sysSettings);

        // 记录批量更新历史：同一次批量更新的多条历史共享一个批次号
        try {
            String operatorId = RequestUtils.getUserId();
            String operatorName = RequestUtils.getUsername();
            String batchNo = UUID.randomUUID().toString().replace("-", "");
            for (SysSetting setting : sysSettings) {
                if (null == setting.getSysSettingId()) {
                    continue;
                }
                // 内部 version 配置仅用于刷新版本号，不纳入业务变更历史
                if (VERSION_SETTING_NAME.equals(setting.getSysSettingName())
                        && VERSION_SETTING_GROUP.equals(setting.getSysSettingGroup())) {
                    continue;
                }
                SysSetting oldSetting = oldValueMap.get(setting.getSysSettingId());
                if (null != oldSetting) {
                    sysSettingHistoryService.recordUpdate(oldSetting, setting, operatorId, operatorName, batchNo);
                }
            }
        } catch (Exception e) {
            log.warn("记录配置批量更新历史失败: {}", e.getMessage());
        }
        return ReturnResult.of(true);
    }

    @Override
    public SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> getAuthRequest(ThirdLoginData loginData) {
        String loginType = StringUtils.trimToEmpty(loginData.loginType()).toLowerCase(Locale.ROOT);        if (StringUtils.isBlank(loginType)) {
            log.warn("第三方登录类型为空");
            return null;
        }

        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(SysSetting::getSysSettingGroup, loginType, "sso")
                .eq(SysSetting::getSysSettingStatus, 1)
        );

        Set<SysSetting> sso = sysSettings.stream().filter(item -> StringUtils.equals(item.getSysSettingGroup(), "sso"))
                .filter(item -> StringUtils.equalsIgnoreCase(item.getSysSettingName(), loginType))
                .filter(item -> StringUtils.equals("true", item.getSysSettingValue())).collect(Collectors.toUnmodifiableSet());

        if(sso.isEmpty()) {
            log.warn("第三方登录开关未开启: loginType={}", loginType);
            return null;
        }

        List<SysSetting> providerSettings = sysSettings.stream()
                .filter(item -> StringUtils.equals(item.getSysSettingGroup(), loginType))
                .toList();

        AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_KEY,
                    item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .clientSecret(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_SECRET,
                    item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
                .redirectUri(ObjectUtils.firstNonNull(providerSettings.stream().filter(item -> ArrayUtils.containsIgnoreCase(APP_REDIRECT_URI,
                    item.getSysSettingName())).map(SysSetting::getSysSettingValue).findFirst(), "").toString())
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
        authStateCache.cache(state + "_thirdType", loginData.thirdType());
        authStateCache.cache(state + "_info", loginData.loginCode());
        authStateCache.cache(state + "_clientId", authConfig.getClientId());
        authStateCache.cache(state + "_clientSecret", authConfig.getClientSecret());
        authStateCache.cache(state + "_callback", authConfig.getRedirectUri());
        // 回调地址写缓存前做结构消毒（拒绝控制字符/协议相对 URL/非 http(s) 绝对地址），
        // 白名单强校验在入口 thirdLoginCode 与回调读取点做（纵深防御）
        authStateCache.cache(state + "_system_callback", SystemCallbackPolicy.sanitizeStructure(loginData.callback()));
        // OIDC 额外参数：issuer 与 scope 随 state 传递，供回调侧重建请求使用
        String oidcIssuerUri = "";
        String oidcScope = "";
        if (StringUtils.equals(loginType, AuthOidcRequest.PLATFORM)) {
            oidcIssuerUri = firstSettingValue(providerSettings, APP_ISSUER_URI);
            oidcScope = firstSettingValue(providerSettings, APP_SCOPE);
            if (StringUtils.isBlank(oidcIssuerUri)) {
                log.warn("OIDC 登录配置不完整: loginType={}, issuerUri 为空", loginType);
                return null;
            }
            authStateCache.cache(state + AuthOidcRequest.ISSUER_CACHE_SUFFIX, oidcIssuerUri);
            authStateCache.cache(state + AuthOidcRequest.SCOPE_CACHE_SUFFIX, oidcScope);
        }
 // TODO: 迁移到 服务提供者（第三方 just认证 OAuth 实现需先建立 SPI 扩展点）
        AuthRequest authRequest = AuthRequestFactory.createAuthRequest(loginType, authConfig, authStateCache, oidcIssuerUri, oidcScope);
        return authRequest == null ? null : new SysSettingThirdPartyRequest<>(authRequest, authStateCache, state);
    }

    @Override
    public List<String> listEnabledThirdPartyProviders() {
        try {
            List<SysSetting> switches = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingGroup, "sso")
                    .eq(SysSetting::getSysSettingStatus, 1)
            );
            if (null == switches || switches.isEmpty()) {
                return List.of();
            }
            Set<String> enabled = new LinkedHashSet<>();
            for (SysSetting item : switches) {
                if (null == item || StringUtils.isBlank(item.getSysSettingName())) {
                    continue;
                }
                if (!StringUtils.equals("true", StringUtils.trimToEmpty(item.getSysSettingValue()))) {
                    continue;
                }
                String loginType = item.getSysSettingName().trim().toLowerCase(Locale.ROOT);
                // 仅返回工厂实际支持的平台，避免前端展示不可用的入口
                if (isSupportedThirdPartyProvider(loginType)) {
                    enabled.add(loginType);
                }
            }
            return List.copyOf(enabled);
        } catch (Exception e) {
            log.warn("查询已开启三方登录来源失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 判断平台是否被工厂实际支持。
     *
     * <p>oidc 需 issuerUri 才能实例化，此处仅做平台白名单校验，避免返回打不开的入口。</p>
     *
     * @param loginType 小写平台标识
     * @return 支持返回 true
     */
    private static boolean isSupportedThirdPartyProvider(String loginType) {
        if (StringUtils.isBlank(loginType)) {
            return false;
        }
        return switch (loginType) {
            case "gitee", "github", "google", "oidc",
                    "wechat", "wechat_mp", "wechat_enterprise",
                    "qq", "alipay", "dingtalk", "weibo", "baidu",
                    "microsoft", "taobao", "douyin", "feishu" -> true;
            default -> false;
        };
    }

    /**
     * 取分组配置中首个匹配名称的值（大小写不敏感名称匹配，缺失时返回空串）。
     *
     * @param providerSettings 平台分组配置
     * @param names 候选配置名称
     * @return 匹配值或空串
     */
    private static String firstSettingValue(List<SysSetting> providerSettings, String[] names) {
        if (null == providerSettings || null == names) {
            return "";
        }
        for (SysSetting item : providerSettings) {
            if (null != item && ArrayUtils.containsIgnoreCase(names, item.getSysSettingName())
                    && StringUtils.isNotBlank(item.getSysSettingValue())) {
                return item.getSysSettingValue();
            }
        }
        return "";
    }

    /**
     * 查询承载全局版本号的配置行。
     *
     * <p>必须限定分组：用户协议分组下同样存在 {@code version} 行，仅按名称查询在大小写不敏感的
     * 排序规则下会命中两行。</p>
     *
     * @return 版本号配置行，未初始化时为空
     */
    private SysSetting findVersionSetting() {
        return baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingName, VERSION_SETTING_NAME)
                .eq(SysSetting::getSysSettingGroup, VERSION_SETTING_GROUP)
                .last("limit 1"));
    }

    /**
     * upgrade版本。
     * @param version 版本
     */
    private void upgradeVersion(SysSetting version) {
        GlobalSettingFactory.getInstance().set("config", "version", version.getSysSettingVersion());
    }

    /**
     * 确保 config/Version 版本行存在（缺失则创建），并将其乐观锁版本号注册为全局配置。
     * 响应头 x-response-version 取自该值；任意配置更新时该行版本号 +1，驱动前端静默刷新配置缓存。
     */
    private void ensureVersionSetting() {
        try {
            SysSetting version = findVersionSetting();
            if (null == version) {
                version = new SysSetting();
                version.setSysSettingName(VERSION_SETTING_NAME);
                version.setSysSettingGroup(VERSION_SETTING_GROUP);
                version.setSysSettingValue("1");
                version.setSysSettingValueType("STRING");
                version.setSysSettingVersion(1);
                version.setSysSettingStatus(1);
                version.setSysSettingInSystem(1);
                version.setSysSettingSort(0);
                baseMapper.insert(version);
            }
            upgradeVersion(version);
        } catch (Exception e) {
            log.warn("初始化版本配置失败: {}", e.getMessage());
        }
    }

    /**
     * 清理缓存
     * @param sysSetting 配置
     * @return 是否成功
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'" +
                SETTING_LIST_CACHE_KEY_LEGACY_PREFIX + "' + #sysSetting.sysSettingGroup", keyGenerator = "customTenantedKeyGenerator"),
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'" + SETTING_LIST_CACHE_KEY_PREFIX +
                "' + #sysSetting.sysSettingGroup", keyGenerator = "customTenantedKeyGenerator"),
            // 键里带了租户段，逐个 pattern 精确清理做不到（注解不支持通配），
            // 这里退化为清空该 cacheName 全部条目：配置变更是低频操作，正确性优先于缓存命中率。
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    })
    /**
     * 清空缓存。
     *
     * @param sysSetting 方法入参 sysSetting
     * @return SysSetting 对象
     */
    public SysSetting clearCache(SysSetting sysSetting) {
        return sysSetting;
    }

    @Override
    @Caching(evict = {
            // 热重载分组同理：键含租户段后无法按 key 精确清理，整体失效更安全
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    })
    public void clearHotReloadCache() {
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

    /**
     * upgrade。
     */
    private void upgrade() {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        try {
            RetryFlow.of("sys-setting-load")
                    .maxRetries(10)
                    .backoff(com.chua.common.support.concurrent.backoff.BackoffFlow.of("sys-setting-load")
                            .initialDelay(3000)
                            .multiplier(1)
                            .getProvider())
                    .retryListener((attempt, cause) -> log.debug("配置加载失败，3000ms 后重试: {}", cause.getMessage()))
                    .execute(() -> {
                        List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                                .eq(SysSetting::getSysSettingStatus, 1)
                        );
                        register(globalSettingFactory, list);
                        ensureVersionSetting();
 // 同步套接字配置
                        syncSocketConfig();
                        log.info("[系统配置][初始化] 系统配置加载完成，共 {} 条配置", list.size());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("配置加载失败（已重试10次）: {}", e.getMessage());
        }
    }

    /**
     * 同步套接字配置到数据库
     * 将YAML中的套接字配置同步到sys_setting表，确保前端获取的配置与后端一致
     */
    private void syncSocketConfig() {
        try {
 // 构建套接字 URL
            var publishHost = resolveSocketPublishHost();
            var publishPort = resolveSocketPublishPort();
            var socketUrl = StringUtils.isNotBlank(socketPublicUrl)
                    ? socketPublicUrl.trim()
                    : String.format("http://%s:%d", publishHost, publishPort);

            var desired = new LinkedHashMap<String, SysSettingDesiredValue>(4);
            desired.put(SOCKET_SETTING_URL, new SysSettingDesiredValue(socketUrl, SOCKET_REMARK_URL));
            desired.put(SOCKET_SETTING_OPEN, new SysSettingDesiredValue(String.valueOf(socketEnable), SOCKET_REMARK_OPEN));
            desired.put(SOCKET_SETTING_PATH, new SysSettingDesiredValue(socketPath, SOCKET_REMARK_PATH));
            desired.put(SOCKET_SETTING_PROTOCOL, new SysSettingDesiredValue(socketProtocol, SOCKET_REMARK_PROTOCOL));

 // 只查询一次，拿到 套接字 相关配置后统一对比并更新/新增
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
                    boolean changed = false;
                    if (!StringUtils.equals(expected.value(), exist.getSysSettingValue())) {
                        exist.setSysSettingValue(expected.value());
                        changed = true;
                    }
                    // 只修空编码，不覆盖管理员自定义过的编码
                    if (StringUtils.isBlank(exist.getSysSettingCode())) {
                        String code = socketSettingCode(name);
                        if (StringUtils.isNotBlank(code)) {
                            exist.setSysSettingCode(code);
                            changed = true;
                        }
                    }
                    if (changed) {
                        toUpdate.add(exist);
                    }
                    continue;
                }

                var newSetting = new SysSetting();
                newSetting.setSysSettingName(name);
                newSetting.setSysSettingCode(socketSettingCode(name));
                newSetting.setSysSettingGroup(SOCKET_SETTING_GROUP);
                newSetting.setSysSettingValue(expected.value());
                newSetting.setSysSettingRemark(expected.remark());
                newSetting.setSysSettingStatus(1);
                newSetting.setSysSettingInSystem(1);
                newSetting.setSysSettingSort(0);
                toInsert.add(newSetting);
            }

            if (!toUpdate.isEmpty()) {
                // 启动同步只覆盖 Socket 配置值，不应参与乐观锁版本比较；使用显式 UpdateWrapper
                // 可避开 MyBatis-Plus 批量/实体更新对 MP_OPTLOCK_VERSION_ORIGINAL 的参数依赖。
                for (var item : toUpdate) {
                    var update = Wrappers.<SysSetting>lambdaUpdate()
                            .eq(SysSetting::getSysSettingId, item.getSysSettingId())
                            .set(SysSetting::getSysSettingValue, item.getSysSettingValue());
                    if (StringUtils.isNotBlank(item.getSysSettingCode())) {
                        update.set(SysSetting::getSysSettingCode, item.getSysSettingCode());
                    }
                    if (baseMapper.update(null, update) <= 0) {
                        log.warn("[系统配置][Socket同步] 配置同步未命中，跳过本次同步: {}", item.getSysSettingName());
                        break;
                    }
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

    /**
     * resolve套接字发布主机。
     * @return resolve套接字发布主机的结果
     */
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

    /**
     * 是否套接字本地发布主机。
     * @param host 主机
     * @return 是否套接字本地发布主机的结果
     */
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

    /**
     * resolvepreferred套接字发布主机。
     * @return resolvepreferred套接字发布主机的结果
     */
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

    /**
     * compare套接字发布主机。
     * @param left left
     * @param right right
     * @return compare套接字发布主机的结果
     */
    private int compareSocketPublishHost(String left, String right) {
        return Integer.compare(scoreSocketPublishHost(right), scoreSocketPublishHost(left));
    }

    /**
     * score套接字发布主机。
     * @param host 主机
     * @return score套接字发布主机的结果
     */
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

    /**
     * 是否sitelocal172地址。
     * @param host 主机
     * @return 是否sitelocal172地址的结果
     */
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

    /**
     * 是否dockerlike接口。
     * @param name 名称
     * @param displayName display名称
     * @return 是否dockerlike接口的结果
     */
    private boolean isDockerLikeInterface(String name, String displayName) {
        return name.startsWith("docker")
                || name.startsWith("br-")
                || name.startsWith("veth")
                || displayName.contains("docker")
                || displayName.contains("container")
                || displayName.contains("podman")
                || displayName.contains("virtual");
    }

    /**
     * 是否dockerlike地址。
     * @param host 主机
     * @return 是否dockerlike地址的结果
     */
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

    /**
     * resolve套接字发布端口。
     * @return resolve套接字发布端口的结果
     */
    private int resolveSocketPublishPort() {
        return socketPublishPort > 0 ? socketPublishPort : socketPort;
    }

    /**
     * 套接字 配置期望值
     *
     * @param value  配置值
     * @param remark 配置备注
     * @return syssettingdesired值的结果
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
            map.put(sysSettingName, ObjectUtils.equalsIgnore("version", sysSettingName) ?
                sysSetting.getSysSettingVersion() : sysSetting.getSysSettingValue());
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
            return ReturnResult.error("没有可导出的配置");
        }

 // 转换为导出格式，去除标识等内部字段
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
    /**
     * 导入settings。
     * @param json json
     * @param overwrite overwrite
     * @param roles 角色
     * @return 导入settings的结果
     */
    public ReturnResult<String> importSettings(String json, boolean overwrite, Set<String> roles) {
        if (StringUtils.isBlank(json)) {
            return ReturnResult.error("导入内容不能为空");
        }

        List<Map<String, Object>> importList;
        try {
            importList = Json.fromJson(json, List.class);
        } catch (Exception e) {
            return ReturnResult.error("JSON格式错误: " + e.getMessage());
        }

        if (importList == null || importList.isEmpty()) {
            return ReturnResult.error("导入内容为空");
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

    /**
     * 敏感配置在列表/详情中展示的掩码值
    */
    public static final String SENSITIVE_MASK = "******";

    /**
     * 校验配置值是否满足正则规则。规则为空、值为空或为掩码占位时不校验。
     *
     * @param value 配置值
     * @param rule  正则规则
     * @return 校验失败返回错误信息，通过返回null
     */
    private String validateSettingValue(String value, String rule) {
        if (StringUtils.isBlank(rule) || StringUtils.isBlank(value) || SENSITIVE_MASK.equals(value)) {
            return null;
        }
        try {
            if (!value.matches(rule)) {
                return "配置值不满足校验规则: " + rule;
            }
        } catch (Exception e) {
            // 正则本身非法不阻断业务，仅记录
            log.warn("配置值校验规则非法，跳过校验: rule={}, error={}", rule, e.getMessage());
        }
        return null;
    }

    @Override
    public ReturnResult<String> getPlainValue(Integer settingId) {
        if (null == settingId) {
            return ReturnResult.error("配置ID不能为空");
        }
        SysSetting sysSetting = baseMapper.selectById(settingId);
        if (null == sysSetting) {
            return ReturnResult.error("配置不存在, 请刷新后重试!");
        }
        return ReturnResult.ok(sysSetting.getSysSettingValue());
    }
}
