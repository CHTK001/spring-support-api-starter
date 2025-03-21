package com.chua.starter.system.service;

import com.chua.common.support.bigmodel.colorization.ImageColorizationAddTaskV1Response;
import com.chua.common.support.bigmodel.colorization.ImageColorizationSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.vincent.ImageColorizationAddTaskV2Request;
import com.chua.starter.system.pojo.vincent.ImageColorizationSearchTaskV2Request;

/**
 * 上色
 *
 * @author CH
 * @since 2024/9/10
 */
public interface ImageColorizationService {

    /**
     * 提交任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<ImageColorizationAddTaskV1Response> addTask(ImageColorizationAddTaskV2Request request);

    /**
     * 查询任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<ImageColorizationSearchTaskV1Response> searchTask(ImageColorizationSearchTaskV2Request request);
}
