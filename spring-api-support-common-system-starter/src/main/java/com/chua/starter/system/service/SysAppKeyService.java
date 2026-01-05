package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysAppKey;

/**
 * 应用密钥 Service
 *
 * @author CH
 * @since 2025/10/23
 */
public interface SysAppKeyService extends IService<SysAppKey> {

    /**
     * 根据 appKey(密钥) 查询应用密钥
     * <p>类似大模型的认证方式，直接通过密钥查询</p>
     *
     * @param appKey 应用密钥(API Key)
     * @return 应用密钥
     */
    SysAppKey getByAppKey(String appKey);

    /**
     * 验证 appKey 和 secret
     *
     * @param appKey 应用密钥
     * @param secret 应用密钥（与appKey相同，用于兼容）
     * @return 是否验证通过
     */
    boolean verify(String appKey, String secret);

    /**
     * 验证 appKey、secret 和 IP
     *
     * @param appKey 应用密钥
     * @param secret 应用密钥（与appKey相同，用于兼容）
     * @param ip     客户端IP
     * @return 是否验证通过
     */
    boolean verify(String appKey, String secret, String ip);

    /**
     * 更新使用次数和最后使用时间（通过appKey值）
     *
     * @param appKey 应用密钥（sys_app_key_secret）
     */
    void updateUseInfoByAppKey(String appKey);
}
