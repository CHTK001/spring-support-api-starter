package com.chua.starter.datalake.server.auth;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.common.support.text.json.Json;
import com.chua.datalake.support.spi.auth.TokenService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 文件持久化令牌服务 — Token 数据存储为 JSON 文件
 * <p>默认存储路径: {@code data/auth/tokens.json}<br>
 * 首次启动自动生成 admin 令牌并打印到控制台。</p>
 *
 * @author CH
 */
@Spi("file-token-service")
@SpiDescribe(value = "file-token-service", desc = "文件持久化令牌服务 - JSON文件存储")
public class FileTokenService implements TokenService {

    private static final String DEFAULT_STORAGE_PATH = "data/auth/tokens.json";

    private final File storageFile;
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    /** 自动生成的 admin 令牌（首次启动） */
    private volatile String adminToken;

    public FileTokenService() {
        this(DEFAULT_STORAGE_PATH);
    }

    public FileTokenService(String storagePath) {
        this.storageFile = new File(storagePath);
    }

    /**
     * 初始化：加载已有令牌，首次运行时自动创建 admin 令牌
     */
    public void init() {
        loadFromFile();
        if (tokens.isEmpty()) {
            adminToken = UUID.randomUUID().toString().replace("-", "");
            TokenInfo adminInfo = new TokenInfo(
                    adminToken, "管理员令牌（自动生成）",
                    System.currentTimeMillis(), 0, true);
            tokens.put(adminToken, adminInfo);
            saveToFile();
            System.out.println("[FileTokenService] ============================================");
            System.out.println("[FileTokenService] 管理员令牌: " + adminToken);
            System.out.println("[FileTokenService] 请妥善保管此令牌，用于采集/订阅 API 鉴权");
            System.out.println("[FileTokenService] 令牌管理端点: POST/DELETE/GET /v2/datalake/auth/token");
            System.out.println("[FileTokenService] ============================================");
        }
    }

    @Override
    public String createToken(String description) {
        ensureLoaded();
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenInfo info = new TokenInfo(
                token, description != null ? description : "",
                System.currentTimeMillis(), 0, true);
        tokens.put(token, info);
        saveToFile();
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null) return false;
        ensureLoaded();
        TokenInfo info = tokens.get(token);
        if (info == null) return false;
        if (!info.isActive()) return false;
        // 检查过期
        if (info.getExpiresAt() > 0 && info.getExpiresAt() < System.currentTimeMillis()) {
            info.setActive(false);
            saveToFile();
            return false;
        }
        return true;
    }

    @Override
    public boolean revokeToken(String token) {
        if (token == null) return false;
        ensureLoaded();
        TokenInfo info = tokens.get(token);
        if (info == null) return false;
        info.setActive(false);
        saveToFile();
        return true;
    }

    @Override
    public List<TokenInfo> listTokens() {
        ensureLoaded();
        return new ArrayList<>(tokens.values()).stream()
                .sorted(Comparator.comparingLong(TokenInfo::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** 获取 admin 令牌（首次启动自动生成） */
    public String getAdminToken() {
        ensureLoaded();
        return adminToken;
    }

    private void ensureLoaded() {
        if (loaded.compareAndSet(false, true)) {
            loadFromFile();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!storageFile.exists()) return;
        try {
            byte[] data = Files.readAllBytes(storageFile.toPath());
            String json = new String(data, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) Json.toList(json);
            if (list != null) {
                for (Map<String, Object> map : list) {
                    TokenInfo info = new TokenInfo();
                    info.setToken((String) map.get("token"));
                    info.setDescription((String) map.get("description"));
                    info.setCreatedAt(map.get("createdAt") instanceof Number n ? n.longValue() : 0);
                    info.setExpiresAt(map.get("expiresAt") instanceof Number n ? n.longValue() : 0);
                    info.setActive(Boolean.TRUE.equals(map.get("active")));
                    if (info.getToken() != null) {
                        tokens.put(info.getToken(), info);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FileTokenService] 加载令牌失败: " + e.getMessage());
        }
    }

    private synchronized void saveToFile() {
        try {
            Path parent = storageFile.getParentFile().toPath();
            if (!Files.exists(parent)) Files.createDirectories(parent);
            List<Map<String, Object>> list = new ArrayList<>();
            for (TokenInfo info : tokens.values()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("token", info.getToken());
                map.put("description", info.getDescription());
                map.put("createdAt", info.getCreatedAt());
                map.put("expiresAt", info.getExpiresAt());
                map.put("active", info.isActive());
                list.add(map);
            }
            String json = Json.toJson(list);
            Files.write(storageFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[FileTokenService] 保存令牌失败: " + e.getMessage());
        }
    }
}
