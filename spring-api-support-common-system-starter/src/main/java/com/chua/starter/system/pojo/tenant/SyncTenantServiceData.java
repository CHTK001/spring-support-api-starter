package com.chua.starter.system.pojo.tenant;

import com.chua.common.support.constant.Action;
import com.chua.starter.system.entity.SysMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 同步租户服务
 *
 * @author CH
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncTenantServiceData {

    private Action action;

    private Integer sysTenantId;

    private List<SysMenu> menus;
}
