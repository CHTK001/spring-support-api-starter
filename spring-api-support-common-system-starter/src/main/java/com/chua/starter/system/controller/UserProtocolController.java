package com.chua.starter.system.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.entity.SysSetting;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import com.chua.starter.service.pojo.setting.SysSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 用户协议
 * @author CH
 * @since 2024/11/29
 */
@Tag(name = "用户协议")
@RestController
@RequestMapping("/v2/protocol")
@RequiredArgsConstructor
@ApiPlatform("system")
public class UserProtocolController {

    private final SysSettingService sysSettingService;
    /**
     * 用户协议
     *
     * @return 设置分页列表
     */
    @Operation(summary = "用户协议")
    @GetMapping("/login")
    @Ignore
    @ApiCacheKey("'sys:setting:protocol:login'")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<List<SysSettingVO>> user(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate("login:protocol"), roles);
        return ReturnResult.ok(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
    }
    /**
     * 用户协议
     *
     * @return 设置分页列表
     */
    @Operation(summary = "隐私协议")
    @GetMapping("/privacy")
    @Ignore
    @ApiCacheKey("'sys:setting:protocol:privacy'")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<List<SysSettingVO>> privacy(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysSetting> aDefault = sysSettingService.list(new SysSettingUpdate("login:privacy"), roles);
        return ReturnResult.ok(BeanUtils.copyPropertiesList(aDefault, SysSettingVO.class));
    }
}
