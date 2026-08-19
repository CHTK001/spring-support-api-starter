package com.chua.starter.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.achievement.model.AchievementTier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 成就段位定义 Mapper
 *
 * @since 2026-08-07
 * @author CH
 */
@Mapper
public interface AchievementTierMapper extends BaseMapper<AchievementTier> {

    /**
     * 查询指定分组下所有段位定义，按排序和等级升序排列。
     *
     * @param type 分组标识
     * @return 段位定义列表
     */
    List<AchievementTier> selectByTypeOrderBySortAndLevel(String type);
}
