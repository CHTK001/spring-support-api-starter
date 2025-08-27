package com.chua.starter.system.controller;

import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.service.SysCityService;
import com.chua.starter.system.entity.SysCity;
import com.chua.starter.system.query.SysCityQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 事件接口
 * @author CH
 * @since 2024/9/7
 */
@RestController
@Slf4j
@Api(tags = "区域规划接口")
@Tag(name = "区域规划接口")
@RequestMapping("/v2/city")
@RequiredArgsConstructor
public class SysCityController {

    final SysCityService sysCityService;


    /**
     * 获取下级行政区划信息
     *
     * @param query 包含父级行政区划代码的查询对象
     * @return 返回下级行政区划列表
     */
    @ApiOperation("获取下级行政区划")
    @Operation(summary = "获取下级行政区划")
    @GetMapping("/children")
    @ApiCacheKey("'sys:city:' + #query.parentCode")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public Result<List<SysCity>> queryCity(@ParameterObject SysCityQuery query) {
        return sysCityService.queryCity(query);
    }

    /**
     * 获取下级以及所有子集行政区划信息
     *
     * @param query 包含父级行政区划代码的查询对象
     * @return 返回下级以及子集行政区划列表
     */
    @ApiOperation("获取下级以及子集行政区划")
    @Operation(summary = "获取下级以及子集行政区划")
    @GetMapping("/childrenAll")
    @ApiCacheKey("'sys:city:all:' + #query.parentCode")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public Result<List<SysCity>> childrenAll(@ParameterObject SysCityQuery query) {
        return sysCityService.childrenAll(query);
    }
}
