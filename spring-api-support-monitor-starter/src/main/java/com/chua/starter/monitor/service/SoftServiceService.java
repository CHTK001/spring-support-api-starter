package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;
import java.util.Map;

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
     * 分页查询并清除命令数据
     *
     * @param query       查询条件
     * @param softService 软件服务
     * @return 分页结果，已清除命令数据
     */
    ReturnPageResult<SoftService> pageForWithoutCommands(Query<SoftService> query, SoftService softService);

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

    /**
     * 判断软件服务是否可以安装
     * 根据安装脚本是否存在来决定
     *
     * @param softService 软件服务
     * @return 是否可以安装
     */
    boolean isInstallable(SoftService softService);

    /**
     * 清除命令数据以减少数据包大小
     *
     * @param services 软件服务列表
     */
    void cleanCommandData(List<SoftService> services);

    /**
     * 检测命令类型
     *
     * @param command 命令内容
     * @return 命令类型信息，包含commandType、commandTypeName和isScript
     */
    ReturnResult<Map<String, Object>> detectCommandType(String command);

    /**
     * 按软件名称分组并处理版本信息
     *
     * @param originalServices 原始软件服务列表
     * @return 分组后的软件服务列表
     */
    List<SoftService> groupServicesByName(List<SoftService> originalServices);

    /**
     * 更新软件服务信息
     *
     * @param softService 软件服务信息
     * @return 是否更新成功
     */
    Boolean updateFor(SoftService softService);

    /**
     * 保存软件服务信息
     *
     * @param softService 软件服务信息
     * @return 是否保存成功
     */
    Boolean saveFor(SoftService softService);
}
