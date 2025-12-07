package com.chua.starter.project.service;

import com.chua.deeplearning.support.ml.bigmodel.colorization.ImageColorizationAddTaskV1Response;
import com.chua.deeplearning.support.ml.bigmodel.colorization.ImageColorizationSearchTaskV1Response;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.pojo.vincent.ImageColorizationAddTaskV2Request;
import com.chua.starter.project.pojo.vincent.ImageColorizationSearchTaskV2Request;

/**
 * 图像上色服务接口
 *
 * @author CH
 * @since 2024/9/10
 */
public interface ImageColorizationService {

    /**
     * 提交图像上色任务
     *
     * @param request 图像上色任务请求参数
     *                <ul>
     *                <li>sysProjectId: 项目ID，不能为空，例如：1001</li>
     *                <li>model: 模型名称，不能为空，例如："colorization_v1"</li>
     *                <li>url: 文件地址，支持base64或URL格式，例如："http://example.com/image.jpg"</li>
     *                </ul>
     * @return 返回提交任务的结果，包含任务ID等信息
     *         {@link ReturnResult<ImageColorizationAddTaskV1Response>}
     */
    ReturnResult<ImageColorizationAddTaskV1Response> addTask(ImageColorizationAddTaskV2Request request);

    /**
     * 查询图像上色任务状态
     *
     * @param request 图像上色任务查询请求参数
     *                <ul>
     *                <li>sysProjectId: 项目ID，例如：1001</li>
     *                <li>taskId: 任务ID，例如："task_1234567890"</li>
     *                <li>model: 模型名称，例如："colorization_v1"</li>
     *                <li>region: 地域信息，例如："cn-hangzhou"</li>
     *                </ul>
     * @return 返回任务查询结果，包含任务状态、输出图片地址等信息
     *         {@link ReturnResult<ImageColorizationSearchTaskV1Response>}
     */
    ReturnResult<ImageColorizationSearchTaskV1Response> searchTask(ImageColorizationSearchTaskV2Request request);
}
