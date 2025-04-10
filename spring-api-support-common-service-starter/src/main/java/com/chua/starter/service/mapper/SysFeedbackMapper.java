package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysFeedback;
import com.chua.starter.system.pojo.feedback.FeedbackV1Request;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysFeedbackMapper extends BaseMapper<SysFeedback> {
    /**
     * 分页查询
     *
     * @param page              分页对象
     * @param feedbackV1Request 请求
     * @return 结果
     */
    IPage<SysFeedback> pageFor(@Param("page") Page<SysFeedback> page, @Param("request") FeedbackV1Request feedbackV1Request);
}