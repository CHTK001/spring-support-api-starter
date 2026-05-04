package com.chua.starter.monitor.configuration;

import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.bigmodel.BigModelResponse;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.pojo.bigmodel.BigModelRequest;
import com.chua.starter.project.service.BigModelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * BigModelService 降级配置
 * <p>当 BigModelService 因 ProjectConfiguration 排除而未注册时，
 * 提供一个返回错误提示的无操作实现。</p>
 *
 * @author CH
 * @since 2024/12/05
 */
@Slf4j
@Configuration
@ConditionalOnClass(BigModelService.class)
@ConditionalOnMissingBean(BigModelService.class)
public class BigModelServiceFallbackConfiguration {

    @Bean
    public BigModelService noopBigModelService() {
        log.warn("[BigModelFallback] BigModelService 不可用 - AI 分析功能将返回不可用提示");
        return new BigModelService() {
            @Override
            public ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request) {
                return ReturnResult.error(ReturnCode.OTHER, "AI 服务未配置");
            }

            @Override
            public SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response) {
                SseEmitter emitter = new SseEmitter(0L);
                try {
                    emitter.send(SseEmitter.event().data("AI 服务未配置"));
                    emitter.complete();
                } catch (Exception ignored) {
                }
                return emitter;
            }

            @Override
            public ReturnResult<String[]> models() {
                return ReturnResult.error(ReturnCode.OTHER, "AI 服务未配置");
            }
        };
    }
}
