package com.chua.starter.geo.model;

/**
 * DEM 高程数据源枚举。
 *
 * @author CH
 * @since 1.0.0
 */
public enum DemSource {
    /** AWS Terrain 瓷砖 - Terrarium 编码 PNG 瓦片，全球，最大 z15 */
    TERRARIUM("terrarium", "AWS Terrain (Terrarium)", 15);

    /** 数据源唯一标识 */
    private final String id;
    /** 显示名称 */
    private final String displayName;
    /**
     * 最大支持缩放级别
     *
     * @return 获取id的结果
     */
    private final int maxZoom;

    DemSource(String id, String displayName, int maxZoom) {
        this.id = id;
        this.displayName = displayName;
        this.maxZoom = maxZoom;
    }

    public String getId() { return id; }
    /**
     * 获取display名称。
     * @return 获取display名称的结果
     */
    public String getDisplayName() { return displayName; }
    /**
     * 获取最大zoom。
     * @return 获取最大zoom的结果
     */
    public int getMaxZoom() { return maxZoom; }

    /**
     * 从字符串解析数据源。
     *
     * @param s 字符串标识
     * @return 对应的 dem源，无法识别返回 空
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
      * 判断字符串是否为有效的 DEM 数据源 标识。
     *
     * @param s 待检查的字符串
     * @return true 如果是有效 DEM 源
     */
    public static boolean isDemSource(String s) {
        return fromString(s) != null;
    }
}
