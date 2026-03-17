import redis.clients.jedis.Jedis;
import java.util.Set;
public class RedisOps {
    public static void main(String[] args) throws Exception {
        try (Jedis j = new Jedis("172.16.0.40", 16379)) {
            j.select(15);
            Set<String> keys = j.keys("oauth:login:*");
            System.out.println("Keys: " + keys.size());
            for (String k : keys) { System.out.println("  DEL " + k); j.del(k); }
            System.out.println("Done");
        }
    }
}
