package com.chua.starter.monitor.controller.file;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.response.FileSystemResponse;
import com.chua.starter.monitor.service.FileSystemService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文件列表
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "文件列表接口")
@Tag(name = "文件列表接口")
@RequestMapping("/v2/filesystem")
@RequiredArgsConstructor
public class FileController {


    private final FileSystemService fileSystemService;
    /**
     * 获取文件列表
     *
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表")
    public ReturnResult<List<FileSystemResponse>> list(String absolutePath, String name) {
        return fileSystemService.list(absolutePath, name);
    }
}
