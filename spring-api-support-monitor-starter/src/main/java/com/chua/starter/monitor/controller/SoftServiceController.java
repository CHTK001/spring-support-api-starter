package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 软件服务安装记录控制器
 *
 * @author CH
 * @since 2024/7/18
 */
@RestController
@RequestMapping("v1/soft/service")
@Tag(name = "软件服务管理")
@Api(tags = "软件服务管理")
@RequiredArgsConstructor
public class SoftServiceController {

    private final SoftServiceService softServiceService;
    private final SoftServiceInstallService softServiceInstallService;

    /**
     * 列表软件服务
     *
     * @param query       查询
     * @param softService 软件服务
     * @return {@link ReturnPageResult}<{@link SoftService}>
     */
    @GetMapping("page")
    @Operation(summary = "列表软件服务")
    public ReturnPageResult<SoftService> listSoftService(@ParameterObject Query<SoftService> query, @ParameterObject SoftService softService) {
        return softServiceService.pageForWithoutCommands(query, softService);
    }

    /**
     * 保存软件服务
     *
     * @param softService 软件服务
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "保存软件服务")
    @PostMapping("save")
    public ReturnResult<Boolean> saveSoftService(@RequestBody SoftService softService) {
        return ReturnResult.ok(softServiceService.save(softService));
    }

    /**
     * 更新软件服务
     *
     * @param softService 软件服务
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "更新软件服务")
    @PutMapping("update")
    public ReturnResult<Boolean> updateSoftService(@RequestBody SoftService softService) {
        return ReturnResult.ok(softServiceService.updateById(softService));
    }

    /**
     * 删除软件服务
     *
     * @param id id
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "删除软件服务")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> deleteSoftService(Integer id) {
        return ReturnResult.ok(softServiceService.removeById(id));
    }

    /**
     * 收藏服务
     *
     * @param softServiceId 软件服务ID
     * @return 操作结果
     */
    @ApiOperation("收藏服务")
    @PostMapping("/favorite/{softServiceId}")
    public ReturnResult<Boolean> favoriteService(@PathVariable("softServiceId") Integer softServiceId) {
        return softServiceService.favoriteService(softServiceId);
    }

    /**
     * 按操作系统查询软件服务
     *
     * @param query 查询
     * @param os    操作系统
     * @return {@link ReturnPageResult}<{@link SoftService}>
     */
    @GetMapping("os/{os}")
    @Operation(summary = "按操作系统查询软件服务")
    public ReturnPageResult<SoftService> listSoftServiceByOs(@ParameterObject Query<SoftService> query, @PathVariable("os") String os) {
        SoftService softService = new SoftService();
        softService.setSoftServiceOs(os);
        return softServiceService.pageForWithoutCommands(query, softService);
    }

    /**
     * 获取所有支持的操作系统
     *
     * @return {@link ReturnResult}<{@link List}<{@link String}>>
     */
    @GetMapping("os/list")
    @Operation(summary = "获取所有支持的操作系统")
    public ReturnResult<List<String>> listAllOs() {
        return ReturnResult.ok(softServiceService.listAllOs());
    }

    /**
     * 检测命令类型
     *
     * @param command 命令内容
     * @return 命令类型信息
     */
    @Operation(summary = "检测命令类型")
    @PostMapping("detect-command-type")
    public ReturnResult<Map<String, Object>> detectCommandType(@RequestBody String command) {
        return softServiceService.detectCommandType(command);
    }

    /**
     * 更新服务状态检查命令
     *
     * @param installId   安装记录ID
     * @param command     状态检查命令
     * @param commandType 命令类型（0:单行命令，1:脚本）
     * @param successFlag 成功标识
     * @return 结果
     */
    @PutMapping("/updateStatusCheckCommand")
    @ApiOperation(value = "更新服务状态检查命令")
    @Operation(summary = "更新服务状态检查命令")
    public ReturnResult<Boolean> updateStatusCheckCommand(
            @RequestParam("installId") Integer installId,
            @RequestParam("command") String command,
            @RequestParam(value = "commandType", required = false, defaultValue = "0") Integer commandType,
            @RequestParam(value = "successFlag", required = false) String successFlag) {
        return softServiceInstallService.updateStatusCheckCommand(installId, command, commandType, successFlag);
    }

    /**
     * 根据ID获取软件服务详情
     *
     * @param softServiceId 软件服务ID
     * @return {@link ReturnResult}<{@link SoftService}>
     */
    @GetMapping("get")
    @Operation(summary = "根据ID获取软件服务详情")
    public ReturnResult<SoftService> getSoftServiceDetail(@RequestParam("softServiceId") Integer softServiceId) {
        return ReturnResult.ok(softServiceService.getById(softServiceId));
    }
}
