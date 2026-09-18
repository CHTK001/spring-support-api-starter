package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.model.cache.SysCacheInfo;

import java.util.List;

/**
 * 缓存管理服务接口
 *
 * <p>提供系统各类缓存的列举与刷新能力，按缓存名清除对应 Redis Key 前缀。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysCacheService {

    /**
     * 缓存列表(固定枚举)
     *
     * @return 缓存信息列表
     */
    List<SysCacheInfo> list();

    /**
     * 刷新指定缓存
     *
     * @param cacheName 缓存名称
     * @return 刷新结果
     */
    ReturnResult<Boolean> refresh(String cacheName);

    /**
     * 刷新全部缓存
     *
     * @return 刷新结果
     */
    ReturnResult<Boolean> refreshAll();
}
