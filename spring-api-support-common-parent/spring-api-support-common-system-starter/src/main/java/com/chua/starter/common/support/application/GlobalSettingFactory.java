package com.chua.starter.common.support.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局设置工厂兼容类
 */
public class GlobalSettingFactory {

    public static String PREFIX = "global.setting";

    private static final GlobalSettingFactory INSTANCE = new GlobalSettingFactory();
    private final Map<String, Object> settings = new ConcurrentHashMap<>();

    public static GlobalSettingFactory getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = settings.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    
    
    public void register(String key, Object value) {
        register(key, value, true);
    }

    public Object get(String key) {
        return settings.get(key);
    }

    public boolean contains(String key) {
        return settings.containsKey(key);
    }

    public void remove(String key) {
        settings.remove(key);
    }

public void register(String key, Object value, boolean override) {
        if (override || !settings.containsKey(key)) {
            settings.put(key, value);
        }
    }

public void set(String key, Object value) {
        settings.put(key, value);
    }

    public void set(String key, Map<String, Object> value) {
        settings.put(key, value);
    }

    public void set(String group, String name, Object value) {
        settings.put(group + "." + name, value);
    }
}