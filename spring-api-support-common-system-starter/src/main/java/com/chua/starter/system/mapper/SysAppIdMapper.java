package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * AppId应用标识 Mapper
 *
 * @author CH
 * @since 2025/01/05
 */
@Mapper
public interface SysAppIdMapper extends BaseMapper<SysAppId> {

    /**
     * 根据 appId值 查询AppId
     *
     * @param appIdValue AppId值
     * @return AppId
     */
    SysAppId selectByAppIdValue(@Param("appIdValue") String appIdValue);

    /**
     * 更新使用次数和最后使用时间
     *
     * @param appIdValue   AppId值
     * @param lastUseTime 最后使用时间
     */
    @Update("UPDATE app_id SET app_id_use_count = app_id_use_count + 1, " +
            "app_id_last_use_time = #{lastUseTime} WHERE app_id_value = #{appIdValue} AND delete_flag = 0")
    void updateUseInfo(@Param("appIdValue") String appIdValue, @Param("lastUseTime") Long lastUseTime);
}

