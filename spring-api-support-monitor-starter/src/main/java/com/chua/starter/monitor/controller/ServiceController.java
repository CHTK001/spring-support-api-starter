package com.chua.starter.monitor.controller;

import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.debounce.Debounce;
import com.chua.starter.discovery.support.service.DiscoveryService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 服务管理
 * @author CH
 * @since 2024/9/12
 */
@RestController
@Slf4j
@Api(tags = "服务管理")
@Tag(name = "服务管理")
@RequestMapping("/v2/service")
@RequiredArgsConstructor
public class ServiceController {

    final DiscoveryService discoveryService;


    /**
     * 服务列表
     * @return 服务列表
     */
    @Operation(summary = "服务列表")
    @GetMapping("/list")
    @Debounce(key = "#{#uriSpec}")
    public ReturnResult<Set<Discovery>> list(@RequestParam String uriSpec) {
        return ReturnResult.success(discoveryService.getDiscoveryAll(uriSpec));
    }
}
