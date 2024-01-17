package com.chua.starter.learning.style;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.image.filter.ImageFilter;
import com.chua.common.support.spi.ServiceProvider;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 样式
 * @author CH
 */
@Api(tags = "样式")
@RestController
@RequestMapping("/v1/style")
@Slf4j
@Spi("style")
@SpiOption(value = "样式")
public class StandardStyle implements Style{

    @Operation(summary = "样式")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<byte[]> index(@RequestPart("file") @RequestParam("file") MultipartFile multipartFile1,
                                        @RequestParam("style") String style) {
        ImageFilter imageFilter = ServiceProvider.of(ImageFilter.class).getNewExtension(style);
        imageFilter.getImageFormat(FilenameUtils.getExtension(multipartFile1.getOriginalFilename()));
        try (OutputStream outputStream = imageFilter.converter(multipartFile1.getInputStream())) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(((ByteArrayOutputStream)outputStream).toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
