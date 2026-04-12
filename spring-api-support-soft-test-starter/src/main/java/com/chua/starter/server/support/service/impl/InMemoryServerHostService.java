package com.chua.starter.server.support.service.impl;

import com.chua.starter.server.support.entity.ServerHost;
import com.chua.starter.server.support.service.ServerHostService;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InMemoryServerHostService implements ServerHostService {

    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ServerHost saveHost(ServerHost host) {
        if (host == null) {
            return null;
        }
        if (host.getServerId() == null) {
            host.setServerId(sequence.getAndIncrement());
        }
        if (!StringUtils.hasText(host.getServerCode())) {
            host.setServerCode("server-" + host.getServerId());
        }
        return host;
    }
}
