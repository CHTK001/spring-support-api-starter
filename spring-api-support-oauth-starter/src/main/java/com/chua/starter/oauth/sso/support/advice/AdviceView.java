package com.chua.starter.oauth.sso.support.advice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.experimental.Accessors;


/**
 * advice
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class AdviceView<T> {
    /**
     * 数据
     */
    @NonNull
    /** 数据 */
    private T data;
    /**
     * 请求
     */
    @JsonIgnore
    private volatile transient HttpServletRequest request; // 请求
}
