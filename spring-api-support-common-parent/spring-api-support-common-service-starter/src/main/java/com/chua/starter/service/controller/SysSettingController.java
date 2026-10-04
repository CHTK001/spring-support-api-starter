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
     * 设置分页列表
     *
     * @return 设置分页列表
     */
    @Operation(summary = "默认设置列表")
    @GetMapping("/default")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "T(com.chua.starter.service.controller.SysSettingController).SETTING_DEFAULT_CACHE_KEY")
    public ReturnResult<List<SysSettingVO>> listForDefault() {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate(String.join(",", SysSettingService.USER_FACING_GROUPS), null), Collections.emptySet());
        aDefault.forEach(this::maskSensitive);
        List<SysSettingVO> records = new LinkedList<>(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
        records.add(emailVerifyCapability());
        records.add(apiBaseUrlConfig());
        SysSettingVO fileServer = fileServerConfig();
        if (null != fileServer) {
            records.add(fileServer);
        }
        return ReturnResult.ok(records);
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
     * 派生运行期配置：文件服务器地址（{@code other.filestorage.server}）。
     *
     * <p>取 {@code plugin.filesystem.storage-server.*}；未启用或未配置端口时返回 空（前端回退当前地址）。</p>
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
     * @return 配置项；未启用文件服务器时返回 空
     */
    private SysSettingVO fileServerConfig() {
        String enable = environment.getProperty("plugin.filesystem.storage-server.enable", "true");
        if ("false".equalsIgnoreCase(enable)) {
            return null;
        }
        String port = environment.getProperty("plugin.filesystem.storage-server.port");
        if (null == port || port.isBlank()) {
            return null;
        }
        String host = environment.getProperty("plugin.filesystem.storage-server.public-host");
        if (isLoopbackHost(host)) {
            // 未配置或配成环回地址：按当前请求的 Host 派生（取不到再回落）
            String derived = currentRequestHost();
            host = (null == derived || derived.isBlank()) ? host : derived;
        }
        if (null == host || host.isBlank() || "0.0.0.0".equals(host)) {
            host = "127.0.0.1";
        }
        SysSettingVO vo = new SysSettingVO();
        vo.setSysSettingName("FileStorageServer");
        vo.setSysSettingCode("other.filestorage.server");
        vo.setSysSettingValue("http://" + host + ":" + port);
        vo.setSysSettingValueType("String");
        vo.setSysSettingGroup("default");
        vo.setSysSettingRemark("文件服务器地址（前端文件服务器页连接地址）");
        return vo;
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
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "T(com.chua.starter.service.controller.SysSettingController).SETTING_LIST_CACHE_KEY_PREFIX + #sysSettingUpdate.sysSettingGroup")
    public ReturnResult<List<SysSettingVO>> list(@ParameterObject SysSettingUpdate sysSettingUpdate, @Parameter(hidden = true) @UserValue("roles") HashSet<String> roles) {
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
