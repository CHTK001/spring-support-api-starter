package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysIpBlackwhite;

/**
* IP黑白名单服务接口
*
* @since 4.0.0.43
* @author CH
 */
public interface SysIpBlackwhiteService extends IService<SysIpBlackwhite> {

    /**
    * 分页查询IP黑白名单
    *
    * @param query 分页参数
    * @param entity 查询条件(type/ip/status)
    * @return 分页结果
     */
    IPage<SysIpBlackwhite> page(Query<SysIpBlackwhite> query, SysIpBlackwhite entity);

    /**
    * 新增IP黑白名单
    *
    * @param entity 实体
    * @return 新增后的实体
     */
    ReturnResult<SysIpBlackwhite> saveIp(SysIpBlackwhite entity);

    /**
    * 更新IP黑白名单
    *
    * @param entity 实体
    * @return 是否成功
     */
    ReturnResult<Boolean> updateIp(SysIpBlackwhite entity);

    /**
    * 删除IP黑白名单
    *
    * @param ipId 主键
    * @return 是否成功
     */
    ReturnResult<Boolean> deleteIp(Integer ipId);

    /**
    * 校验IP是否允许访问
    * <p>规则: 命中启用的黑名单则拒绝; 存在启用的白名单且IP不在白名单则拒绝; 其余放行。</p>
    *
    * @param ip 客户端IP
    * @return true=允许, false=拒绝
     */
    boolean checkIp(String ip);
}
