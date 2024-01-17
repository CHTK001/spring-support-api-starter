package com.chua.starter.learning.reg.face.service.impl;

import ai.djl.modality.cv.Image;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.FileUtils;
import com.chua.pytorch.support.utils.LocationUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.properties.FaceRegProperties;
import com.chua.starter.learning.reg.face.service.FaceRegService;
import com.chua.starter.learning.service.face.detection.FaceDetectionService;
import com.chua.starter.learning.service.face.feature.FaceFeatureService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人脸识别服务器
 *
 * @author CH
 */
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FaceRegServiceImpl implements FaceRegService {

    private final FaceDetectionService faceDetectionService;

    private final FaceFeatureService faceFeatureService;
    private final FaceRegProperties faceRegProperties;

    private final DocumentService documentService;

    @Override
    public Result<PredictResult> analysis(String indexName, SysFace sysFace, String originalFilename, InputStream inputStream) {
        File file = createFile(originalFilename, inputStream);
        List<PredictResult> faceResults = checkFace(file);
        if (faceResults.isEmpty()) {
            return Result.failed("未检测到人脸");
        }

        if (!faceRegProperties.isOvering() && faceResults.size() > 1) {
            return Result.failed("单张图片人脸数一个以上");
        }
        PredictResult result = faceResults.get(0);
        try {
            documentService.checkIndex(indexName);
            SysFace sysFace1 = analysisSysFace(sysFace, result, file);
            documentService.addDocument(indexName, sysFace1);
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }
        return Result.success(result);
    }

    /**
     * 特征值提取
     *
     * @param sysFace     条件
     * @param boundingBox 区域
     * @param file        图片
     */
    @Override
    public void doFeatureExtract(SysFace sysFace, Object boundingBox, File file) {
        Image subImage = LocationUtils.getSubImage(LocationUtils.getImage(file), boundingBox);
        List<PredictResult> recognize = faceFeatureService.recognize(subImage);
        sysFace.setFeature((float[]) recognize.get(0).getNdArray());
    }

    @Override
    public Result<float[]> feature(String originalFilename, InputStream inputStream) {
        File file = createFile(originalFilename, inputStream);
        try {
            return feature(originalFilename, file);
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }
    }
    @Override
    public Result<float[]> feature(String originalFilename, File file) {
        try {
            List<PredictResult> faceResults = checkFace(file);
            if (faceResults.isEmpty()) {
                return Result.failed("未检测到人脸");
            }

            if (!faceRegProperties.isOvering() && faceResults.size() > 1) {
                return Result.failed("单张图片人脸数一个以上");
            }

            PredictResult result = faceResults.get(0);
            SysFace sysFace = new SysFace();
            doFeatureExtract(sysFace, result.getBoundingBox(), file);
            return Result.success(sysFace.getFeature());
        } finally {
//            try {
//                FileUtils.forceDelete(file);
//            } catch (IOException ignored) {
//            }
        }
    }

    /**
     * 数据组装
     *
     * @param sysFace 请求条件
     * @param result  结果
     * @param file    图像
     * @return 文档数据
     */
    private SysFace analysisSysFace(SysFace sysFace, PredictResult result, File file) {
        SysFace sysFace1 = new SysFace();
        BeanUtils.copyProperties(sysFace, sysFace1);
        sysFace1.setScore(result.getScore());
        sysFace1.setBox(Json.toJson(result.getBoundingBox()));
        sysFace1.setCreateTime(new Date());
        doFeatureExtract(sysFace1, result.getBoundingBox(), file);
        return sysFace1;
    }


    /**
     * 检测人脸
     *
     * @param file 图片
     * @return 结果
     */
    @Override
    public List<PredictResult> checkFace(File file) {
        List<PredictResult> recognize = faceDetectionService.recognize(file);
        if (recognize.isEmpty()) {
            return Collections.emptyList();
        }
        return recognize.stream().filter(it -> it.getScore() > faceRegProperties.getThreshold()).collect(Collectors.toList());
    }

    /**
     * 创建文件
     *
     * @param originalFilename 文件名
     * @param inputStream      文件
     * @return 文件
     */
    private File createFile(String originalFilename, InputStream inputStream) {
        String cachePath = faceRegProperties.getCachePath();
        File file = FileUtils.createTempFile(cachePath, originalFilename);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             InputStream is = inputStream) {
            IOUtils.copy(is, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("请联系管理员");
        }
        return file;
    }
}
