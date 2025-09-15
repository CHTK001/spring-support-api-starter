package com.chua.starter.video.enums;

/**
 * 网盘类型枚举
 * 支持的网盘类型定义
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
public enum PanType {

    /**
     * 百度网盘
     */
    BAIDU("baidu", "百度网盘"),

    /**
     * 阿里云盘
     */
    ALIYUN("aliyun", "阿里云盘"),

    /**
     * 夸克网盘
     */
    QUARK("quark", "夸克网盘"),

    /**
     * 天翼云盘
     */
    TIANYI("tianyi", "天翼云盘"),

    /**
     * UC网盘
     */
    UC("uc", "UC网盘"),

    /**
     * 移动云盘
     */
    MOBILE("mobile", "移动云盘"),

    /**
     * 115网盘
     */
    PAN_115("115", "115网盘"),

    /**
     * PikPak网盘
     */
    PIKPAK("pikpak", "PikPak"),

    /**
     * 迅雷网盘
     */
    XUNLEI("xunlei", "迅雷网盘"),

    /**
     * 123网盘
     */
    PAN_123("123", "123网盘"),

    /**
     * 磁力链接
     */
    MAGNET("magnet", "磁力链接"),

    /**
     * 电驴链接
     */
    ED2K("ed2k", "电驴链接"),

    /**
     * 其他
     */
    OTHERS("others", "其他");

    /**
     * 网盘类型代码
     */
    private final String code;

    /**
     * 网盘类型名称
     */
    private final String name;

    /**
     * 构造函数
     *
     * @param code 网盘类型代码
     * @param name 网盘类型名称
     */
    PanType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取网盘类型
     *
     * @param code 网盘类型代码
     * @return 网盘类型枚举，未找到返回OTHERS
     */
    public static PanType fromCode(String code) {
        if (code == null) {
            return OTHERS;
        }

        for (PanType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return OTHERS;
    }

    /**
     * 获取网盘类型代码
     *
     * @return 网盘类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取网盘类型名称
     *
     * @return 网盘类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 判断是否为磁力链接类型
     *
     * @return 是否为磁力链接
     */
    public boolean isMagnetType() {
        return this == MAGNET || this == ED2K;
    }

    /**
     * 判断是否为网盘类型
     *
     * @return 是否为网盘类型
     */
    public boolean isPanType() {
        return !isMagnetType() && this != OTHERS;
    }
}