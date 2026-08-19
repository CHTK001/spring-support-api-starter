package com.chua.starter.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.achievement.model.UserAchievement;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 用户成就记录 Mapper
 *
 * @since 2026-08-07
 * @author CH
 */
@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    /**
     * 查询指定用户在指定分组下的已解锁成就记录。
     *
     * @param userId 用户ID
     * @param type   分组标识
     * @return 用户成就记录，不存在返回 null
     */
    UserAchievement selectByUserIdAndType(Long userId, String type);

    /**
     * 查询指定用户所有已解锁的成就分组及最高等级。
     *
     * @param userId 用户ID
     * @return 以 type 为键、maxLevel 为值的映射
     */
    @MapKey("type")
    Map<String, Integer> selectMaxLevelByUserId(Long userId);

    /**
     * 批量保存或更新用户成就记录。
     *
     * @param records 用户成就记录列表
     * @return 影响行数
     */
    int batchUpsert(List<UserAchievement> records);
}
