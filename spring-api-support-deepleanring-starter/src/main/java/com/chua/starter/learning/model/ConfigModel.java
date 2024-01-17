package com.chua.starter.learning.model;

import com.chua.common.support.annotations.SpiIgnore;
import com.chua.common.support.image.filter.ImageFilter;
import com.chua.common.support.spi.Option;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.learning.compare.Compare;
import com.chua.starter.learning.pojo.LearningOption;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

/**
 * 基础配置
 *
 * @author CH
 */
@SpiIgnore
@Api(tags = "基础配置")
@RestController
@RequestMapping("/v1/options")
@Slf4j
public class ConfigModel implements Model {

    @PatchMapping
    public Result<LearningOption> options() {
        LearningOption rs = new LearningOption();
        List<Option<String>> model = new LinkedList<>();
        List<Option<String>> optionList = ServiceProvider.of(Model.class).options();
        for (Option<String> stringOption : optionList) {
            if(StringUtils.isEmpty(stringOption.getType()) || "default".equalsIgnoreCase(stringOption.getType())) {
                Class<?> impl = stringOption.getImpl();
                if(null == impl) {
                    continue;
                }
                RequestMapping declaredAnnotation = impl.getDeclaredAnnotation(RequestMapping.class);
                if(null == declaredAnnotation) {
                    continue;
                }
                stringOption.setType(declaredAnnotation.value()[0]);
            }
            model.add(stringOption);
        }
        rs.setModel(model);
        return Result.success(rs);
    }
    @PatchMapping("compare")
    public Result<LearningOption> compare() {
        LearningOption rs = new LearningOption();
        List<Option<String>> model = new LinkedList<>();
        List<Option<String>> optionList = ServiceProvider.of(Compare.class).options();
        for (Option<String> stringOption : optionList) {
            if(StringUtils.isEmpty(stringOption.getType()) || "default".equalsIgnoreCase(stringOption.getType())) {
                Class<?> impl = stringOption.getImpl();
                if(null == impl) {
                    continue;
                }
                RequestMapping declaredAnnotation = impl.getDeclaredAnnotation(RequestMapping.class);
                if(null == declaredAnnotation) {
                    continue;
                }
                stringOption.setType(declaredAnnotation.value()[0]);
            }
            model.add(stringOption);
        }
        rs.setModel(model);
        return Result.success(rs);
    }
    @PatchMapping("style")
    public Result<LearningOption> style() {
        LearningOption rs = new LearningOption();
        rs.setModel(ServiceProvider.of(ImageFilter.class).options());
        return Result.success(rs);
    }
}
