package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysServiceModule;

/**
 * 服务子项服务接口
 * <p>
 * 提供对服务子项的增删改查操作
 *
 * @author CH
 */
public interface SysServiceModuleService extends IService<SysServiceModule> {
    /**
     * 分页查询服务子项
     * <p>
     * 根据查询对象中的条件和分页参数，返回分页后的服务子项列表
     *
     * @param query       查询对象，包含查询条件和分页信息
     * @param serviceItem 服务子项对象，用于查询条件
     * @return 返回包含服务子项的分页对象
     */
    IPage<SysServiceModule> pageFor(Query<SysServiceModule> query, SysServiceModule serviceItem);

    /**
     * 保存服务子项
     * <p>
     * 新增或修改服务子项，根据传入对象的主键是否存在来决定操作类型
     *
     * @param sysServiceItem 要保存的服务子项对象
     * @return 返回保存后的服务子项对象和操作结果
     */
    ReturnResult<SysServiceModule> saveFor(SysServiceModule sysServiceItem);

    /**
     * 更新服务子项
     * <p>
     * 根据传入的服务子项对象更新数据库中对应的记录
     *
     * @param sysServiceItem 要更新的服务子项对象
     * @return 返回更新操作的结果
     */
    ReturnResult<Boolean> updateFor(SysServiceModule sysServiceItem);

    /**
     * 删除服务子项
     * <p>
     * 根据服务子项的ID删除数据库中的对应记录
     *
     * @param sysServiceItemId 服务子项的ID
     * @return 返回删除操作的结果
     */
    ReturnResult<Boolean> deleteFor(Long sysServiceItemId);

}
