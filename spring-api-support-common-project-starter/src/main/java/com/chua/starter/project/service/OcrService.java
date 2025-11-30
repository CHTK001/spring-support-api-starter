package com.chua.starter.project.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.PredictResult;
import com.chua.starter.project.pojo.ImageRequest;

import java.io.File;

/**
 * OCR检测接口
 *
 * @author CH
 * @since 2025/9/8 21:34
 */
public interface OcrService {

    /**
     * OCR检测接口
     * 根据给定的图片文件和请求参数，检测图片中的人脸位置和关键点信息
     *
     * @param file    图片文件，支持常见图片格式如JPG、PNG等
     *                示例：new File("/path/to/image.jpg")
     * @param request 图片请求参数，包含请求ID和模型名称等信息
     *                示例：ImageRequest(requestId="req-001", model="face-detect-v1")
     * @return 返回人脸检测结果，包含检测到的人脸位置、关键点等信息
     * 如果检测成功，返回包含PredictResult的ReturnResult对象
     * 如果检测失败，返回包含错误信息的ReturnResult对象
     */
    ReturnResult<PredictResult> detect(File file, ImageRequest request);
}
