package com.chua.starter.protocol.test;

import com.chua.protocol.support.network.protocol.ServerSetting;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * SMTP 服务器手动测试类
 * @author CH
 */
@Slf4j
public class SmtpServerTest {

    /** Smtp_port */
    private static final int SMTP_PORT = 2525;

    public static void main(String[] args) throws Exception {
        log.info("=== SMTP 服务器测试开始 ===");

        // 1. 创建并启动 SMTP 服务器
        ServerSetting setting = ServerSetting.builder()
                .port(SMTP_PORT)
                .host("0.0.0.0")
                .build();
        setting.getOptions().addOption("hostname", "test.local");
        setting.getOptions().addOption("maxMessageSize", 10485760L);
        setting.getOptions().addOption("requireAuth", false);
        setting.getOptions().addOption("failOnFilterRejection", false);

        ProtocolServer server = ProtocolServer.create("smtp", setting);
        server.start();
        log.info("SMTP 服务器已启动: 0.0.0.0:{}", SMTP_PORT);

        // 2. 等待服务器就绪
        Thread.sleep(1000);

        try {
            testBasicSmtp();
            testAuthSmtp();
            log.info("=== 所有 SMTP 测试通过 ===");
        } catch (Exception e) {
            log.error("测试失败", e);
            throw e;
        } finally {
            server.stop();
            log.info("SMTP 服务器已停止");
        }
    }

    /**
     * 测试基本 SMTP 邮件发送（无认证）
     */
    static void testBasicSmtp() throws IOException {
        log.info("--- 测试: 基本 SMTP 邮件发送 ---");

        try (Socket socket = new Socket("127.0.0.1", SMTP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(5000);

            // 读取欢迎信息
            String response = readResponse(reader);
            assert response.startsWith("220") : "期望 220 欢迎信息，得到: " + response;
            log.info("Server: {}", response);

            // EHLO
            sendCommand(writer, reader, "EHLO testclient");

            // MAIL FROM
            sendCommand(writer, reader, "MAIL FROM:<sender@test.com>");

            // RCPT TO
            sendCommand(writer, reader, "RCPT TO:<recipient@test.com>");

            // DATA
            sendCommand(writer, reader, "DATA");

            // 邮件内容
            writer.write("From: sender@test.com\r\n");
            writer.write("To: recipient@test.com\r\n");
            writer.write("Subject: Test Email\r\n");
            writer.write("\r\n");
            writer.write("Hello, this is a test email.\r\n");
            writer.write(".\r\n");
            writer.flush();

            // 读取 DATA 响应
            response = readResponse(reader);
            log.info("Server (after DATA): {}", response);
            assert response.startsWith("250") || response.startsWith("554") :
                    "期望 250 或 554 响应，得到: " + response;

            // QUIT
            sendCommand(writer, reader, "QUIT");
        }

        log.info("--- 基本 SMTP 测试通过 ---");
    }

    /**
     * 测试带认证的 SMTP AUTH LOGIN
     */
    static void testAuthSmtp() throws Exception {
        log.info("--- 测试: 带认证的 SMTP AUTH LOGIN ---");

        // 启动一个带认证的 SMTP 服务器
        ServerSetting authSetting = ServerSetting.builder()
                .port(2526)
                .host("0.0.0.0")
                .build();
        authSetting.getOptions().addOption("hostname", "auth.local");
        authSetting.getOptions().addOption("requireAuth", "true");
        authSetting.getOptions().addOption("requireTls", "false");
        java.util.Map<String, Object> account = new java.util.HashMap<>();
        account.put("username", "testuser");
        account.put("password", "testpass");
        account.put("enabled", true);
        authSetting.getOptions().addOption("accounts", new Object[]{account});

        ProtocolServer authServer = ProtocolServer.create("smtp", authSetting);
        authServer.start();
        Thread.sleep(500);

        try {
            // 测试认证成功
            try (Socket socket = new Socket("127.0.0.1", 2526);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

                socket.setSoTimeout(5000);

                // 读取欢迎信息
                String response = readResponse(reader);
                assert response.startsWith("220") : "期望 220 欢迎信息，得到: " + response;
                log.info("Auth Server: {}", response);

                // EHLO
                sendCommand(writer, reader, "EHLO testclient");

                // AUTH LOGIN
                sendCommand(writer, reader, "AUTH LOGIN");

                // 发送用户名（Base64 编码）
                String userB64 = java.util.Base64.getEncoder().encodeToString("testuser".getBytes(StandardCharsets.UTF_8));
                sendCommand(writer, reader, userB64);

                // 发送密码（Base64 编码）
                String passB64 = java.util.Base64.getEncoder().encodeToString("testpass".getBytes(StandardCharsets.UTF_8));
                sendCommand(writer, reader, passB64);

                // MAIL FROM
                sendCommand(writer, reader, "MAIL FROM:<auth-sender@test.com>");

                // RCPT TO
                sendCommand(writer, reader, "RCPT TO:<auth-recipient@test.com>");

                // QUIT
                sendCommand(writer, reader, "QUIT");
            }

            log.info("认证 SMTP 测试通过");
        } finally {
            authServer.stop();
        }
    }

    static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        log.info("Client: {}", command);
        writer.write(command + "\r\n");
        writer.flush();
        String response = readResponse(reader);
        log.info("Server: {}", response);
        // SMTP 2xx 是成功，3xx 是中间响应（如 AUTH challenge）
        assert !response.startsWith("5") : "服务器返回错误: " + response;
    }

    static String readResponse(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            // SMTP 多行响应：如果第4个字符是空格表示最后一行
            if (line.length() >= 4 && line.charAt(3) == ' ') {
                break;
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

