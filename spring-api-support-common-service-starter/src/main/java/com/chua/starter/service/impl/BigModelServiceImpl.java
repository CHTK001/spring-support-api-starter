package com.chua.starter.service.impl;

import com.chua.common.support.bigmodel.BigModelCallback;
import com.chua.common.support.bigmodel.BigModelClient;
import com.chua.common.support.bigmodel.BigModelResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.BigModelService;
import com.chua.starter.service.setting.BigModelSetting;
import com.chua.starter.sse.support.Emitter;
import com.chua.starter.sse.support.SseTemplate;
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
 * @author CH
 * @since 2024/9/10
 */
@Service
@RequiredArgsConstructor
public class BigModelServiceImpl implements BigModelService, InitializingBean {


    private ExecutorService executorService;
    private final SseTemplate sseTemplate;

    @Override
    public ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request) {
        BigModelSetting bigModelSetting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        BigModelClient bigModelClient = bigModelSetting.getBigModelClient();
        return ReturnResult.of(bigModelClient.call(com.chua.common.support.bigmodel.BigModelRequest .builder()
                .prompt(request.getUser())
                .system(request.getSystem())
                .model(request.getModel())
                .build()
        ));
    }

    @Override
    public SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response) {
        if(StringUtils.isBlank(request.getRequestId())) {
            throw new RuntimeException("requestId不能为空");
        }

        if(StringUtils.isBlank(request.getModel())) {
            throw new RuntimeException("模型不能为空");
        }

        if(StringUtils.isBlank(request.getUser())) {
            throw new RuntimeException("问题不能为空");
        }

        BigModelSetting bigModelSetting = GlobalSettingFactory.getInstance().get("llm", BigModelSetting.class);
        BigModelClient bigModelClient = bigModelSetting.getBigModelClient();
        String event = "bigModel_" + request.getRequestId();
        String clientId = request.getRequestId();
        Emitter emitter = Emitter.builder()
                .event(event)
                .clientId(clientId)
                .entity(response)
                .build();
        SseEmitter sseEmitter = sseTemplate.createSseEmitter(emitter);
        executorService.execute(() -> {
            bigModelClient.callStream(com.chua.common.support.bigmodel.BigModelRequest.builder()
                            .prompt(request.getUser())
                            .system(request.getSystem())
                            .model(request.getModel())
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
                            } catch (IOException ignored) {
                            }
                        }
                    }
            );
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
