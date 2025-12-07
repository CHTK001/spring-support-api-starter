package com.chua.starter.system.mapper;

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
     * 鍒嗛〉鏌ヨ
     *
     * @param page              鍒嗛〉瀵硅薄
     * @param feedbackV1Request 璇锋眰
     * @return 缁撴灉
     */
    IPage<SysFeedback> pageFor(@Param("page") Page<SysFeedback> page, @Param("request") FeedbackV1Request feedbackV1Request);
}