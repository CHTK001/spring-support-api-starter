package com.chua.starter.oauth.sso.support.fastjson;//package com.chua.starter.oauth.server.support.fastjson;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Data;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author CH
 */
@Data
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    /** 类型 */
    private Class<T> type;
    /** Features */
    private JSONReader.Feature[] features;

    /** Writefeatures */
    private JSONWriter.Feature[] writeFeatures;

    /** Filters */
    private Filter[] filters;

    /** 格式 */
    private String format = "yyyy-MM-dd HH:mm:ss";

    public FastJsonRedisSerializer(Class<T> type) {
        this.type = type;
    }


    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        } else {
            try {
                return JSON.toJSONBytes(t, format, filters, writeFeatures);
            } catch (Exception var3) {
                throw new SerializationException("Could not serialize: " + var3.getMessage(), var3);
            }
        }
    }

    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes != null && bytes.length != 0) {
            try {
                return JSON.parseObject(bytes, type, features);
            } catch (Exception var3) {
                throw new SerializationException("Could not deserialize: " + var3.getMessage(), var3);
            }
        } else {
            return null;
        }
    }

}
