package com.chua.starter.scheduler.support.ddl;

/**
 * DDL 执行结果。
 *
 * @author CH
 */
public class DdlResult {

    /**
     * 是否包含错误
     */
    private final boolean error;

    /**
     * 错误信息
     */
    private final String firstError;

    /**
     * 构造器。
     *
     * @param error      是否包含错误
     * @param firstError 错误信息
     */
    public DdlResult(boolean error, String firstError) {
        this.error = error;
        this.firstError = firstError;
    }

    /**
     * 是否包含错误
     *
     * @return 是否包含错误
     */
    public boolean hasError() {
        return error;
    }

    /**
     * 获取第一个错误信息
     *
     * @return 错误信息
     */
    public String getFirstError() {
        return firstError;
    }
}
