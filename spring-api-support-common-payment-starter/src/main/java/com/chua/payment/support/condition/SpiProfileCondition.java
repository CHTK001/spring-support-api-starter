package com.chua.payment.support.condition;

import com.chua.common.support.core.annotation.SpiProfile;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Spring {@link Condition} 实现，用于评估 {@link SpiProfile} 注解。
 * <p>
 * 读取目标类上的 {@link SpiProfile} 注解，与 Spring 当前活跃的 Profile 进行匹配。
 * 结合 {@code @Conditional(SpiProfileCondition.class)} 使用，可控制 Spring Bean
 * 仅在指定环境(Profile)下创建。
 * </p>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Component
 * @Conditional(SpiProfileCondition.class)
 * @SpiProfile({"dev", "test"})
 * public class TestResolver implements SomeResolver { }
 * }</pre>
 * </p>
 *
 * @author CH
 * @since 4.0.0.41
 */
public class SpiProfileCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!metadata.isAnnotated(SpiProfile.class.getName())) {
            return true;
        }

        var attributes = metadata.getAnnotationAttributes(SpiProfile.class.getName());
        if (attributes == null) {
            return true;
        }

        String[] profiles = (String[]) attributes.get("value");
        if (profiles == null || profiles.length == 0) {
            return true;
        }

        boolean negative = Boolean.TRUE.equals(attributes.get("negative"));

        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        List<String> activeList = Arrays.asList(activeProfiles);

        boolean matches = Arrays.stream(profiles)
                .anyMatch(p -> activeList.stream().anyMatch(a -> Objects.equals(a, p)));

        return negative != matches;
    }
}
