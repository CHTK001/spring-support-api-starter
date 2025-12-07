package com.chua.starter.project.service;

import com.chua.deeplearning.support.ml.bigmodel.generations.video.VideoGenerationsSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1SearchTaskRequest;

/**
 * 视频生成服务接口
 *
 * @author CH
 * @since 2024/9/10
 */
public interface VideoGenerationService {

    /**
     * 提交视频生成任务
     *
     * @param request 视频生成任务请求参数，包含模型名称、输入参数和生成参数等信息
     *                例如：{
     *                "sysProjectId": 1001,
     *                "model": "video-gen-v1",
     *                "input": {
     *                "prompt": "一个美丽的风景视频",
     *                "negativePrompt": "模糊的图像",
     *                "refImg": "https://example.com/reference.jpg"
     *                },
     *                "parameters": {
     *                "style": "realistic",
     *                "size": "1920x1080",
     *                "quality": "high",
     *                "seed": 12345,
     *                "fps": 30,
     *                "withAudio": 1,
     *                "number": 1,
     *                "refStrength": 0.7
     *                }
     *                }
     * @return 视频生成任务提交结果，包含任务ID和初始状态等信息
     */
    ReturnResult<VincentDiagramV1AddTaskResponse> addTask(VincentDiagramV1AddTaskRequest request);

    /**
     * 查询视频生成任务状态
     *
     * @param request 视频生成任务查询请求参数，包含项目ID和任务ID等信息
     *                例如：{
     *                "sysProjectId": 1001,
     *                "taskId": "task-20240910-001"
     *                }
     * @return 视频生成任务的当前状态和结果信息
     */
    ReturnResult<VideoGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request);
}
