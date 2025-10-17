package com.chua.docker.support.protocol;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.protocol.ProtocolSetting;
import com.chua.common.support.protocol.AbstractProtocol;
import com.chua.common.support.protocol.client.ProtocolClient;
import com.chua.common.support.protocol.server.ProtocolServer;
import com.chua.docker.support.client.DockerProtocolClient;

/**
 * Docker V2协议实现
 * <p>
 * 提供Docker协议的V2版本实现，支持增强的客户端和服务端功能：
 * 1. 支持异步和同步Docker API调用
 * 2. 支持监听器机制，用于监听容器事件
 * 3. 支持Filter过滤器链
 * 4. 支持ConfigureObjectContext上下文管理
 * 5. 支持Docker REST API的完整功能
 * 6. 支持容器生命周期管理
 * 7. 支持镜像管理和网络管理
 * 8. 支持Docker Swarm集群管理
 *
 * @author CH
 * @since 2024/7/8
 */
@Spi("DOCKER")
public class DockerProtocol extends AbstractProtocol {

    public DockerProtocol(ProtocolSetting protocolSetting) {
        super(protocolSetting);
    }

    @Override
    public Class<? extends ProtocolClient> getClientType() {
        return DockerProtocolClient.class;
    }

    @Override
    public Class<? extends ProtocolServer> getServerType() {
        return null;
    }

    @Override
    protected int getDefaultPort() {
        return 2376;
    }
}
