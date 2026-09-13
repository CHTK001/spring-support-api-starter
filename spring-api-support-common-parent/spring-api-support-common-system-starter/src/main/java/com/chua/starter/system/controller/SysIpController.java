package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.system.service.SysIpBlackwhiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* IP 地址查询接口
*
* <p>提供 IP 地址的地理位置和运营商信息查询, 以及 IP 黑白名单管理能力。</p>
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

    /** IP黑白名单服务 */
    private final SysIpBlackwhiteService sysIpBlackwhiteService;

    /**
    * 查询 IP 地址的地理位置和运营商信息
    *
    * @param ip IP 地址
    * @return 地理位置信息(国家/省份/城市/ISP 等)
     */
    @Operation(summary = "IP 地址归属地查询")
    @GetMapping("/lookup")
    public ReturnResult<?> lookup(@RequestParam String ip) {
        return iptablesService.transferAddress(ip);
    }

    /**
    * IP黑白名单分页
    *
    * @param query  分页参数
    * @param entity 查询条件
    * @return 分页结果
     */
    @Operation(summary = "IP黑白名单分页")
    @GetMapping("/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysIpBlackwhite> page(@ParameterObject Query<SysIpBlackwhite> query,
                                                 @ParameterObject SysIpBlackwhite entity) {
        return PageResultUtils.ok(sysIpBlackwhiteService.page(query, entity));
    }

    /**
    * 新增IP黑白名单
    *
    * @param entity 实体
    * @return 新增结果
     */
    @Operation(summary = "新增IP黑白名单")
    @PostMapping("/save")
    @Permission(value = "sys:ip:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysIpBlackwhite> save(@RequestBody SysIpBlackwhite entity) {
        return sysIpBlackwhiteService.saveIp(entity);
    }

    /**
    * 更新IP黑白名单
    *
    * @param entity 实体
    * @return 更新结果
     */
    @Operation(summary = "更新IP黑白名单")
    @PutMapping("/update")
    @Permission(value = "sys:ip:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(@RequestBody SysIpBlackwhite entity) {
        return sysIpBlackwhiteService.updateIp(entity);
    }

    /**
    * 删除IP黑白名单
    *
    * @param sysIpId 主键
    * @return 删除结果
     */
    @Operation(summary = "删除IP黑白名单")
    @DeleteMapping("/delete")
    @Permission(value = "sys:ip:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Integer sysIpId) {
        if (null == sysIpId) {
            return ReturnResult.illegal("记录不存在");
        }
        return sysIpBlackwhiteService.deleteIp(sysIpId);
    }
}
