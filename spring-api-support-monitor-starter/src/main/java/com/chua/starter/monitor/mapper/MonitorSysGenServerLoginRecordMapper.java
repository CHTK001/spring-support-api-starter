package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServerLoginRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器登录记录Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MonitorSysGenServerLoginRecordMapper extends BaseMapper<MonitorSysGenServerLoginRecord> {

    /**
     * 获取最近N条登录记录
     *
     * @param serverId 服务器ID（null表示所有服务器）
     * @param topN     获取记录数
     * @return 登录记录列表
     */
    List<MonitorSysGenServerLoginRecord> getTopNLoginRecords(@Param("serverId") Integer serverId, @Param("topN") int topN);
}