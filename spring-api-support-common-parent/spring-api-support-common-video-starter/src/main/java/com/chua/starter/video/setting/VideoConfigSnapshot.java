package com.chua.starter.video.setting;

import com.chua.starter.video.live.model.LiveCatalog;

import java.util.Map;

/**
 * 视频配置快照 —— 用于 `video` 分组的导出与导入迁移。
 *
 * <p>相对通用 {@code /v2/setting} 导出（原始 {@code sys_setting} 行），此快照是
 * 领域化结构：标量 + 整段直播目录，可直接导入全新环境。</p>
 *
 * @param catalogUrl     资源站采集接口地址
 * @param defaultKeyword 兜底检索关键词
 * @param browseKeywords 分类码 → 关键词映射
 * @param liveCatalog    直播目录（分组 + 频道 + 候选源）
 * @author CH
 * @since 4.0.0.42
 */
public record VideoConfigSnapshot(
        String catalogUrl,
        String defaultKeyword,
        Map<String, String> browseKeywords,
        LiveCatalog liveCatalog) {
}
