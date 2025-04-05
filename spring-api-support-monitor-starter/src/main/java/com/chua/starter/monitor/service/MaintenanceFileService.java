package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.pojo.request.FileDeployRequest;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceFileRequest;
import com.chua.starter.monitor.pojo.response.FileDeployResponse;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 维护文件服务
 *
 * @author CH
 */
public interface MaintenanceFileService {

    /**
     * 上传单个文件到维护组下的所有主机
     *
     * @param file    文件
     * @param request 请求参数
     * @return 上传结果
     */
    ReturnResult<FileUploadResponse> uploadFile(MultipartFile file, FileUploadRequest request);
    
    /**
     * 批量上传文件到维护组下的所有主机
     *
     * @param files   文件数组
     * @param request 请求参数
     * @return 上传结果
     */
    ReturnResult<FileUploadResponse> uploadFiles(MultipartFile[] files, FileUploadRequest request);
    
    /**
     * 获取指定维护组下的文件列表
     *
     * @param groupId 维护组ID
     * @return 文件列表
     */
    ReturnResult<List<MaintenanceFile>> getFilesByGroupId(Long groupId);
    
    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteFile(Long fileId);
    
    /**
     * 更新文件信息
     *
     * @param request 文件信息请求
     * @return 操作结果
     */
    ReturnResult<Boolean> updateFile(MaintenanceFileRequest request);
    
    /**
     * 部署文件到维护组下的所有主机
     *
     * @param request 部署请求
     * @return 部署结果
     */
    ReturnResult<FileDeployResponse> deployFile(FileDeployRequest request);
    
    /**
     * 同步文件到远程主机
     * 将指定文件同步到维护组下的所有启用主机
     *
     * @param fileId 文件ID
     * @return 同步任务ID
     */
    ReturnResult<String> syncFile(Long fileId);
} 