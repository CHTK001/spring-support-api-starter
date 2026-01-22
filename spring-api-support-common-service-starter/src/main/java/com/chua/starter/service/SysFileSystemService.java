package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.service.pojo.sso.SysFileInstantUploadRequest;
import com.chua.starter.service.pojo.sso.SysFileInstantUploadResponse;
import com.chua.starter.service.pojo.sso.SysFileTaskInfoResponse;
import com.chua.starter.service.pojo.sso.SysFileUploadRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskPartRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskPartResponse;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件系统服务接口
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
public interface SysFileSystemService extends IService<SysFileSystem> {

    /**
     * 上传文件到对象存储系统
     *
     * @param request       包含上传文件所需信息的请求对象
     * @param multipartFile 要上传的多部分文件对象
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, MultipartFile multipartFile);

    /**
     * 上传文件到对象存储系统
     *
     * @param request 包含上传文件所需信息的请求对象
     * @param file    要上传的文件对象
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, File file);

    /**
     * 上传文件到对象存储系统
     *
     * @param request  包含上传文件所需信息的请求对象
     * @param fileName 要上传文件的名称
     * @param bytes    要上传文件的字节数组
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, String fileName, byte[] bytes);

    /**
     * 创建文件上传任务
     *
     * @param request 包含创建上传任务所需信息的请求对象
     * @return 包含上传任务创建结果的返回对象
     */
    ReturnResult<SysFileUploadTaskResponse> createTaskForFileSystem(SysFileUploadTaskRequest request);

    /**
     * 执行文件上传任务的一部分
     *
     * @param request       包含上传任务部分所需信息的请求对象
     * @param multipartFile 要上传的多部分文件对象
     * @return 包含上传任务部分上传结果的返回对象
     */
    ReturnResult<SysFileUploadTaskPartResponse> uploadTaskForFileSystem(SysFileUploadTaskPartRequest request, MultipartFile multipartFile);

    /**
     * 检查文件是否支持秒传
     * 通过MD5和文件大小检查文件是否已存在
     *
     * @param request 秒传检查请求
     * @return 秒传检查结果
     */
    ReturnResult<SysFileInstantUploadResponse> checkInstantUploadForFileSystem(SysFileInstantUploadRequest request);

    /**
     * 获取上传任务信息（用于断点续传）
     * 返回任务详情和已上传的分片列表
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    ReturnResult<SysFileTaskInfoResponse> getTaskInfoForFileSystem(String taskId);

    /**
     * 根据MD5查找未完成的上传任务（用于断点续传）
     *
     * @param fileMd5  文件MD5
     * @param fileSize 文件大小
     * @return 任务信息
     */
    ReturnResult<SysFileTaskInfoResponse> findTaskByMd5ForFileSystem(String fileMd5, Long fileSize);
}
