package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysAppKey;

/**
* 应用密钥 服务
*
* @since 2025/10/23
* @author CH
 */
public interface SysAppKeyService extends IService<SysAppKey> {

    /**
    * 根据 app键(密钥) 查询应用密钥
    * <p>类似大模型的认证方式，直接通过密钥查询</p>
    *
    * @param appKey 应用密钥(API 键)
    * @return 应用密钥
     */
    SysAppKey getByAppKey(String appKey);

    /**
    * 验证 app键 和 secret
    *
    * @param appKey 应用密钥
    * @param secret 应用密钥（与app键相同，用于兼容）
    * @return 是否验证通过
     */
    boolean verify(String appKey, String secret);

    /**
    * 验证 app键、secret 和 IP
    *
    * @param appKey 应用密钥
    * @param secret 应用密钥（与app键相同，用于兼容）
    * @param ip     客户端IP
    * @return 是否验证通过
     */
    boolean verify(String appKey, String secret, String ip);

    /**
    * 判断应用密钥是否已过期
    *
    * @param appKey 应用密钥实体
    * @return 是否已过期
     */
    boolean isExpired(SysAppKey appKey);

    /**
    * 解析应用密钥实际生效的过期时间（秒）
    *
    * @param appKey 应用密钥实体
    * @return 过期时间戳（秒），不限时返回 空
     */
    Long resolveExpireTime(SysAppKey appKey);

    /**
    * 更新使用次数和最后使用时间（通过app键值）
    *
    * @param appKey 应用密钥（sys_app_键_secret）
     */
    void updateUseInfoByAppKey(String appKey);
}
