package com.chua.starter.service;

import com.chua.starter.entity.service.SysHoliday;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 节假日服务接口
 *
 * @author CH
 * @since 2025-09-11
 */
public interface SysHolidayService extends IService<SysHoliday> {

    /**
     * 同步节假日数据
     * <p>
     * 该方法用于从外部数据源同步节假日信息到本地数据库。
     * </p>
     *
     * @return 同步结果，true表示同步成功，false表示同步失败
     */
    Boolean sync();

    /**
     * 根据年份获取节假日列表
     * <p>
     * 该方法根据指定的年份查询对应的节假日信息列表。
     * </p>
     *
     * @param year 年份，例如：2025
     * @return 节假日列表
     */
    List<SysHoliday> getHolidays(Integer year);
}
