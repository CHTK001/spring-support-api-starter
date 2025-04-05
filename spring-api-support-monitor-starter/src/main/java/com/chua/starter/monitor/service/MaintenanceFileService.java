package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 维护文件服务
 *
 * @author CH
 */
public interface MaintenanceFileService {

    /**
     * 上传文件到维护组下的所有主机
     *
     * @param file    文件
     * @param request 请求参数
     * @return 上传结果
     */
    ReturnResult<FileUploadResponse> uploadFile(MultipartFile file, FileUploadRequest request);
} 