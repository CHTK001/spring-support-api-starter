package com.chua.starter.oauth.sso.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.oauth.sso.support.entity.SysFingerprint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 浏览器指纹映射 映射器
 *
 * @since 4.0.0.42
 * @author CH
 */
@Mapper
public interface SysFingerprintMapper extends BaseMapper<SysFingerprint> {
}
