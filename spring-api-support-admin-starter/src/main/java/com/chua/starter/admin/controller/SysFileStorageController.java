package com.chua.starter.admin.controller;

import com.chua.starter.admin.service.SysFileStorageService;
import com.chua.starter.common.support.filestorage.FileStorageProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * sys文件存储控制器
 *
 * @author CH
 */
@Tag(name = "OSS接口")
@RestController
@RequestMapping("/v1/oss")
@RequiredArgsConstructor
public class SysFileStorageController {

    private final SysFileStorageService sysFileStorageService;


    public final FileStorageProvider fileStorageProvider;
}
