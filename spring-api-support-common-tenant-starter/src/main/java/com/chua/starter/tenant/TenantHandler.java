package com.chua.starter.tenant;

import com.chua.starter.system.entity.SysTenant;

/**
 * 租户处理器
 *
 * @author CH
 */
public interface TenantHandler {
    /**
     * 保存或者更新
     *
     * @param tenant 租户
     */
    void saveOrUpdate(SysTenant tenant);

    /**
     * 删除
     *
     * @param tenant 租户
     */
    void delete(SysTenant tenant);
}
