package com.chua.starter.project.service;

import com.chua.deeplearning.support.ml.bigmodel.generations.image.ImageGenerationsSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysAiVincentTask;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskRequest;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1AddTaskResponse;
import com.chua.starter.project.pojo.vincent.VincentDiagramV1SearchTaskRequest;

import java.util.List;

/**
 * 文生图服务接口
 *
 * @author CH
 * @since 2024/9/10
 */
public interface ImageGenerationService {

    /**
     * 提交文生图任务
     *
     * @param request 文生图任务请求参数
     *                例如：{
     *                "sysProjectId": 1001,
     *                "model": "stable-diffusion-v1.5",
     *                "input": {
     *                "prompt": "A beautiful landscape with mountains and lakes",
     *                "negativePrompt": "ugly, blurry",
     *                "refImg": "https://example.com/reference.jpg"
     *                },
     *                "parameters": {
     *                "style": "realistic",
     *                "size": "1024*1024",
     *                "quality": "high",
     *                "seed": 12345,
     *                "fps": 30,
     *                "withAudio": 0,
     *                "number": 4,
     *                "refStrength": 0.7
     *                }
     *                }
     * @return 返回任务提交结果，包含任务ID和初始状态
     *         例如：{
     *         "output": {
     *         "taskStatus": "submitted",
     *         "taskId": "task_1234567890"
     *         },
     *         "request_id": "req_0987654321"
     *         }
     */
    ReturnResult<VincentDiagramV1AddTaskResponse> addTask(VincentDiagramV1AddTaskRequest request);

    /**
     * 查询文生图任务状态
     *
     * @param request 查询任务请求参数
     *                例如：{
     *                "sysProjectId": 1001,
     *                "taskId": "task_1234567890",
     *                "model": "stable-diffusion-v1.5",
     *                "region": "cn-hangzhou"
     *                }
     * @return 返回任务查询结果，包含任务当前状态和生成的图片链接
     *         例如：{
     *         "output": {
     *         "taskStatus": "completed",
     *         "taskId": "task_1234567890",
     *         "images": [
     *         "https://example.com/image1.jpg",
     *         "https://example.com/image2.jpg"
     *         ]
     *         },
     *         "request_id": "req_0987654321"
     *         }
     */
    ReturnResult<ImageGenerationsSearchTaskV1Response> searchTask(VincentDiagramV1SearchTaskRequest request);

    /**
     * 获取用户历史任务记录
     *
     * @param userId 用户ID，用于标识哪个用户的历史记录
     *               例如：10001
     * @param sysAiVincentTaskType 任务类型，用于筛选特定类型的记录
     *                             例如："text_to_image" 或 "image_to_video"
     * @return 返回用户的历史任务列表
     *         例如：[
     *         {
     *         "sysAiVincentTaskId": 1,
     *         "sysAiVincentTaskUid": "uid_1234567890",
     *         "sysAiVincentTaskStatus": 1,
     *         "sysProjectId": 1001,
     *         "sysAiModuleId": 2001,
     *         "sysAiVincentTaskCondition": "{\"prompt\":\"mountain\"}",
     *         "sysAiVincentTaskType": "text_to_image",
     *         "sysAiVincentTaskUrl": "https://example.com/result.jpg",
     *         "sysAiVincentTaskLocalUrl": "/local/path/result.jpg",
     *         "sysAiVincentTaskRequestId": "req_0987654321"
     *         }
     *         ]
     */
    List<SysAiVincentTask> history(Integer userId, String sysAiVincentTaskType);
}
