package com.chua.starter.geo.model;

import lombok.Data;
import java.util.List;

/**
* 瓦片下载估算结果，包含瓦片数量和预估文件大小。
*
* @author CH
* @since 1.0.0
 */
@Data
public class TileEstimate {
    /** 瓦片总数 */
    private int tileCount;
    /** 横向瓦片列数 */
    private int cols;
    /** 纵向瓦片行数 */
    private int rows;
    /** 原始大小（MB，未压缩） */
    private double rawSizeMb;
    /** 预估输出大小（MB，含压缩） */
    private Double estimatedOutputMb;
    /** 缩放级别 */
    private int zoom;
    /** 最大缩放级别（可为 空） */
    private Integer zoomMax;
    /** 各级别明细（仅多级别下载时有值） */
    private List<LevelEstimate> levels;
    /** 错误消息（估算失败时） */
    private String message;

    /**
    * 单级别瓦片估算明细。
    *
    * @param zoom          缩放级别
    * @param tileCount     该级别瓦片数量
    * @param estimatedSizeMb 该级别预估大小（MB）
    * @author CH
    * @since 4.0.0
     */
    @Data
    public static class LevelEstimate {
        /** 缩放级别 */
        private int zoom;
        /** 该级别瓦片数量 */
        private int tileCount;
        /** 该级别预估大小（MB） */
        private Double estimatedSizeMb;
    }
}
