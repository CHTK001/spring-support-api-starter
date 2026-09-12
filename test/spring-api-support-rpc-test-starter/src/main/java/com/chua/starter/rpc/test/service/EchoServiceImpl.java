package com.chua.starter.rpc.test.service;

import com.chua.common.support.network.rpc.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * echo服务impl类。
 *
 * @author CH
 * @since 4.0.0
 */

@Slf4j
@Component
@RpcService(interfaceClass = EchoService.class, version = "1.0.0", timeout = 5000)
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
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
