package com.chua.starter.service.event;

/**
 * 热更新运行时消费者。
 *
 * <p>关心配置热更新的组件实现本接口并注册为 Bean，变更事件到达时按 {@link #consumerId()}
 * 匹配是否由自己处理，避免无关组件被逐个唤醒。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
public interface HotReloadRuntimeConsumer {

    /**
     * 消费者标识，与配置项声明的 runtimeConsumers 对应。
     *
     * @return 消费者唯一标识
     */
    String consumerId();

    /**
     * 处理配置热更新。
     *
     * @param event 配置变更事件
     */
    void onHotReloadChanged(HotReloadChangedEvent event);
}
