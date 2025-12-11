package com.chua.starter.oauth.sso.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysAppKey;

/**
 * 应用密钥Service
 *
 * @author CH
 * @since 2025/10/23
 */
public interface SysAppKeyService extends IService<SysAppKey> {

    /**
     * 根据 appId 查询应用密钥
     *
     * @param appId 应用ID
     * @return 应用密钥
     */
    SysAppKey getByAppId(String appId);

    /**
     * 验证 appId 和 secret
     *
     * @param appId  应用ID
     * @param secret 应用密钥
     * @return 是否验证通过
     */
    boolean verify(String appId, String secret);

    /**
     * 验证 appId、secret 和 IP
     *
     * @param appId  应用ID
     * @param secret 应用密钥
     * @param ip     客户端IP
     * @return 是否验证通过
     */
    boolean verify(String appId, String secret, String ip);

    /**
     * 更新使用次数和最后使用时间
     *
     * @param appId 应用ID
     */
    void updateUseInfo(String appId);
}

