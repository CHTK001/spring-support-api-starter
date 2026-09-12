package com.chua.starter.protocol.test;

import com.chua.protocol.support.network.protocol.ServerSetting;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import java.io.File;

/**
 * 启动 FTP 文件服务器
 * @author CH
 * @since 4.0.0
 */
public class StartFtpServer {
    /**
     * Main
     *
     * @param args 参数
     */
    public static void main(String[] args) throws Exception {
        int port = 2121;
        String host = "0.0.0.0";
        String baseDir = new File("ftp-home").getAbsolutePath();

        // 确保目录存在
        new File(baseDir).mkdirs();

        ServerSetting setting = ServerSetting.builder()
                .port(port)
                .host(host)
                .build();
        // FTP 配置：匿名可读可写
        setting.getOptions().addOption("enableAnonymous", true);
        setting.getOptions().addOption("anonymousWriteEnabled", true);
        setting.getOptions().addOption("maxConnections", 10);
        setting.getOptions().addOption("homeDirectory", baseDir);
        setting.getOptions().addOption("requireFilters", false);

        ProtocolServer server = ProtocolServer.create("ftp", setting);
        server.start();
        server.awaitReady(5, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println("========================================");
        System.out.println("FTP 服务器已启动!");
        System.out.println("地址: " + host + ":" + port);
        System.out.println("根目录: " + baseDir);
        System.out.println("匿名访问: 是 (可读写)");
        System.out.println("========================================");
        System.out.println("连接方式:");
        System.out.println("  ftp://127.0.0.1:" + port);
        System.out.println("  用户名: anonymous");
        System.out.println("  密码: (任意或留空)");
        System.out.println("========================================");
        System.out.println("按 Ctrl+C 停止服务器");

        synchronized (StartFtpServer.class) {
            StartFtpServer.class.wait();
        }
    }
}

