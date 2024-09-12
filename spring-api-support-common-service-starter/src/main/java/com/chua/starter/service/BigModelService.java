package com.chua.starter.service;

import com.chua.common.support.bigmodel.BigModelResponse;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.bigmodel.BigModelRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 大模型服务
 * @author CH
 * @since 2024/9/10
 */
public interface BigModelService {
    /**
     * 调用大模型
     * @param query      查询
     * @param request    请求
     * @return 结果
     */
    ReturnResult<BigModelResponse> call(Query<?> query, BigModelRequest request);

    /**
     * 调用大模型
     *
     * @param query    查询
     * @param request  请求
     * @param response
     * @return 结果
     */
    SseEmitter callStream(Query<?> query, BigModelRequest request, HttpServletResponse response);

    /**
     * 获取模型
     * @return 模型
     */
    ReturnResult<String[]> models();
}
