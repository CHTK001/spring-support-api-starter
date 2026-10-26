package com.chua.starter.video.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.video.repository.VideoInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频本地索引 Mapper。
 *
 * <p>由 {@code VideoAutoConfiguration} 内的 {@code @MapperScan} 注册；
 * 宿主未引入 MyBatis-Plus 时不装配，检索自动退回资源站直查。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Mapper
public interface VideoCatalogMapper extends BaseMapper<VideoInfoEntity> {
}
