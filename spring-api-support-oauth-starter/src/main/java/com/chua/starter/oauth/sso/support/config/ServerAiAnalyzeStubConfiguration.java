package com.chua.starter.oauth.sso.support.config;

import com.chua.starter.ai.support.model.ServerAlertHistoryAnalyzeRequest;
import com.chua.starter.ai.support.model.ServerAiStructuredAdvice;
import com.chua.starter.ai.support.model.ServerHostStabilityAnalyzeRequest;
import com.chua.starter.ai.support.model.ServerMetricHistoryAnalyzeRequest;
import com.chua.starter.ai.support.model.ServerProcessAnalyzeRequest;
import com.chua.starter.ai.support.service.ServerAiAnalyzeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 专用的 Server AI 分析 stub，用于解决 spring-support-server-starter 的硬依赖。
 */
@Configuration
public class ServerAiAnalyzeStubConfiguration {

    @Bean
    public ServerAiAnalyzeService serverAiAnalyzeService() {
        return new ServerAiAnalyzeService() {
            @Override
            public ServerAiStructuredAdvice analyzeHostStability(ServerHostStabilityAnalyzeRequest request) {
                return new ServerAiStructuredAdvice();
            }

            @Override
            public ServerAiStructuredAdvice analyzeMetricHistory(ServerMetricHistoryAnalyzeRequest request) {
                return new ServerAiStructuredAdvice();
            }

            @Override
            public ServerAiStructuredAdvice analyzeAlertHistory(ServerAlertHistoryAnalyzeRequest request) {
                return new ServerAiStructuredAdvice();
            }

            @Override
            public ServerAiStructuredAdvice analyzeProcess(ServerProcessAnalyzeRequest request) {
                return new ServerAiStructuredAdvice();
            }
        };
    }
}
