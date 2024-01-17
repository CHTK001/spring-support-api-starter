package com.chua.starter.learning.compare;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.properties.FaceCompareProperties;
import com.chua.starter.learning.service.compare.face.FaceCompareService;
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 人脸比对
 * @author CH
 */
@Api(tags = "人脸比对")
@RestController
@RequestMapping("/v1/compare/faceN")
@Slf4j
@Spi("face")
@SpiOption(value = "人脸比对")
public class FaceNCompare implements Compare {

    @Resource
    private FaceCompareService faceService;
    @Resource
    private FaceCompareProperties faceCompareProperties;

    @Operation(summary = "比对")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<List<PredictResult>> reg(
            @RequestPart("file1") @RequestParam("file1") MultipartFile multipartFile1,
            @RequestPart("file2") @RequestParam("file2") MultipartFile[] multipartFile2) {
        List<PredictResult> recognize = new LinkedList<>();
        String cachePath = faceCompareProperties.getCachePath();
        File file1 = FileUtils.createTempFile(cachePath, multipartFile1.getOriginalFilename());
        try (FileOutputStream fileOutputStream1 = new FileOutputStream(file1);
             InputStream is1 = multipartFile1.getInputStream();) {
            IOUtils.copy(is1, fileOutputStream1);
        } catch (Exception ignored) {
        }
        try {
            for (int i = 0; i < multipartFile2.length; i++) {
                MultipartFile multipartFile = multipartFile2[i];
                File file2 = FileUtils.createTempFile(cachePath, multipartFile.getOriginalFilename());
                try (FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                     InputStream is2 = multipartFile.getInputStream();) {
                    IOUtils.copy(is2, fileOutputStream2);
                    List<PredictResult> recognize1 = faceService.recognize(file1, file2);
                    for (PredictResult result : recognize1) {
                        result.setIndex(i);
                    }
                    recognize.addAll(recognize1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return Result.failed("请联系管理员");
                } finally {
                    try {
                        FileUtils.forceDelete(file2);
                    } catch (IOException ignored) {
                    }
                }
            }
        } finally {
            try {
                FileUtils.forceDelete(file1);
            } catch (IOException ignored) {
            }
        }

        recognize.sort(new Comparator<PredictResult>() {
            @Override
            public int compare(PredictResult o1, PredictResult o2) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
        });
        return Result.success(recognize);
    }

}
