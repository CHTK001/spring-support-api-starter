package com.chua.starter.monitor.starter.controller;

import com.chua.starter.monitor.starter.entity.MonitorNginxHttpServer;
import com.chua.starter.monitor.starter.service.MonitorNginxHttpServerService;
import com.chua.starter.mybatis.controller.AbstractSwaggerController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * nginx配置http -> Server接口
 * @author CH
 * @since 2024/12/29
 */
@RestController
@RequestMapping("v1/nginx/config/http/server")
@Tag(name = "nginx配置server接口")
@RequiredArgsConstructor
public class NginxConfigHttpServerController extends AbstractSwaggerController<MonitorNginxHttpServerService, MonitorNginxHttpServer> {

    final MonitorNginxHttpServerService monitorNginxHttpServerService;
    @Override
    public MonitorNginxHttpServerService getService() {
        return monitorNginxHttpServerService;
    }
}
