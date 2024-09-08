package com.chua.starter.system.pojo.event;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.dto.event.ProjectEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件请求
 * @author CH
 * @since 2024/9/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "事件请求")
public class EventRequest extends ProjectEvent {

    /**
     * 事件
     */
    @Schema(description = "事件")
    private String event;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private String sort;

    /**
     * 内容
     */
    @Schema(description = "关键词")
    private String keyword;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Long startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private Long endTime;

    public String getKeyword() {
        if(StringUtils.isBlank(keyword)) {
            if(null == startTime && null == endTime) {
                return "*";
            }

            if(null != startTime && null != endTime) {
                return "timestamp:["+ startTime+"~"+ endTime+"]";
            }

            if(null != startTime) {
                return "timestamp >="+ startTime+"]";
            }
            return "timestamp <="+ endTime+"]";
        }

        if(null == startTime && null == endTime) {
            return keyword;
        }

        if(null != startTime && null != endTime) {
            return keyword + " and timestamp:["+ startTime+"~"+ endTime+"]";
        }

        if(null != startTime) {
            return keyword + " and timestamp >="+ startTime+"]";
        }
        return keyword + " and timestamp <="+ endTime+"]";
    }
}
