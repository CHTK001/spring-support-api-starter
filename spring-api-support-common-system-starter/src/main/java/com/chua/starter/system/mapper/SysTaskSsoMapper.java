package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysTaskSso;
import org.apache.ibatis.annotations.Mapper;

/**
 *
 * 分片表
 * @since 2024/12/4
 * @author CH    
 */
@Mapper
public interface SysTaskSsoMapper extends BaseMapper<SysTaskSso> {
}