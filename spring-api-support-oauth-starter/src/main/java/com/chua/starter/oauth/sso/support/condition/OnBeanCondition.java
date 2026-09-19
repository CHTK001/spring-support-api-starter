package com.chua.starter.oauth.sso.support.condition;


import com.chua.common.support.utils.ArrayUtils;
import com.chua.common.support.utils.ClassUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 用于检查特定 bean 是否存在或缺失的 {@link Condition}。
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Jakub Kubrynski
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @see ConditionalOnBean
 * @see ConditionalOnMissingBean
 * @see ConditionalOnSingleCandidate
 * @author CH
 * @since 4.0.0
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class OnBeanCondition implements Condition {

    @Override
    /**
     * 条件是否匹配
    */
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !ArrayUtils.isEmpty(context.getBeanFactory().getBeanNamesForType(ClassUtils.forName(metadata.toString())));
    }
}

