package com.chua.starter.system.service;

import com.chua.common.support.bigmodel.generations.video.VideoGenerationsSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1SearchTaskRequest;

/**
 * 文生视频
 *
 * @author CH
 * @since 2024/9/10
 */
public interface VideoGenerationService {

    /**
     * 提交任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<VincentDiagramV1AddTaskResponse> addTask(VincentDiagramV1AddTaskRequest request);

    /**
     * 查询任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<VideoGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request);
}
