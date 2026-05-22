package com.chua.starter.project.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.bigmodel.BigModelResponse;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.pojo.bigmodel.BigModelRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * BigModelService 存根 — 由 monitor-starter 在缺乏 spring-api-support-common-project-starter 时提供
 */
public interface BigModelService {
    ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request);
    SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response);
    ReturnResult<String[]> models();
}
