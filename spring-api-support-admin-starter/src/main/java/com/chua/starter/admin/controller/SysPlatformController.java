package com.chua.starter.admin.controller;

import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.service.IptablesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@Tag(name = "操作系统信息")
@RequestMapping("/v1/platform")
public class SysPlatformController {

    @Resource
    private IptablesService iptablesService;
    /**
     * 翻译地址
     * @param address 地址
     * @return 结果
     */
    @Operation(summary = "查询设置列表")
    @GetMapping("/transfer")
    public ReturnResult<GeoCity> transferAddress(String address) {
       return iptablesService.transferAddress(address);
    }
}
