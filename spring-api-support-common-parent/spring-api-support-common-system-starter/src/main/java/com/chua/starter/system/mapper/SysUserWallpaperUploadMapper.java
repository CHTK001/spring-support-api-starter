package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserWallpaperUpload;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户上传壁纸记录 映射器
 *
 * @author CH
 * @since 4.0.0
 */
@Mapper
public interface SysUserWallpaperUploadMapper extends BaseMapper<SysUserWallpaperUpload> {
}
