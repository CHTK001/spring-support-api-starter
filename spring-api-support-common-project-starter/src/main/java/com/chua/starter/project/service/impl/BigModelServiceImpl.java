package com.chua.starter.project.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.service.BigModelService;
import com.chua.starter.project.service.SysAiGroupMessageService;
import com.chua.starter.project.service.SysAiModuleService;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.sse.support.SseTemplate;
import com.chua.starter.project.pojo.bigmodel.BigModelRequest;
import com.chua.starter.project.pojo.bigmodel.BigModelResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 大模型服务
 * 注意: BigModel 功能需要 utils-support-deeplearning-starter 模块
 *
 * @author CH
 * @since 2024/9/10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BigModelServiceImpl implements BigModelService, InitializingBean {

    private final SseTemplate sseTemplate;
    final SysProjectService sysProjectService;
    final SysAiModuleService sysAiModuleService;
    final SysAiGroupMessageService sysAiGroupMessageService;

    @Override
    public ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request) {
        log.warn("BigModel 功能需要 utils-support-deeplearning-starter 模块");
        return ReturnResult.error("BigModel 功能需要 utils-support-deeplearning-starter 模块");
    }

    @Override
    public SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response) {
        throw new RuntimeException("BigModel 功能需要 utils-support-deeplearning-starter 模块");
    }

    @Override
    public ReturnResult<String[]> models() {
        log.warn("BigModel 功能需要 utils-support-deeplearning-starter 模块");
        return ReturnResult.of(new String[0]);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.warn("BigModel 功能需要 utils-support-deeplearning-starter 模块");
    }
}
