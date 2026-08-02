package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 应用密钥 Mapper
 *
 * @author CH
 * @since 2025/10/23
 */
@Mapper
public interface SysAppKeyMapper extends BaseMapper<SysAppKey> {

    /**
     * 根据 appKey(密钥) 查询应用密钥
     *
     * @param appKey 应用密钥
     * @return 应用密钥
     */
    SysAppKey selectByAppKey(@Param("appKey") String appKey);

    /**
     * 更新使用次数和最后使用时间（通过appKey值）
     *
     * @param appKey       应用密钥（sys_app_key_secret）
     * @param lastUsedTime 最后使用时间
     */
    @Update("UPDATE sys_app_key " +
            "SET sys_app_key_use_count = sys_app_key_use_count + 1, " +
            "sys_app_key_last_used_time = #{lastUsedTime} " +
            "WHERE sys_app_key_secret = #{appKey} AND sys_app_key_delete = 0")
    void updateUseInfoByAppKey(@Param("appKey") String appKey, @Param("lastUsedTime") Long lastUsedTime);
}
