package com.chua.starter.payment.test;

import com.chua.common.support.core.annotation.SpiProfile;
import com.chua.spring.support.configuration.spi.SpiProfileCondition;
import com.chua.payment.support.spi.UserOpenIdResolver;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 测试环境 UserOpenIdResolver
 * <p>
 * 仅在 dev/test profile 下加载。
 * 硬编码映射：系统 userId → 支付宝沙箱买家 openId
 * 沙箱买家 openId 来自 https://open.alipay.com/develop/sandbox/app
 * </p>
 */
@Component
@Conditional(SpiProfileCondition.class)
@SpiProfile({"dev", "test"})
public class TestUserOpenIdResolver implements UserOpenIdResolver {

    private static final Map<String, Map<Long, String>> MAPPING = Map.of(
            "ALIPAY", Map.of(
                    1L, "076w-gnWNqQdv651sF73RtN0yPqEQ7lgf47H7tk-wcBXCo8"
            )
    );

    @Override
    public String resolveOpenId(Long merchantId, Long userId, String channelType) {
        Map<Long, String> channelMapping = MAPPING.get(channelType);
        if (channelMapping == null) {
            return null;
        }
        return channelMapping.get(userId);
    }
}
