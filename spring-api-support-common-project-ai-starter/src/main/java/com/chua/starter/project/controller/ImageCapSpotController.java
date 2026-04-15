package com.chua.starter.project.controller;

import com.chua.common.support.ai.result.PredictResult;
import com.chua.common.support.ai.result.PredictResultObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.utils.MultipartFileUtils;
import com.chua.starter.project.pojo.ImageRequest;
import com.chua.starter.project.service.CapSpotService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * CapSpot 模板点击定位接口。
 *
 * @author CH
 * @since 2026/4/7 10:00
 */
@RestController
@Api(tags = "CapSpot 模板点击定位接口")
@Tag(name = "CapSpot 模板点击定位接口")
@RequestMapping("/v2/ai/image/capspot")
@ConditionalOnClass(name = "com.chua.deeplearning.support.onnx.OnnxIdentificationEngine")
public class ImageCapSpotController {
    private static final Logger log = LoggerFactory.getLogger(ImageCapSpotController.class);

    private final CapSpotService capSpotService;

    public ImageCapSpotController(CapSpotService capSpotService) {
        this.capSpotService = capSpotService;
    }

    /**
     * 在目标图中定位模板图中的多个点击区域。
     *
     * @param template 模板图
     * @param target   目标图
     * @param request  请求参数
     * @return 返回完整命中列表
     */
    @PostMapping(value = "detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "模板点击定位")
    public ReturnResult<PredictResultObject<PredictResult>> detect(
            @Parameter(description = "模板图片文件") @RequestPart("template") MultipartFile template,
            @Parameter(description = "目标图片文件") @RequestPart("target") MultipartFile target,
            @RequestPart("request") ImageRequest request) {
        try {
            File templateFile = MultipartFileUtils.toFile(template);
            File targetFile = MultipartFileUtils.toFile(target);
            return capSpotService.detect(templateFile, targetFile, request);
        } catch (Exception e) {
            log.error("CapSpot 模板点击定位失败", e);
            return ReturnResult.illegal("CapSpot 模板点击定位失败: " + e.getMessage());
        }
    }
}
