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
 * @since 2024/12/10
 * @author CH
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
     * 套接字 消息事件名（与前端 套接字 订阅保持一致）
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
     * 套接字 会话操作模板（未接入 套接字 时为 空，不影响反馈处理主流程）
     */
    @Autowired(required = false)
    /**
     * 套接字会话模板
    */
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

    /**
     * 列排序白名单：前端属性名 -> 数据库列名（防 SQL 注入，仅允许下列字段排序）
     */
    private static final Map<String, String> SORT_COLUMN_WHITELIST = new HashMap<>();
    static {
        SORT_COLUMN_WHITELIST.put("sysFeedbackPriority", "sys_feedback_priority");
        SORT_COLUMN_WHITELIST.put("sysFeedbackRecoverTime", "sys_feedback_recover_time");
        SORT_COLUMN_WHITELIST.put("createTime", "create_time");
        SORT_COLUMN_WHITELIST.put("sysFeedbackId", "sys_feedback_id");
    }

    /**
     * 构造完整 ORDER BY：用户点击的列排序（白名单）优先，默认业务排序兜底
     */
    private String buildOrderBy(FeedbackV1Request request) {
        StringBuilder userOrder = new StringBuilder();
        String order = request.getOrder();
        if (StringUtils.isNotBlank(order)) {
            for (String item : order.split(",")) {
                String trimmed = item.trim();
                String direction = null;
                String attr = null;
                if (trimmed.endsWith(" desc")) {
                    direction = "DESC";
                    attr = trimmed.substring(0, trimmed.length() - 5).trim();
                } else if (trimmed.endsWith(" asc")) {
                    direction = "ASC";
                    attr = trimmed.substring(0, trimmed.length() - 4).trim();
                }
                String column = SORT_COLUMN_WHITELIST.get(attr);
                if (null != column && null != direction) {
                    userOrder.append(column).append(' ').append(direction).append(',');
                }
            }
        }
        return "ORDER BY " + userOrder
                + "sys_feedback_status ASC,"
                + "sys_feedback_priority DESC,"
                + "CASE WHEN sys_feedback_type = 'BUG' THEN 0 ELSE 1 END ASC,"
                + "create_time DESC,"
                + "sys_feedback_id DESC";
    }

    @Override
    /**
     * pagefor
    */
    public ReturnPageResult<SysFeedback> pageFor(FeedbackV1Request feedbackV1Request) {
        FeedbackV1Request request =
                feedbackV1Request == null ? new FeedbackV1Request() : feedbackV1Request;
        Page<SysFeedback> page = request.createPage();
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
                                .eq(
                                        null != request.getSysFeedbackPriority(),
                                        SysFeedback::getSysFeedbackPriority,
                                        request.getSysFeedbackPriority()
                                )
                                .eq(
                                        StringUtils.isNotBlank(request.getSysFeedbackPlatform()),
                                        SysFeedback::getSysFeedbackPlatform,
                                        request.getSysFeedbackPlatform()
                                )
                                .ge(
                                        StringUtils.isNotBlank(request.getCreateTimeStart()),
                                        SysFeedback::getCreateTime,
                                        dayStart(request.getCreateTimeStart())
                                )
                                .le(
                                        StringUtils.isNotBlank(request.getCreateTimeEnd()),
                                        SysFeedback::getCreateTime,
                                        dayEnd(request.getCreateTimeEnd())
                                )
                                .ge(
                                        StringUtils.isNotBlank(request.getRecoverTimeStart()),
                                        SysFeedback::getSysFeedbackRecoverTime,
                                        dayStart(request.getRecoverTimeStart())
                                )
                                .le(
                                        StringUtils.isNotBlank(request.getRecoverTimeEnd()),
                                        SysFeedback::getSysFeedbackRecoverTime,
                                        dayEnd(request.getRecoverTimeEnd())
                                )
                                .last(buildOrderBy(request))
                )
        );
    }

    /** 解析 yyyy-MM-dd 为当天起始 00:00:00 */
    private static LocalDateTime dayStart(String day) {
        if (StringUtils.isBlank(day)) {
            return null;
        }
        return LocalDate.parse(day.trim()).atStartOfDay();
    }

    /** 解析 yyyy-MM-dd 为当天结束 23:59:59 */
    private static LocalDateTime dayEnd(String day) {
        if (StringUtils.isBlank(day)) {
            return null;
        }
        return LocalDate.parse(day.trim()).atTime(23, 59, 59);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * Issue
     *
     * @param request 请求
     * @param userResume 用户resume
     * @return issue的结果
     */
    public ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume) {
        if (request == null || request.sysFeedbackId() == null) {
            return ReturnResult.illegal("反馈ID不能为空");
        }

        SysFeedback sysFeedback = getById(request.sysFeedbackId());
        if (sysFeedback == null) {
            return ReturnResult.illegal("反馈不存在");
        }

        if (StringUtils.isBlank(request.sysFeedbackRecoverContent())) {
            return ReturnResult.illegal("回复内容不能为空");
        }

        sysFeedback.setSysFeedbackStatus(1);
        sysFeedback.setSysFeedbackRecoverContent(request.sysFeedbackRecoverContent());
        sysFeedback.setSysFeedbackRecoverImages(request.sysFeedbackRecoverImages());
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
    /**
     * Statistic
    */
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

            // 今日新增统计
            LocalDate today = LocalDate.now();
            response.setTodayCount(allFeedbacks.stream()
                    .filter(f -> f.getCreateTime() != null && f.getCreateTime().toLocalDate().equals(today))
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            List<FeedbackStatisticV1Response.TrendStatistic> trendStatistics = new ArrayList<>();
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(formatter);
                List<SysFeedback> dayFeedbacks = allFeedbacks.stream()
                        .filter(f -> f.getCreateTime() != null &&
                                f.getCreateTime().toLocalDate().equals(date))
                        .collect(Collectors.toList());
                long count = dayFeedbacks.size();
                long pendingCount = dayFeedbacks.stream()
                        .filter(f -> f.getSysFeedbackStatus() == null || f.getSysFeedbackStatus() == 0)
                        .count();
                long resolvedCount = dayFeedbacks.stream()
                        .filter(f -> f.getSysFeedbackStatus() != null && f.getSysFeedbackStatus() == 1)
                        .count();
                FeedbackStatisticV1Response.TrendStatistic stat = new FeedbackStatisticV1Response.TrendStatistic();
                stat.setDate(dateStr);
                stat.setCount(count);
                stat.setPendingCount(pendingCount);
                stat.setResolvedCount(resolvedCount);
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
     * 推送反馈处理结果站内通知（通过 套接字 推送给反馈提交人）
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
     * 通过 套接字 向指定用户推送站内消息
     *
     * @param userId   目标用户标识
     * @param title    消息标题
     * @param content  消息内容
     * @param bizId    业务标识（反馈标识）
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

    /**
     * 解析用户id
     *
     * @param rawUserId raw用户id
     * @return 解析用户id的结果
     */
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
