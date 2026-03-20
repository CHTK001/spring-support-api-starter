import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class ClearRedisLock {
    public static void main(String[] args) {
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName("172.16.0.40");
            config.setPort(16379);
            config.setDatabase(6);

            LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
            factory.afterPropertiesSet();

            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            template.afterPropertiesSet();

            // 清除所有key
            template.getConnectionFactory().getConnection().flushDb();

            System.out.println("✅ Redis数据库已清空，登录锁定已解除");

            factory.destroy();
        } catch (Exception e) {
            System.err.println("❌ 清除失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
