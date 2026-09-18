package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-岗位关系 映射器接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
}
