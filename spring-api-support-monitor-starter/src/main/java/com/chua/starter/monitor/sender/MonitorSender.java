package com.chua.starter.monitor.sender;

import com.chua.common.support.constant.CommonConstant;
import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.protocol.ProtocolSetting;
import com.chua.common.support.protocol.client.BaseProtocolClient;
import com.chua.common.support.protocol.client.ProtocolClient;
import com.chua.common.support.protocol.client.ProtocolClientFactory;
import com.chua.common.support.protocol.request.HttpServletRequest;
import com.chua.common.support.protocol.request.ServletResponse;
import com.chua.common.support.utils.StringUtils;
import com.chua.report.client.starter.endpoint.ModuleType;
import com.chua.report.client.starter.environment.ReportDiscoveryEnvironment;
import com.chua.report.client.starter.setting.SettingFactory;
import com.chua.starter.monitor.entity.MonitorJobLog;
import org.springframework.boot.SpringBootConfiguration;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.chua.common.support.utils.MathUtils.ZERO;

/**
 * 监控发送
 *
 * @author CH
 * @since 2025/8/13 14:03
 */
@SpringBootConfiguration
public class MonitorSender {

    /**
     * 发送监控日志信息
     *
     * @param jobLog         任务日志对象，包含具体的任务执行信息
     * @param discovery 发现服务的实例，用于获取应用相关信息
     * @param jsonString     日志信息的JSON字符串格式
     * @param moduleType     模块类型，标识当前发送日志所属的功能模块
     * @author CH
     * @since 2025/8/13 14:03
     */
    public void send(MonitorJobLog jobLog, Discovery discovery, String jsonString, ModuleType moduleType) {
        String s = discovery.getMetadata().get(ReportDiscoveryEnvironment.REPORT_CLIENT_PORT);
        if(StringUtils.isEmpty(s)) {
            jobLog.setJobLogTriggerMsg("未获取到服务端口");
            jobLog.setJobLogTriggerCode("FAILURE");
            jobLog.setJobLogExecuteCode("FAILURE");
            jobLog.setJobLogCost(ZERO);
            jobLog.setJobLogExecuteCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            return;
        }

        int port = Integer.parseInt(s);
        ProtocolSetting protocolSetting = ProtocolSetting.builder()
                .host(discovery.getHost())
                .port(port)
                .codec(SettingFactory.getInstance().getCodec())
                .build();
        BaseProtocolClient baseProtocolClient = ProtocolClientFactory.create(discovery.getServerId(), "http", protocolSetting.getClientSetting());
        long startTime = System.currentTimeMillis();
        try {
            ProtocolClient protocolClient = (ProtocolClient) baseProtocolClient;
            CompletableFuture<ServletResponse> servletResponseCompletableFuture = protocolClient.sendAsync(
                    HttpServletRequest.builder()
                            .url(StringUtils.startWithAppend(moduleType.name().toLowerCase(), CommonConstant.SYMBOL_LEFT_SLASH))
                            .body(jsonString.getBytes(StandardCharsets.UTF_8))
                            .build()
            );
            ServletResponse servletResponse = servletResponseCompletableFuture.get(30, TimeUnit.SECONDS);
            jobLog.setJobLogExecuteCode(servletResponse.getStatusCode() == 200 ? "SUCCESS" : "FAILURE");
            jobLog.setJobLogTriggerMsg(servletResponse.getBodyAsString());
            jobLog.setJobLogTriggerCode(servletResponse.getStatusCode() == 200 ? ReturnCode.SUCCESS.getCode() : ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            jobLog.setJobLogCost(new BigDecimal(System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
            jobLog.setJobLogExecuteCode("FAILURE");
            jobLog.setJobLogCost(ZERO);
            jobLog.setJobLogExecuteCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
        }
        jobLog.setJobLogCost(new BigDecimal(System.currentTimeMillis() - startTime));
    }
}
