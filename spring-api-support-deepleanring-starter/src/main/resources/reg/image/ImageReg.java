package com.chua.starter.learning.reg.image;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.elasticsearch.support.service.DocumentService;
import com.chua.starter.learning.properties.ImageRegProperties;
import com.chua.starter.learning.reg.entity.SysFace;
import com.chua.starter.learning.reg.image.service.ImageRegService;
import com.chua.starter.learning.reg.query.IndexQuery;
import com.chua.starter.learning.reg.query.LibraryQuery;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 图像识别
 *
 * @author CH
 */
@Api(tags = "图像识别")
@RestController
@RequestMapping("/v1/reg/image")
@Slf4j
@Spi("reg")
@AllArgsConstructor
@SpiOption(value = "图像识别")
public class ImageReg {


    @Resource
    private ImageRegService imageRegService;

    @Resource
    private DocumentService documentService;

    @Resource
    private ImageRegProperties imageRegProperties;

    /**
     * 图像库刪除
     *
     * @return 响应
     */
    @Operation(summary = "图像库刪除")
    @GetMapping(value = "delete")
    public Result<Boolean> delete(String indexName, String code) {
        if (null == code || null == indexName) {
            return Result.failed("索引不能为空");
        }
        return Result.success(documentService.deleteDocument(indexName, code));
    }

    /**
     * 图像库入库
     *
     * @return 响应
     */
    @Operation(summary = "以图搜图")
    @PostMapping(value = "image")
    public ReturnPageResult<SysFace> image(IndexQuery query, MultipartFile files) {
        if (null == files && Strings.isNullOrEmpty(query.getUrl())) {
            return ReturnPageResult.illegal("图片不能为空");
        }
        Result<float[]> feature = null;
        if (null != files) {
            try {
                feature = imageRegService.feature(files.getOriginalFilename(), files.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                feature = imageRegService.feature(files.getOriginalFilename(), new FileInputStream(UrlUtils.toFile(query.getUrl(), imageRegProperties.getCachePath())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        if (feature.getData() == null) {
            return ReturnPageResult.illegal(feature.getMsg());
        }
        return ReturnPageResult.ok(documentService.queryDocument(feature.getData(), query.getIndexName(),
                query.getKeyword(),
                SysFace.class,
                query.getPage(),
                query.getPageSize()));
    }
    /**
     * 人脸库入库
     *
     * @return 响应
     */
    @Operation(summary = "以图搜图(URL)")
    @PostMapping(value = "image/url")
    public ReturnPageResult<SysFace> image(@RequestBody IndexQuery query) {
        if (Strings.isNullOrEmpty(query.getUrl())) {
            return ReturnPageResult.illegal("图片不能为空");
        }
        Result<float[]> feature = null;
        File file = UrlUtils.toFile(query.getUrl(), imageRegProperties.getCachePath());
        try {
            feature = imageRegService.feature(UUID.randomUUID().toString(), file);
        } finally {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException ignored) {
            }
        }

        if (feature.getData() == null) {
            return ReturnPageResult.illegal(feature.getMsg());
        }
        return ReturnPageResult.ok(documentService.queryDocument(feature.getData(), query.getIndexName(),
                query.getKeyword(),
                SysFace.class,
                query.getPage(),
                query.getPageSize()));
    }
    /**
     * 图像库入库
     *
     * @return 响应
     */
    @Operation(summary = "图像库信息查询")
    @PostMapping(value = "list")
    public ReturnPageResult<SysFace> list(@RequestBody IndexQuery query) {
        if (null == query.getIndexName()) {
            return ReturnPageResult.illegal("索引不能为空");
        }
        return ReturnPageResult.ok(documentService.queryDocument(query.getIndexName(),
                query.getKeyword(),
                SysFace.class,
                query.getPage(),
                query.getPageSize()));
    }

    /**
     * 图像库入库
     *
     * @return 响应
     */
    @Operation(summary = "图像库入库")
    @PostMapping(value = "libraryFile")
    public Result<PredictResult> libraryFile(String indexName, MultipartFile[] files, @Valid SysFace sysFace, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if (null == files || files.length == 0) {
            return Result.failed("文件不能为空");
        }
        try {
            if(files.length > 1) {
                for (MultipartFile file : files) {
                    SysFace sysFace1 = new SysFace();
                    sysFace1.setCode(FilenameUtils.getBaseName(file.getOriginalFilename()));
                    sysFace1.setName(FilenameUtils.getBaseName(file.getOriginalFilename()));
                    imageRegService.analysis(indexName, sysFace1, file.getOriginalFilename(), file.getInputStream());
                }
                return Result.success();
            }
            MultipartFile file = files[0];
            return imageRegService.analysis(indexName, sysFace, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            return Result.failed("文件解析失败");
        }
    }

    /**
     * 图像库入库
     *
     * @return 响应
     */
    @Operation(summary = "图像库入库")
    @PostMapping(value = "library")
    public Result<PredictResult> register(@RequestBody LibraryQuery query, @Valid SysFace sysFace, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if (Strings.isNullOrEmpty(query.getUrl())) {
            return Result.failed("索引/图片不能为空");
        }
        String cachePath = imageRegProperties.getCachePath();
        try {
            return imageRegService.analysis(query.getIndexName(), sysFace, IdUtils.createTimeId() + ".jpg", new FileInputStream(UrlUtils.toFile(query.getUrl(), cachePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failed("远程文件解析失败");
        }
    }
}
