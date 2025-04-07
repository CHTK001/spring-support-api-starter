package com.chua.starter.monitor.socketio;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.service.MaintenanceHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;


/**
 * 显示器插座定位器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/06
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class MaintenanceSocketIOListener implements SocketIOListener {

    final MaintenanceHostService maintenanceHostService;
    @OnEvent("maintenance")
    public void onEvent(SocketSession session, JsonObject command) {
        String genId = command.getString("genId");
        if(StringUtils.isEmpty(genId)) {
            session.send("maintenance-error", JsonObject.create().fluentPut("message", "当前请求不合法").toJSONString());
            return;
        }

        SshClient sshClient = null;
        try {
            sshClient = maintenanceHostService.getSshClient(genId);
        } catch (Exception e) {
            session.send("maintenance-" + genId,  ReturnResult.illegal(e.getMessage()));
            return;
        }
        if(null == sshClient) {
            session.send("maintenance-" + genId,
                    ReturnResult.illegal("当前请求不合法"));
            return;
        }

        String command1 = command.getString("command");

        ChannelSession clientSession = (ChannelSession) sshClient.getSession();
        Channel channel = clientSession.openChannel(genId, "terminal");
//        clientSession.closeChannel(requestId);
        channel.setListener(s -> {
            session.send("maintenance-" + genId,
                    ReturnResult.success(s));
        });

        if("connect".equalsIgnoreCase(command1)) {
            try {
                channel.execute("cd /", 1000);
            } catch (Exception ignored) {
            }
            return;
        }
        try {
            channel.execute(command1, 1000);
        } catch (Exception ignored) {
        }
    }
}
