package com.chua.starter.service.dto.setting;

/**
 * 第三方配置
 *
 * @param request    请求
 * @param stateCache 状态缓存
 * @param state      状态
 * @param <T>        请求类型
 * @param <StateCache> 状态缓存类型
 * @since 2024/9/5
 * @author CH
 */
public record SysSettingThirdPartyRequest<T, StateCache>(
        T request,
        StateCache stateCache,
        String state) {
}
