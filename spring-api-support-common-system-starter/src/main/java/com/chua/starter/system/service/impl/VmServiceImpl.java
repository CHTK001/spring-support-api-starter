package com.chua.starter.system.service.impl;

import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.system.service.VmService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * 虚拟机接�?
 * @author CH
 * @since 2024/9/11
 */
@Service
public class VmServiceImpl implements VmService, InitializingBean {
    private ExecutorService executorService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executorService = ThreadUtils.newVirtualThreadExecutor();
    }
}
