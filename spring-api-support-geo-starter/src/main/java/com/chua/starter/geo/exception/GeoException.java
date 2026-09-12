package com.chua.starter.geo.exception;

/**
 * Geo 服务业务异常，携带业务错误码和错误信息。
 *
 * @author CH
 * @since 1.0.0
 */
public class GeoException extends RuntimeException {
    /** 业务错误码 */
    private final String code;

    /**
    * 构造业务异常。
    *
    * @param message 错误信息
     */
    public GeoException(String message) {
        super(message);
        this.code = "FAIL";
    }

    /**
     * 构造业务异常。
     *
     * @param code    业务错误码
     * @param message 错误信息
     */
    public GeoException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public String getCode() {
        return code;
    }
}
