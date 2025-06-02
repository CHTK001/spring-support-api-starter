package com.chua.starter.monitor.starter.controller;


import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorApplication;
import com.chua.starter.monitor.starter.pojo.OpenApi;
import com.chua.starter.monitor.starter.service.MonitorAppService;
import com.chua.starter.mybatis.controller.AbstractSwaggerUpdateController;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控应用控制器
 */
@RestController
@RequestMapping("v1/app")
@Tag(name = "上报数据接口")
@RequiredArgsConstructor
public class MonitorAppController extends AbstractSwaggerUpdateController<MonitorAppService, MonitorApplication> {

    @Getter
    private final MonitorAppService service;


    /**
     * 查询数据详情
     *
     * @param monitorId monitorId
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("api-docs/{monitorId}/{index}")
    public ReturnResult<List<OpenApi>> apiDocs(@PathVariable("monitorId") Integer monitorId,
                                               @PathVariable("index") String index
    ) {
        return service.render(monitorId, index);
    }

    /**
     * 查询数据详情
     *
     * @param monitorId monitorId
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("detail/{monitorId}")
    public ReturnResult<MonitorApplication> detail(@PathVariable("monitorId") Integer monitorId) {
        return service.detail(monitorId);
    }

    /**
     * 分页查询数据
     *
     * @param entity 结果
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询基础数据")
    @GetMapping("page")
    public ReturnPageResult<MonitorApplication> page(Query<MonitorApplication> page, @Valid MonitorApplication entity, BindingResult bindingResult) {
        return PageResultUtils.ok(service.pageFor(page, entity));
    }

    /**
     * 分页查询数据
     *
     * @param entity 结果
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "查询基础数据")
    @GetMapping("list")
    public ReturnResult<List<MonitorApplication>> list(MonitorApplication entity) {
        return service.list(entity);
    }


}
