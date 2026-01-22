package com.chua.starter.monitor.controller.alert;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenMessagePushTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertLevel;
import com.chua.starter.monitor.enums.AlertStatus;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.monitor.service.message.MonitorSysGenMessagePushTemplateService;
import com.chua.starter.monitor.spi.MessagePushSender;
import com.chua.common.support.core.spi.ServiceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/v1/gen/server/alert/push-template-test")
@Tag(name = "消息推送模板测试")
@RequiredArgsConstructor
public class AlertPushTemplateTestController {

    private final MonitorSysGenMessagePushTemplateService templateService;

    @PostMapping("/send/{templateId}")
    @Operation(summary = "用模板测试发送一条告警")
    public ReturnResult<Boolean> testSend(
            @Parameter(description = "模板ID") @PathVariable Integer templateId,
            @RequestParam(required = false, defaultValue = "CPU") String type,
            @RequestParam(required = false, defaultValue = "WARNING") String level) {
        try {
            var rr = templateService.getEnabledById(templateId);
            if (!rr.isOk() || rr.getData() == null || rr.getData().getMonitorSysGenMessagePushTemplateChannel() == null) {
                return ReturnResult.error("模板不存在或未启用");
            }
            String channel = rr.getData().getMonitorSysGenMessagePushTemplateChannel().getCode();
            MessagePushSender sender = ServiceProvider.of(MessagePushSender.class).getNewExtension(channel);
            if (sender == null) {
                return ReturnResult.error("未找到SPI实现: " + channel);
            }
            // 构造一条示例告警
            MonitorSysGenServerAlertHistory alert = new MonitorSysGenServerAlertHistory();
            alert.setMonitorSysGenServerAlertHistoryServerId(0);
            alert.setMonitorSysGenServerAlertHistoryType(AlertType.fromCode(type));
            alert.setMonitorSysGenServerAlertHistoryLevel(AlertLevel.valueOf(level));
            alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
            alert.setMonitorSysGenServerAlertHistoryTitle("[测试] 模板发送验证");
            alert.setMonitorSysGenServerAlertHistoryContent("这是一条测试告警，仅用于验证通道配置是否正确");
            alert.setMonitorSysGenServerAlertHistoryCurrentValue(new BigDecimal("90"));
            alert.setMonitorSysGenServerAlertHistoryThresholdValue(new BigDecimal("80"));
            alert.setMonitorSysGenServerAlertHistoryUnit("%");

            boolean ok = sender.send(rr.getData(), alert);
            return ok ? ReturnResult.ok(true) : ReturnResult.error("发送失败");
        } catch (Exception e) {
            log.error("模板测试发送失败", e);
            return ReturnResult.error("模板测试发送失败: " + e.getMessage());
        }
    }
}

