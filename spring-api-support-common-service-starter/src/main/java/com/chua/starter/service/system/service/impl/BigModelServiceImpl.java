package com.chua.starter.service.system.service.impl;

import com.chua.common.support.bigmodel.BigModelCallback;
import com.chua.common.support.bigmodel.BigModelClient;
import com.chua.common.support.bigmodel.BigModelResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.setting.BigModelSetting;
import com.chua.starter.service.system.service.BigModelService;
import com.chua.starter.service.system.service.SysAiModuleService;
import com.chua.starter.service.system.service.SysProjectService;
import com.chua.starter.sse.support.Emitter;
import com.chua.starter.sse.support.SseTemplate;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.bigmodel.BigModelRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * 大模型服务
 *
 * @author CH
 * @since 2024/9/10
 */
@Service
@RequiredArgsConstructor
public class BigModelServiceImpl implements BigModelService, InitializingBean {


    private ExecutorService executorService;
    private final SseTemplate sseTemplate;
    final SysProjectService sysProjectService;
    final SysAiModuleService sysAiModuleService;

    @Override
    public ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request) {
        BigModelSetting bigModelSetting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        BigModelClient bigModelClient = bigModelSetting.getBigModelClient();
        return ReturnResult.of(bigModelClient.call(com.chua.common.support.bigmodel.BigModelRequest.builder()
                .prompt(request.getUser())
                .system(request.getSystem())
                .model(request.getModel())
                .build()
        ));
    }

    @Override
    public SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(request.getRequestId())) {
            throw new RuntimeException("requestId不能为空");
        }

        if (StringUtils.isBlank(request.getModel())) {
            throw new RuntimeException("模型不能为空");
        }

        if (StringUtils.isBlank(request.getUser())) {
            throw new RuntimeException("问题不能为空");
        }

        Integer sysProjectId = Integer.valueOf(request.getRequestId());
        SysAiModule sysAiModule = sysAiModuleService.getOneByCode(sysProjectId, request.getModel());
        if (null == sysAiModule) {
            throw new RuntimeException("模型不存在");
        }

        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (null == sysProject) {
            throw new RuntimeException("项目不存在");
        }
        BigModelClient bigModelClient = ServiceProvider.of(BigModelClient.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(),
                        com.chua.common.support.bigmodel.BigModelSetting.builder()
                                .host(sysProject.getSysProjectEndpoint())
                                .appId(sysProject.getSysProjectAppId())
                                .appSecret(sysProject.getSysProjectAppSecret())
                                .appKey(sysProject.getSysProjectAppKey())
                                .build()
                );
        if (null == bigModelClient) {
            throw new RuntimeException("暂不支持");
        }
        String event = "bigModel_" + request.getRequestId();
        String clientId = request.getRequestId();
        Emitter emitter = Emitter.builder()
                .event(event)
                .clientId(clientId)
                .entity(response)
                .build();
        SseEmitter sseEmitter = sseTemplate.createSseEmitter(emitter);
        executorService.execute(() -> {
            try {
                bigModelClient.callStream(com.chua.common.support.bigmodel.BigModelRequest.builder()
                                .prompt(request.getUser())
                                .files(request.getFiles())
                                .system(request.getSystem())
                                .model(request.getModel())
                                .temperature(request.getTemperature())
                                .tokens(request.getTokens())
                                .version(sysAiModule.getSysAiModuleVersion())
                                .topK(request.getTopK())
                                .build(), new BigModelCallback() {
                            @Override
                            public void exception(Throwable throwable) {
                                try {
                                    IoUtils.closeQuietly(response.getOutputStream());
                                } catch (IOException ignored) {
                                }
                            }

                            @Override
                            public void onComplete() {
                                try {
                                    IoUtils.closeQuietly(response.getOutputStream());
                                } catch (IOException ignored) {
                                }
                            }

                            @Override
                            public void accept(BigModelResponse bigModelResponse) {
                                try {
                                    sseEmitter.send(Json.toJson(bigModelResponse), MediaType.TEXT_PLAIN);
                                } catch (IllegalStateException e) {
                                    try {
                                        IoUtils.closeQuietly(response.getOutputStream());
                                    } catch (IOException ignored) {
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                );
            } catch (Exception e) {
                throw e;
            }
        });
        return sseEmitter;
    }

    @Override
    public ReturnResult<String[]> models() {
        BigModelSetting bigModelSetting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        BigModelClient bigModelClient = bigModelSetting.getBigModelClient();
        return ReturnResult.of(bigModelClient.modelList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executorService = ThreadUtils.newVirtualThreadExecutor();
    }
}
