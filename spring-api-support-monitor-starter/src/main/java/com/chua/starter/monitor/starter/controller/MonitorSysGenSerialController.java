package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.starter.entity.MonitorSysGenSerial;
import com.chua.starter.monitor.starter.service.MonitorSysGenSerialService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 串口配置管理
 *
 * @author CH
 * @since 2024/7/23
 */
@RestController
@RequestMapping("v1/gen/serial")
@Tag(name = "串口配置")
@RequiredArgsConstructor
public class MonitorSysGenSerialController {

    private final MonitorSysGenSerialService monitorSysGenSerialService;

    /**
     * 根据主键删除数据
     *
     * @param id 主键
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "删除数据")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(@Parameter(name = "主键") String id) {
        if (null == id) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, "主键不能为空");
        }

        Set<String> ids = Splitter.on(",").trimResults().omitEmptyStrings().splitToSet(id);
        if (ids.isEmpty()) {
            return ReturnResult.ok();
        }
        return ReturnResult.of(monitorSysGenSerialService.removeBatchByIds(ids));
    }

    /**
     * 根据主键更新数据
     *
     * @param monitorSysGenSerial 实体
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "更新数据")
    @PutMapping("update")
    public ReturnResult<Boolean> updateById(@Validated(UpdateGroup.class) @RequestBody MonitorSysGenSerial monitorSysGenSerial, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return ReturnResult.of(monitorSysGenSerialService.updateById(monitorSysGenSerial));
    }

    /**
     * 添加数据
     *
     * @param monitorSysGenSerial 实体
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "添加数据")
    @PostMapping("save")
    public ReturnResult<MonitorSysGenSerial> save(@Validated(AddGroup.class) @RequestBody MonitorSysGenSerial monitorSysGenSerial, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        // 设置默认值
        if (null == monitorSysGenSerial.getMonitorSysGenSerialStatus()) {
            monitorSysGenSerial.setMonitorSysGenSerialStatus(0);
        }

        monitorSysGenSerialService.save(monitorSysGenSerial);
        return ReturnResult.ok(monitorSysGenSerial);
    }

    /**
     * 分页查询数据
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询数据")
    @GetMapping("page")
    public ReturnPageResult<MonitorSysGenSerial> page(Query<MonitorSysGenSerial> page, MonitorSysGenSerial entity) {
        return PageResultUtils.ok(monitorSysGenSerialService.pageFor(page.createPage(),  entity));
    }

    /**
     * 开启串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Operation(summary = "开启串口")
    @GetMapping("start")
    public ReturnResult<Boolean> start(String id) {
        return monitorSysGenSerialService.start(id);
    }

    /**
     * 关闭串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Operation(summary = "关闭串口")
    @GetMapping("stop")
    public ReturnResult<Boolean> stop(String id) {
        return monitorSysGenSerialService.stop(id);
    }

    /**
     * 测试串口连接
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Operation(summary = "测试串口连接")
    @GetMapping("test")
    public ReturnResult<Boolean> test(String id) {
        return monitorSysGenSerialService.test(id);
    }

    /**
     * 向串口发送数据
     *
     * @param id 串口配置ID
     * @param data 要发送的数据
     * @return 操作结果
     */
    @Operation(summary = "向串口发送数据")
    @PostMapping("send")
    public ReturnResult<Boolean> sendData(@Parameter(name = "串口配置ID") String id,
                                         @Parameter(name = "发送数据") @RequestBody String data) {
        return monitorSysGenSerialService.sendData(id, data);
    }

    /**
     * 获取串口状态
     *
     * @param id 串口配置ID
     * @return 串口状态信息
     */
    @Operation(summary = "获取串口状态")
    @GetMapping("status")
    public ReturnResult<Map<String, Object>> getStatus(@Parameter(name = "串口配置ID") String id) {
        return monitorSysGenSerialService.getStatus(id);
    }

    /**
     * 获取当前服务器可用的串口列表
     *
     * @return 串口列表
     */
    @Operation(summary = "获取可用串口列表")
    @GetMapping("available-ports")
    public ReturnResult<List<Map<String, Object>>> getAvailableSerialPorts() {
        return monitorSysGenSerialService.getAvailableSerialPorts();
    }
}