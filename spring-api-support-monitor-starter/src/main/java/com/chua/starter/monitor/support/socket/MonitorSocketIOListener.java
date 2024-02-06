package com.chua.starter.monitor.support.socket;

import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.protocol.boot.BootResponse;
import com.chua.common.support.protocol.boot.CommandType;
import com.chua.common.support.protocol.boot.ModuleType;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import com.chua.starter.monitor.request.MonitorRequest;
import com.chua.starter.monitor.server.factory.MonitorServerFactory;
import com.chua.starter.monitor.server.service.MonitorAppService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * 显示器插座定位器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/06
 */
@Configuration
public class MonitorSocketIOListener implements SocketIOListener {

    @Resource
    private MonitorServerFactory monitorServerFactory;
    @Resource
    private MonitorAppService monitorAppService;
    @OnEvent("terminal-request")
    public void onEvent(SocketSession session, Command command) {
        String appName = command.getAppName();
        if(StringUtils.isBlank(appName)) {
            session.send("terminal", "");
            return;
        }
        String appModel = command.getAppModel();
        if(StringUtils.isBlank(appModel)) {
            session.send("terminal", "");
            return;
        }


        MonitorRequest heart = monitorServerFactory.getHeart(appName, appModel);
        if(ObjectUtils.isEmpty(heart)) {
            return;
        }

        try {
            BootResponse bootResponse = monitorAppService.upload(null, heart, Json.toJSONString(command), ModuleType.SHELL, CommandType.REQUEST);
            session.send("terminal", new JsonObject().fluent("data", bootResponse.getContent()).fluent("command", command.getCommand()).toJSONString());
        } catch (Exception ignored) {
        }
    }
}
