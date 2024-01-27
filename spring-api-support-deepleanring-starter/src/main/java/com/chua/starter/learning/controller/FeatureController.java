package com.chua.starter.learning.controller;


import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.ml.PredictResult;
import com.chua.common.support.ml.PredictResultObject;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.learning.pojo.LearningRequest;
import com.chua.starter.learning.service.LearningConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 检测控制器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/25
 */
@Tag(name = "特征值接口")
@RestController
@RequestMapping("/v1/learning/feature")
@RequiredArgsConstructor
public class FeatureController {

    @Resource
    private LearningConfigService learningConfigService;



    /**
     * 接收学习请求并进行预测
     *
     * @param request 学习请求对象
     * @return 预测结果对象
     */
    @PostMapping
    @Operation(summary = "检测远程文件")
    public ReturnResult<PredictResultObject<PredictResult>> detect(@RequestParam("file") MultipartFile multipartFile, LearningRequest request) {
        File file = null;
        try {
            if(null != multipartFile) {
                file = FileUtils.createTempFile("/feature/" + IdUtils.createUlid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())
                        , multipartFile.getInputStream());
                request.setFile(file);
            }
            return ReturnResult.of(learningConfigService.detect(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(null != file) {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 接收学习请求并进行预测
     *
     * @param request 学习请求对象
     * @return 预测结果对象
     */
    @GetMapping
    @Operation(summary = "检测上传文件")
    public ReturnResult<PredictResultObject<PredictResult>> detectGet(@RequestParam("file") MultipartFile multipartFile,  LearningRequest request) {
        try {
            request.setFile(multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ReturnResult.of(learningConfigService.detect(request));
    }

}
