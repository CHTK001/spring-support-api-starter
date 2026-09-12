package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAppKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* 应用密钥 映射器
*
* @since 2025/10/23
* @author CH
 */
@Mapper
public interface SysAppKeyMapper extends BaseMapper<SysAppKey> {

    /**
    * 根据 app键(密钥) 查询应用密钥
    *
    * @param appKey 应用密钥
    * @return 应用密钥
     */
    SysAppKey selectByAppKey(@Param("appKey") String appKey);

    /**
    * 更新使用次数和最后使用时间（通过app键值）
    *
    * @param appKey       应用密钥（sys_app_键_secret）
    * @param lastUsedTime 最后使用时间
     */
    void updateUseInfoByAppKey(@Param("appKey") String appKey, @Param("lastUsedTime") Long lastUsedTime);
}
