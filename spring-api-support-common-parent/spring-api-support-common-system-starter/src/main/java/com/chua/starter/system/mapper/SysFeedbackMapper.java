package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户反馈 映射器
 *
 * @since 2026/08/03
 * @author CH
 */
@Mapper
public interface SysFeedbackMapper extends BaseMapper<SysFeedback> {
    /**
     * 分页查询反馈记录
     *
     * @param page              分页对象
     * @param feedbackV1Request 查询请求
     * @return 分页后的反馈记录
     */
    IPage<SysFeedback> pageFor(@Param("page") Page<SysFeedback> page, @Param("request") FeedbackV1Request feedbackV1Request);
}