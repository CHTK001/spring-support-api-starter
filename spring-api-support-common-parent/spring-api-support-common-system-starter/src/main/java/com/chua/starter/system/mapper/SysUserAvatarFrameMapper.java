package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserAvatarFrame;
import org.apache.ibatis.annotations.Mapper;

/**
   * 用户头像框关联 映射器
 *
 * @since 2024/8/7
 * @author CH
 */
@Mapper
public interface SysUserAvatarFrameMapper extends BaseMapper<SysUserAvatarFrame> {
}