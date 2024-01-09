package com.chua.starter.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.admin.entity.SysOperateLogger;
import com.chua.starter.admin.pojo.LogStaticVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CH
 */
@Mapper
public interface SysOperateLoggerMapper extends BaseMapper<SysOperateLogger> {

    /**
     * 按时间统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    List<LogStaticVO> near(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}