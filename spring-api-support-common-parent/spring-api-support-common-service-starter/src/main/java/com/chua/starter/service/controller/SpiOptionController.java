package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.definition.ServiceDefinition;
import com.chua.common.support.spi.definition.SpiOption;
import com.chua.common.support.utils.ArrayUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.api.properties.ApiProperties;
import com.chua.starter.common.support.properties.OptionalProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.chua.starter.common.support.constant.CacheConstant.CACHE_MANAGER_FOR_SYSTEM;
import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
* SPI 选项查询接口
* <p>
* 前端传入 SPI 类型（类名或 {@code plugin.api.spi.mapping} 配置的别名），
* 返回该 SPI 的全部实现选项，供下拉选择使用。
* </p>
*
* @since 2026/08/03
* @author CH
 */
@RestController
@RequestMapping("/v1/option")
@ConditionalOnProperty(prefix = OptionalProperties.PRE, name = "enable", havingValue = "true", matchIfMissing = true)
@Tag(name = "选项")
public class SpiOptionController {

    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(SpiOptionController.class);

    /** API属性 */
    private final ApiProperties apiProperties;

    /**
    * 构造函数
    *
    * @param apiProperties API 配置
     */
    public SpiOptionController(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    /**
    * 获取已经加载的选项
    *
    * @param type 类型
    * @param name 名称过滤（可选）
    * @return 选项列表
    * @param false false
     */
    @Operation(summary = "获取已经加载选项")
    @GetMapping("objects/get")
    @Cacheable(cacheManager = CACHE_MANAGER_FOR_SYSTEM, cacheNames = REDIS_CACHE_ALWAYS, key = "'OPTION:OBJECT_GET' + #type + '' + #name")
    public ReturnResult<List<SpiOption>> objects(String type, @RequestParam(required = false) String name) {
        if (StringUtils.isBlank(type)) {
            return ReturnResult.error("参数不能为空");
        }

        type = StringUtils.utf8Str(type);
        List<?> rawOptions = ServiceProvider.of(getType(type)).options();
        List<SpiOption> options = rawOptions.stream().map(item -> {
            if (item instanceof SpiOption spiOption) {
                return spiOption;
            }
            if (item instanceof ServiceDefinition definition) {
                return SpiOption.of(definition.getName(), definition.getDescribe(), definition.getSupportedTypes());
            }
            return SpiOption.of(String.valueOf(item), null, null);
        }).toList();
        if (!StringUtils.isBlank(name)) {
            for (SpiOption option : options) {
                String optionName = option.name();
                if (StringUtils.equalsIgnoreCase(optionName, name)) {
                    return ReturnResult.ok(Collections.singletonList(option));
                }
            }
        }

        return ReturnResult.ok(options);
    }

    /**
    * 获取选项（多类型展开）
    *
    * @param type 类型
    * @return 类型数组
     */
    private String[] getTypes(String type) {
        ApiProperties.SpiConfig spi = apiProperties.getSpi();
        if (spi != null && spi.isEnable() && spi.getMapping() != null) {
            Object obj = spi.getMapping().get(type);
            if (obj instanceof String[] strings) {
                return strings;
            }
            if (obj instanceof String str) {
                return str.split(",");
            }
            return new String[]{type};
        }
        return new String[]{type};
    }

    /**
    * 获取类型映射后的真实 SPI 类型
    *
    * @param type 前端传入类型
    * @return 真实 SPI 类型
     */
    private String getType(String type) {
        ApiProperties.SpiConfig spi = apiProperties.getSpi();
        if (spi != null && spi.isEnable() && spi.getMapping() != null) {
            return MapUtils.getString(spi.getMapping(), type);
        }
        return type;
    }

    /**
    * 获取选项
    *
    * @param type 类型
    * @param name 名称过滤（可选）
    * @return 选项列表
    * @param false false
     */
    @Operation(summary = "获取选项")
    @GetMapping("get")
    @Cacheable(cacheManager = CACHE_MANAGER_FOR_SYSTEM, cacheNames = REDIS_CACHE_ALWAYS, key = "'OPTION:GET' + #type + '' + #name")
    public ReturnResult<List<SpiOption>> get(String type, @RequestParam(required = false) String name) {
        if (StringUtils.isBlank(type)) {
            return ReturnResult.error("参数不能为空");
        }

        type = StringUtils.utf8Str(type);
        List<?> rawOptions = ServiceProvider.of(getType(type)).options();
        List<SpiOption> options = rawOptions.stream().map(item -> {
            if (item instanceof SpiOption spiOption) {
                return spiOption;
            }
            if (item instanceof ServiceDefinition definition) {
                return SpiOption.of(definition.getName(), definition.getDescribe(), definition.getSupportedTypes());
            }
            return SpiOption.of(String.valueOf(item), null, null);
        }).toList();
        if (!StringUtils.isBlank(name)) {
            List<SpiOption> collect = options.stream().filter(spiOption -> {
                String[] supportedTypes = spiOption.supportedTypes();
                String optionName = spiOption.name();
                return ArrayUtils.containsIgnoreCase(supportedTypes, name)
                        || StringUtils.containsIgnoreCase(optionName, name);
            }).toList();
            return ReturnResult.ok(collect);
        }

        return ReturnResult.ok(options);
    }

    /**
    * 获取选项（多类型分组）
    *
    * @param type 类型，逗号分隔
    * @param name 名称过滤（可选）
    * @return 类型到选项列表的映射
    * @param false false
     */
    @Operation(summary = "获取选项")
    @GetMapping("list")
    @Cacheable(cacheManager = CACHE_MANAGER_FOR_SYSTEM, cacheNames = REDIS_CACHE_ALWAYS, key = "'OPTION:LIST' + #type + '' + #name")
    public ReturnResult<Map<String, List<SpiOption>>> list(String type, @RequestParam(required = false) String name) {
        String[] split = StringUtils.utf8Str(type).split(",");
        Map<String, List<SpiOption>> map = MapUtils.newHashMap();
        for (String s : split) {
            String[] types = getTypes(s);
            for (String item : types) {
                List<SpiOption> options = (List<SpiOption>) (List<?>) ServiceProvider.of(item).options();
                if (!StringUtils.isBlank(name)) {
                    for (SpiOption option : options) {
                        String optionName = option.name();
                        if (StringUtils.equalsIgnoreCase(optionName, name)) {
                            map.put(s, Collections.singletonList(option));
                            break;
                        }
                    }
                    continue;
                }
                map.computeIfAbsent(s, it -> new LinkedList<>()).addAll(options);
            }
        }

        return ReturnResult.ok(map);
    }
}
