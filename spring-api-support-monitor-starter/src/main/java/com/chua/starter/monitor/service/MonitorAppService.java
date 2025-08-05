package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.pojo.OpenApi;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;

/**
 * 监视器应用服务接口
 * 继承自IService，提供了对MonitorApplication实体的通用操作
 */
public interface MonitorAppService extends IService<MonitorApplication>{

    /**
     * 根据监视器ID获取监视器应用详细信息
     *
     * @param monitorId 监视器ID
     * @return 包含监视器应用详细信息的ReturnResult对象
     */
    ReturnResult<MonitorApplication> detail(Integer monitorId);

    /**
     * 分页查询监视器应用信息
     *
     * @param page   分页查询对象，包含分页参数
     * @param entity 查询条件实体
     * @return 包含分页信息的IPage对象
     */
    IPage<MonitorApplication> pageFor(Query<MonitorApplication> page, MonitorApplication entity);

    /**
     * 查询监视器应用列表
     *
     * @param entity 查询条件实体
     * @return 包含监视器应用列表的ReturnResult对象
     */
    ReturnResult<List<MonitorApplication>> list(MonitorApplication entity);

    /**
     * 渲染监视器应用信息
     *
     * @param monitorId 监视器ID
     * @param index     渲染索引
     * @return 渲染结果字符串
     */
    ReturnResult<List<OpenApi>> render(Integer monitorId, String index);
}
