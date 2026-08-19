package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务器布局配置 Mapper，提供 monitor_sys_gen_server_layout 表的基础增删改查能力。
 *
 * @since 2026-07-28
 * @author CH
 */
@Mapper
public interface MonitorSysGenServerLayoutMapper extends BaseMapper<MonitorSysGenServerLayout> {
}
