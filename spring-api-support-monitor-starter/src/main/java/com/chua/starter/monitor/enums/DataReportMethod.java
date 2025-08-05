package com.chua.starter.monitor.enums;

/**
 * 数据上报方式枚举
 *
 * @author CH
 * @since 2024/12/26
 */
public enum DataReportMethod {
    NONE("NONE", "不支持上报"),
    LOCAL("LOCAL", "本地上报"),
    API("API", "接口上报"),
    PROMETHEUS("PROMETHEUS", "Prometheus");

    private final String code;
    private final String desc;

    DataReportMethod(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataReportMethod fromCode(String code) {
        for (DataReportMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        return NONE;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
