package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.service.IptablesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IP 地址查询接口
 *
 * <p>提供 IP 地址的地理位置和运营商信息查询，内部使用 IptablesService 解析并缓存。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Tag(name = "IP 地址查询接口")
@RestController
@RequestMapping("/v2/ip")
@RequiredArgsConstructor
public class SysIpController {

    /** Iptables服务 */
    private final IptablesService iptablesService;

    /**
     * 查询 IP 地址的地理位置和运营商信息
     *
     * @param ip IP 地址
     * @return 地理位置信息（国家/省份/城市/ISP 等）
     */
    @Operation(summary = "IP 地址归属地查询")
    @GetMapping("/lookup")
    public ReturnResult<?> lookup(@RequestParam String ip) {
        return iptablesService.transferAddress(ip);
    }
}