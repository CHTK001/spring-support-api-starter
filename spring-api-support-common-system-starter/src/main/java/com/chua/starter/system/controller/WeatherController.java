package com.chua.starter.system.controller;

import com.chua.advanced.support.external.Weather;
import com.chua.advanced.support.external.WeatherResolver;
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
 * 澶栭儴鎺у埗鍣?
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/22
 */
@RestController
@Api(value = "澶╂皵鎺ュ彛", tags = "澶╂皵鎺ュ彛")
@Tag(name = "澶╂皵鎺ュ彛")
@RequestMapping("/v1/weather")
public class WeatherController {

    /**
     * 澶╂皵
     *
     * @param request 鍩庡競
     * @return {@link ReturnResult}<{@link Weather}>
     */
    @ApiOperation("鑾峰彇褰撳墠鍦板潃澶╂皵")
    @Operation(summary = "鑾峰彇褰撳墠鍦板潃澶╂皵")
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
     * 澶╂皵
     *
     * @param city 鍩庡競
     * @return {@link ReturnResult}<{@link Weather}>
     */
    @Operation(summary = "鏍规嵁鍩庡競鑾峰彇澶╂皵")
    @ApiOperation("鏍规嵁鍩庡競鑾峰彇澶╂皵")
    @GetMapping("city")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_HOUR, cacheNames = CacheConstant.REDIS_CACHE_HOUR, key = "'cache:weather:'+#p0")
    public ReturnResult<Weather> getWeather(@RequestParam(value = "city", required = false) String city) {
        city = city == null ? "" : city;
        Weather weather = WeatherResolver.create("jike").getWeather(city);
        return ReturnResult.of(weather);
    }
}
