package com.chua.starter.system.service;

import com.chua.common.support.bigmodel.generations.image.ImageGenerationsSearchTaskV1Response;
import com.chua.common.support.bigmodel.generations.video.VideoGenerationsSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysAiVincentTask;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.system.pojo.vincent.VincentDiagramV1SearchTaskRequest;

import java.util.List;

/**
 * 文生图
 *
 * @author CH
 * @since 2024/9/10
 */
public interface VincentDiagramService {

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
    ReturnResult<ImageGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request);

    /**
     * 历史
     *
     * @param userId 用户ID
     * @return 结果
     */
    List<SysAiVincentTask> history(Integer userId);

    /**
     * 提交任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<VincentDiagramV1AddTaskResponse> addVideoTask(VincentDiagramV1AddTaskRequest request);

    /**
     * 查询任务
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<VideoGenerationsSearchTaskV1Response> searchVideoTask(VincentDiagramV1SearchTaskRequest request);
}
