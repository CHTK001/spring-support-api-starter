package com.chua.starter.monitor.controller.tool;

import com.chua.extension.support.indicator.DataIndicator;
import com.chua.extension.support.indicator.TimeIndicator;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.constant.RedisConstant;
import com.chua.starter.monitor.pojo.IndicatorQuery;
import com.chua.starter.redis.support.service.SimpleRedisService;
import com.chua.starter.redis.support.service.TimeSeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chua.redis.support.constant.RedisConstant.REDIS_SIMPLE_SERIES_PREFIX;


/**
 * 时序信息
 * @author CH
 * @since 2024/5/13
 */
@RestController("timeSeriesToolController")
@RequestMapping("v1/time")
@Tag(name = "时序信息")
@RequiredArgsConstructor
@Getter
public class TimeSeriesController {

    private final TimeSeriesService timeSeriesService;
    private final SimpleRedisService simpleRedisService;
    /**
     * 查询基本信息。
     *
     * @param indicatorQuery 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询指标信息")
    @GetMapping("series")
    public ReturnResult<List<TimeIndicator>> list(IndicatorQuery indicatorQuery) {
        var result = timeSeriesService.range(RedisConstant.REDIS_TIME_SERIES_PREFIX + indicatorQuery.getName(),
                indicatorQuery.getFromTimestamp(),
                indicatorQuery.getToTimestamp(),
                indicatorQuery.isLatest(),
                indicatorQuery.getCount());
        if (result == null || !result.isSuccess()) {
            return result != null ? ReturnResult.error(result.getMsg()) : ReturnResult.error("查询失败");
        }
        var data = result.getData();
        if (data == null) {
            return ReturnResult.ok(List.of());
        }
        var converted = data.stream().map(item -> {
            var ti = new TimeIndicator("");
            ti.setTimestamp(item.getTimestamp());
            ti.setValue(item.getValue());
            return ti;
        }).collect(Collectors.toList());
        return ReturnResult.ok(converted);
    }


    /**
     * 查询基本信息。
     *
     * @param indicatorQuery 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @GetMapping("qps")
    @Operation(summary = "查询QPS")
    public ReturnResult<BigDecimal> qps(IndicatorQuery indicatorQuery) {
        return simpleRedisService.qps(REDIS_SIMPLE_SERIES_PREFIX + indicatorQuery.getName());
    }
    /**
     * 查询基本信息。
     *
     * @param indicatorQuery 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询指标信息")
    @GetMapping("multi")
    public ReturnResult<Map<String, List<TimeIndicator>>> mRange(IndicatorQuery indicatorQuery) {
        var result = timeSeriesService.mRange(RedisConstant.REDIS_TIME_SERIES_PREFIX + indicatorQuery.getName(),
                indicatorQuery.getFromTimestamp(),
                indicatorQuery.getToTimestamp(),
                indicatorQuery.isLatest(),
                indicatorQuery.getCount());
        if (result == null || !result.isSuccess()) {
            return result != null ? ReturnResult.error(result.getMsg()) : ReturnResult.error("查询失败");
        }
        var data = result.getData();
        if (data == null) {
            return ReturnResult.ok(Map.of());
        }
        var converted = data.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(item -> {
                    var ti = new TimeIndicator("");
                    ti.setTimestamp(item.getTimestamp());
                    ti.setValue(item.getValue());
                    return ti;
                }).collect(Collectors.toList())
        ));
        return ReturnResult.ok(converted);
    }
    /**
     * 查询基本信息。
     *
     * @param indicatorQuery 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询指标信息")
    @GetMapping("get")
    public ReturnResult<DataIndicator> get(IndicatorQuery indicatorQuery) {
        var result = timeSeriesService.get(REDIS_SIMPLE_SERIES_PREFIX + indicatorQuery.getName(), indicatorQuery.getFromTimestamp(), indicatorQuery.getToTimestamp(), indicatorQuery.getCount());
        if (result == null || !result.isSuccess()) {
            return result != null ? ReturnResult.error(result.getMsg()) : ReturnResult.error("查询失败");
        }
        var data = result.getData();
        if (data == null) {
            return ReturnResult.error("数据为空");
        }
        var di = new DataIndicator("");
        di.setTimestamp(data.getTimestamp());
        di.setValue(data.getValue() != null ? String.valueOf(data.getValue()) : "");
        return ReturnResult.ok(di);
    }
    /**
     * 查询基本信息。
     *
     * @param indicatorQuery 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询指标信息")
    @GetMapping("hGet")
    public ReturnResult<Map<String, String>> hGet(IndicatorQuery indicatorQuery) {
        return ReturnResult.of(timeSeriesService.hGet(REDIS_SIMPLE_SERIES_PREFIX + indicatorQuery.getName()));
    }
}
