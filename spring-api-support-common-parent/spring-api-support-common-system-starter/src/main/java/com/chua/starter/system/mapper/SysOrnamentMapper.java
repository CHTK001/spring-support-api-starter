package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysOrnament;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饰品目录 映射器
 *
 * @author CH
 * @since 4.0.0
 */
@Mapper
public interface SysOrnamentMapper extends BaseMapper<SysOrnament> {
}
