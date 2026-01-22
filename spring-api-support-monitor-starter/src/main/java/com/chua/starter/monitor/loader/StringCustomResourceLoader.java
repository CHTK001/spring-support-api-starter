package com.chua.starter.monitor.loader;

// Velocity 依赖可能缺失，暂时注释
// import org.apache.velocity.exception.ResourceNotFoundException;
// import org.apache.velocity.runtime.resource.Resource;
// import org.apache.velocity.runtime.resource.loader.URLResourceLoader;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author CH
 * Velocity 资源加载器（已暂时禁用，需要添加 Velocity 依赖）
 */
// public class StringCustomResourceLoader extends URLResourceLoader {
public class StringCustomResourceLoader {
    // @Override
    public synchronized Reader getResourceReader(String name, String encoding) {
        return new StringReader(name);
    }

    // @Override
    public long getLastModified(Object resource){
        // return resource.getLastModified();
        return 0;
    }
}
