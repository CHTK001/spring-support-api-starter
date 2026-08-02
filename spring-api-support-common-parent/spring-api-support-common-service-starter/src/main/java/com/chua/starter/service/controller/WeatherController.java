package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气接口（接口桩）
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/22
 */
@Tag(name = "天气接口")
@RestController
@RequestMapping("/v1/weather")
public class WeatherController {

    @Operation(summary = "获取当前地址天气")
    @GetMapping("address")
    public ReturnResult<String> getWeather() {
        return ReturnResult.error("天气接口暂未实现");
    }

    @Operation(summary = "根据城市获取天气")
    @GetMapping("city")
    public ReturnResult<String> getWeatherByCity(@RequestParam(value = "city", required = false) String city) {
        return ReturnResult.error("天气接口暂未实现");
    }
}