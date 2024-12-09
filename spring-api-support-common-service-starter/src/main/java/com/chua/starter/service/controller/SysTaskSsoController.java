package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.result.PutObjectResult;
import com.chua.common.support.validator.group.UploadGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.service.SysTaskSsoService;
import com.chua.starter.system.pojo.sso.*;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 * @author CH
 * @since 2024/12/4
 */
@RestController
@Slf4j
@Api(tags = "文件上传接口")
@Tag(name = "文件上传接口")
@RequestMapping("/v2/file")
@RequiredArgsConstructor
public class SysTaskSsoController {


    private final SysTaskSsoService sysTaskSsoService;

    /**
     * 上传文件
     * @param request 请求
     * @param file 文件
     * @return 结果
     */
    @Operation(summary = "上传文件")
    @PutMapping("/upload")
    @OperateLog
    public ReturnResult<PutObjectResult> upload(@ParameterObject @Validated(UploadGroup.class) SysFileUploadRequest request,
                                                @RequestPart @RequestParam("file") MultipartFile file,
                                                BindingResult bindingResult
                                          ) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(null == file) {
            return Result.illegal("文件不能为空");
        }
        return sysTaskSsoService.upload(request, file);
    }


    /**
     * 创建分片任务
     * @param request 请求
     * @return 结果
     */
    @Operation(summary = "创建分片任务")
    @PostMapping("/task/create")
    @OperateLog
    public ReturnResult<SysFileUploadTaskResponse> createTask(@Validated(UploadGroup.class) @RequestBody SysFileUploadTaskRequest request,
                                                              BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysTaskSsoService.createTask(request);
    }
    /**
     * 创建分片任务
     * @param request 请求
     * @return 结果
     */
    @Operation(summary = "上传分片任务")
    @PostMapping("/task/upload")
    @OperateLog
    public ReturnResult<SysFileUploadTaskPartResponse> uploadTask(@ParameterObject @Validated(UploadGroup.class) SysFileUploadTaskPartRequest request,
                                                                  @RequestPart@RequestParam("file") @Parameter(name= "file", description = "文件") MultipartFile multipartFile,
                                                                  BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if(null == multipartFile) {
            return Result.illegal("文件不能为空");
        }
        return sysTaskSsoService.uploadTask(request, multipartFile);
    }


}
