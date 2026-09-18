package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 映射器。
 *
 * @since 2026-08-11
 * @author CH
 */
@Mapper
public interface MsOperationLogMapper extends BaseMapper<MsOperationLog> {
}
