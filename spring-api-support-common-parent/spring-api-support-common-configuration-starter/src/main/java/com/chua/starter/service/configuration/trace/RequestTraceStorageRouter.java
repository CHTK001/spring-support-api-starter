package com.chua.starter.service.configuration.trace;

import com.chua.common.support.reflection.ReflectUtils;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.service.configuration.trace.storage.FileRequestTraceRawRecordStorage;
import com.chua.starter.service.configuration.trace.storage.InMemoryRequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.storage.MemoryRequestTraceBehaviorStorage;
import org.springframework.stereotype.Component;

/**
* 请求追踪 SPI 路由器。
*
* @since 2026/03/25
* @author CH
 */
@Component
public class RequestTraceStorageRouter {

    /** Database_配置_storage */
    private static final String DATABASE_CONFIG_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceConfigStorage";
    /** Database_raw_storage */
    private static final String DATABASE_RAW_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceRawRecordStorage";
    /** Database_行为_storage */
    private static final String DATABASE_BEHAVIOR_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceBehaviorStorage";

    /**
    * 配置storage
    *
    * @param type 类型
    * @return 配置storage的结果
     */
    public RequestTraceConfigStorage configStorage(String type) {
        RequestTraceConfigStorage storage = ServiceProvider.of(RequestTraceConfigStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "database"));
        if (storage != null) {
            return storage;
        }
        storage = instantiate(DATABASE_CONFIG_STORAGE, RequestTraceConfigStorage.class);
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("database");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("redis");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("memory");
        return storage != null ? storage : new InMemoryRequestTraceConfigStorage();
    }

    /**
    * rawstorage
    *
    * @param type 类型
    * @return rawStorage的结果
     */
    public RequestTraceRawRecordStorage rawStorage(String type) {
        RequestTraceRawRecordStorage storage = ServiceProvider.of(RequestTraceRawRecordStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "file"));
        if (storage != null) {
            return storage;
        }
        if ("database".equalsIgnoreCase(type)) {
            storage = instantiate(DATABASE_RAW_STORAGE, RequestTraceRawRecordStorage.class);
            if (storage != null) {
                return storage;
            }
        }
        storage = ServiceProvider.of(RequestTraceRawRecordStorage.class).getNewExtension("file");
        return storage != null ? storage : new FileRequestTraceRawRecordStorage();
    }

    /**
    * 行为storage
    *
    * @param type 类型
    * @return 行为storage的结果
     */
    public RequestTraceBehaviorStorage behaviorStorage(String type) {
        RequestTraceBehaviorStorage storage = ServiceProvider.of(RequestTraceBehaviorStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "database"));
        if (storage != null) {
            return storage;
        }
        storage = instantiate(DATABASE_BEHAVIOR_STORAGE, RequestTraceBehaviorStorage.class);
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceBehaviorStorage.class).getNewExtension("database");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceBehaviorStorage.class).getNewExtension("memory");
        return storage != null ? storage : new MemoryRequestTraceBehaviorStorage();
    }

    /**
    * Instantiate
    *
    * @param className 类名称
    * @param type 类型
    * @return instantiate的结果
     */
    private <T> T instantiate(String className, Class<T> type) {
        T instance = ReflectUtils.instantiate(className);
        if (instance != null && type.isInstance(instance)) {
            return type.cast(instance);
        }
        return null;
    }
}
