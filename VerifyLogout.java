import redis.clients.jedis.Jedis;
import java.util.Set;

/**
 * 验证注销后 Redis 中的 token 是否被删除
 * 用法：
 *   1. 先登录获取 token
 *   2. java VerifyLogout <token>
 *   3. 注销后再次运行：java VerifyLogout <token>
 *
 * 编译：javac -cp jedis-*.jar VerifyLogout.java
 * 运行：java -cp .;jedis-*.jar VerifyLogout <token>
 */
public class VerifyLogout {

    static final String HOST = "172.16.0.40";
    static final int PORT = 16379;
    static final int DB = 15;
    static final String TOKEN_PRE = "oauth:token:";
    static final String REFRESH_PRE = "oauth:token:refresh:";
    static final String BLACKLIST_PRE = "oauth:blacklist:";

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = new Jedis(HOST, PORT)) {
            jedis.select(DB);

            if (args.length > 0) {
                // 查指定 token
                String token = args[0];
                checkToken(jedis, token);
            } else {
                // 列出所有 oauth:token:* key
                System.out.println("=== 当前 Redis DB" + DB + " 中的 oauth:token:* ===");
                Set<String> keys = jedis.keys(TOKEN_PRE + "*");
                if (keys.isEmpty()) {
                    System.out.println("  (空) — 没有任何 token");
                } else {
                    for (String key : keys) {
                        long ttl = jedis.ttl(key);
                        System.out.printf("  key=%-60s  TTL=%ds%n", key, ttl);
                    }
                }

                System.out.println("\n=== oauth:token:refresh:* ===");
                Set<String> refreshKeys = jedis.keys(REFRESH_PRE + "*");
                if (refreshKeys.isEmpty()) {
                    System.out.println("  (空)");
                } else {
                    for (String key : refreshKeys) {
                        long ttl = jedis.ttl(key);
                        System.out.printf("  key=%-60s  TTL=%ds%n", key, ttl);
                    }
                }

                System.out.println("\n=== oauth:blacklist:* ===");
                Set<String> blacklistKeys = jedis.keys(BLACKLIST_PRE + "*");
                if (blacklistKeys.isEmpty()) {
                    System.out.println("  (空)");
                } else {
                    for (String key : blacklistKeys) {
                        long ttl = jedis.ttl(key);
                        System.out.printf("  key=%-60s  TTL=%ds%n", key, ttl);
                    }
                }
            }
        }
    }

    static void checkToken(Jedis jedis, String token) {
        String tokenKey = TOKEN_PRE + token;
        boolean exists = jedis.exists(tokenKey);
        System.out.println("=== Token 验证 ===");
        System.out.println("  token    : " + token);
        System.out.println("  redis key: " + tokenKey);
        System.out.println("  存在?    : " + (exists ? "YES ✗ (未注销)" : "NO  ✓ (已注销)"));
        if (exists) {
            long ttl = jedis.ttl(tokenKey);
            System.out.println("  TTL      : " + ttl + "s");
        }

        // 检查黑名单
        String blacklistKey = BLACKLIST_PRE + token;
        boolean inBlacklist = jedis.exists(blacklistKey);
        System.out.println("  黑名单?  : " + (inBlacklist ? "YES ✓ (已加入黑名单)" : "NO"));
    }
}
