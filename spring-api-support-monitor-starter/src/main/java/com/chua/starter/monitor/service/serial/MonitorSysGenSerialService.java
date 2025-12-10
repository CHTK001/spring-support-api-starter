package com.chua.starter.monitor.service.serial;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenSerial;

import java.util.List;
import java.util.Map;

/**
 * 串口配置服务接口
 *
 * @author CH
 * @since 2024/7/23
 */
public interface MonitorSysGenSerialService extends IService<MonitorSysGenSerial> {

    /**
     * 开启串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> start(String id);

    /**
     * 关闭串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> stop(String id);

    /**
     * 测试串口连接
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> test(String id);

    /**
     * 向串口发送数据
     *
     * @param id 串口配置ID
     * @param data 要发送的数据
     * @return 操作结果
     */
    ReturnResult<Boolean> sendData(String id, String data);

    /**
     * 获取串口状态
     *
     * @param id 串口配置ID
     * @return 串口状态信息
     */
    ReturnResult<Map<String, Object>> getStatus(String id);

    /**
     * 获取当前服务器可用的串口列表
     *
     * @return 串口列表
     */
    ReturnResult<List<Map<String, Object>>> getAvailableSerialPorts();

    /**
     * 分页查询数据
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenSerial> pageFor(Page<MonitorSysGenSerial> page, MonitorSysGenSerial entity);
}