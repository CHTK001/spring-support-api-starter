package com.chua.starter.system.dto.setting;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 第三方配置
 * @author CH
 * @since 2024/9/5
 */
@Data
@AllArgsConstructor
public class SysSettingThirdPartyRequest<T, StateCache> {

    private T request;

    private StateCache stateCache;

    private String state;

}
