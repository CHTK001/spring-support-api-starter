import redis.clients.jedis.Jedis;
import java.util.Set;
public class UnlockRedis {
    public static void main(String[] args) throws Exception {
        try (Jedis j = new Jedis("172.16.0.40", 16379)) {
            j.select(15);
            Set<String> keys = j.keys("oauth:login:locked:*");
            keys.addAll(j.keys("oauth:login:attempt:*"));
            System.out.println("Deleting " + keys.size() + " keys:");
            for (String k : keys) { System.out.println("  DEL " + k); j.del(k); }
            System.out.println("Done");
        }
    }
}
