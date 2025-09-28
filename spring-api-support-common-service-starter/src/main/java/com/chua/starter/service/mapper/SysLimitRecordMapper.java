package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysLimitRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 限流记录映射器接口
 * 
 * @author CH
 * @since 2025/9/28
 */
@Mapper
public interface SysLimitRecordMapper extends BaseMapper<SysLimitRecord> {
}