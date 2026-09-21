package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 管理员发送消息请求
 *
 * @param title 消息标题
 * @param content 消息内容
 * @param type 消息类型
 * @param level 消息级别
 * @param url 跳转链接
 * @param targetUserIds 目标用户ID列表(为空则广播给所有用户)
 *
 * @since 4.0.0.43
 * @author CH
 */
@Schema(description = "管理员发送消息请求")
public record MessageBroadcastRequest(
        @Schema(description = "消息标题") String title,
        @Schema(description = "消息内容") String content,
        @Schema(description = "消息类型") String type,
        @Schema(description = "消息级别") String level,
        @Schema(description = "跳转链接") String url,
        @Schema(description = "目标用户ID列表(为空则广播给所有用户)") List<Integer> targetUserIds
) {
}
