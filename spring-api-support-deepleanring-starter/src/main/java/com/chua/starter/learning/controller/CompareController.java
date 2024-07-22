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

import java.io.File;
import java.io.IOException;

/**
 * 检测控制器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/25
 */
@Tag(name = "比较接口")
@RestController
@RequestMapping("/v1/learning/compare")
@RequiredArgsConstructor
public class CompareController {

    final LearningConfigService learningConfigService;



    /**
     * 接收学习请求并进行预测
     *
     * @param request 学习请求对象
     * @return 预测结果对象
     */
    @PostMapping
    @Operation(summary = "检测远程文件")
    public ReturnResult<PredictResultObject<PredictResult>> detect(
            @RequestParam("file1") MultipartFile multipartFile1,
            @RequestParam("file2") MultipartFile multipartFile2,
                                                                   LearningRequest request) {
        File file1 = null;
        File file2 = null;
        try {
            if(null != multipartFile1) {
                file1 = FileUtils.createTempFile("/feature/" + IdUtils.createUlid() + "." + FileUtils.getExtension(multipartFile1.getOriginalFilename())
                        , multipartFile1.getInputStream());
                request.setFile(file1);
            }
            if(null != multipartFile2) {
                file2 = FileUtils.createTempFile("/feature/" + IdUtils.createUlid() + "." + FileUtils.getExtension(multipartFile2.getOriginalFilename())
                        , multipartFile2.getInputStream());
                request.setFile2(file2);
            }
            return ReturnResult.of(learningConfigService.compare(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(null != file1) {
                try {
                    FileUtils.forceDelete(file1);
                } catch (IOException ignored) {
                }
            }
            if(null != file2) {
                try {
                    FileUtils.forceDelete(file2);
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
