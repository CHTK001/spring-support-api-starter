package com.chua.starter.system.pojo.tenant;

import com.chua.common.support.constant.Action;
import com.chua.starter.system.entity.SysTenant;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 同步租户
 *
 * @author CH
 */
@Data
@AllArgsConstructor
public class SyncTenantData {

    private Action action;

    private SysTenant tenant;
}
