package com.chua.starter.rustserver;

/**
 * 错误页面定义。
 */
public class ErrorPage {

    private final int statusCode;
    private final String location;

    public ErrorPage(int statusCode, String location) {
        this.statusCode = statusCode;
        this.location = location;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLocation() {
        return location;
    }
}
