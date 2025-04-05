package com.chua.starter.monitor.pojo;

import lombok.Data;

@Data
public class ReturnResult<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ReturnResult<T> success(T data) {
        ReturnResult<T> result = new ReturnResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> ReturnResult<T> success() {
        return success(null);
    }

    public static <T> ReturnResult<T> error(String message) {
        ReturnResult<T> result = new ReturnResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
} 