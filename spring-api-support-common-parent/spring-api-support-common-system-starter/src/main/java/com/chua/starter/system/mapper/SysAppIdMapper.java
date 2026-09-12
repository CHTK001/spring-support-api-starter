package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
   * appid应用标识 映射器
 *
 * @since 2025/01/05
 * @author CH
 */
@Mapper
public interface SysAppIdMapper extends BaseMapper<SysAppId> {

    /**
      * 根据 appid值 查询appid
     *
     * @param appIdValue appid值
     * @return AppId
     */
    SysAppId selectByAppIdValue(@Param("appIdValue") String appIdValue);

    /**
     * 更新使用次数和最后使用时间
     *
     * @param appIdValue   appid值
     * @param lastUseTime 最后使用时间
     */
    void updateUseInfo(@Param("appIdValue") String appIdValue, @Param("lastUseTime") Long lastUseTime);
}

