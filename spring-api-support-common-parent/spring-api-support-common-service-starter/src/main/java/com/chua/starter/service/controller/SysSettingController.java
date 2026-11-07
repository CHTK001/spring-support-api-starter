// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.controller;
import com.chua.starter.common.support.annotations.SysLog;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.common.support.spi.annotations.SpiIgnore;
import com.chua.common.support.utils.BeanUtils;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysSettingHistoryService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingHistory;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import com.chua.starter.service.pojo.setting.SysSettingVO;
import com.chua.starter.service.setting.SmtpSetting;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.service.impl.SysSettingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 系统设置接口(缓存)
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "系统全局设置接口")
@RestController
@RequestMapping("/v2/setting")
@RequiredArgsConstructor
public class SysSettingController {
    /**
     * Setting_列表_缓存_键_前缀
    */
    public static final String SETTING_LIST_CACHE_KEY_PREFIX = "sys:setting:v2:";
    /**
     * 默认设置聚合缓存键：跨 default、sso、default-1 三个分组，任一分组变更都需失效
    */
    public static final String SETTING_DEFAULT_CACHE_KEY = "sys:setting:default";
    /**
     * 默认设置聚合缓存键中「按租户隔离」的键值。
     *
     * <p><b>为什么必须带租户维度</b>：开启按租户切库后 {@code /v2/setting} 会路由到租户库读
     * {@code sys_setting}，但 Redis 缓存是全局的。键里不含租户时，第一个访问的租户会把结果
     * 写进同一个键，其它租户读到的就是<b>别人库里的配置</b>——接口地址、三方登录密钥、
     * 功能开关全部串台，且不会报错。</p>
     *
     * <p>取值来自线程上的数据源上下文（租户库名为 {@code tenant-<id>}），平台请求为空串，
     * 因此平台与各租户的键天然分开。刻意不用会话：{@code /default} 未登录可调，此时无会话可用。</p>
     *
     * <p><b>为什么整段用 try/catch 兜住</b>：数据源模块在本模块是 {@code provided}，
     * 只用本模块而未引入 {@code spring-support-datasource-starter} 的应用里该类不存在，
     * 直接调用会抛 {@code NoClassDefFoundError}，且发生在缓存键求值阶段——
     * 会让 {@code /v2/setting/default} 整体 500，连登录页都打不开。
     * 这种部署下本来也没有租户切库，退化成平台维度即可。</p>
     *
     * @return 租户键段，形如 {@code 1950000000000000001:}；非租户上下文返回空串
     */
    public static String tenantCacheSegment() {
        String dbType;
        try {
            dbType = com.chua.starter.datasource.support.DataSourceContextSupport.getDbType();
        } catch (Throwable notAvailable) {
            // 未引入数据源模块：无租户切库能力，全部按平台维度缓存
            return "";
        }
        if (dbType == null || dbType.isBlank()) {
            return "";
        }
        // 只认租户库前缀：主库名/负载均衡组等其它上下文不该改变全局配置缓存
        int index = dbType.indexOf(TENANT_DB_PREFIX);
        if (index < 0) {
            return "";
        }
        return dbType.substring(index + TENANT_DB_PREFIX.length()) + ":";
    }
    /**
     * 租户库注册名前缀，与 TenantProperties.ServerConfig 默认 datasource-routing-prefix 一致
     */
    private static final String TENANT_DB_PREFIX = "tenant-";
    /**
     * Runtime_公共_codec_请求_键
    */
    private static final String RUNTIME_PUBLIC_CODEC_REQUEST_KEY = "CodecRequestKey";
    /**
     * Admin
    */
    private static final String ADMIN = "ADMIN";

    /**
     * SYS设置服务
    */
    private final SysSettingService sysSettingService;
/**
     * SYS设置历史服务
     */
    private final SysSettingHistoryService sysSettingHistoryService;

    /**
     * 运行环境配置（下发 API 基础路径与文件服务器地址等运行期派生项）
     */
    private final Environment environment;


