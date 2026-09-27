package com.chua.starter.video.live.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播频道。
 *
 * <p>一个频道可配置多条候选流地址（多 CDN / 官方源 + 公开源），前端按顺序
 * 回退尝试，后端 HLS 代理按 {@code sources} 下标拉流。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class LiveChannel {

    /**
     * 频道标识，如 cctv1。
     */
    private String id;

    /**
     * 频道名称，如 CCTV-1 综合。
     */
    private String name;

    /**
     * 所属分组标识，如 cctv / satellite。
     */
    private String group;

    /**
     * 所属分组名称（由服务端按分组回填）。
     */
    private String groupLabel;

    /**
     * 是否央视分组（由服务端回填，前端「CCTV直播」据此过滤）。
     */
    private boolean cctv;

    /**
     * 排序值。
     */
    private int sort;

    /**
     * 候选流地址（m3u8），按优先级排列。
     */
    private List<String> sources = new ArrayList<>();

    /**
     * 候选流数量。
     *
     * @return 数量
     */
    public int getSourceCount() {
        return sources == null ? 0 : sources.size();
    }
}
