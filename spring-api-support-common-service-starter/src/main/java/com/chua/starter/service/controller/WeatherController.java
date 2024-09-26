package com.chua.starter.service.controller;

import com.chua.common.support.external.Weather;
import com.chua.common.support.external.WeatherResolver;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.NetUtils;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.common.support.utils.RequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部控制器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/22
 */
@RestController
@Api(value = "天气接口", tags = "天气接口")
@Tag(name = "天气接口")
@RequestMapping("/v1/weather")
public class WeatherController {

    /**
     * 天气
     *
     * @param request 城市
     * @return {@link ReturnResult}<{@link Weather}>
     */
    @ApiOperation("获取当前地址天气")
    @Operation(summary = "获取当前地址天气")
    @GetMapping("address")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_HOUR, cacheNames = CacheConstant.REDIS_CACHE_HOUR,
            key = "'cache:weather:' + T(com.chua.starter.common.support.utils.RequestUtils).getIpAddress()")
    public ReturnResult<Weather> getWeather(HttpServletRequest request) {
        String ipAddress = RequestUtils.getIpAddress(request);
        if(NetUtils.isLocalHost(ipAddress)) {
            ipAddress = NetUtils.getPublicAddress();
        }
        Weather weather = WeatherResolver.create("jike").getWeather(ipAddress);
        return ReturnResult.of(weather);
    }
    /**
     * 天气
     *
     * @param city 城市
     * @return {@link ReturnResult}<{@link Weather}>
     */
    @Operation(summary = "根据城市获取天气")
    @ApiOperation("根据城市获取天气")
    @GetMapping("city")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_HOUR, cacheNames = CacheConstant.REDIS_CACHE_HOUR, key = "'cache:weather:'+#p0")
    public ReturnResult<Weather> getWeather(@RequestParam(value = "city", required = false) String city) {
        city = city == null ? "" : city;
        Weather weather = WeatherResolver.create("jike").getWeather(city);
        return ReturnResult.of(weather);
    }
}