    /**
     * 设置分页列表
     *
     * @param sysSetting 条件
     * @return 设置分页列表
     */
    @Operation(summary = "设置分页列表")
    @GetMapping("/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysSetting> pageForSetting(
            @ParameterObject Query<SysSetting> query,
            @ParameterObject SysSetting sysSetting,
            @RequestParam(required = false) String sysSettingValueType,
            @RequestParam(required = false) Integer sysSettingInSystem,
            @RequestParam(required = false) String scope,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if (null != sysSettingValueType && !sysSettingValueType.isBlank()) {
            sysSetting.setSysSettingValueType(sysSettingValueType);
        }
        if (null != sysSettingInSystem) {
            sysSetting.setSysSettingInSystem(sysSettingInSystem);
        }
        IPage<SysSetting> page = sysSettingService.pageForSetting(query, sysSetting, scope, roles);
        // 敏感配置掩码
        page.getRecords().forEach(this::maskSensitive);
        return PageResultUtils.ok(page);
    }
    /**
     * 默认设置列表。
     *
     * <p>未登录可调：登录页需要它渲染品牌、三方登录、API 基础路径与文件服务器地址等配置，
     * 因此不加令牌校验；返回值已对敏感项做掩码（{@link #maskSensitive}）。</p>
     *
     * @return 设置列表
     */
    @Operation(summary = "默认设置列表")
    @GetMapping("/default")
    @TokenForIgnore
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "T(com.chua.starter.service.controller.SysSettingCo" +
        "ntroller).SETTING_DEFAULT_CACHE_KEY + T(com.chua.starter.service.controller.SysSettingController).tenantCacheSegment()")
    public ReturnResult<List<SysSettingVO>> listForDefault() {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate(String.join(",", SysSettingService.USER_FACING_GROUPS),
            null), Collections.emptySet());
        aDefault.forEach(this::maskSensitive);
        List<SysSettingVO> records = new LinkedList<>(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
        records.add(emailVerifyCapability());
        records.add(apiBaseUrlConfig());
        SysSettingVO fileServer = fileServerConfig();
        if (null != fileServer) {
            records.add(fileServer);
        }
        SysSettingVO fileStorageMode = fileStorageModeConfig();
        if (null != fileStorageMode) {
            records.add(fileStorageMode);
        }
        return ReturnResult.ok(records);
    }

    /**
     * 已开启的三方登录来源（登录页与绑定面板共用同一配置源）。
     *
     * <p>未登录可调，仅返回小写 loginType 列表，不含任何密钥。</p>
     *
     * @return 已开启的 loginType 列表
     */
    @Operation(summary = "已开启的三方登录来源")
    @GetMapping("/sso/providers")
    @TokenForIgnore
    public ReturnResult<List<String>> listEnabledThirdPartyProviders() {
        return ReturnResult.ok(sysSettingService.listEnabledThirdPartyProviders());
    }

    /**
     * 派生运行期配置：API 基础路径（{@code http.baseUrl}）。
     *
     * <p>取当前应用 servlet 上下文路径，供前端 {@code http()} 解析请求地址（空串=同源相对路径）。</p>
     *
     * @return 配置项
     */
    private SysSettingVO apiBaseUrlConfig() {
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        SysSettingVO vo = new SysSettingVO();
        vo.setSysSettingName("ApiBaseUrl");
        vo.setSysSettingCode("http.baseUrl");
        vo.setSysSettingValue(contextPath);
        vo.setSysSettingValueType("String");
        vo.setSysSettingGroup("default");
        vo.setSysSettingRemark("系统 API 基础路径（前端 http() 取此值，空=同源）");
        return vo;
    }

    /**
     * 派生运行期配置：文件服务器地址（{@code other.filestorage.server}）与部署形态。
     *
     * <p>取 {@code plugin.filesystem.storage-server.*}；未启用时返回 {@code null}（前端按嵌入式回退）。</p>
     *
     * <p><b>部署形态与下发模型（2026-10-20 修正）</b>：默认嵌入启动，只有需要独立端口的业务
     * 才配置 {@code mode=standalone}。</p>
     * <ul>
     *   <li><b>embedded（默认）</b>：文件服务器挂在应用端口 {@code contextPath + path-prefix}
     *       （默认 {@code /fs}）之下，<b>不下发端口</b>；下发相对路径，由前端拼当前页面
     *       的 origin。另外下发 {@code other.filestorage.mode=embedded}，供前端明确分支。</li>
     *   <li><b>standalone</b>：下发 {@code http://host:port} 完整地址（含端口），
     *       并下发 {@code other.filestorage.mode=standalone}。</li>
     * </ul>
     *
     * <p><b>对外可达地址的取法（2026-10-04 修正）</b>：显式配置 {@code public-host} 优先；
     * 未配置、**或配成环回地址**（{@code 127.0.0.1} / {@code localhost}）时，
     * <b>按当前请求的 Host 派生</b>。理由：文件服务器与应用是<b>同机部署</b>
     * （{@code plugin.filesystem.storage-server} 的默认值就是如此），而环回地址作为
     * 「对外可达地址」本身没有意义 —— 它会让<b>从别的机器访问页面</b>的浏览器去连
     * <b>访问者自己的</b> {@code localhost:18080}，直接打不通。
     * 实测就是这个问题：前端 {@code app.yaml} 里写对了 {@code 192.168.50.198:18080}，
     * 却被这条运行期下发的 {@code http://127.0.0.1:18080} 盖掉（前端优先级更高）。
     * 按请求 Host 派生后，dev（vite 代理 {@code changeOrigin:true}，后端看到的 Host 即目标机）
     * 与生产（访问入口的 Host）都能得到与接口同机的正确地址。</p>
     *
     * @return 配置项；未启用文件服务器时返回 {@code null}
     */
    private SysSettingVO fileServerConfig() {
        String enable = environment.getProperty("plugin.filesystem.storage-server.enable", "true");
        if ("false".equalsIgnoreCase(enable)) {
            return null;
        }
        String mode = environment.getProperty("plugin.filesystem.storage-server.mode", "embedded");
        boolean standalone = "standalone".equalsIgnoreCase(mode);
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String pathPrefix = environment.getProperty("plugin.filesystem.storage-server.path-prefix", "/fs");
        String host = null;
        String port = null;
        if (standalone) {
            port = environment.getProperty("plugin.filesystem.storage-server.port");
            if (null == port || port.isBlank()) {
                return null;
            }
            host = environment.getProperty("plugin.filesystem.storage-server.public-host");
            if (isLoopbackHost(host)) {
                // 未配置或配成环回地址：按当前请求的 Host 派生（取不到再回落）
                String derived = currentRequestHost();
                host = (null == derived || derived.isBlank()) ? host : derived;
            }
        }
        String server = resolveFileStorageServer(mode, contextPath, pathPrefix, port, host);
        if (server == null || server.isEmpty()) {
            return null;
        }
        String remark = standalone
                ? "文件服务器地址（独立服务，前端文件服务器页连接地址）"
                : "文件服务器内置地址（挂载在应用端口，相对路径，由前端拼 origin）";
        SysSettingVO vo = new SysSettingVO();
        vo.setSysSettingName("FileStorageServer");
        vo.setSysSettingCode("other.filestorage.server");
        vo.setSysSettingValue(server);
        vo.setSysSettingValueType("String");
        vo.setSysSettingGroup("default");
        vo.setSysSettingRemark(remark);
        return vo;
    }

    /**
     * 派生运行期配置：文件服务器部署形态（{@code other.filestorage.mode}）。
     *
     * <p>默认 embedded；与 {@link #fileServerConfig()} 配套，供前端决定
     * 嵌入式用相对路径、独立用绝对地址。</p>
     *
     * @return 配置项
     */
    private SysSettingVO fileStorageModeConfig() {
        String enable = environment.getProperty("plugin.filesystem.storage-server.enable", "true");
        if ("false".equalsIgnoreCase(enable)) {
            return null;
        }
        String mode = environment.getProperty("plugin.filesystem.storage-server.mode", "embedded");
        SysSettingVO vo = new SysSettingVO();
        vo.setSysSettingName("FileStorageMode");
        vo.setSysSettingCode("other.filestorage.mode");
        vo.setSysSettingValue(mode);
        vo.setSysSettingValueType("String");
        vo.setSysSettingGroup("default");
        vo.setSysSettingRemark("文件服务器部署形态：embedded（应用端口+路径前缀）/ standalone（独立端口）");
        return vo;
    }

    /**
     * 推导文件服务器返回模型（核心纯逻辑，供单元测试）。
     *
     * @param mode        部署形态（embedded / standalone）
     * @param contextPath 应用上下文路径
     * @param pathPrefix  内置形态路径前缀
     * @param port        独立形态端口（standalone 必填）
     * @param host        独立形态对外主机
     * @return server 配置值：embedded=相对路径，standalone=http://host:port；返回空串表示不适用
     */
    static String resolveFileStorageServer(String mode, String contextPath, String pathPrefix,
                                           String port, String host) {
        boolean standalone = "standalone".equalsIgnoreCase(mode);
        if (standalone) {
            if (null == port || port.isBlank()) {
                return "";
            }
            String h = host;
            if (null == h || h.isBlank() || "0.0.0.0".equals(h)) {
                h = "127.0.0.1";
            }
            return "http://" + h + ":" + port;
        }
        return normalizePath(contextPath, pathPrefix);
    }

    /**
     * 拼接 contextPath 与 pathPrefix，保证只有一个斜杠边界。
     *
     * @param base 上下文路径（可能为空）
     * @param tail 路径前缀（可能为空、{@code /} 或 {@code /fs}）
     * @return 拼接后的路径；均为空时返回空串
     */
    static String normalizePath(String base, String tail) {
        String b = base == null ? "" : base.trim();
        String t = tail == null ? "" : tail.trim();
        if (t.equals("/")) {
            t = "";
        }
        while (b.startsWith("/")) {
            b = b.substring(1);
        }
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        while (t.startsWith("/")) {
            t = t.substring(1);
        }
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        if (b.isEmpty() && t.isEmpty()) {
            return "/";
        }
        if (t.isEmpty()) {
            return "/" + b;
        }
        return "/" + (b.isEmpty() ? "" : b + "/") + t;
    }

    /**
     * 判断主机名是否为环回地址（作为「对外可达地址」无意义）。
     *
     * @param host 主机名，可为 null
     * @return true 表示未配置或为环回地址
     */
    private boolean isLoopbackHost(String host) {
        if (null == host || host.isBlank()) {
            return true;
        }
        String h = host.trim().toLowerCase();
        return "127.0.0.1".equals(h) || "localhost".equals(h) || "::1".equals(h);
    }

    /**
     * 取当前请求的 Host（不含端口），用于派生「与接口同机」的文件服务器地址。
     *
     * <p>优先 {@code X-Forwarded-Host}（反向代理场景），其次 {@code request.getServerName()}；
     * 取不到或异常时返回 空，由调用方回落。用 {@code RequestContextHolder} 取当前请求，
     * 避免为此改动接口签名。</p>
     *
     * @return 主机名；取不到返回 空
     */
    private String currentRequestHost() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                String h = sra.getRequest().getHeader("X-Forwarded-Host");
                if (null == h || h.isBlank()) {
                    h = sra.getRequest().getServerName();
                }
                if (null != h && !h.isBlank()) {
                    // X-Forwarded-Host 可能是逗号分隔的链，取第一个
                    int idx = h.indexOf(',');
                    if (idx > 0) {
                        h = h.substring(0, idx);
                    }
                    return h.trim();
                }
            }
        } catch (Exception ignore) {
            // 无请求上下文（如定时任务触发）时回落
        }
        return null;
    }

    /**
     * 派生能力开关：服务器邮箱发信通道是否已配置
     *
     * <p>smtp 分组本身不下发给普通用户，个人中心只需要一个"能不能验证邮箱"的结论。
     * 该值不落 sys_setting，由运行期配置实时计算，配置写入后随本接口缓存失效而更新。</p>
     *
     * @return 能力开关配置项
     */
    private SysSettingVO emailVerifyCapability() {
        SmtpSetting smtpSetting = GlobalSettingFactory.getInstance().get("smtp", SmtpSetting.class);
        SysSettingVO capability = new SysSettingVO();
        capability.setSysSettingName("EmailVerifyOpen");
        capability.setSysSettingCode("other.emailVerify.enable");
        capability.setSysSettingValue(String.valueOf(null != smtpSetting && smtpSetting.isValid()));
        capability.setSysSettingValueType("Boolean");
        capability.setSysSettingGroup("default");
        capability.setSysSettingRemark("服务器邮箱发信通道是否已配置");
        return capability;
    }

    /**
     * 设置分页列表
     *
     * @param sysSettingUpdate 条件
     * @return 设置分页列表
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "设置列表")
    @GetMapping("/list")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "T(com.chua.starter.service.controller.SysSettingController).SETTING_LIST_CACHE_KEY_PREFIX + T(com.chua.starter.service.controller.SysSettingController).tenantCacheSegment() + #sysSettingUpdate.sysSettingGroup")
    public ReturnResult<List<SysSettingVO>> list(@ParameterObject SysSettingUpdate sysSettingUpdate,
        @Parameter(hidden = true) @UserValue("roles") HashSet<String> roles) {
        Set<String> safeRoles = null == roles ? Collections.emptySet() : roles;
        List<SysSetting> list = sysSettingService.list(sysSettingUpdate, roles);
        if(!AuthConstant.isAdmin(safeRoles)) {
            list = list.stream()
                    .filter(it -> {
                        if (RUNTIME_PUBLIC_CODEC_REQUEST_KEY.equals(it.getSysSettingName())) {
                            return true;
                        }
                        return null == it.getSysSettingAdmin() || it.getSysSettingAdmin() != 1;
                    })
                    .toList();
        }
        // 敏感配置掩码
        list.forEach(this::maskSensitive);
        return ReturnResult.ok(BeanUtils.copyPropertiesList(list, SysSettingVO.class));
    }

    /**
     * 删除设置
     *
     * @return 更新设置
     * @param true true
     * @param roles 方法入参 roles
     * @param sysSettingId sysSettingID，不允许为 null
     */
    @Operation(summary = "删除设置")
    @DeleteMapping("/delete")
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:delete", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> delete(@Parameter(hidden = true) @UserValue("roles") Set<String> roles, Long sysSettingId) {
        if (null == sysSettingId) {
            return ReturnResult.illegal("设置信息不存在");
        }

        return sysSettingService.delete(sysSettingId);
    }

    /**
     * 更新设置
     *
     * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("/updateBatch")
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:update", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> updateBatch(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody List<SysSetting> sysSetting,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.updateBatch(roles, sysSetting);
    }

    /**
     * 更新设置
     *
     * @return 更新设置
     */
    @Operation(summary = "更新设置")
    @PutMapping("/update")
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:update", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysSetting sysSetting,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.update(roles, sysSetting);
    }

    /**
     * 新增设置
     *
     * @return 新增设置
     * @param true true
     * @param roles 方法入参 roles
     * @param sysSetting 方法入参 sysSetting
     * @param bindingResult binding结果，不允许为 null
     */
    @Operation(summary = "新增设置")
    @PostMapping("/save")
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:setting:add", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<SysSetting> save( @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("userId") String operatorId,
            @Parameter(hidden = true) @UserValue("username") String operatorName,
            @Validated(AddGroup.class) @RequestBody SysSetting sysSetting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingService.saveForEntity(sysSetting, roles, operatorId, operatorName);
    }

    /**
     * 导出配置
     *
     * @param group 配置分组（可选）
     * @return JSON配置字符串
     * @param false false
     */
    @Operation(summary = "导出配置")
    @GetMapping("/export")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<String> exportSettings(@RequestParam(required = false) String group) {
        return sysSettingService.exportSettings(group);
    }

    /**
     * 导入配置
     *
     * @param json 配置JSON字符串
     * @param overwrite 是否覆盖已存在的配置
     * @return 导入结果，返回批次号
     */
    @Operation(summary = "导入配置")
    @PostMapping("/import")
    @SysLog(module = "导入")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<String> importSettings(
            @RequestBody String json,
            @RequestParam(defaultValue = "false") boolean overwrite,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysSettingService.importSettings(json, overwrite, roles);
    }

    /**
     * 查询配置变更历史
     *
     * @param query 分页参数
     * @param settingId 配置标识（可选）
     * @param group 配置分组（可选）
     * @return 变更历史分页列表
     */
    @Operation(summary = "查询配置变更历史")
    @GetMapping("/history/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysSettingHistory> pageHistory(
            @ParameterObject Query<SysSettingHistory> query,
            @RequestParam(required = false) Integer settingId,
            @RequestParam(required = false) String group) {
        return PageResultUtils.ok(sysSettingHistoryService.pageHistory(query, settingId, group));
    }

    /**
     * 根据批次号查询变更历史
     *
     * @param batchNo 批次号
     * @return 变更历史列表
     */
    @Operation(summary = "根据批次号查询变更历史")
    @GetMapping("/history/batch")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<List<SysSettingHistory>> listByBatchNo(@RequestParam String batchNo) {
        return ReturnResult.ok(sysSettingHistoryService.listByBatchNo(batchNo));
    }

    /**
     * 回滚到指定批次
     *
     * @param batchNo 批次号
     * @return 回滚结果
     */
    @Operation(summary = "回滚到指定批次")
    @PostMapping("/history/rollback/batch")
    @SysLog(module = "回滚")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> rollbackToBatch(
            @RequestParam String batchNo,
            @Parameter(hidden = true) @UserValue("userId") String operatorId,
            @Parameter(hidden = true) @UserValue("username") String operatorName) {
        return sysSettingHistoryService.rollbackToBatch(batchNo, operatorId, operatorName);
    }

    /**
     * 回滚到指定历史记录
     *
     * @param historyId 历史记录标识
     * @return 回滚结果
     */
    @Operation(summary = "回滚单条配置到指定历史记录")
    @PostMapping("/history/rollback/single")
    @SysLog(module = "回滚")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> rollbackToHistory(
            @RequestParam Long historyId,
            @Parameter(hidden = true) @UserValue("userId") String operatorId,
            @Parameter(hidden = true) @UserValue("username") String operatorName) {
        return sysSettingHistoryService.rollbackToHistory(historyId, operatorId, operatorName);
    }

    /**
     * 获取敏感配置明文（用于"显示明文"按钮，需专属权限）
     *
     * @param settingId 配置标识
     * @return 明文配置值
     */
    @Operation(summary = "查看敏感配置明文")
    @GetMapping("/decrypt")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:decrypt", role = {SUPER_ADMIN})
    public ReturnResult<String> decrypt(@RequestParam Integer settingId) {
        return sysSettingService.getPlainValue(settingId);
    }

    /**
     * 敏感配置掩码：sysSettingIsSensitive=1 时将配置值替换为掩码占位符
     *
     * @param setting 配置
     */
    private void maskSensitive(SysSetting setting) {
        if (null != setting && null != setting.getSysSettingIsSensitive() && setting.getSysSettingIsSensitive() == 1) {
            setting.setSysSettingValue(SysSettingServiceImpl.SENSITIVE_MASK);
        }
    }
}
