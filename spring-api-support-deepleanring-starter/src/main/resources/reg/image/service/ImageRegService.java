package com.chua.starter.learning.reg.image.service;

import com.chua.common.support.constant.PredictResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.reg.entity.SysFace;

import java.io.File;
import java.io.InputStream;

/**
 * @author CH
 */
public interface ImageRegService {
    /**
     * 人脸上传到人脸库
     *
     * @param indexName        索引
     * @param sysFace          基本信息
     * @param originalFilename 图片名称
     * @param inputStream      图片
     * @return 结果
     */
    Result<PredictResult> analysis(String indexName, SysFace sysFace, String originalFilename, InputStream inputStream);

    /**
     * 特征值提取
     *
     * @param sysFace     条件
     * @param file        图片
     */
    void doFeatureExtract(SysFace sysFace, File file);

    /**
     * 特征值提取
     *
     * @param originalFilename 文件名
     * @param inputStream      文件
     * @return 特征值
     */
    Result<float[]> feature(String originalFilename, InputStream inputStream);

    /**
     * 特征值提取
     *
     * @param originalFilename 文件名
     * @param file      文件
     * @return 特征值
     */
    Result<float[]> feature(String originalFilename, File file);
}
