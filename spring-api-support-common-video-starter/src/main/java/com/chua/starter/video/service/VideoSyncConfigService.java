package com.chua.starter.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoSyncConfig;

/**
 * 视频同步配置服务
 *
 * @author CH
 * @since 2024/6/21
 */
public interface VideoSyncConfigService {

    /**
     * 获取所有同步配置
     *
     * @param query 查询条件封装对象，包含分页、排序等信息
     *              <pre><code>
     *              Query<VideoSyncConfig> query = new Query<>();
     *              query.setPage(1);
     *              query.setPageSize(10);
     *              query.setOrder("createTime desc");
     *              IPage<VideoSyncConfig> result = videoSyncConfigService.getAllConfigs(query);
     *              </code></pre>
     * @return 同步配置分页列表
     */
    IPage<VideoSyncConfig> getAllConfigs(Query<VideoSyncConfig> query);

    /**
     * 根据ID获取同步配置
     *
     * @param id 配置ID，不能为空
     *           <pre><code>
     *           String id = "123456";
     *           VideoSyncConfig config = videoSyncConfigService.getConfigById(id);
     *           </code></pre>
     * @return 同步配置对象，如果未找到返回null
     */
    VideoSyncConfig getConfigById(String id);

    /**
     * 添加同步配置
     *
     * @param config 同步配置对象，包含要添加的配置信息
     *               <pre><code>
     *               VideoSyncConfig config = new VideoSyncConfig();
     *               config.setVideoSyncConfigName("测试配置");
     *               config.setVideoSyncConfigType("http");
     *               config.setVideoSyncConfigUrl("http://example.com/api");
     *               boolean result = videoSyncConfigService.addConfig(config);
     *               </code></pre>
     * @return 是否添加成功，true表示成功，false表示失败
     */
    boolean addConfig(VideoSyncConfig config);

    /**
     * 更新同步配置
     *
     * @param config 同步配置对象，包含要更新的配置信息，ID不能为空
     *               <pre><code>
     *               VideoSyncConfig config = new VideoSyncConfig();
     *               config.setVideoSyncConfigId("123456");
     *               config.setVideoSyncConfigName("更新后的配置名称");
     *               boolean result = videoSyncConfigService.updateConfig(config);
     *               </code></pre>
     * @return 是否更新成功，true表示成功，false表示失败
     */
    boolean updateConfig(VideoSyncConfig config);

    /**
     * 删除同步配置
     *
     * @param id 配置ID，不能为空
     *           <pre><code>
     *           String id = "123456";
     *           boolean result = videoSyncConfigService.deleteConfig(id);
     *           </code></pre>
     * @return 是否删除成功，true表示成功，false表示失败
     */
    boolean deleteConfig(String id);

    /**
     * 同步配置
     *
     * @param id                             配置ID，不能为空
     *                                       <pre><code>
     *                                       String id = "123456";
     *                                       ReturnResult<Boolean> result = videoSyncConfigService.syncConfig(id, true);
     *                                       </code></pre>
     * @param doYouWantToManuallySynchronize 是否手动同步，true表示手动同步，false表示自动同步
     * @return 同步结果，包含是否同步成功及相应信息
     */
    ReturnResult<Boolean> syncConfig(String id, boolean doYouWantToManuallySynchronize);

    /**
     * 切换配置状态（启用/禁用）
     *
     * @param id 配置ID，不能为空
     *           <pre><code>
     *           String id = "123456";
     *           ReturnResult<Boolean> result = videoSyncConfigService.toggleStatus(id);
     *           </code></pre>
     * @return 切换结果，包含是否切换成功及相应信息
     */
    ReturnResult<Boolean> toggleStatus(String id);

    /**
     * 获取同步配置状态
     *
     * @param id 配置ID，不能为空
     *           <pre><code>
     *                     String id = "123456";
     *                     ReturnResult<String> result = videoSyncConfigService.getStatus(id);
     *                     </code></pre>
     * @return 配置状态信息，包含状态码和描述
     * @author CH
     * @since 2024/6/21
     */
    ReturnResult<String> getSyncStatus(String id);

    /**
     * 停止同步配置
     *
     * @param id                             配置ID，不能为空
     *                                       <pre><code>
     *                                                                             String id = "123456";
     *                                                                             ReturnResult<Boolean> result = videoSyncConfigService.stopSync(id, true);
     *                                                                             </code></pre>
     * @param doYouWantToManuallySynchronize 是否手动同步，true表示手动同步，false表示自动同步
     * @return 停止同步结果，包含是否停止同步成功及相应信息
     * @author CH
     * @since 2024/6/21
     */
    ReturnResult<Boolean> stopSync(String id, boolean doYouWantToManuallySynchronize);
}