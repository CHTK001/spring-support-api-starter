package com.chua.starter.learning.reg.image.service.impl;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.elasticsearch.support.service.DocumentService;
import com.chua.starter.learning.properties.ImageRegProperties;
import com.chua.starter.learning.reg.entity.SysFace;
import com.chua.starter.learning.reg.image.service.ImageRegService;
import com.chua.starter.learning.service.image.feature.ImageFeatureService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 人脸识别服务器
 *
 * @author CH
 */
@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ImageRegServiceImpl implements ImageRegService {

    private final ImageFeatureService faceFeatureService;
    private final ImageRegProperties imageRegProperties;

    private final DocumentService documentService;

    @Override
    public Result<PredictResult> analysis(String indexName, SysFace sysFace, String originalFilename, InputStream inputStream) {
        File file = createFile(originalFilename, inputStream);

        try {
            documentService.checkIndex(indexName);
            SysFace sysFace1 = analysisSysImage(sysFace, file);
            documentService.addDocument(indexName, sysFace1);
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }
        return Result.success();
    }

    /**
     * 特征值提取
     *
     * @param sysFace     条件
     * @param file        图片
     */
    @Override
    public void doFeatureExtract(SysFace sysFace, File file) {
        List<PredictResult> recognize = faceFeatureService.recognize(file);
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
            SysFace sysFace = new SysFace();
            doFeatureExtract(sysFace, file);
            return Result.success(sysFace.getFeature());
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 数据组装
     *
     * @param sysFace 请求条件
     * @param file    图像
     * @return 文档数据
     */
    private SysFace analysisSysImage(SysFace sysFace, File file) {
        SysFace sysFace1 = new SysFace();
        BeanUtils.copyProperties(sysFace, sysFace1);
        sysFace1.setCreateTime(new Date());
        doFeatureExtract(sysFace1, file);
        return sysFace1;
    }


    /**
     * 创建文件
     *
     * @param originalFilename 文件名
     * @param inputStream      文件
     * @return 文件
     */
    private File createFile(String originalFilename, InputStream inputStream) {
        String cachePath = imageRegProperties.getCachePath();
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
