package com.chua.starter.service.event;

import org.springframework.context.ApplicationEvent;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 热更新配置变更事件。
 *
 * <p>配置中心某个设置项落库并提交事务后发布，携带变更项的标识、值与需要感知的运行时消费者集合，
 * 由 {@code HotReloadChangedListener} 负责分发给对应的消费者。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
public class HotReloadChangedEvent extends ApplicationEvent {

    private final String group;
    private final Integer settingId;
    private final String name;
    private final String code;
    private final String value;
    private final String valueType;
    private final Integer status;
    private final Set<String> runtimeConsumers;

    public HotReloadChangedEvent(Object source, Integer settingId, String name, String code,
                                 String value, String valueType, Integer status,
                                 Set<String> runtimeConsumers) {
        super(source);
        this.settingId = settingId;
        this.name = name;
        this.code = code;
        this.value = value;
        this.valueType = valueType;
        this.status = status;
        this.group = "hot_reload";
        this.runtimeConsumers = runtimeConsumers == null || runtimeConsumers.isEmpty()
                ? Set.of()
                : runtimeConsumers.stream().filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** getGroup 方法 */
    public String getGroup() {
        return group;
    }

    /** getSettingId 方法 */
    public Integer getSettingId() {
        return settingId;
    }

    /** getName 方法 */
    public String getName() {
        return name;
    }

    /** getCode 方法 */
    public String getCode() {
        return code;
    }

    /** getValue 方法 */
    public String getValue() {
        return value;
    }

    /** getValueType 方法 */
    public String getValueType() {
        return valueType;
    }

    /** getStatus 方法 */
    public Integer getStatus() {
        return status;
    }

    public Set<String> getRuntimeConsumers() {
        return runtimeConsumers;
    }

    /** isForConsumer 方法 */
    public boolean isForConsumer(String consumerId) {
        return runtimeConsumers.contains(consumerId);
    }
}
