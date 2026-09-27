package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 规范构造器：目标用户ID列表做防御性拷贝。
     *
     * <p>value class 前置条件——集合组件必须深不可变。
     * 该列表为 null 时表示广播给所有用户（见 {@code SysMessageServiceImpl#broadcast}），
     * 故保留 null 语义；元素按可含 null 的安全写法包装，避免 {@code List.copyOf} 抛 NPE。</p>
     *
     * @param targetUserIds 目标用户ID列表(为空则广播给所有用户)
     */
    public MessageBroadcastRequest {
        targetUserIds = targetUserIds == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(targetUserIds));
    }
}
