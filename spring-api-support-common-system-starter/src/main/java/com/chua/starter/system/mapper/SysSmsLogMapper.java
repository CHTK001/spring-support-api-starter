package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.project.SysSmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信发送日志 Mapper
 *
 * @author CH
 * @since 2026/3/19
 */
@Mapper
public interface SysSmsLogMapper extends BaseMapper<SysSmsLog> {
}
