package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenSerial;

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
} 