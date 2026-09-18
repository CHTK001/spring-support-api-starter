package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务器组件布局 映射器，提供 监控_sys_gen_服务端_组件_layout 表的基础增删改查能力。
 *
 * @since 2026-07-28
 * @author CH
 */
@Mapper
public interface MonitorSysGenServerComponentLayoutMapper extends BaseMapper<MonitorSysGenServerComponentLayout> {
}
