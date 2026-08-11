package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.system.model.feedback.FeedbackStatisticV1Response;
import com.chua.starter.system.mapper.SysFeedbackMapper;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import com.chua.starter.system.service.SysFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 反馈服务实现
 *
 * @author CH
 * @since 2024/12/10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements SysFeedbackService {

    /**
     * 反馈处理结果站内消息地址
     */
    private static final String FEEDBACK_MESSAGE_URL = "/message/center";

    /**
     * Socket 消息事件名（与前端 socket 订阅保持一致）
     */
    private static final String SOCKET_EVENT_MESSAGE = "message";

    /**
     * 站内消息模块标识
     */
    private static final String MESSAGE_MODULE = "message";

    /**
     * 站内消息推送事件
     */
    private static final String MESSAGE_PUSH_EVENT = "push";

    /**
     * Socket 会话操作模板（未接入 socket 时为 null，不影响反馈处理主流程）
     */
    @Autowired(required = false)
    private SocketSessionTemplate socketSessionTemplate;

    /**
     * 反馈类型名称映射
     */
    private static final Map<String, String> TYPE_NAME_MAP = new HashMap<>();
    static {
        TYPE_NAME_MAP.put("SUGGESTION", "功能建议");
        TYPE_NAME_MAP.put("BUG", "BUG反馈");
        TYPE_NAME_MAP.put("OTHER", "其他问题");
    }

    @Override
    public ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request) {
        FeedbackV1Request request =
                feedbackV1Request == null ? new FeedbackV1Request() : feedbackV1Request;
        Page<SysFeedback> page = request.createFullPage();
        return PageResultUtils.ok(
                page(
                        page,
                        Wrappers.<SysFeedback>lambdaQuery()
                                .eq(
                                        StringUtils.isNotBlank(request.getSysFeedbackType()),
                                        SysFeedback::getSysFeedbackType,
                                        request.getSysFeedbackType()
                                )
                                .like(
                                        StringUtils.isNotBlank(request.getSysFeedbackContent()),
                                        SysFeedback::getSysFeedbackContent,
                                        request.getSysFeedbackContent()
                                )
                                .like(
                                        StringUtils.isNotBlank(request.getSysFeedbackImages()),
                                        SysFeedback::getSysFeedbackImages,
                                        request.getSysFeedbackImages()
                                )
                                .orderByDesc(SysFeedback::getCreateTime)
                                .orderByDesc(SysFeedback::getSysFeedbackId)
                )
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume) {
        if (request == null || request.getSysFeedbackId() == null) {
            return ReturnResult.illegal("反馈ID不能为空");
        }

        SysFeedback sysFeedback = getById(request.getSysFeedbackId());
        if (sysFeedback == null) {
            return ReturnResult.illegal("反馈不存在");
        }

        if (StringUtils.isBlank(request.getSysFeedbackRecoverContent())) {
            return ReturnResult.illegal("回复内容不能为空");
        }

        sysFeedback.setSysFeedbackStatus(1);
        sysFeedback.setSysFeedbackRecoverContent(request.getSysFeedbackRecoverContent());
        sysFeedback.setSysFeedbackRecoverImages(request.getSysFeedbackRecoverImages());
        sysFeedback.setSysFeedbackRecoverTime(LocalDateTime.now());

        if (userResume != null) {
            sysFeedback.setSysFeedbackDealName(userResume.getUsername());
            if (StringUtils.isNotBlank(userResume.getUserId())) {
                try {
                    sysFeedback.setSysFeedbackDealBy(Integer.parseInt(userResume.getUserId()));
                } catch (NumberFormatException e) {
                    log.warn("反馈处理人ID格式不正确: {}", userResume.getUserId());
                }
            }
            sysFeedback.setUpdateName(userResume.getUsername());
            sysFeedback.setUpdateBy(ObjectUtils.defaultIfNull(sysFeedback.getSysFeedbackDealBy(), sysFeedback.getUpdateBy()));
        }

        boolean updated = updateById(sysFeedback);
        if (!updated) {
            return ReturnResult.of(false);
        }

        pushFeedbackResultMessage(sysFeedback, userResume);
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<FeedbackStatisticV1Response> statistic() {
        log.debug("开始获取反馈统计数据");
        FeedbackStatisticV1Response response = new FeedbackStatisticV1Response();

        try {
            // 获取所有反馈数据
            List<SysFeedback> allFeedbacks = list();

            // 总数统计
            response.setTotal((long) allFeedbacks.size());
            response.setPending(allFeedbacks.stream()
                    .filter(f -> f.getSysFeedbackStatus() == null || f.getSysFeedbackStatus() == 0)
                    .count());
            response.setResolved(allFeedbacks.stream()
                    .filter(f -> f.getSysFeedbackStatus() != null && f.getSysFeedbackStatus() == 1)
                    .count());

            // 类型统计（饼图数据）
            Map<String, Long> typeCountMap = allFeedbacks.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getSysFeedbackType() != null ? f.getSysFeedbackType() : "OTHER",
                            Collectors.counting()
                    ));
            List<FeedbackStatisticV1Response.TypeStatistic> typeStatistics = new ArrayList<>();
            typeCountMap.forEach((type, count) -> {
                FeedbackStatisticV1Response.TypeStatistic stat = new FeedbackStatisticV1Response.TypeStatistic();
                stat.setType(type);
                stat.setName(TYPE_NAME_MAP.getOrDefault(type, type));
                stat.setValue(count);
                typeStatistics.add(stat);
            });
            response.setTypeStatistics(typeStatistics);

            // 趋势统计（最近30天，曲线图数据）
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            List<FeedbackStatisticV1Response.TrendStatistic> trendStatistics = new ArrayList<>();
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(formatter);
                long count = allFeedbacks.stream()
                        .filter(f -> f.getCreateTime() != null &&
                                f.getCreateTime().toLocalDate().equals(date))
                        .count();
                FeedbackStatisticV1Response.TrendStatistic stat = new FeedbackStatisticV1Response.TrendStatistic();
                stat.setDate(dateStr);
                stat.setCount(count);
                trendStatistics.add(stat);
            }
            response.setTrendStatistics(trendStatistics);

            log.debug("反馈统计数据获取成功: 总数={}, 待处理={}, 已处理={}",
                    response.getTotal(), response.getPending(), response.getResolved());
            return ReturnResult.ok(response);
        } catch (Exception e) {
            log.error("获取反馈统计数据失败", e);
            return ReturnResult.error("获取统计数据失败");
        }
    }

    /**
     * 推送反馈处理结果站内通知（通过 socket 推送给反馈提交人）
     *
     * @param sysFeedback 反馈信息
     * @param userResume  当前处理人信息
     */
    private void pushFeedbackResultMessage(SysFeedback sysFeedback, UserResume userResume) {
        Integer receiverId = parseUserId(sysFeedback.getCreateBy());
        if (receiverId == null || receiverId <= 0) {
            return;
        }

        String dealName = userResume != null && StringUtils.isNotBlank(userResume.getUsername())
                ? userResume.getUsername()
                : ObjectUtils.defaultIfNull(sysFeedback.getSysFeedbackDealName(), "系统");
        String typeName = TYPE_NAME_MAP.getOrDefault(sysFeedback.getSysFeedbackType(), "反馈");
        String title = typeName + "已处理";
        String content = String.format(
                "%s 已处理你提交的反馈，处理结果：%s",
                dealName,
                ObjectUtils.defaultIfNull(sysFeedback.getSysFeedbackRecoverContent(), "暂无回复内容")
        );

        pushStationMessage(String.valueOf(receiverId), title, content, sysFeedback.getSysFeedbackId());
    }

    /**
     * 通过 socket 向指定用户推送站内消息
     *
     * @param userId   目标用户ID
     * @param title    消息标题
     * @param content  消息内容
     * @param bizId    业务ID（反馈ID）
     */
    private void pushStationMessage(String userId, String title, String content, Integer bizId) {
        if (socketSessionTemplate == null) {
            log.debug("未接入 socket，跳过站内消息推送: userId={}, title={}", userId, title);
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>(8);
            payload.put("module", MESSAGE_MODULE);
            payload.put("event", MESSAGE_PUSH_EVENT);
            payload.put("title", title);
            payload.put("content", content);
            payload.put("bizType", "feedback");
            payload.put("bizId", String.valueOf(bizId));
            payload.put("url", FEEDBACK_MESSAGE_URL);
            payload.put("sendTime", LocalDateTime.now().toString());
            socketSessionTemplate.sendToUser(userId, SOCKET_EVENT_MESSAGE, Json.toJson(payload));
            log.debug("反馈结果站内消息推送成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("反馈结果站内消息推送失败: userId={}", userId, e);
        }
    }

    private Integer parseUserId(Object rawUserId) {
        String normalizedUserId = rawUserId == null ? "" : String.valueOf(rawUserId).trim();
        if (StringUtils.isBlank(normalizedUserId)) {
            return null;
        }
        try {
            return Integer.parseInt(normalizedUserId);
        } catch (NumberFormatException ex) {
            log.warn("反馈提交人ID格式不正确: {}", normalizedUserId);
            return null;
        }
    }
}
