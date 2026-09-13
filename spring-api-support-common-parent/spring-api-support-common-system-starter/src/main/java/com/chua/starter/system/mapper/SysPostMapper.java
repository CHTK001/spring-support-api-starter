package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysPost;
import org.apache.ibatis.annotations.Mapper;

/**
* 岗位 映射器接口
*
* @since 4.0.0.50
* @author CH
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
}
