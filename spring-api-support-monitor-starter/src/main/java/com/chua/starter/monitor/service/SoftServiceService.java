package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;

/**
 * 软件服务服务接口
 *
 * @author CH
 */
public interface SoftServiceService extends IService<SoftService> {

    /**
     * 分页查询
     */
    IPage<SoftService> pageFor(Query<SoftService> query, SoftService softService);

    /**
     * 收藏服务
     *
     * @param softServiceId 软件服务ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> favoriteService(Integer softServiceId);

    /**
     * 增加安装次数
     *
     * @param softServiceId 软件服务ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> incrementInstallCount(Integer softServiceId);

    /**
     * 获取所有支持的操作系统
     *
     * @return 操作系统列表
     */
    List<String> listAllOs();
}
