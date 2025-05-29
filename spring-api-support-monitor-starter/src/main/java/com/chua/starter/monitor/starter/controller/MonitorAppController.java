package com.chua.starter.monitor.starter.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.discovery.support.service.DiscoveryService;
import com.chua.starter.monitor.starter.entity.MonitorApplication;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    final DiscoveryService discoveryService;


    /**
     * 查询数据详情
     *
     * @param entity 结果
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("detail/{monitorId}")
    public ReturnResult<MonitorApplication> detail(@PathVariable("monitorId") Integer monitorId) {
        MonitorApplication monitorApplication = getService().getById(monitorId);
        if (null == monitorApplication) {
            return ReturnResult.error("数据不存在");
        }
        List<MonitorApplication> monitorApplications = Collections.singletonList(monitorApplication);
        mergePage(monitorApplications);
        return ReturnResult.ok(monitorApplications.getFirst());
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
        Page<MonitorApplication> page1 = getService().page(page.createPage(), Wrappers.lambdaQuery(entity));
        mergePage(page1);
        return PageResultUtils.ok(page1);
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
        List<MonitorApplication> page1 = getService().list(Wrappers.lambdaQuery(entity));
        mergePage(page1);
        return ReturnResult.ok(page1);
    }

    /**
     * 分页查询数据
     * @param page 分页
     */
    private void mergePage(Page<MonitorApplication> page) {
        mergePage(page.getRecords());
    }
    /**
     * 分页查询数据
     * @param page1 分页
     */
    private void mergePage(List<MonitorApplication> page1) {
        Set<Discovery> discoveryAll = discoveryService.getDiscoveryAll("monitor");
        Map<String, List<Discovery>> applicationName = discoveryAll.stream().collect(Collectors.groupingBy(it -> MapUtils.getString(it.getMetadata(), "applicationName")));
        for (MonitorApplication record : page1) {
            record.setMonitorRequests(applicationName.get(record.getMonitorApplicationName()));
        }
    }

}
