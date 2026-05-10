package com.chua.starter.protocol.test;

import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.common.support.network.protocol.server.ProtocolServer;

/**
 * 启动 SMTP 服务器供外部客户端连接测试
 */
public class StartSmtpServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 2525;
        String host = args.length > 1 ? args[1] : "0.0.0.0";

        ServerSetting setting = ServerSetting.builder()
                .port(port)
                .host(host)
                .build();
        setting.getOptions().addOption("hostname", "mail.test.local");
        setting.getOptions().addOption("maxMessageSize", 10485760L);
        setting.getOptions().addOption("requireAuth", false);
        setting.getOptions().addOption("failOnFilterRejection", false);
        setting.getOptions().addOption("requireTls", false);

        ProtocolServer server = ProtocolServer.create("smtp", setting);
        server.start();
        server.awaitReady(5, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println("========================================");
        System.out.println("SMTP 服务器已启动!");
        System.out.println("地址: " + host + ":" + port);
        System.out.println("无需认证(No Auth)");
        System.out.println("========================================");
        System.out.println("请在邮件客户端中配置:");
        System.out.println("  SMTP 服务器: " + ("0.0.0.0".equals(host) ? "127.0.0.1" : host));
        System.out.println("  端口: " + port);
        System.out.println("  无需认证 / 不勾选 SSL/TLS");
        System.out.println("========================================");
        System.out.println("按 Ctrl+C 停止服务器");
        System.out.println();

        // 保持运行
        synchronized (StartSmtpServer.class) {
            StartSmtpServer.class.wait();
        }
    }
}
