package com.chua.starter.system.external;

import com.chua.common.support.external.Weather;
import com.chua.common.support.external.WeatherResolver;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.value.Value;
import com.chua.starter.common.support.utils.RequestUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

import static com.chua.starter.common.support.constant.Constant.REDIS_CACHE_HOUR;
import static java.util.concurrent.TimeUnit.HOURS;

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
    private static final Cache<String, Value<Weather>> CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, HOURS).build();



    /**
     * 天气
     *
     * @param request 城市
     * @return {@link ReturnResult}<{@link Weather}>
     */
    @ApiOperation("获取当前地址天气")
    @Operation(summary = "获取当前地址天气")
    @GetMapping("address")
    @Cacheable(cacheManager = REDIS_CACHE_HOUR, cacheNames = REDIS_CACHE_HOUR,
            key = "'cache:weather:' + T(com.chua.starter.common.support.utils.RequestUtils).getIpAddress()")
    public ReturnResult<Weather> getWeather(HttpServletRequest request) {
        String ipAddress = RequestUtils.getIpAddress(request);
        Value<Weather> ifPresent = CACHE.getIfPresent(ipAddress);
        if(null != ifPresent) {
            return ReturnResult.of(ifPresent.getValue());
        }
        Weather weather = WeatherResolver.newDefault().getWeather(ipAddress);
        CACHE.put(ipAddress, Value.of(weather));
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
    @Cacheable(cacheManager = REDIS_CACHE_HOUR, cacheNames = REDIS_CACHE_HOUR, key = "'cache:weather:'+#p0")
    public ReturnResult<Weather> getWeather(@RequestParam(value = "city", required = false) String city) {
        city = city == null ? "" : city;
        Value<Weather> ifPresent = CACHE.getIfPresent(city);
        if(null != ifPresent) {
            return ReturnResult.of(ifPresent.getValue());
        }
        Weather weather = WeatherResolver.newDefault().getWeather(city);
        CACHE.put(city, Value.of(weather));
        return ReturnResult.of(weather);
    }
}
