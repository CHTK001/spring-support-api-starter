package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorApplication;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitorAppMapper extends BaseMapper<MonitorApplication> {
}