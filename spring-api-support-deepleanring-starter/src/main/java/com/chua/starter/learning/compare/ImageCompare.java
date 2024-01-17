package com.chua.starter.learning.compare;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.properties.ImageCompareProperties;
import com.chua.starter.learning.service.compare.image.ImageCompareService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 图片比对
 * @author CH
 */
@Api(tags = "图片比对")
@RestController("ImageCompareV1")
@RequestMapping("/v1/compare/image")
@Slf4j
@Spi("image")
@SpiOption(value = "图片比对")
public class ImageCompare implements Compare {

    @Resource
    private ImageCompareService imageService;
    @Resource
    private ImageCompareProperties imageProperties;

    @Operation(summary = "比对")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<List<PredictResult>> reg(
            @RequestPart("file1") @RequestParam("file1") MultipartFile multipartFile1,
            @RequestPart("file2") @RequestParam("file2") MultipartFile multipartFile2) {
        List<PredictResult> recognize = null;
        String cachePath = imageProperties.getCachePath();
        File file1 = FileUtils.createTempFile(cachePath, multipartFile1.getOriginalFilename());
        File file2 = FileUtils.createTempFile(cachePath, multipartFile2.getOriginalFilename());
        try (FileOutputStream fileOutputStream1 = new FileOutputStream(file1);
             InputStream is1 = multipartFile1.getInputStream();
             FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
             InputStream is2 = multipartFile2.getInputStream();){
            IOUtils.copy(is1, fileOutputStream1);
            IOUtils.copy(is2, fileOutputStream2);
            recognize = imageService.recognize(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failed("请联系管理员");
        } finally{
            try {
                FileUtils.forceDelete(file1);
            } catch (IOException ignored) {
            }
            try {
                FileUtils.forceDelete(file2);
            } catch (IOException ignored) {
            }
        }
        return Result.success(recognize);
    }

    @Operation(summary = "比对(URL)")
    @PostMapping("url")
    public Result<List<PredictResult>> url(@RequestParam("file1") String file1,
                                           @RequestParam("file2") String file2) {
        List<PredictResult> recognize = null;
        String cachePath = imageProperties.getCachePath();
        File inFile1 = UrlUtils.toFile(file1, cachePath);
        File inFile2 = UrlUtils.toFile(file2, cachePath);
        try {
            recognize = imageService.recognize(inFile1, inFile2);
        } finally {
            try {
                FileUtils.forceDelete(inFile1);
            } catch (IOException ignored) {
            }
            try {
                FileUtils.forceDelete(inFile2);
            } catch (IOException ignored) {
            }
        }
        return Result.success(recognize);
    }
}
