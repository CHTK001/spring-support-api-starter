import java.net.InetSocketAddress;
import com.chua.kcp.support.client.KcpClient;

/**
 * KCP 集成测试(在 oauth 模块内运行,自动有完整 classpath)
 * @author CH
 */
public class KcpIntegrationTest {
    /** Main */
    public static void main(String[] args) throws Exception {
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        KcpClient client = new KcpClient("kcp-test", "kcp://localhost:19186");
        try {
            System.out.println("Connecting...");
            client.connect();
            System.out.println("KCP CONNECTED");
            String response = client.execute("login", body, 15000);
            System.out.println("KCP RESPONSE: " + response);
        } catch (Exception e) {
            System.out.println("KCP ERROR: " + e);
            e.printStackTrace();
        } finally {
            client.disconnect();
        }
    }
}

