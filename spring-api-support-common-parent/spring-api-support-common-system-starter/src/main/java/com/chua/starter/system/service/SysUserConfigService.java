package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.entity.system.SysUserConfig;

/**
 * 用户个性化配置服务
 *
 * <p>管理用户维度的轻量配置(主题/语言/通知偏好/快捷键等)。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public interface SysUserConfigService extends IService<SysUserConfig> {

    /**
     * 根据主键查询用户配置(快速访问)。
     *
     * @param configId 配置主键
     * @return 用户配置,不存在返回 空
     */
    SysUserConfig getByConfigId(Integer configId);

    /**
     * 根据微信 unionid 查找用户配置。
     *
     * @param unionid 微信 unionid
     * @return 用户配置
     */
    SysUserConfig getByWxUnionid(String unionid);

    /**
     * 根据微信 openid 查找用户配置。
     *
     * @param openid 微信 openid
     * @return 用户配置
     */
    SysUserConfig getByWxOpenid(String openid);

    /**
     * 保存或更新用户配置(upsert)。
     *
     * @param config 配置对象
     * @return 保存后的对象
     */
    SysUserConfig saveOrUpdateConfig(SysUserConfig config);

    /**
     * 解绑微信(openid 与 unionid 置空)。
     *
     * @param configId 配置主键
     * @return 是否成功
     */
    boolean unbindWx(Integer configId);
}
