package com.chua.starter.system.fix;

import com.chua.common.support.reflection.ReflectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.SetCondition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * H2 档位下的内存版 Redis，用进程内 Map 承接模板调用，不依赖外部服务端。
 *
 * <p>连接对象由 JDK 动态代理提供：未识别的指令按方法返回类型给默认值，
 * 否则基本类型会拿到 null 并在拆箱处抛空指针。</p>
 *
 * @author CH
 * @since 1.0.0
 */
@Profile("h2")
@Configuration
public class MockRedisConfiguration {

    /**
     * 键到值的映射，值为字节数组（字符串类型）或字段映射（哈希类型）。
     */
    private static final Map<String, Object> STORE = new ConcurrentHashMap<>();

    /**
     * 键到过期时刻（毫秒时间戳）的映射。
     */
    private static final Map<String, Long> EXPIRES = new ConcurrentHashMap<>();

    /**
     * 表示指令未被识别的标记。
     */
    private static final Object UNHANDLED = new Object();

    /**
     * mockRedis连接工厂。
     *
     * @return Redis连接工厂 对象
     */
    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        return newProxy(RedisConnectionFactory.class, new FactoryHandler());
    }

    /**
     * mock字符串Redis模板。
     *
     * @param factory 工厂，不允许为 null
     * @return 字符串Redis模板 对象
     */
    @Bean
    public StringRedisTemplate mockStringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * mockRedis模板。
     *
     * @param factory 工厂，不允许为 null
     * @return 结果值
     */
    @Bean
    public RedisTemplate<String, Object> mockRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.java());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.java());
        template.afterPropertiesSet();
        return template;
    }

    private static <T> T newProxy(Class<T> type, InvocationHandler handler) {
        return ReflectUtils.newProxy(MockRedisConfiguration.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    /**
     * 连接工厂代理：只负责派发连接对象。
     */
    static class FactoryHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getConnection":
                    return newProxy(RedisConnection.class, new ConnectionHandler(proxy));
                case "getClusterConnection":
                case "getSentinelConnection":
                    throw new UnsupportedOperationException("Mock: not supported");
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return args != null && args.length == 1 && proxy == args[0];
                case "toString":
                    return "MockRedisConnectionFactory";
                default:
                    return defaultValue(method);
            }
        }
    }

    /**
     * 连接代理：把指令翻译成进程内 Map 读写，其余按返回类型兜底。
     */
    static class ConnectionHandler implements InvocationHandler {

        /**
         * 所属工厂代理
         */
        private final Object factory;

        ConnectionHandler(Object factory) {
            this.factory = factory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (args == null || args.length == 0) {
                return noArg(proxy, method, name);
            }
            if ("equals".equals(name) && args.length == 1) {
                return proxy == args[0];
            }
            Object handled = command(name, args);
            return handled == UNHANDLED ? defaultValue(method) : handled;
        }

        private Object noArg(Object proxy, Method method, String name) {
            switch (name) {
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "MockRedisConnection";
                case "factory":
                    return factory;
                case "isClosed":
                    return false;
                case "isConnected":
                    return true;
                case "isPipelined":
                    return false;
                case "isTransactionActive":
                    return false;
                case "ping":
                    return "PONG";
                default:
                    break;
            }
            Class<?> type = method.getReturnType();
            if (name.endsWith("Commands") && type.isInterface()) {
                return type.isInstance(proxy) ? proxy : newProxy(type, this);
            }
            return defaultValue(method);
        }

        private Object command(String name, Object[] args) {
            switch (name) {
                case "get":
                    return args[0] instanceof byte[] ? readString(key(args[0])) : UNHANDLED;
                case "getSet": {
                    String key = key(args[0]);
                    byte[] previous = readString(key);
                    save(key, (byte[]) args[1], null);
                    return previous;
                }
                case "set":
                    return args[0] instanceof byte[] && args[1] instanceof byte[]
                            ? save(key(args[0]), (byte[]) args[1], expireAt(args), condition(args))
                            : UNHANDLED;
                case "setNX":
                    return save(key(args[0]), (byte[]) args[1], null, "ABSENT");
                case "setEx":
                    return save(key(args[0]), (byte[]) args[2], deadline(args[1], 1000L), "UPSERT");
                case "pSetEx":
                    return save(key(args[0]), (byte[]) args[2], deadline(args[1], 1L), "UPSERT");
                case "mSet":
                    return saveAll((Map<byte[], byte[]>) args[0], "UPSERT");
                case "mSetNX":
                    return saveAll((Map<byte[], byte[]>) args[0], "ABSENT");
                case "mGet":
                    return readAll(args[0]);
                case "append": {
                    String key = key(args[0]);
                    byte[] current = readString(key);
                    byte[] merged = current == null ? (byte[]) args[1] : concat(current, (byte[]) args[1]);
                    save(key, merged, null);
                    return (long) merged.length;
                }
                case "strLen": {
                    byte[] value = readString(key(args[0]));
                    return value == null ? 0L : (long) value.length;
                }
                case "incr":
                case "incrBy":
                case "decr":
                case "decrBy":
                    return moveCounter(args, name.startsWith("decr") ? -1 : 1);
                case "exists":
                    return args[0] instanceof byte[][] ? countExists((byte[][]) args[0])
                            : readString(key(args[0])) != null;
                case "del":
                case "unlink":
                    return removeKeys(args[0]);
                case "expire":
                    return applyExpire(key(args[0]), deadline(args[1], 1000L));
                case "pExpire":
                    return applyExpire(key(args[0]), deadline(args[1], 1L));
                case "expireAt":
                    return applyExpire(key(args[0]), timestamp(args[1], 1000L));
                case "pExpireAt":
                    return applyExpire(key(args[0]), timestamp(args[1], 1L));
                case "persist":
                    return EXPIRES.remove(key(args[0])) != null;
                case "ttl":
                    return remaining(key(args[0]), 1000L);
                case "pTtl":
                    return remaining(key(args[0]), 1L);
                case "type":
                    return typeOf(key(args[0]));
                case "keys":
                    return matchingKeys((byte[]) args[0]);
                case "randomKey":
                    return STORE.keySet().stream().map(MockRedisConfiguration::raw).findFirst().orElse(null);
                case "rename":
                    moveKey(key(args[0]), key(args[1]), true);
                    return null;
                case "renameNX":
                    return moveKey(key(args[0]), key(args[1]), false);
                case "hSet":
                    return args[1] instanceof Map
                            ? putHash(key(args[0]), (Map<byte[], byte[]>) args[1])
                            : putHashField(key(args[0]), key(args[1]), (byte[]) args[2], false);
                case "hSetNX":
                    return putHashField(key(args[0]), key(args[1]), (byte[]) args[2], true);
                case "hMSet":
                    return putHash(key(args[0]), (Map<byte[], byte[]>) args[1]);
                case "hGet":
                    return hashOf(key(args[0]), false).get(key(args[1]));
                case "hGetAll":
                    return rawHash(hashOf(key(args[0]), false));
                case "hKeys":
                    return rawFields(hashOf(key(args[0]), false).keySet());
                case "hVals":
                    return new ArrayList<>(hashOf(key(args[0]), false).values());
                case "hLen":
                    return (long) hashOf(key(args[0]), false).size();
                case "hExists":
                    return hashOf(key(args[0]), false).containsKey(key(args[1]));
                case "hDel":
                    return removeHashFields(key(args[0]), args);
                case "hIncrBy":
                    return moveHashCounter(key(args[0]), key(args[1]), args[2]);
                default:
                    return UNHANDLED;
            }
        }
    }

    // ------------------------------------------------------------------ 字符串与键

    private static String key(Object raw) {
        return raw instanceof byte[] ? new String((byte[]) raw, StandardCharsets.UTF_8) : String.valueOf(raw);
    }

    private static byte[] raw(String key) {
        return key.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] readString(String key) {
        prune(key);
        return STORE.get(key) instanceof byte[] ? (byte[]) STORE.get(key) : null;
    }

    private static Boolean save(String key, byte[] value, Long deadline, String condition) {
        boolean present = readString(key) != null || !hashOf(key, false).isEmpty();
        if (condition.contains("ABSENT") && present) {
            return false;
        }
        if (condition.contains("PRESENT") && !present) {
            return false;
        }
        save(key, value, deadline);
        return true;
    }

    private static void save(String key, byte[] value, Long deadline) {
        STORE.put(key, value);
        if (deadline != null) {
            EXPIRES.put(key, deadline);
        }
    }

    private static void prune(String key) {
        Long deadline = EXPIRES.get(key);
        if (deadline != null && deadline <= System.currentTimeMillis()) {
            EXPIRES.remove(key);
            STORE.remove(key);
        }
    }

    /**
     * 从 set 指令参数里取过期时刻，兼容时长与绝对时间两种写法。
     */
    private static Long expireAt(Object[] args) {
        long now = System.currentTimeMillis();
        for (Object arg : args) {
            if (arg instanceof Expiration) {
                Expiration expiration = (Expiration) arg;
                if (expiration.isPersistent() || expiration.isKeepTtl()) {
                    continue;
                }
                long value = expiration.getExpirationTimeInMilliseconds();
                return expiration.isUnixTimestamp() ? value : now + value;
            }
            if (arg instanceof Duration) {
                return now + ((Duration) arg).toMillis();
            }
        }
        return null;
    }

    private static String condition(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof SetCondition) {
                return String.valueOf(((SetCondition) arg).getKeyCondition());
            }
            if (arg instanceof RedisStringCommands.SetOption) {
                return String.valueOf(arg);
            }
        }
        return "UPSERT";
    }

    /**
     * 时长参数换算为过期时刻；秒与毫秒由调用方给出倍率。
     */
    private static Long deadline(Object arg, long millisPerUnit) {
        long now = System.currentTimeMillis();
        if (arg instanceof Duration) {
            return now + ((Duration) arg).toMillis();
        }
        if (arg instanceof Expiration) {
            Expiration expiration = (Expiration) arg;
            long value = expiration.getExpirationTimeInMilliseconds();
            return expiration.isUnixTimestamp() ? value : now + value;
        }
        return arg instanceof Number ? now + ((Number) arg).longValue() * millisPerUnit : null;
    }

    /**
     * 绝对时间参数换算为毫秒时间戳；秒或毫秒由调用方给出倍率。
     */
    private static Long timestamp(Object arg, long millisPerUnit) {
        if (arg instanceof java.time.Instant) {
            return ((java.time.Instant) arg).toEpochMilli();
        }
        if (arg instanceof Duration) {
            return System.currentTimeMillis() + ((Duration) arg).toMillis();
        }
        return arg instanceof Number ? ((Number) arg).longValue() * millisPerUnit : null;
    }

    private static Boolean applyExpire(String key, Long deadline) {
        if (deadline == null) {
            return false;
        }
        prune(key);
        if (!STORE.containsKey(key)) {
            return false;
        }
        EXPIRES.put(key, deadline);
        return true;
    }

    private static Long remaining(String key, long divisor) {
        prune(key);
        if (!STORE.containsKey(key)) {
            return -2L;
        }
        Long deadline = EXPIRES.get(key);
        return deadline == null ? -1L : (deadline - System.currentTimeMillis()) / divisor;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] merged = new byte[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }

    private static Boolean saveAll(Map<byte[], byte[]> values, String condition) {
        boolean done = false;
        for (Map.Entry<byte[], byte[]> entry : values.entrySet()) {
            done |= Boolean.TRUE.equals(save(key(entry.getKey()), entry.getValue(), null, condition));
        }
        return done;
    }

    private static List<byte[]> readAll(Object arg) {
        List<byte[]> values = new ArrayList<>();
        if (arg instanceof byte[][]) {
            for (byte[] item : (byte[][]) arg) {
                values.add(readString(key(item)));
            }
        } else if (arg instanceof Iterable) {
            for (Object item : (Iterable<?>) arg) {
                values.add(readString(key(item)));
            }
        }
        return values;
    }

    private static Long countExists(byte[][] keys) {
        long count = 0;
        for (byte[] item : keys) {
            count += readString(key(item)) == null ? 0 : 1;
        }
        return count;
    }

    private static Long removeKeys(Object arg) {
        long count = 0;
        for (String key : flatten(arg)) {
            count += STORE.remove(key) == null ? 0 : 1;
            EXPIRES.remove(key);
        }
        return count;
    }

    private static List<String> flatten(Object arg) {
        List<String> keys = new ArrayList<>();
        if (arg instanceof byte[][]) {
            for (byte[] item : (byte[][]) arg) {
                keys.add(key(item));
            }
        } else if (arg instanceof byte[]) {
            keys.add(key(arg));
        } else if (arg instanceof Iterable) {
            for (Object item : (Iterable<?>) arg) {
                keys.add(key(item));
            }
        }
        return keys;
    }

    private static Boolean moveKey(String from, String to, boolean force) {
        prune(from);
        Object value = STORE.get(from);
        if (value == null || (!force && STORE.containsKey(to))) {
            return false;
        }
        moveEntry(from, to);
        return true;
    }

    private static void moveEntry(String from, String to) {
        STORE.put(to, STORE.remove(from));
        Long deadline = EXPIRES.remove(from);
        if (deadline != null) {
            EXPIRES.put(to, deadline);
        }
    }

    private static DataType typeOf(String key) {
        prune(key);
        Object value = STORE.get(key);
        if (value == null) {
            return DataType.NONE;
        }
        return value instanceof Map ? DataType.HASH : DataType.STRING;
    }

    private static Set<byte[]> matchingKeys(byte[] pattern) {
        String glob = key(pattern);
        Set<byte[]> matched = new LinkedHashSet<>();
        for (String candidate : new ArrayList<>(STORE.keySet())) {
            prune(candidate);
            if (STORE.containsKey(candidate) && matches(glob, candidate)) {
                matched.add(raw(candidate));
            }
        }
        return matched;
    }

    private static boolean matches(String glob, String value) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                regex.append(".*");
            } else if (c == '?') {
                regex.append('.');
            } else {
                regex.append(java.util.regex.Pattern.quote(String.valueOf(c)));
            }
        }
        return value.matches(regex.toString());
    }

    private static Object moveCounter(Object[] args, int sign) {
        String key = key(args[0]);
        byte[] current = readString(key);
        double step = args.length > 1 ? ((Number) args[1]).doubleValue() : 1d;
        double base = current == null ? 0d : Double.parseDouble(new String(current, StandardCharsets.UTF_8));
        double next = base + sign * step;
        boolean decimal = args.length > 1 && !(args[1] instanceof Long) && !(args[1] instanceof Integer);
        String text = decimal ? String.valueOf(next) : String.valueOf((long) next);
        save(key, text.getBytes(StandardCharsets.UTF_8), null);
        return decimal ? (Object) next : (Object) (long) next;
    }

    // ------------------------------------------------------------------ 哈希

    @SuppressWarnings("unchecked")
    private static Map<String, byte[]> hashOf(String key, boolean create) {
        prune(key);
        Object value = STORE.get(key);
        if (value instanceof Map) {
            return (Map<String, byte[]>) value;
        }
        if (value == null && create) {
            Map<String, byte[]> created = new ConcurrentHashMap<>();
            STORE.put(key, created);
            return created;
        }
        return create ? new ConcurrentHashMap<>() : Collections.emptyMap();
    }

    private static Boolean putHashField(String key, String field, byte[] value, boolean onlyIfAbsent) {
        Map<String, byte[]> hash = hashOf(key, true);
        if (onlyIfAbsent && hash.containsKey(field)) {
            return false;
        }
        boolean added = !hash.containsKey(field);
        hash.put(field, value);
        return added;
    }

    private static Boolean putHash(String key, Map<byte[], byte[]> values) {
        Map<String, byte[]> hash = hashOf(key, true);
        values.forEach((field, value) -> hash.put(key(field), value));
        return true;
    }

    private static Map<byte[], byte[]> rawHash(Map<String, byte[]> hash) {
        Map<byte[], byte[]> entries = new LinkedHashMap<>();
        hash.forEach((field, value) -> entries.put(raw(field), value));
        return entries;
    }

    private static Set<byte[]> rawFields(Set<String> fields) {
        Set<byte[]> names = new LinkedHashSet<>();
        fields.forEach(field -> names.add(raw(field)));
        return names;
    }

    private static Long removeHashFields(String key, Object[] args) {
        Map<String, byte[]> hash = hashOf(key, false);
        long count = 0;
        for (int i = 1; i < args.length; i++) {
            for (String field : flatten(args[i])) {
                count += hash.remove(field) == null ? 0 : 1;
            }
        }
        return count;
    }

    private static Object moveHashCounter(String key, String field, Object stepArg) {
        Map<String, byte[]> hash = hashOf(key, true);
        byte[] current = hash.get(field);
        double next = (current == null ? 0d : Double.parseDouble(new String(current, StandardCharsets.UTF_8)))
                + ((Number) stepArg).doubleValue();
        boolean decimal = stepArg instanceof Double;
        String text = decimal ? String.valueOf(next) : String.valueOf((long) next);
        hash.put(field, text.getBytes(StandardCharsets.UTF_8));
        return decimal ? (Object) next : (Object) (long) next;
    }

    // ------------------------------------------------------------------ 兜底

    /**
     * 未识别指令按返回类型给默认值，基本类型不能返回 null。
     */
    private static Object defaultValue(Method method) {
        Class<?> type = method.getReturnType();
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == float.class) {
            return 0f;
        }
        return (char) 0;
    }
}
