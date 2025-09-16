package com.chua.starter.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.video.entity.VideoSyncConfigRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;
import java.util.Set;

/**
 * 视频同步配置Mapper
 *
 * @author CH
 * @since 2024/6/21
 */
@Mapper
public interface VideoSyncConfigRecordMapper extends BaseMapper<VideoSyncConfigRecord> {
    /**
     * 根据配置ID统计记录数
     *
     * @param videoSyncConfigIds 视频同步配置ID集合
     *                           <p>示例:</p>
     *                           <pre><code>
     *                                                     Set<String> ids = new HashSet<>();
     *                                                     ids.add("config1");
     *                                                     ids.add("config2");
     *                                                     Map<Integer, Integer> result = mapper.countByConfigId(ids);
     *                                                     </code></pre>
     * @return 配置ID与记录数的映射关系
     */
    Map<String, Integer> countByConfigId(@Param("videoSyncConfigIds") Set<String> videoSyncConfigIds);
}