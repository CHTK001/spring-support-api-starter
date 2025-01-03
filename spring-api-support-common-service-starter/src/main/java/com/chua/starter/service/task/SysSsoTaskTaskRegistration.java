package com.chua.starter.service.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.service.SysTaskSsoItemService;
import com.chua.starter.service.SysTaskSsoService;
import com.chua.starter.service.properties.SsoProperties;
import com.chua.starter.system.entity.SysTaskSso;
import com.chua.starter.system.entity.SysTaskSsoItem;
import com.chua.starter.system.pojo.sso.SysFileUploadRequest;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CH
 * @since 2024/12/4
 */
@SpringBootApplication
public class SysSsoTaskTaskRegistration implements Runnable, DisposableBean, InitializingBean {
    private static final ExecutorService FULL_SUB_TASK = ThreadUtils.newFixedThreadExecutor(100, "SysTaskSsoServiceImpl#合并分片子任务");
    private static final LinkedBlockingQueue<SysTaskSsoWrapper> SYS_TASK_SSO = new LinkedBlockingQueue<>();
    private static final ExecutorService FULL_TASK = ThreadUtils.newFixedThreadExecutor(1, "SysTaskSsoServiceImpl#合并分片任务");
    private static final AtomicBoolean STATUS = new AtomicBoolean(true);
    @Autowired
    private SsoProperties ssoProperties;
    @Autowired
    private SysTaskSsoItemService sysTaskSsoItemService;

    /**
     * 注册
     * @param sysTaskSsoWrapper 任务
     */
    public void register(SysTaskSsoWrapper sysTaskSsoWrapper) {
        SYS_TASK_SSO.add(sysTaskSsoWrapper);
    }


    @Override
    public void run() {
        while (STATUS.get()) {
            SysTaskSsoWrapper sysTaskSsoWrapper = SYS_TASK_SSO.poll();
            if(null != sysTaskSsoWrapper) {
                submitTask(sysTaskSsoWrapper);
            }
            ThreadUtils.sleepMillisecondsQuietly(300);
        }
    }

    /**
     * 提交任务
     * @param sysTaskSsoWrapper  sysTaskSso
     */
    private void submitTask(SysTaskSsoWrapper sysTaskSsoWrapper) {
        FULL_SUB_TASK.execute(() -> {
            submitItem(sysTaskSsoWrapper);
        });
    }

    /**
     * 提交子任务
     * @param sysTaskSsoWrapper  sysTaskSso
     */
    private void submitItem(SysTaskSsoWrapper sysTaskSsoWrapper) {
        if(null == sysTaskSsoWrapper) {
            return;
        }
        SysTaskSso sysTaskSso = sysTaskSsoWrapper.getSysTaskSso();
        SysTaskSsoService service = sysTaskSsoWrapper.getService();
        List<SysTaskSsoItem> list = sysTaskSsoItemService.list(Wrappers.<SysTaskSsoItem>lambdaQuery()
                .eq(SysTaskSsoItem::getSysTaskSsoId, sysTaskSso.getSysTaskSsoId())
                .eq(SysTaskSsoItem::getSysTaskSsoItemStatus, 2)
                .orderByAsc(SysTaskSsoItem::getSysTaskSsoItemSeq)
        );
        SysFileUploadRequest request = new SysFileUploadRequest();
        request.setFileBucket(sysTaskSso.getSysTaskBucket());
        request.setFileName(sysTaskSso.getSysTaskSsoName());
        request.setOssType(sysTaskSso.getSysTaskSsoType());
        try {
            service.upload(request, sysTaskSso.getSysTaskSsoName(), createByteArray(list));
        } catch (Exception e) {
            register(sysTaskSsoWrapper);
        }
    }

    /**
     * 创建字节数组
     * @param list
     * @return
     */
    private byte[] createByteArray(List<SysTaskSsoItem> list) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            for (SysTaskSsoItem sysTaskSsoItem : list) {
                byteArrayOutputStream.writeBytes(sysTaskSsoItem.copyByteArray());
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        STATUS.set(false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        FULL_TASK.execute(this);
    }
}
