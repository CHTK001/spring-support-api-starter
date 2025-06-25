//package com.chua.starter.monitor.controller;
//
//import com.chua.common.support.lang.code.ReturnResult;
//import com.chua.common.support.validator.group.AddGroup;
//import com.chua.common.support.validator.group.UpdateGroup;
//import com.chua.starter.monitor.entity.SoftServiceInstall;
//import com.chua.starter.monitor.pojo.SoftServiceStatusInfo;
//import com.chua.starter.monitor.service.SoftServiceInstallService;
//import io.swagger.annotations.Api;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 软件服务安装记录控制器
// *
// * @author CH
// * @since 2024/7/18
// */
//@RestController
//@RequestMapping("v1/soft/service/install")
//@Tag(name = "软件服务安装管理")
//@Api(tags = "软件服务安装管理")
//@RequiredArgsConstructor
//public class SoftServiceInstallController {
//
//    private final SoftServiceInstallService softServiceInstallService;
//
//    /**
//     * 根据ID获取安装记录
//     *
//     * @param installId 安装记录ID
//     * @return 安装记录
//     */
//    @GetMapping("/{installId}")
//    @Operation(summary = "根据ID获取安装记录")
//    public ReturnResult<SoftServiceInstall> getInstallById(@PathVariable Integer installId) {
//        return ReturnResult.success(softServiceInstallService.getInstallById(installId));
//    }
//
//    /**
//     * 根据SSH ID获取安装记录列表
//     *
//     * @param sshId SSH ID
//     * @return 安装记录列表
//     */
//    @GetMapping("/ssh/{sshId}")
//    @Operation(summary = "根据SSH ID获取安装记录列表")
//    public ReturnResult<List<SoftServiceInstall>> listInstallBySshId(@PathVariable String sshId) {
//        return ReturnResult.success(softServiceInstallService.listInstallBySshId(sshId));
//    }
//
//    /**
//     * 根据软件服务ID获取安装记录列表
//     *
//     * @param softServiceId 软件服务ID
//     * @return 安装记录列表
//     */
//    @GetMapping("/service/{softServiceId}")
//    @Operation(summary = "根据软件服务ID获取安装记录列表")
//    public ReturnResult<List<SoftServiceInstall>> listInstallBySoftServiceId(@PathVariable Integer softServiceId) {
//        return ReturnResult.success(softServiceInstallService.listInstallBySoftServiceId(softServiceId));
//    }
//
//    /**
//     * 添加安装记录
//     *
//     * @param softServiceInstall 安装记录
//     * @param result             校验结果
//     * @return 操作结果
//     */
//    @PostMapping("/add")
//    @Operation(summary = "添加安装记录")
//    public ReturnResult<Boolean> addInstall(@Validated(AddGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
//        if (result.hasErrors()) {
//            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
//        }
//        return softServiceInstallService.addInstall(softServiceInstall);
//    }
//
//    /**
//     * 更新安装记录
//     *
//     * @param softServiceInstall 安装记录
//     * @param result             校验结果
//     * @return 操作结果
//     */
//    @PutMapping("/update")
//    @Operation(summary = "更新安装记录")
//    public ReturnResult<Boolean> updateInstall(@Validated(UpdateGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
//        if (result.hasErrors()) {
//            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
//        }
//        return softServiceInstallService.updateInstall(softServiceInstall);
//    }
//
//    /**
//     * 删除安装记录
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @DeleteMapping("/delete/{installId}")
//    @Operation(summary = "删除安装记录")
//    public ReturnResult<Boolean> removeInstallById(@PathVariable Integer installId) {
//        return softServiceInstallService.removeInstallById(installId);
//    }
//
//    /**
//     * 批量安装软件服务
//     *
//     * @param softServiceId 软件服务ID
//     * @param sshIds        SSH ID列表
//     * @return 操作结果
//     */
//    @PostMapping("/batch/install")
//    @Operation(summary = "批量安装软件服务")
//    public ReturnResult<Boolean> batchInstall(@RequestParam Integer softServiceId, @RequestBody List<String> sshIds) {
//        return softServiceInstallService.batchInstall(softServiceId, sshIds);
//    }
//
//    /**
//     * 开始安装软件
//     *
//     * @param softServiceInstall 安装记录
//     * @param result             校验结果
//     * @return 操作结果
//     */
//    @PostMapping("/start")
//    @Operation(summary = "开始安装软件")
//    public ReturnResult<Boolean> startInstall(@Validated(AddGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
//        if (result.hasErrors()) {
//            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
//        }
//        return softServiceInstallService.startInstall(softServiceInstall);
//    }
//
//    /**
//     * 停止安装软件
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/stopInstall/{installId}")
//    @Operation(summary = "停止安装软件")
//    public ReturnResult<Boolean> stopInstall(@PathVariable Integer installId) {
//        return softServiceInstallService.stopInstall(installId);
//    }
//
//    /**
//     * 卸载软件
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/uninstall/{installId}")
//    @Operation(summary = "卸载软件")
//    public ReturnResult<Boolean> uninstall(@PathVariable Integer installId) {
//        return softServiceInstallService.uninstall(installId);
//    }
//
//    /**
//     * 启动服务
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/start/{installId}")
//    @Operation(summary = "启动服务")
//    public ReturnResult<Boolean> startService(@PathVariable Integer installId) {
//        return softServiceInstallService.startService(installId);
//    }
//
//    /**
//     * 停止服务
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/stop/{installId}")
//    @Operation(summary = "停止服务")
//    public ReturnResult<Boolean> stopService(@PathVariable Integer installId) {
//        return softServiceInstallService.stopService(installId);
//    }
//
//    /**
//     * 重启服务
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/restart/{installId}")
//    @Operation(summary = "重启服务")
//    public ReturnResult<Boolean> restartService(@PathVariable Integer installId) {
//        return softServiceInstallService.restartService(installId);
//    }
//
//    /**
//     * 查看服务日志
//     *
//     * @param installId 安装记录ID
//     * @param lines     要查看的行数，默认100行
//     * @return 操作结果
//     */
//    @GetMapping("/log/{installId}")
//    @Operation(summary = "查看服务日志")
//    public ReturnResult<String> viewServiceLog(@PathVariable Integer installId, @RequestParam(required = false, defaultValue = "100") Integer lines) {
//        return softServiceInstallService.viewServiceLog(installId, lines);
//    }
//
//    /**
//     * 开始实时监控服务日志
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/log/monitor/start/{installId}")
//    @Operation(summary = "开始实时监控服务日志")
//    public ReturnResult<Boolean> startMonitorServiceLog(@PathVariable Integer installId) {
//        return softServiceInstallService.startMonitorServiceLog(installId);
//    }
//
//    /**
//     * 停止实时监控服务日志
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果
//     */
//    @PostMapping("/log/monitor/stop/{installId}")
//    @Operation(summary = "停止实时监控服务日志")
//    public ReturnResult<Boolean> stopMonitorServiceLog(@PathVariable Integer installId) {
//        return softServiceInstallService.stopMonitorServiceLog(installId);
//    }
//
//    /**
//     * 检测服务状态
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果，包含服务状态信息
//     */
//    @GetMapping("/status/{installId}")
//    @Operation(summary = "检测服务状态")
//    public ReturnResult<SoftServiceStatusInfo> checkServiceStatus(@PathVariable Integer installId) {
//        return softServiceInstallService.checkServiceStatus(installId);
//    }
//
//    /**
//     * 批量检测服务状态
//     *
//     * @param installIds 安装记录ID列表
//     * @return 操作结果，包含服务状态信息列表
//     */
//    @PostMapping("/status/batch")
//    @Operation(summary = "批量检测服务状态")
//    public ReturnResult<List<SoftServiceStatusInfo>> batchCheckServiceStatus(@RequestBody List<Integer> installIds) {
//        return softServiceInstallService.batchCheckServiceStatus(installIds);
//    }
//
//    /**
//     * 获取服务配置项
//     *
//     * @param installId 安装记录ID
//     * @return 操作结果，包含服务配置项JSON字符串
//     */
//    @GetMapping("/config/{installId}")
//    @Operation(summary = "获取服务配置项")
//    public ReturnResult<String> getServiceConfig(@PathVariable Integer installId) {
//        return softServiceInstallService.getServiceConfig(installId);
//    }
//
//    /**
//     * 更新服务配置项
//     *
//     * @param installId 安装记录ID
//     * @param config    配置项JSON字符串
//     * @return 操作结果
//     */
//    @PostMapping("/config/{installId}")
//    @Operation(summary = "更新服务配置项")
//    public ReturnResult<Boolean> updateServiceConfig(@PathVariable Integer installId, @RequestBody String config) {
//        return softServiceInstallService.updateServiceConfig(installId, config);
//    }
//}