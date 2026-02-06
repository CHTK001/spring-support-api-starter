package com.chua.starter.monitor.controller.tool;

import com.chua.common.support.image.phnatom.ColoredTankUtils;
import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 图片工具管理
 * @author CH
 * @since 2024/12/28
 */
@RestController
@Slf4j
@Api(tags = "图片工具管理")
@Tag(name = "图片工具管理")
@RequestMapping("/v2/tool/image")
@RequiredArgsConstructor
public class ImageToolController {


    /**
     * phantom
     * @return byte[]
     */
    @PostMapping("phantom")
    @Operation(summary = "幻影图")
    public ReturnResult<byte[]> phantom(MultipartFile[] files) {
        if(files.length != 2) {
            return ReturnResult.error("请上传两个文件");
        }
        try {
            return ReturnResult.success(ColoredTankUtils.generate(
                    files[0].getInputStream(),
                    files[1].getInputStream()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
