package com.chua.starter.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.signin.model.SignInPointsRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 签到积分规则 Mapper
 *
 * <p>基于 MyBatis-Plus {@link BaseMapper} 提供积分规则的基础 CRUD，
 * 并通过 XML 提供按状态查询启用的规则列表。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Mapper
public interface SignInPointsRuleMapper extends BaseMapper<SignInPointsRule> {

    /**
     * 查询指定状态的所有积分规则，按积分降序排列。
     *
     * @param status 状态：1 启用，0 禁用
     * @return 积分规则列表
     */
    List<SignInPointsRule> selectByStatus(Integer status);
}