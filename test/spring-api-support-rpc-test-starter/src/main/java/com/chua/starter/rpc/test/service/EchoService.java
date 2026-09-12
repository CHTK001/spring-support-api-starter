package com.chua.starter.rpc.test.service;
/**
 * echo服务接口。
 *
 * @author CH
 * @since 4.0.0
 */

public interface EchoService {

    String echo(String message);

    int add(int a, int b);

    String hello(String name);
}
