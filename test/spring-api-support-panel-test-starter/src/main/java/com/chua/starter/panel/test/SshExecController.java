package com.chua.starter.panel.test;

import com.chua.starter.panel.service.NginxPanelService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 为测试环境提供 SSH 脚本执行端点。
 * <p>生产环境由 utils-support-nginx-starter 提供，测试环境临时补全。</p>
 */
@Slf4j
@RestController
@RequestMapping("/v1/panel/ssh")
@RequiredArgsConstructor
public class SshExecController {

    private static final Path DEFAULT_PRIVATE_KEY = Paths.get(
            System.getProperty("user.home"), ".ssh", "id_rsa"
    );

    private final NginxPanelService nginxPanelService;

    /**
     * 通过持久 SSH 连接执行脚本（委托 NginxPanelService）。
     * 前端 executeSshScript 传入 connectionId + {script}，
     * NginxPanelService 从 PanelSshSessionRegistry 找到对应会话并执行。
     */
    @PostMapping("/{connectionId}/execute-script")
    public ResponseEntity<Map<String, Object>> executeScript(
            @PathVariable String connectionId,
            @RequestBody Map<String, String> body) {
        String script = body.get("script");
        if (script == null || script.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "S400", "msg", "script is required", "data", ""
            ));
        }
        try {
            String output = nginxPanelService.executeScript(connectionId, script);
            return ResponseEntity.ok(Map.of("code", "S200", "msg", "success", "data", output));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "code", "S500", "msg", "Script execution failed: " + e.getMessage(), "data", ""
            ));
        }
    }

    /**
     * 打开 SSH 连接（委托 NginxPanelService，兼容 PanelSshController 缺失的场景）。
     */
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openConnection(@RequestBody Map<String, String> body) {
        String host = body.getOrDefault("host", "127.0.0.1");
        int port = parsePort(body.get("port"));
        String username = body.getOrDefault("username", "root");
        String password = body.get("password");
        String privateKey = body.get("privateKey");
        // 当密码为空且未传 privateKey 时，自动检测默认 SSH 密钥
        if (isBlank(password) && isBlank(privateKey)) {
            privateKey = detectDefaultPrivateKey();
        }
        try {
            String connectionId = nginxPanelService.open(host, port, username, password, privateKey);
            return ResponseEntity.ok(Map.of("code", "S200", "msg", "success", "data", connectionId));
        } catch (Exception e) {
            log.warn("SSH 连接失败 {}@{}:{}: {}", username, host, port, e.getMessage());
            return ResponseEntity.ok(Map.of("code", "S500", "msg", "SSH 连接失败: " + e.getMessage(), "data", ""));
        }
    }

    /**
     * 关闭 SSH 连接（委托 NginxPanelService）。
     */
    @DeleteMapping("/{connectionId}")
    public ResponseEntity<Map<String, Object>> closeConnection(@PathVariable String connectionId) {
        try {
            nginxPanelService.close(connectionId);
            return ResponseEntity.ok(Map.of("code", "S200", "msg", "success"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("code", "S500", "msg", "关闭连接失败: " + e.getMessage()));
        }
    }

    /**
     * 直接执行 SSH 脚本：前端 runSshCommand 降级时传入完整连接参数。
     */
    @PostMapping("/exec")
    public ResponseEntity<Map<String, Object>> directExec(@RequestBody Map<String, String> body) {
        String script = body.get("script");
        String host = body.get("host");
        String username = body.get("username");

        if (script == null || script.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "S400", "msg", "script is required", "data", ""
            ));
        }
        if (host == null || username == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "S400", "msg", "host and username are required", "data", ""
            ));
        }

        try {
            String output = executeSshCommand(host, parsePort(body.get("port")), username,
                    body.getOrDefault("password", ""), script);
            return ResponseEntity.ok(Map.of("code", "S200", "msg", "success", "data", output));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("code", "S500", "msg", "SSH execution failed: " + e.getMessage(), "data", ""));
        }
    }

    private static int parsePort(String portStr) {
        if (portStr == null) return 22;
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return 22;
        }
    }

    /**
     * 当密码为空时从默认路径 ~/.ssh/id_rsa 读取私钥。
     */
    private static String detectDefaultPrivateKey() {
        if (Files.exists(DEFAULT_PRIVATE_KEY)) {
            try {
                return Files.readString(DEFAULT_PRIVATE_KEY, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.debug("读取默认 SSH 密钥失败: {}", e.getMessage());
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String executeSshCommand(String host, int port, String username, String password, String script) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "password");
        session.connect(15000);

        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(script);
            channel.setErrStream(new ByteArrayOutputStream());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            channel.setOutputStream(out);
            channel.connect(60000);

            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            return out.toString(StandardCharsets.UTF_8);
        } finally {
            session.disconnect();
        }
    }
}
