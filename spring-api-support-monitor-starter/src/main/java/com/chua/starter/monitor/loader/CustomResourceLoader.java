package com.chua.starter.monitor.loader;

import com.chua.common.support.core.utils.IoUtils;
// Velocity 依赖可能缺失，暂时注释
// import org.apache.velocity.exception.ResourceNotFoundException;
// import org.apache.velocity.runtime.resource.Resource;
// import org.apache.velocity.runtime.resource.loader.URLResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author CH
 * Velocity 资源加载器（已暂时禁用，需要添加 Velocity 依赖）
 */
// public class CustomResourceLoader extends URLResourceLoader {
public class CustomResourceLoader {
    // Velocity 依赖可能缺失，暂时注释
    // @Override
    public synchronized Reader getResourceReader(String name, String encoding) {
        InputStream rawStream = null;
        try {
            URL u = new URL(name);
            URLConnection conn = u.openConnection();
            int timeout = getTimeout();
            if(timeout > 0) {
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
            }
            rawStream = conn.getInputStream();
            return buildReader(rawStream, encoding);
        } catch (IOException e) {
            IoUtils.closeQuietly(rawStream);
        }
        // return super.getResourceReader(name, encoding);
        return null;
    }

    /**
     * 获取超时时间
     */
    private int getTimeout() {
        return 5000; // 默认5秒
    }

    /**
     * 构建 Reader
     */
    private Reader buildReader(InputStream inputStream, String encoding) throws IOException {
        if (encoding == null || encoding.isEmpty()) {
            encoding = "UTF-8";
        }
        return new java.io.InputStreamReader(inputStream, encoding);
    }

    // @Override
    public long getLastModified(Object resource){
        // return resource.getLastModified();
        return 0;
    }
}
