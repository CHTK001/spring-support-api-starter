package com.chua.starter.admin.controller;

import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.geo.IpPosition;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.properties.IpProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.chua.starter.common.support.constant.Constant.REDIS_CACHE;

@RestController
@Tag(name = "操作系统信息")
@RequestMapping("/v1/platform")
public class SysPlatformController {

    @Resource
    private IpProperties ipProperties;
    /**
     * 翻译地址
     * @param address 地址
     * @return 结果
     */
    @Operation(summary = "查询设置列表")
    @GetMapping("/transfer")
    @Cacheable(cacheManager = REDIS_CACHE, cacheNames = REDIS_CACHE, key = "#address")
    public ReturnResult<GeoCity> transferAddress(String address) {
        if(StringUtils.isEmpty(address)) {
            return ReturnResult.illegal("地址不能为空");
        }

        IpPosition ipPosition = ServiceProvider.of(IpPosition.class).getExtension(ipProperties.getIpType());
        return null == ipPosition ? ReturnResult.illegal("解析失败") : ReturnResult.ok(ipPosition.getCity(address));
    }
}
