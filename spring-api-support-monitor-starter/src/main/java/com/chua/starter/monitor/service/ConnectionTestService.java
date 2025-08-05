package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.ConnectionTestResultDTO;

/**
 * 连接测试服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface ConnectionTestService {

    /**
     * 测试HTTP连接
     *
     * @param url HTTP地址
     * @return 测试结果
     */
    ReturnResult<ConnectionTestResultDTO> testHttpConnection(String url);

    /**
     * 测试Socket连接
     *
     * @param host 主机地址
     * @param port 端口
     * @return 测试结果
     */
    ReturnResult<ConnectionTestResultDTO> testSocketConnection(String host, int port);

    /**
     * 测试Prometheus服务器连接
     *
     * @param prometheusUrl Prometheus服务器地址
     * @return 测试结果
     */
    ReturnResult<ConnectionTestResultDTO> testPrometheusConnection(String prometheusUrl);

    /**
     * 通用连接测试
     *
     * @param address 地址 (支持http://、https://、socket://格式)
     * @return 测试结果
     */
    ReturnResult<ConnectionTestResultDTO> testConnection(String address);
}
