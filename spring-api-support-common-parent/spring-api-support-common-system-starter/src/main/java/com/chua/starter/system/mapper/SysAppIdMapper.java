package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AppId应用标识 Mapper
 *
 * @since 2025/01/05
 * @author CH
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
    void updateUseInfo(@Param("appIdValue") String appIdValue, @Param("lastUseTime") Long lastUseTime);
}

