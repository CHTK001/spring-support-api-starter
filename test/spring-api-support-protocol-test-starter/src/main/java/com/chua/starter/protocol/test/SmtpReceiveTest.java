package com.chua.starter.protocol.test;

import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * SMTP 收邮件测试 - 启动服务器并发送测试邮件
 * <p>
 * 验证本地 SMTP 服务器能够正常接收邮件。
 * 验证方式：通过 SMTP 协议原始命令连接服务器并发送邮件，
 * 通过 SMTP 服务器日志观察邮件是否正确接收。
 */
@Slf4j
public class SmtpReceiveTest {

    private static final int SMTP_PORT = 2525;
    /** 成功计数 */
    private static int successCount = 0;
    /** 失败计数 */
    private static int failCount = 0;

    public static void main(String[] args) throws Exception {
        log.info("=== SMTP 邮件接收测试开始 ===");

        // 1. 创建并启动 SMTP 服务器
        ServerSetting setting = ServerSetting.builder()
                .port(SMTP_PORT)
                .host("0.0.0.0")
                .build();
        setting.getOptions().addOption("hostname", "mail.test.local");
        setting.getOptions().addOption("maxMessageSize", 10485760L);
        setting.getOptions().addOption("requireAuth", false);
        setting.getOptions().addOption("failOnFilterRejection", false);

        ProtocolServer server = ProtocolServer.create("smtp", setting);
        server.start();
        log.info("SMTP 服务器已启动: 0.0.0.0:{}", SMTP_PORT);

        // 等待服务器就绪
        server.awaitReady(5, java.util.concurrent.TimeUnit.SECONDS);
        Thread.sleep(500);

        try {
            // 2. 发送测试邮件 - 纯文本
            log.info("--- 发第一封邮件: 纯文本 ---");
            testSendEmail("sender@example.com", "recipient@example.com",
                    "测试邮件 - 来自 SMTP 客户端",
                    "您好！\n这是一封来自 Java SMTP 客户端的测试邮件。\n\n如果收到这封邮件，说明 SMTP 服务器工作正常！\n\n祝好！");

            // 3. 发送测试邮件 - HTML 格式
            log.info("--- 发第二封邮件: HTML ---");
            testSendEmail("admin@test.com", "user@test.com",
                    "第二封测试邮件 - HTML 格式",
                    "<html><body><h1>测试邮件</h1><p>这是一封 <b>HTML</b> 格式的测试邮件。</p></body></html>");

            // 4. 发送测试邮件 - 带多收件人
            log.info("--- 发第三封邮件: 多收件人 ---");
            testSendEmailToMultiple("notify@test.com",
                    new String[]{"alice@test.com", "bob@test.com"},
                    "第三封测试邮件 - 群发",
                    "这是一封群发测试邮件。");

            // 5. 输出测试结果
            log.info("=== SMTP 邮件发送测试结果 ===");
            log.info("成功: {} 封, 失败: {} 封", successCount, failCount);
            if (failCount == 0) {
                log.info("=== SMTP 邮件接收测试全部通过 ===");
            } else {
                log.error("=== SMTP 邮件接收测试有失败项 ===");
            }
        } catch (Exception e) {
            log.error("测试失败", e);
            throw e;
        } finally {
            server.stop();
            log.info("SMTP 服务器已停止");
        }
    }

    static void testSendEmail(String from, String to, String subject, String body) throws IOException {
        log.info("发送邮件: {} -> {}", from, to);

        try (Socket socket = new Socket("127.0.0.1", SMTP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(10000);

            // 读取欢迎信息
            String response = readResponse(reader);
            checkResponse(response, "220");

            // EHLO
            sendCommand(writer, reader, "EHLO testclient");

            // MAIL FROM
            sendCommand(writer, reader, "MAIL FROM:<" + from + ">");

            // RCPT TO
            sendCommand(writer, reader, "RCPT TO:<" + to + ">");

            // DATA
            sendCommand(writer, reader, "DATA");

            // 邮件内容
            writer.write("From: " + from + "\r\n");
            writer.write("To: " + to + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Message-ID: <" + System.currentTimeMillis() + "@testclient>\r\n");
            writer.write("Date: " + new java.util.Date() + "\r\n");
            writer.write("MIME-Version: 1.0\r\n");
            if (body.startsWith("<html")) {
                writer.write("Content-Type: text/html; charset=UTF-8\r\n");
            } else {
                writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            }
            writer.write("\r\n");
            writer.write(body);
            writer.write("\r\n.\r\n");
            writer.flush();

            response = readResponse(reader);
            checkResponse(response, "250");
            log.info("DATA 响应: {}", response);

            // QUIT
            sendCommand(writer, reader, "QUIT");
        }

        log.info("邮件发送完成: {} -> {}", from, to);
        successCount++;
    }

    static void testSendEmailToMultiple(String from, String[] tos, String subject, String body) throws IOException {
        log.info("群发邮件: {} -> {}", from, String.join(", ", tos));

        try (Socket socket = new Socket("127.0.0.1", SMTP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(10000);

            // 读取欢迎信息
            readResponse(reader);

            // EHLO
            sendCommand(writer, reader, "EHLO testclient");

            // MAIL FROM
            sendCommand(writer, reader, "MAIL FROM:<" + from + ">");

            // RCPT TO 多个收件人
            for (String to : tos) {
                sendCommand(writer, reader, "RCPT TO:<" + to + ">");
            }

            // DATA
            sendCommand(writer, reader, "DATA");

            writer.write("From: " + from + "\r\n");
            writer.write("To: " + String.join(", ", tos) + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Message-ID: <" + System.currentTimeMillis() + "@testclient>\r\n");
            writer.write("Date: " + new java.util.Date() + "\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write(body);
            writer.write("\r\n.\r\n");
            writer.flush();

            String response = readResponse(reader);
            checkResponse(response, "250");

            sendCommand(writer, reader, "QUIT");
        }

        log.info("群发完成: {} -> {}", from, String.join(", ", tos));
        successCount++;
    }

    static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        log.info(">>> {}", command);
        writer.write(command + "\r\n");
        writer.flush();
        String response = readResponse(reader);
        log.info("<<< {}", response);
        checkResponse(response);
    }

    static void checkResponse(String response) {
        if (response.startsWith("5")) {
            failCount++;
            log.error("SMTP 命令返回错误: {}", response);
        }
    }

    static void checkResponse(String response, String expectedCode) {
        if (!response.startsWith(expectedCode)) {
            failCount++;
            log.error("期望响应以 {} 开头，实际: {}", expectedCode, response);
        }
    }

    static String readResponse(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            if (line.length() >= 4 && line.charAt(3) == ' ') {
                break;
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
