package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.search.SearchResultItem;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysCityService;
import com.chua.starter.service.event.EventRequest;
import com.chua.starter.service.query.SysCityQuery;
import com.chua.starter.system.entity.SysCity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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


    @ApiOperation("行政区划级联查询")
    @Operation(summary = "行政区划级联查询")
    @GetMapping("/tree")
    public Result<List<SysCity>> queryCity(@ParameterObject SysCityQuery query) {
        return sysCityService.queryCity(query);
    }

    /**
     * 行政区划级子集查询
     * @param query
     * @return
     */
    @ApiOperation("获取省份下所有子行政机构")
    @Operation(summary = "获取省份下所有子行政机构")
    @GetMapping("/province")
    public Result<List<SysCity>> queryCityForProvince(@ParameterObject SysCityQuery query) {
        return sysCityService.queryCityForProvince(query);
    }
}
