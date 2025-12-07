package com.chua.starter.project.service;

import com.chua.deeplearning.support.ml.bigmodel.resolution.ImageResolutionAddTaskV1Response;
import com.chua.deeplearning.support.ml.bigmodel.resolution.ImageResolutionSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.pojo.vincent.ImageResolutionAddTaskV2Request;
import com.chua.starter.project.pojo.vincent.ImageResolutionSearchTaskV2Request;

/**
 * 图像超分辨率服务接口
 * 用于处理图像超分辨率任务的提交和查询
 *
 * @author CH
 * @since 2024/9/10
 */
public interface ImageResolutionService {

    /**
     * 提交图像超分辨率任务
     *
     * @param request 图像超分辨率任务提交请求参数
     *                包含以下主要参数:
     *                - sysProjectId: 项目ID，不能为空，示例: 1001
     *                - model: 模型名称，不能为空，示例: "ESRGAN"
     *                - url: 输入文件地址，支持base64或URL格式，示例: "https://example.com/image.jpg"
     *                - scaleFactor: 输出图像尺寸缩放因子，示例: 2 (表示放大2倍)
     * @return 返回任务提交结果，包含任务ID等信息
     *         成功时返回: {@link ImageResolutionAddTaskV1Response}
     */
    ReturnResult<ImageResolutionAddTaskV1Response> addTask(ImageResolutionAddTaskV2Request request);

    /**
     * 查询图像超分辨率任务状态
     *
     * @param request 图像超分辨率任务查询请求参数
     *                包含以下可选参数:
     *                - sysProjectId: 项目ID，示例: 1001
     *                - taskId: 任务ID，示例: "task_20240910_001"
     *                - model: 模型名称，示例: "ESRGAN"
     *                - region: 地域信息，示例: "cn-hangzhou"
     * @return 返回任务查询结果，包含任务状态、输出图像等信息
     *         成功时返回: {@link ImageResolutionSearchTaskV1Response}
     */
    ReturnResult<ImageResolutionSearchTaskV1Response> searchTask(ImageResolutionSearchTaskV2Request request);
}
