//package com.chua.starter.monitor.socket;
//
//import com.chua.common.support.json.Json;
//import com.chua.common.support.json.JsonArray;
//import com.chua.common.support.json.JsonObject;
//import com.chua.common.support.json.JsonReference;
//import com.chua.common.support.protocol.protocol.CommandType;
//import com.chua.common.support.protocol.request.Response;
//import com.chua.common.support.utils.ObjectUtils;
//import com.chua.common.support.utils.StringUtils;
//import com.chua.socketio.support.SocketIOListener;
//import com.chua.socketio.support.annotations.OnEvent;
//import com.chua.socketio.support.session.SocketSession;
//import com.chua.starter.monitor.request.MonitorRequest;
//import com.chua.starter.monitor.server.factory.MonitorServerFactory;
//import com.chua.starter.monitor.server.service.MonitorAppService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Optional;
//
//
///**
// * 显示器插座定位器
// *
// * @author CH
// * @version 1.0.0
// * @since 2024/02/06
// */
//@Configuration
//@RequiredArgsConstructor
//public class MonitorSocketIOListener implements SocketIOListener {
//
//    private final MonitorServerFactory monitorServerFactory;
//    private final MonitorAppService monitorAppService;
//    @OnEvent("terminal-request")
//    public void onEvent(SocketSession session, Command command) {
//        String appName = command.getAppName();
//        if(StringUtils.isBlank(appName)) {
//            session.send("terminal", "");
//            return;
//        }
//        String appModel = command.getAppModel();
//        if(StringUtils.isBlank(appModel)) {
//            session.send("terminal", "");
//            return;
//        }
//
//
//        MonitorRequest heart = monitorServerFactory.getHeart(appName, appModel);
//        if(ObjectUtils.isEmpty(heart)) {
//            return;
//        }
//
//        try {
//            Response response = monitorAppService.upload(null, heart, Json.toJSONString(command), "SHELL", CommandType.REQUEST);
//            JsonReference reference = Json.getJsonReference(response.getBody(String.class));
//            session.send("terminal", new JsonObject()
//                    .fluent("data", Optional.ofNullable(reference.isArray() ? reference.getJsonArray() : reference.getJsonObject())
//                            .orElse(JsonArray.empty())).fluent("command", command.getCommand()).toJSONString());
//        } catch (Exception ignored) {
//        }
//    }
//}
