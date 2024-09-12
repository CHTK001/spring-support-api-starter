package com.chua.starter.system.pojo.event;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.dto.event.ProjectEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

/**
 * 事件请求
 *
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
     * 动作
     */
    @Schema(description = "动作")
    private String action;
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

    public String getEvent() {
        return null == event ? null : event.replaceAll("'", "");
    }

    public String getKeyword() {
        List<String> line = new LinkedList<>();
        if (StringUtils.isNotBlank(keyword)) {
            line.add("text:" + keyword);
        }


         if (null != startTime && null != endTime) {
            line.add("timestamp:[" + startTime + "~" + endTime + "]");
        } else if (null != startTime) {
            line.add("timestamp >=" + startTime + "]");
        } else if(null != endTime) {
            line.add("timestamp <=" + endTime + "]");
        }

        if(StringUtils.isNotBlank(action)) {
            line.add("event:" + action);
        }

        line.add("type:" +  getEvent());

        return StringUtils.join(line, " AND ");
    }
}
