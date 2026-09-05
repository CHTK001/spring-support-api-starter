package com.chua.starter.rpc.test.service;

import com.chua.common.support.network.rpc.RpcResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EchoServiceClient {

    @RpcResource(interfaceClass = EchoService.class, version = "1.0.0", timeout = 5000)
    private EchoService echoService;

    public String callEcho(String message) {
        return echoService.echo(message);
    }

    public int callAdd(int a, int b) {
        return echoService.add(a, b);
    }

    public String callHello(String name) {
        return echoService.hello(name);
    }
}
