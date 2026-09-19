package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理员发送消息请求
 *
 * @since 4.0.0.43
 * @author CH
 */
@Data
@Schema(description = "管理员发送消息请求")
public class MessageBroadcastRequest {

    /**
     * 消息标题
    */
    @Schema(description = "消息标题")
    private String title;

    /**
     * 消息内容
    */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息类型
    */
    @Schema(description = "消息类型")
    private String type;

    /**
     * 消息级别
    */
    @Schema(description = "消息级别")
    private String level;

    /**
     * 跳转链接
    */
    @Schema(description = "跳转链接")
    private String url;

    /**
     * 目标用户ID列表(为空则广播给所有用户)
    */
    @Schema(description = "目标用户ID列表(为空则广播给所有用户)")
    private List<Integer> targetUserIds;
}
