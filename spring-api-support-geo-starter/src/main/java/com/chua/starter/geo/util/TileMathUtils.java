package com.chua.starter.geo.util;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.TileCoord;
import java.util.ArrayList;
import java.util.List;

/**
 * Web Mercator (EPSG:3857) 瓦片坐标计算工具类。
 * 提供经纬度与瓦片坐标之间的转换、瓦片数量估算等核心数学运算。
 * 本类为纯静态工具类，禁止实例化。
 *
 * @author CH
 * @since 1.0.0
 */
public class TileMathUtils {
    /** 最大支持缩放级别 */
    private static final int MAX_ZOOM = 24;

    /** 私有构造函数，防止实例化 */
    private TileMathUtils() {
    }

    public static int clampZoom(int zoom) {
        return Math.min(zoom, MAX_ZOOM);
    }

    /** 经纬度转瓦片坐标 */
    public static int[] toTile(double lat, double lng, int zoom) {
        zoom = clampZoom(zoom);
        lat = Math.max(-85.05112878, Math.min(85.05112878, lat));
        int n = (int) Math.pow(2, zoom);
        int x = (int) ((lng + 180.0) / 360.0 * n);
        double latRad = Math.toRadians(lat);
        int y = (int) ((1.0 - (Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI)) / 2.0 * n);
        x = Math.max(0, Math.min(n - 1, x));
        y = Math.max(0, Math.min(n - 1, y));
        return new int[]{x, y};
    }

    /** 瓦片坐标转地理边界 [north, south, east, west] */
    public static double[] tileToBounds(int x, int y, int zoom) {
        zoom = clampZoom(zoom);
        double n = Math.pow(2, zoom);
        double west = x / n * 360.0 - 180.0;
        double north = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2.0 * y / n))));
        double east = (x + 1) / n * 360.0 - 180.0;
        double south = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2.0 * (y + 1) / n))));
        return new double[]{north, south, east, west};
    }

    /** 获取边界内所有瓦片坐标 */
    public static TileCoord[] getTilesInBounds(Bounds bounds, int zoom) {
        zoom = clampZoom(zoom);
        int[] tl = toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = toTile(bounds.getSouth(), bounds.getEast(), zoom);
        List<TileCoord> tiles = new ArrayList<>();
        for (int x = tl[0]; x <= br[0]; x++) {
            for (int y = tl[1]; y <= br[1]; y++) {
                tiles.add(new TileCoord(x, y, zoom));
            }
        }
        return tiles.toArray(new TileCoord[0]);
    }

    /** 估算瓦片数量 */
    public static long estimateTileCount(Bounds bounds, int zoom) {
        zoom = clampZoom(zoom);
        int[] tl = toTile(bounds.getNorth(), bounds.getWest(), zoom);
        int[] br = toTile(bounds.getSouth(), bounds.getEast(), zoom);
        long cols = (long) br[0] - tl[0] + 1;
        long rows = (long) br[1] - tl[1] + 1;
        return Math.max(0, cols * rows);
    }

    /** 计算拼接后的地理边界 [north, south, east, west] */
    public static double[] getMergedBounds(int xMin, int yMin, int xMax, int yMax, int zoom) {
        double[] nw = tileToBounds(xMin, yMin, zoom);
        double[] se = tileToBounds(xMax, yMax, zoom);
        return new double[]{nw[0], se[1], se[2], nw[3]}; // north, south, east, west
    }

    /** WGS-84 经纬度 → Web Mercator 米坐标 */
    public static double[] latLngToMercator(double lat, double lng) {
        double R = 6378137.0;
        double x = R * Math.toRadians(lng);
        double y = R * Math.log(Math.tan(Math.PI / 4.0 + Math.toRadians(lat) / 2.0));
        return new double[]{x, y};
    }

    /** 计算每像素米数（中纬度） */
    public static double metersPerPixel(double lat, int zoom) {
        return 40075016.686 * Math.cos(Math.toRadians(lat)) / (256.0 * Math.pow(2, zoom));
    }
}
