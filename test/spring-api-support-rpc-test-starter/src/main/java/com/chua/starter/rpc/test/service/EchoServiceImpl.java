package com.chua.starter.rpc.test.service;

import com.chua.common.support.network.rpc.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RpcService(interfaceClass = EchoService.class, version = "1.0.0", timeout = 5000)
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String message) {
        log.info("[EchoService] received: {}", message);
        return "Echo: " + message;
    }

    @Override
    public int add(int a, int b) {
        log.info("[EchoService] add({},{})", a, b);
        return a + b;
    }

    @Override
    public String hello(String name) {
        log.info("[EchoService] hello: {}", name);
        return "Hello, " + name + " !";
    }
}
