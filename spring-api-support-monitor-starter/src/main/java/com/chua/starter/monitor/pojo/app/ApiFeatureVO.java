package com.chua.starter.monitor.pojo.app;

import com.chua.common.support.core.utils.MapUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API 功能开关信息 VO
 *
 * @author CH
 * @since 2024/12/08
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiFeatureVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 功能标识
     */
    private String featureId;

    /**
     * 功能描述
     */
    private String description;

    /**
     * 功能分组
     */
    private String group;

    /**
     * 默认是否启用
     */
    private boolean defaultEnabled;

    /**
     * 当前是否启用
     */
    private boolean enabled;

    /**
     * 关闭时的响应消息
     */
    private String disabledMessage;

    /**
     * 关闭时的响应状态码
     */
    private int disabledStatus;

    /**
     * 所属类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 接口路径
     */
    private Set<String> patterns;

    /**
     * 从 Map 转换为 ApiFeatureVO
     *
     * @param map 数据 Map
     * @return ApiFeatureVO 对象
     */
    public static ApiFeatureVO fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        List<String> patternList = MapUtils.getList(map, "patterns");
        Set<String> patterns = patternList != null ? new HashSet<>(patternList) : null;

        return ApiFeatureVO.builder()
                .featureId(MapUtils.getString(map, "featureId"))
                .description(MapUtils.getString(map, "description"))
                .group(MapUtils.getString(map, "group"))
                .defaultEnabled(MapUtils.getBoolean(map, "defaultEnabled", false))
                .enabled(MapUtils.getBoolean(map, "enabled", false))
                .disabledMessage(MapUtils.getString(map, "disabledMessage"))
                .disabledStatus(MapUtils.getInteger(map, "disabledStatus", 503))
                .className(MapUtils.getString(map, "className"))
                .methodName(MapUtils.getString(map, "methodName"))
                .patterns(patterns)
                .build();
    }
}
