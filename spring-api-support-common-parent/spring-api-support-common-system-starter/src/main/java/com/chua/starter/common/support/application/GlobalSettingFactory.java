package com.chua.starter.common.support.application;

import com.chua.common.support.reflection.ReflectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局设置工厂兼容类
 */
public class GlobalSettingFactory {

    public static String PREFIX = "global.setting";

    private static final GlobalSettingFactory INSTANCE = new GlobalSettingFactory();
    private final Map<String, Object> settings = new ConcurrentHashMap<>();

    /**
     * 获取实例。
     *
     * @return GlobalSetting工厂 对象
     */
    public static GlobalSettingFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获取。
     *
     * @param key 键，不允许为 null
     * @param clazz 类，不允许为 null
     * @return T 对象
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = settings.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
 // 默认值实例化统一经 ReflectUtils（无参构造缺失或实例化失败时返回 null）
        return ReflectUtils.instantiate(clazz);
    }

    
    
    /**
     * 注册。
     *
     * @param key 键，不允许为 null
     * @param value 值，不允许为 null
     */
    public void register(String key, Object value) {
        register(key, value, true);
    }

    /**
     * 获取。
     *
     * @param key 键，不允许为 null
     * @return 对象 对象
     */
    public Object get(String key) {
        return settings.get(key);
    }

    /**
     * contains。
     *
     * @param key 键，不允许为 null
     * @return 是否成功（true 表示成功）
     */
    public boolean contains(String key) {
        return settings.containsKey(key);
    }

    /**
     * 移除。
     *
     * @param key 键，不允许为 null
     */
    public void remove(String key) {
        settings.remove(key);
    }

/**
 * 注册。
 *
 * @param key 键，不允许为 null
 * @param value 值，不允许为 null
 * @param override override（布尔开关）
 */
public void register(String key, Object value, boolean override) {
        if (override || !settings.containsKey(key)) {
            settings.put(key, value);
        }
    }

/**
 * 设置。
 *
 * @param key 键，不允许为 null
 * @param value 值，不允许为 null
 */
public void set(String key, Object value) {
        settings.put(key, value);
    }

    /**
     * 设置。
     *
     * @param key 键，不允许为 null
     * @param value 值，不允许为 null
     */
    public void set(String key, Map<String, Object> value) {
        settings.put(key, value);
    }

    /**
     * 设置。
     *
     * @param group 分组，不允许为 null
     * @param name 名称，不允许为 null
     * @param value 值，不允许为 null
     */
    public void set(String group, String name, Object value) {
        settings.put(group + "." + name, value);
    }
}