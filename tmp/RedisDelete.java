import redis.clients.jedis.Jedis;

public class RedisDelete {
    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: RedisDelete <host> <port> <db> <key1> [key2...]");
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int db = Integer.parseInt(args[2]);

        try (Jedis jedis = new Jedis(host, port)) {
            jedis.select(db);
            for (int i = 3; i < args.length; i++) {
                long deleted = jedis.del(args[i]);
                System.out.println(args[i] + "=" + deleted);
            }
        }
    }
}
