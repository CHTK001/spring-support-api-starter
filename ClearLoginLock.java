import redis.clients.jedis.Jedis;
import java.util.Set;

public class ClearLoginLock {
    public static void main(String[] args) {
        String redisHost = "172.16.0.40";
        int redisPort = 16379;
        int redisDb = 6;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            jedis.select(redisDb);
            System.out.println("Connected to Redis: " + redisHost + ":" + redisPort + " DB:" + redisDb);

            // Find ALL keys
            Set<String> allKeys = jedis.keys("*");
            System.out.println("\nFound " + allKeys.size() + " total keys in Redis");

            // Filter for sa or admin related keys
            System.out.println("\nKeys related to 'sa' or 'admin':");
            for (String key : allKeys) {
                if (key.toLowerCase().contains("sa") || key.toLowerCase().contains("admin") ||
                    key.toLowerCase().contains("lock") || key.toLowerCase().contains("error")) {
                    System.out.println("  - " + key);
                    try {
                        String value = jedis.get(key);
                        if (value != null && value.length() < 200) {
                            System.out.println("    Value: " + value);
                        }
                    } catch (Exception e) {
                        System.out.println("    (not a string value)");
                    }
                }
            }

            // Delete all keys to clear any locks
            System.out.println("\nClearing all Redis keys to remove any locks...");
            jedis.flushDB();
            System.out.println("✅ All Redis keys cleared!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
