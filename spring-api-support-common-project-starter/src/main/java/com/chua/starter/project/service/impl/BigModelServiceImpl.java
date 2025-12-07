package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.deeplearning.support.ml.bigmodel.BigModelCallback;
import com.chua.deeplearning.support.ml.bigmodel.BigModelClient;
import com.chua.deeplearning.support.ml.bigmodel.BigModelRequest.HistoryMessage;
import com.chua.deeplearning.support.ml.bigmodel.BigModelResponse;
import com.chua.deeplearning.support.ml.bigmodel.BigModelSetting;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.entity.SysAiGroupMessage;
import com.chua.starter.project.service.BigModelService;
import com.chua.starter.project.service.SysAiGroupMessageService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.sse.support.Emitter;
import com.chua.starter.sse.support.SseTemplate;
import com.chua.starter.project.entity.SysAiModule;
import com.chua.starter.project.entity.SysProject;
import com.chua.starter.project.pojo.bigmodel.BigModelRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    final SysAiGroupMessageService sysAiGroupMessageService;

    @Override
    public ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request) {
        BigModelSetting bigModelSetting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        BigModelClient bigModelClient = bigModelSetting.getBigModelClient();
        return ReturnResult.of(bigModelClient.call(com.chua.deeplearning.support.ml.bigmodel.BigModelRequest.builder()
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
        BigModelSetting binModelSetting = BigModelSetting.builder()
                .host(StringUtils.defaultString(sysAiModule.getSysAiModuleUrl(), sysProject.getSysProjectEndpoint()))
                .appId(sysProject.getSysProjectAppId())
                .appSecret(sysProject.getSysProjectAppSecret())
                .appKey(sysProject.getSysProjectAppKey())
                .build();

        BigModelClient bigModelClient = ServiceProvider.of(BigModelClient.class)
                .getNewExtension(sysProject.getSysProjectDictItemCode(), binModelSetting);
        if (null == bigModelClient) {
            throw new RuntimeException("暂不支持");
        }

        List<HistoryMessage> historyMessages = new ArrayList<>();
        String requestGroupId = request.getGroupId();
        if (null != requestGroupId) {
            List<SysAiGroupMessage> list = sysAiGroupMessageService.list(
                    Wrappers.<SysAiGroupMessage>lambdaQuery()
                            .eq(SysAiGroupMessage::getSysAiGroupId, sysProjectId)
                            .eq(SysAiGroupMessage::getSysAiGroupId, requestGroupId)
                            .eq(SysAiGroupMessage::getSysAiGroupMessageRole, "user")
                            .orderByDesc(SysAiGroupMessage::getCreateTime)
            );
            historyMessages.addAll(list.stream().map(item -> HistoryMessage.builder()
                    .role(item.getSysAiGroupMessageRole())
                    .content(item.getSysAiGroupMessageContent())
                    .build()).toList());
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
                bigModelClient.callStream(com.chua.deeplearning.support.ml.bigmodel.BigModelRequest.builder()
                                .prompt(request.getUser())
                                .files(request.getFiles())
                                .system(request.getSystem())
                                .model(request.getModel())
                                .temperature(request.getTemperature())
                        .historyMessages(historyMessages)
                                .tokens(request.getTokens())
                        .seed(request.getSeed())
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
                                    ThreadUtils.sleep(1000);
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
                try {
                    sseEmitter.send(Json.toJson(BigModelResponse.builder()
                            .output("系统繁忙, 请稍后重试!")
                            .code(ReturnCode.OK.getCode())
                            .state(BigModelResponse.State.PADDING)
                            .build()
                    ), MediaType.TEXT_PLAIN);

                    sseEmitter.send(Json.toJson(BigModelResponse.builder()
                            .code(ReturnCode.OK.getCode())
                            .state(BigModelResponse.State.DONE)
                            .done(true)
                            .build()
                    ), MediaType.TEXT_PLAIN);

                } catch (IllegalStateException e1) {
                    try {
                        IoUtils.closeQuietly(response.getOutputStream());
                    } catch (IOException ignored) {
                    }
                } catch (Throwable ignored) {
                }
                try {
                    IoUtils.closeQuietly(response.getOutputStream());
                } catch (IOException ignored) {
                }
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
        GlobalSettingFactory.getInstance().register("llm", BigModelSetting.builder().build());
        this.executorService = ThreadUtils.newVirtualThreadExecutor();
    }
}
