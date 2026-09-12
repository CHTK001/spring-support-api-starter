package com.chua.starter.rpc.test.service;

import com.chua.common.support.network.rpc.RpcResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * echo服务客户端类。
 *
 * @author CH
 * @since 4.0.0
 * @param name 名称
 * @return callHello的结果
 * @param message 消息
 */

@Slf4j
@Component
public class EchoServiceClient {

    @RpcResource(interfaceClass = EchoService.class, version = "1.0.0", timeout = 5000)
    private EchoService echoService; // echo服务

    public String callEcho(String message) {
        return echoService.echo(message);
    /**
     * call添加。
     * @param a a
     * @param b b
     * @return call添加的结果
     */
    }

    public int callAdd(int a, int b) {
        return echoService.add(a, b);
    }

    public String callHello(String name) {
        return echoService.hello(name);
    }
}
