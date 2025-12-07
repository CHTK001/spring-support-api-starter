package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 应用密钥Mapper
 *
 * @author CH
 * @since 2025/10/23
 */
@Mapper
public interface SysAppKeyMapper extends BaseMapper<SysAppKey> {

    /**
     * 根据 appId 查询应用密钥
     *
     * @param appId 应用ID
     * @return 应用密钥
     */
    SysAppKey selectByAppId(@Param("appId") String appId);

    /**
     * 更新使用次数和最后使用时间
     *
     * @param appId        应用ID
     * @param lastUsedTime 最后使用时间
     */
    @Update("UPDATE sys_app_key SET sys_app_key_use_count = sys_app_key_use_count + 1, " +
            "sys_app_key_last_used_time = #{lastUsedTime} WHERE sys_app_key_app_id = #{appId}")
    void updateUseInfo(@Param("appId") String appId, @Param("lastUsedTime") Long lastUsedTime);
}

