package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.task.message.MessageResponse;
import com.chua.starter.system.pojo.message.MessageContentSendRequest;
import com.chua.starter.system.pojo.message.MessageSendRequest;
import com.chua.starter.system.pojo.message.SendByCodeRequest;

import java.util.List;
import java.util.Map;

/**
 * 消息推送服务接口
 *
 * <p>模板自带渠道(EMAIL/SMS)，账号凭据统一从 sys_setting(smtp/sms) 读取，
 * 发送结果落到 {@code sys_message_log}，并在站内消息中心 {@code sys_message} 留痕。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysMessagePushService {

    /**
     * 按模板ID发送消息
     *
     * @param request 发送请求(模板 + 接收人 + 变量)
     * @return 每个接收人的发送结果
     */
    ReturnResult<List<MessageResponse>> send(MessageSendRequest request);

    /**
     * 按模板编码发送消息(前端发送弹窗使用)
     *
     * @param request 发送请求(模板编码 + 单个接收人 + 变量)
     * @return 每个接收人的发送结果
     */
    ReturnResult<List<MessageResponse>> sendByCode(SendByCodeRequest request);

    /**
     * 按内容发送消息
     *
     * @param request 发送请求(渠道 + 接收人 + 内容)
     * @return 每个接收人的发送结果
     */
    ReturnResult<List<MessageResponse>> sendContent(MessageContentSendRequest request);

    /**
     * 从运营商/渠道侧同步已审核模板到本地模板表(按模板编码新增或覆盖)
     *
     * @param channel 渠道: EMAIL/SMS，为空默认 SMS
     * @return 同步条数，渠道不支持模板列表时返回 0
     */
    ReturnResult<Integer> syncTemplates(String channel);

    /**
     * 查询某条站内消息的送达/已读统计
     *
     * @param sysMessageId 站内消息ID
     * @return total/sent/failed/read/unread 与送达/已读名单
     */
    ReturnResult<Map<String, Object>> recipientStats(Long sysMessageId);
}
