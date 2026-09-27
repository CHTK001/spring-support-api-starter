package com.chua.starter.system.configuration;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import com.chua.starter.system.support.ApiRouteExecutor;
import com.chua.starter.system.support.RegisteredRouteIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 接口停用 / 转发启动期装配。
 *
 * <p>挂在 {@link ApplicationReadyEvent} 而不是 {@code ApplicationRunner}：路由索引要
 * 覆盖业务控制器，而业务控制器的 {@code @RequestMapping} 由派生的
 * {@code requestMappingHandlerMapping} 注册，只有 context 完全就绪后才拿得到。
 * 与 {@code MenuAnnotationRegistrar} 取同一个 bean，取法一致。</p>
 *
 * <p>取 bean 失败或索引为空时只禁用转发能力，<b>不阻断启动</b>；停用规则不依赖索引，
 * 仍然生效。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRouteStartupInitializer {

    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * 执行器
     */
    private final ApiRouteExecutor apiRouteExecutor;

    /**
     * 策略表映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * 上下文就绪后采集路由索引并装载策略。
     *
     * @param event 就绪事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        refresh();
    }

    /**
     * 重新采集路由索引并重载策略。
     *
     * @return 路由条数；采集失败返回 -1
     */
    public int refresh() {
        int count = -1;
        try {
            RegisteredRouteIndex.refresh(applicationContext);
            count = RegisteredRouteIndex.size();
        } catch (Exception e) {
            log.warn("[ApiRoute] 路由索引采集失败，转发功能不可用（接口停用不受影响）: {}", e.getMessage());
        }
        try {
            List<SysPolicyConfig> rows = sysPolicyConfigMapper.selectList(
                    Wrappers.<SysPolicyConfig>lambdaQuery()
                            .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_API_ROUTE));
            apiRouteExecutor.rebuild(rows);
            log.info("[ApiRoute] 接口停用/转发已装载：路由 {} 条，策略 {} 条",
                    count, rows == null ? 0 : rows.size());
        } catch (Exception e) {
            log.warn("[ApiRoute] 策略装载失败，接口停用/转发不生效: {}", e.getMessage());
        }
        return count;
    }
}
