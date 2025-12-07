package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.service.SysMessageHistory;

import java.util.List;

/**
 * 系统消息历史记录服务接口
 *
 * @author CH
 * @since 2024/12/04
 */
public interface SysMessageHistoryService extends IService<SysMessageHistory> {

    /**
     * 分页查询历史记录
     *
     * @param query   分页查询对象
     * @param history 查询条件
     * @return 分页结果
     */
    IPage<SysMessageHistory> pageForHistory(Query<SysMessageHistory> query, SysMessageHistory history);

    /**
     * 查询用户的历史消息列表
     *
     * @param userId 用户ID
     * @return 历史消息列表
     */
    List<SysMessageHistory> listByUserId(Integer userId);

    /**
     * 清理过期历史记录
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanOldHistory(int days);
}
