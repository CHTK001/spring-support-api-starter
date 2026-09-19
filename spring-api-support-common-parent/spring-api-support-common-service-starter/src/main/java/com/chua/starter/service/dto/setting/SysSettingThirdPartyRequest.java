package com.chua.starter.service.dto.setting;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 第三方配置
 * @since 2024/9/5
 * @author CH
 */
@Data
@AllArgsConstructor
public class SysSettingThirdPartyRequest<T, StateCache> {

    /**
     * 请求
    */
    private T request;

    /**
     * 状态缓存
    */
    private StateCache stateCache;

    /**
     * 状态
    */
    private String state;

}
