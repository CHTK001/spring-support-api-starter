package com.chua.starter.service.event;

import org.springframework.context.ApplicationEvent;

import java.util.Set;
import java.util.stream.Collectors;

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

    public String getGroup() {
        return group;
    }

    public Integer getSettingId() {
        return settingId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getValueType() {
        return valueType;
    }

    public Integer getStatus() {
        return status;
    }

    public Set<String> getRuntimeConsumers() {
        return runtimeConsumers;
    }

    public boolean isForConsumer(String consumerId) {
        return runtimeConsumers.contains(consumerId);
    }
}
