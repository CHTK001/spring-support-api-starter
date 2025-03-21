package com.chua.starter.system.service;

import com.chua.common.support.bigmodel.resolution.ImageResolutionAddTaskV1Response;
import com.chua.common.support.bigmodel.resolution.ImageResolutionSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.vincent.ImageResolutionAddTaskV2Request;
import com.chua.starter.system.pojo.vincent.ImageResolutionSearchTaskV2Request;

/**
 * 文生图
 *
 * @author CH
 * @since 2024/9/10
 */
public interface ImageResolutionService {

    /**
     * 提交任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<ImageResolutionAddTaskV1Response> addTask(ImageResolutionAddTaskV2Request request);

    /**
     * 查询任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<ImageResolutionSearchTaskV1Response> searchTask(ImageResolutionSearchTaskV2Request request);
}
