package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.model.feedback.FeedbackStatisticV1Response;
import com.chua.starter.system.mapper.SysFeedbackMapper;
import com.chua.starter.system.pojo.feedback.FeedbackIssueV1Request;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import com.chua.starter.system.service.SysFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements SysFeedbackService {

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
        return null;
    }

    @Override
    public ReturnResult<Boolean> issue(FeedbackIssueV1Request request, UserResume userResume) {
        return null;
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
}
