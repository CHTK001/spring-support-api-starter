package com.chua.starter.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.signin.model.SignInPointsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到积分流水 Mapper
 *
 * <p>基于 MyBatis-Plus {@link BaseMapper} 提供积分流水的基础 CRUD，
 * 目前仅使用通用方法，无需自定义查询。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Mapper
public interface SignInPointsRecordMapper extends BaseMapper<SignInPointsRecord> {
}