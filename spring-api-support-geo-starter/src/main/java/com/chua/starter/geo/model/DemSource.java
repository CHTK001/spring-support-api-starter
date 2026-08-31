package com.chua.starter.geo.model;

/**
 * DEM 高程数据源枚举。
 *
 * @author CH
 * @since 1.0.0
 */
public enum DemSource {
    /** AWS Terrain Tiles - Terrarium 编码 PNG 瓦片，全球，最大 z15 */
    TERRARIUM("terrarium", "AWS Terrain (Terrarium)", 15);

    /** 数据源唯一标识 */
    private final String id;
    /** 显示名称 */
    private final String displayName;
    /** 最大支持缩放级别 */
    private final int maxZoom;

    DemSource(String id, String displayName, int maxZoom) {
        this.id = id;
        this.displayName = displayName;
        this.maxZoom = maxZoom;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxZoom() { return maxZoom; }

    /**
     * 从字符串解析数据源。
     *
     * @param s 字符串标识
     * @return 对应的 DemSource，无法识别返回 null
     */
    public static DemSource fromString(String s) {
        for (DemSource ds : values()) {
            if (ds.id.equalsIgnoreCase(s) || ds.displayName.equalsIgnoreCase(s)) {
                return ds;
            }
        }
        return null;
    }

    /**
     * 判断字符串是否为有效的 DEM 数据源 ID。
     *
     * @param s 待检查的字符串
     * @return true 如果是有效 DEM 源
     */
    public static boolean isDemSource(String s) {
        return fromString(s) != null;
    }
}
