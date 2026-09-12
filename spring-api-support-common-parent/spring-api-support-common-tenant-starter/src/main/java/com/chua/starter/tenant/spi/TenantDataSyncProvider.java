package com.chua.starter.tenant.spi;

import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.tenant.entity.SysTenant;

import java.util.List;
import java.util.Map;

/**
* 租户数据同步提供者接口
* <p>
* 使用 SPI 机制，允许不同实现处理租户数据同步
* </p>
*
* @版本 1.0.0
* @since 2024/12/12
* @author CH
 */
public interface TenantDataSyncProvider {

    /**
    * 获取提供者名称
    *
    * @return 提供者名称
     */
    String getName();

    /**
    * 获取优先级，数值越小优先级越高
    *
    * @return 优先级
     */
    default int getOrder() {
        return 0;
    }

    /**
    * 同步租户用户信息
    * <p>
    * 接收到租户信息后，创建/更新/删除对应的系统用户
    * </p>
    *
    * @param tenant    租户信息
    * @param operation 操作类型: 创建, 更新, 删除
    * @return 创建的用户信息，删除时返回 空
     */
    SysUser syncTenantUser(SysTenant tenant, String operation);

    /**
    * 同步租户菜单信息
    * <p>
    * 接收到服务数据后，更新租户可访问的菜单
    * </p>
    *
    * @param tenantId 租户标识
    * @param menuIds  菜单标识列表
    * @return 同步结果
     */
    boolean syncTenantMenu(String tenantId, List<Integer> menuIds);

    /**
    * 批量同步菜单数据
    * <p>
    * 接收到菜单数据后，创建/更新/删除对应的菜单
    * </p>
    *
    * @param tenantId  租户标识
    * @param menus     菜单数据列表
    * @param operation 操作类型: 创建, 更新, 删除
    * @return 同步结果
     */
    boolean syncMenuData(String tenantId, List<Map<String, Object>> menus, String operation);

    /**
    * 同步租户服务数据
    * <p>
    * 将平台下发的服务、模块、模块项写入租户库, 并记录租户自身与租户-服务绑定关系
    * </p>
    *
    * @param tenant   租户信息
    * @param services 服务列表
    * @param modules  服务模块列表
    * @param items    服务模块项列表
    * @param operation 操作类型: 创建, 更新, 删除, 替换
    * @return 同步结果
     */
    default boolean syncServiceData(SysTenant tenant,
                                    List<Map<String, Object>> services,
                                    List<Map<String, Object>> modules,
                                    List<Map<String, Object>> items,
                                    String operation) {
        return false;
    }

    /**
    * 处理原始同步数据
    * <p>
    * 用于处理自定义格式的同步数据
    * </p>
    *
    * @param topic 主题
    * @param data  数据
    * @return 处理结果
     */
    default Object handleRawData(String topic, Map<String, Object> data) {
        return null;
    }
}
