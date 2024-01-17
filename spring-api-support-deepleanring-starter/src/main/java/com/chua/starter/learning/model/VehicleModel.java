package com.chua.starter.learning.model;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.properties.VehicleProperties;
import com.chua.starter.learning.service.vehicle.VehicleService;
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
 * 车辆检测
 * @author CH
 */
@Api(tags = "车辆检测")
@RestController
@RequestMapping("/v1/vehicle")
@Slf4j
@Spi("vehicle")
@SpiOption(value = "车辆检测")
public class VehicleModel implements Model {

    @Resource
    private VehicleService vehicleService;
    @Resource
    private VehicleProperties vehicleProperties;

    @Operation(summary = "检测")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<List<PredictResult>> reg(@RequestPart("file") @RequestParam("file") MultipartFile multipartFile) {
        List<PredictResult> recognize = null;
        String cachePath = vehicleProperties.getCachePath();
        File file = FileUtils.createTempFile(cachePath, multipartFile.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             InputStream is = multipartFile.getInputStream();){
            IOUtils.copy(is, fileOutputStream);
            recognize = vehicleService.recognize(file);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failed("请联系管理员");
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }
        return Result.success(recognize);
    }

    @Operation(summary = "检测(URL)")
    @PostMapping("url")
    public Result<List<PredictResult>> reg(@RequestParam("file") String url) {
        List<PredictResult> recognize = null;
        String cachePath = vehicleProperties.getCachePath();
        File inFile = UrlUtils.toFile(url, cachePath);
        try {
            recognize = vehicleService.recognize(inFile);
        } finally {
            try {
                FileUtils.forceDelete(inFile);
            } catch (IOException ignored) {
            }
        }
        return Result.success(recognize);
    }

}
