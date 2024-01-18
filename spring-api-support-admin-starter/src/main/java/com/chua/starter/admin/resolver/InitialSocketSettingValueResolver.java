package com.chua.starter.admin.resolver;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.net.NetUtils;
import com.chua.socketio.support.properties.SocketIoProperties;
import com.chua.starter.admin.entity.SysSetting;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.List;

/**
 * 值解析器
 * @author Administrator
 * @date 2024/01/18
 */
@Spi("initial_socket")
public class InitialSocketSettingValueResolver implements SettingValueResolver{

    @Resource
    private Environment environment;

    @Resource
    private SocketIoProperties socketIoProperties;

    @Override
    public void resolve(List<SysSetting> result) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSettingName("initial_socket_ports");
        String host = socketIoProperties.getHost();
        if(!NetUtils.isLocalHost(host)) {
            sysSetting.setSettingValue(host + ":" + socketIoProperties.getPort() + "");
        } else {
            sysSetting.setSettingValue(socketIoProperties.getPort() + "");
        }
        result.add(sysSetting);
    }
}
