package com.chua.starter.geo.model;

/**
 * 输出格式枚举，定义瓦片下载后的导出格式。
 *
 * @author CH
 * @since 1.0.0
 */
public enum OutputFormat {
    /** geotiff 格式（带地理坐标信息） */
    GEOTIFF("geotiff", ".tif", "image/tiff"),
    /** PNG 格式（无损，支持透明） */
    PNG("png", ".png", "image/png"),
    /** JPEG 格式（有损压缩） */
    JPEG("jpeg", ".jpg", "image/jpeg"),
    /** 原始瓦片目录（不做拼接） */
    TILES("tiles", "", "application/octet-stream"),
    /** 矢量瓦片 PBF 格式 */
    PBF("pbf", ".pbf", "application/x-protobuf");

    /** 字符串标识 */
    private final String label;
    /** 文件扩展名 */
    private final String extension;
    /**
     * MIME 类型
     *
     * @return 获取标签的结果
     */
    private final String mimeType;

    OutputFormat(String label, String extension, String mimeType) {
        this.label = label;
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getLabel() { return label; }
    /**
     * 获取延伸。
     * @return 获取延伸的结果
     */
    public String getExtension() { return extension; }
    /**
     * 获取mime类型。
     * @return 获取mime类型的结果
     */
    public String getMimeType() { return mimeType; }

    /**
     * 从字符串解析输出格式。
     *
     * @param value 格式字符串（如 "geotiff"、"png"）
     * @return 对应的 输出格式化 枚举值，无法识别返回 空
     */
    public static OutputFormat fromString(String value) {
        if (value == null) {
            return null;
        }
        for (OutputFormat format : values()) {
            if (format.getLabel().equalsIgnoreCase(value)
                    || format.name().equalsIgnoreCase(value)
                    || format.getExtension().equalsIgnoreCase("." + value)) {
                return format;
            }
        }
        return null;
    }
}
