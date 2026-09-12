package com.chua.starter.geo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
* 全局异常处理器，统一处理 Geo 服务中的各类异常。
*
* @author CH
* @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
    * 处理 geo异常 业务异常。
    *
    * @param e 业务异常
    * @return 错误响应 映射
     */
    @ExceptionHandler(GeoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handle(GeoException e) {
        return Map.of("code", e.getCode(), "message", e.getMessage());
    }

    /**
    * 处理通用异常。
    *
    * @param e 异常对象
    * @return 错误响应 映射
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handle(Exception e) {
        log.error("Geo服务异常", e);
        return Map.of("code", "ERROR", "message", e.getMessage());
    }
}
