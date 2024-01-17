package com.chua.starter.learning.compare;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.service.compare.text.TextCompareService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

;

/**
 * 文本比对
 * @author CH
 */
@Api(tags = "文本比对")
@RestController
@RequestMapping("/v1/compare/text")
@Slf4j
@Spi("text")
@SpiOption(value = "文本比对")
public class TextCompare implements Compare {

    @Resource
    private TextCompareService textService;

    @Operation(summary = "比对")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<List<PredictResult>> reg(
            @RequestPart("text1") @RequestParam("text1") String multipartFile1,
            @RequestPart("text2") @RequestParam("text2") String multipartFile2) {
        return Result.success(textService.recognize(multipartFile1, multipartFile2));
    }

}
