package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.result.PutObjectResult;
import com.chua.starter.system.entity.SysTaskSso;
import com.chua.starter.system.pojo.sso.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
*
 * 分片表
* @since 2024/12/4
* @author CH
*/
public interface SysTaskSsoService extends IService<SysTaskSso>{

    /**
     * 上传文件到对象存储系统
     *
     * @param request 包含上传文件所需信息的请求对象
     * @param multipartFile 要上传的多部分文件对象
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, MultipartFile multipartFile);

    /**
     * 上传文件到对象存储系统
     *
     * @param request 包含上传文件所需信息的请求对象
     * @param file 要上传的文件对象
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, File file);

    /**
     * 上传文件到对象存储系统
     *
     * @param request 包含上传文件所需信息的请求对象
     * @param fileName 要上传文件的名称
     * @param bytes 要上传文件的字节数组
     * @return 包含上传结果的返回对象
     */
    ReturnResult<PutObjectResult> upload(SysFileUploadRequest request, String fileName, byte[] bytes);

    /**
     * 创建文件上传任务
     *
     * @param request 包含创建上传任务所需信息的请求对象
     * @return 包含上传任务创建结果的返回对象
     */
    ReturnResult<SysFileUploadTaskResponse> createTask(SysFileUploadTaskRequest request);

    /**
     * 执行文件上传任务的一部分
     *
     * @param request 包含上传任务部分所需信息的请求对象
     * @param multipartFile 要上传的多部分文件对象
     * @return 包含上传任务部分上传结果的返回对象
     */
    ReturnResult<SysFileUploadTaskPartResponse> uploadTask(SysFileUploadTaskPartRequest request, MultipartFile multipartFile);
}
