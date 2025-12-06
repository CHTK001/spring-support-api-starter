package com.chua.starter.project.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.ml.PredictResult;
import com.chua.starter.project.pojo.ImageRequest;

import java.io.File;

/**
 * 人脸服务接口，提供人脸检测、特征提取、人脸识别和人脸比对功能
 *
 * @author CH
 * @since 2025/9/8 21:34
 */
public interface FaceService {

    /**
     * 人脸检测接口
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

    /**
     * 人脸特征提取接口
     * 根据给定的图片文件和请求参数，提取图片中人脸的特征向量
     *
     * @param file    图片文件，支持常见图片格式如JPG、PNG等
     *                示例：new File("/path/to/image.jpg")
     * @param request 图片请求参数，包含请求ID和模型名称等信息
     *                示例：ImageRequest(requestId="req-002", model="face-feature-v1")
     * @return 返回人脸特征提取结果，包含人脸特征向量等信息
     * 如果特征提取成功，返回包含PredictResult的ReturnResult对象
     * 如果特征提取失败，返回包含错误信息的ReturnResult对象
     */
    ReturnResult<PredictResult> feature(File file, ImageRequest request);

    /**
     * 人脸识别接口
     * 根据给定的图片文件和请求参数，识别人脸对应的身份信息
     *
     * @param file    图片文件，支持常见图片格式如JPG、PNG等
     *                示例：new File("/path/to/image.jpg")
     * @param request 图片请求参数，包含请求ID和模型名称等信息
     *                示例：ImageRequest(requestId="req-003", model="face-recognition-v1")
     * @return 返回人脸识别结果，包含识别到的身份信息等
     * 如果识别成功，返回包含PredictResult的ReturnResult对象
     * 如果识别失败，返回包含错误信息的ReturnResult对象
     */
    ReturnResult<PredictResult> recognizer(File file, ImageRequest request);

    /**
     * 人脸比对接口
     * 根据给定的两张图片文件和请求参数，比对两张图片中人脸的相似度
     *
     * @param file    第一张图片文件，支持常见图片格式如JPG、PNG等
     *                示例：new File("/path/to/image1.jpg")
     * @param file2   第二张图片文件，支持常见图片格式如JPG、PNG等
     *                示例：new File("/path/to/image2.jpg")
     * @param request 图片请求参数，包含请求ID和模型名称等信息
     *                示例：ImageRequest(requestId="req-004", model="face-compare-v1")
     * @return 返回人脸比对结果，包含两张人脸的相似度等信息
     * 如果比对成功，返回包含PredictResult的ReturnResult对象
     * 如果比对失败，返回包含错误信息的ReturnResult对象
     */
    ReturnResult<PredictResult> compare(File file, File file2, ImageRequest request);
}
