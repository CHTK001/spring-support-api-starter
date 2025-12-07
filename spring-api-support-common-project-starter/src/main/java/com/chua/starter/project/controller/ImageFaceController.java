package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.PredictResult;
import com.chua.starter.common.support.utils.MultipartFileUtils;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.FaceService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 人脸接口
 *
 * @author CH
 * @since 2025/9/8 21:29
 */
@RestController
@Slf4j
@Api(tags = "人脸接口")
@Tag(name = "人脸接口")
@RequestMapping("/v2/ai/image/face")
@RequiredArgsConstructor
public class ImageFaceController {

    private final FaceService faceService;

    /**
     * 人脸检测接口
     *
     * @param file    上传的人脸图片文件，支持JPG、PNG等常见图片格式
     *                示例：@RequestPart("file") MultipartFile file
     * @param request 图片处理请求参数，包含请求ID和模型名称等信息
     *                示例：{"requestId": "detect-20250908-001", "model": "face-detect-v1"}
     * @return 人脸检测结果，包含检测到的人脸位置、关键点等信息
     * 如果检测成功，返回包含PredictResult的ReturnResult对象
     * 如果检测失败，返回包含错误信息的ReturnResult对象
     */
    @PostMapping("detect")
    @Operation(summary = "检测")
    public ReturnResult<PredictResult> detect(
            @Parameter(description = "人脸图片文件") MultipartFile file,
            @RequestBody ImageRequest request) {
        try {
            File imageFile = MultipartFileUtils.toFile(file);
            return faceService.detect(imageFile, request);
        } catch (Exception e) {
            log.error("人脸检测失败", e);
            return ReturnResult.illegal("人脸检测失败: " + e.getMessage());
        }
    }

    /**
     * 人脸特征提取接口
     *
     * @param file    上传的人脸图片文件，支持JPG、PNG等常见图片格式
     *                示例：@RequestPart("file") MultipartFile file
     * @param request 图片处理请求参数，包含请求ID和模型名称等信息
     *                示例：{"requestId": "feature-20250908-001", "model": "face-feature-v1"}
     * @return 人脸特征提取结果，包含人脸特征向量等信息
     * 如果特征提取成功，返回包含PredictResult的ReturnResult对象
     * 如果特征提取失败，返回包含错误信息的ReturnResult对象
     */
    @PostMapping("feature")
    @Operation(summary = "特征")
    public ReturnResult<PredictResult> feature(
            @Parameter(description = "人脸图片文件") MultipartFile file,
            @RequestBody ImageRequest request) {
        try {
            File imageFile = MultipartFileUtils.toFile(file);
            return faceService.feature(imageFile, request);
        } catch (Exception e) {
            log.error("人脸特征提取失败", e);
            return ReturnResult.illegal("人脸特征提取失败: " + e.getMessage());
        }
    }

    /**
     * 人脸识别接口
     *
     * @param file    上传的人脸图片文件，支持JPG、PNG等常见图片格式
     *                示例：@RequestPart("file") MultipartFile file
     * @param request 图片处理请求参数，包含请求ID和模型名称等信息
     *                示例：{"requestId": "recognizer-20250908-001", "model": "face-recognition-v1"}
     * @return 人脸识别结果，包含识别到的身份信息等
     * 如果识别成功，返回包含PredictResult的ReturnResult对象
     * 如果识别失败，返回包含错误信息的ReturnResult对象
     */
    @PostMapping("recognizer")
    @Operation(summary = "识别")
    public ReturnResult<PredictResult> recognizer(
            @Parameter(description = "人脸图片文件") MultipartFile file,
            @RequestBody ImageRequest request) {
        try {
            File imageFile = MultipartFileUtils.toFile(file);
            return faceService.recognizer(imageFile, request);
        } catch (Exception e) {
            log.error("人脸识别失败", e);
            return ReturnResult.illegal("人脸识别失败: " + e.getMessage());
        }
    }

    /**
     * 人脸比对接口
     *
     * @param file    第一张人脸图片文件，支持JPG、PNG等常见图片格式
     *                示例：@RequestPart("file") MultipartFile file
     * @param file2   第二张人脸图片文件，支持JPG、PNG等常见图片格式
     *                示例：@RequestPart("file2") MultipartFile file2
     * @param request 图片处理请求参数，包含请求ID和模型名称等信息
     *                示例：{"requestId": "compare-20250908-001", "model": "face-compare-v1"}
     * @return 人脸比对结果，包含两张人脸的相似度等信息
     * 如果比对成功，返回包含PredictResult的ReturnResult对象
     * 如果比对失败，返回包含错误信息的ReturnResult对象
     */
    @PostMapping("compare")
    @Operation(summary = "对比")
    public ReturnResult<PredictResult> compare(
            @Parameter(description = "第一张人脸图片文件") MultipartFile file,
            @Parameter(description = "第二张人脸图片文件") MultipartFile file2,
            @RequestBody ImageRequest request) {
        try {
            File imageFile1 = MultipartFileUtils.toFile(file);
            File imageFile2 = MultipartFileUtils.toFile(file2);
            return faceService.compare(imageFile1, imageFile2, request);
        } catch (Exception e) {
            log.error("人脸比对失败", e);
            return ReturnResult.illegal("人脸比对失败: " + e.getMessage());
        }
    }
}
