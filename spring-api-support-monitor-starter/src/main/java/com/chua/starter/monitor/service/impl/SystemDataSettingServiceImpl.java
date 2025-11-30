package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.document.Document;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.net.NetAddress;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.pojo.SystemDataSettingPageItem;
import com.chua.starter.monitor.service.SystemDataConsoleService;
import com.chua.starter.monitor.service.SystemDataSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * 数据管理配置服务实现
 *
 * 负责数据源配置的持久化与控制台权限配置的读写。
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDataSettingServiceImpl extends ServiceImpl<SystemDataSettingMapper, SystemDataSetting>
        implements SystemDataSettingService {

    /**
     * AES加密算法
     */
    private static final String ALGORITHM = "AES";
    
    /**
     * AES加密密钥（实际项目中应该从配置文件或环境变量中获取）
     */
    private static final String SECRET_KEY = "MySecretKey12345"; // 16字节密钥

    final SystemDataConsoleService systemDataConsoleService;

    @Override
    public ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting) {
        try {
            String systemDataSettingServer = setting.getSystemDataSettingServer();
            if(systemDataSettingServer != null && null == setting.getSystemDataSettingProtocol()) {
                // 从连接地址解析协议
                NetAddress netAddress = NetAddress.of(systemDataSettingServer);
                setting.setSystemDataSettingProtocol(netAddress.getProtocol());
                setting.setSystemDataSettingHost(netAddress.getHost());
                setting.setSystemDataSettingPort(netAddress.getPort(8080));
            } else if (isBlank(systemDataSettingServer) && setting.getSystemDataSettingProtocol() != null) {
                // 如果没有连接地址，则根据协议自动拼接
                String generatedUrl = buildConnectionUrl(setting);
                if (generatedUrl != null) {
                    setting.setSystemDataSettingServer(generatedUrl);
                }
            }
            
            // 在保存前加密敏感数据
            encryptSensitiveData(setting);
            
            if (setting.getSystemDataSettingId() == null) {
                setting.setSystemDataSettingTimeoutMs(ObjectUtils.defaultIfNull(setting.getSystemDataSettingTimeoutMs(), 10_000));
                save(setting);
            } else {
                Boolean systemDataSettingIdle = setting.getSystemDataSettingIdle();
                Boolean systemDataSettingEnabled = setting.getSystemDataSettingEnabled();
                if (updateById(setting) && null != systemDataSettingEnabled && systemDataSettingEnabled && null != systemDataSettingIdle && systemDataSettingIdle) {
                    systemDataConsoleService.getRoot(setting.getSystemDataSettingId(), new MetadataQuery());
                }

            }
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("保存数据配置失败", e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteById(Integer id) {
        try {
            removeById(id);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("删除数据配置失败", e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getConsoleConfig(Integer id) {
        SystemDataSetting setting = getById(id);
        if (setting == null) {
            return ReturnResult.error("配置不存在");
        }
        // 在返回前解密敏感数据
        decryptSensitiveData(setting);
        return ReturnResult.ok(setting.getSystemDataSettingConfig());
    }

    @Override
    public ReturnResult<Boolean> saveConsoleConfig(Integer id, String jsonConfig) {
        try {
            SystemDataSetting setting = getById(id);
            if (setting == null) { return ReturnResult.error("配置不存在"); }
            setting.setSystemDataSettingConfig(jsonConfig);
            // 在保存前加密敏感数据
            encryptSensitiveData(setting);
            updateById(setting);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("保存控制台配置失败", e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Page<SystemDataSettingPageItem>> pageWithCapabilities(long current, long size, String name, String type) {
        Page<SystemDataSetting> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemDataSetting> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            qw.like(SystemDataSetting::getSystemDataSettingName, name);
        }
        if (type != null && !type.isEmpty()) {
            qw.eq(SystemDataSetting::getSystemDataSettingType, type);
        }
        this.page(page, qw);

        Page<SystemDataSettingPageItem> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(setting -> {
            // 在返回前解密敏感数据
            decryptSensitiveData(setting);
            SystemDataSettingPageItem item = new SystemDataSettingPageItem();
            BeanUtils.copyProperties(setting, item);
            // 能力判断（SPI）
            boolean backup = ServiceProvider.of(Backup.class).isSupport(setting.getSystemDataSettingProtocol());
            boolean document = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
            boolean isFile = "FILE".equalsIgnoreCase(setting.getSystemDataSettingMode());
            item.setCapabilitiesBackup(backup);
            item.setCapabilitiesDocument(document);
            item.setCapabilitiesFile(isFile);
            return item;
        }).toList());
        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<String> uploadJdbcDriver(Integer id, MultipartFile file) {
        try {
            SystemDataSetting setting = getById(id);
            if (setting == null) { return ReturnResult.error("配置不存在"); }
            String baseDir = System.getProperty("login.home") + File.separator + "data-drivers";
            Files.createDirectories(Path.of(baseDir));
            String filename = file.getOriginalFilename();
            if (filename == null || filename.isEmpty()) {
                filename = "driver-" + id + ".jar";
            }
            Path target = Path.of(baseDir, id + "-" + filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            setting.setSystemDataSettingDriverPath(target.toAbsolutePath().toString());
            updateById(setting);
            return ReturnResult.ok(setting.getSystemDataSettingDriverPath());
        } catch (Exception e) {
            log.error("上传驱动失败", e);
            return ReturnResult.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 加密后的字符串（Base64编码）
     */
    private static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new RuntimeException("数据加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 加密后的字符串（Base64编码）
     * @return 明文
     */
    private static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encryptedText;
        }
    }

    /**
     * 加密SystemDataSetting中的敏感数据字段
     *
     * @param setting 数据设置对象
     */
    public static void encryptSensitiveData(SystemDataSetting setting) {
        if (setting == null) {
            return;
        }
        
        // 加密密码
        if (setting.getSystemDataSettingPassword() != null && !setting.getSystemDataSettingPassword().isEmpty()) {
            setting.setSystemDataSettingPassword(encrypt(setting.getSystemDataSettingPassword()));
        }
        
        // 加密连接字符串（可能包含敏感信息）
        if (setting.getSystemDataSettingServer() != null && !setting.getSystemDataSettingServer().isEmpty()) {
            // 只有当连接字符串包含密码信息时才加密
            if (setting.getSystemDataSettingServer().contains("password=") || 
                setting.getSystemDataSettingServer().contains("pwd=")) {
                setting.setSystemDataSettingServer(encrypt(setting.getSystemDataSettingServer()));
            }
        }
        
        // 加密配置JSON（可能包含敏感信息）
        if (setting.getSystemDataSettingConfig() != null && !setting.getSystemDataSettingConfig().isEmpty()) {
            // 只有当配置包含密码相关信息时才加密
            if (setting.getSystemDataSettingConfig().contains("password") || 
                setting.getSystemDataSettingConfig().contains("secret") ||
                setting.getSystemDataSettingConfig().contains("token")) {
                setting.setSystemDataSettingConfig(encrypt(setting.getSystemDataSettingConfig()));
            }
        }
    }

    /**
     * 解密SystemDataSetting中的敏感数据字段
     *
     * @param setting 数据设置对象
     */
    public static void decryptSensitiveData(SystemDataSetting setting) {
        if (setting == null) {
            return;
        }
        
        // 解密密码
        if (setting.getSystemDataSettingPassword() != null && !setting.getSystemDataSettingPassword().isEmpty()) {
            setting.setSystemDataSettingPassword(decrypt(setting.getSystemDataSettingPassword()));
        }
        
        // 解密连接字符串
        if (setting.getSystemDataSettingServer() != null && !setting.getSystemDataSettingServer().isEmpty()) {
            setting.setSystemDataSettingServer(decrypt(setting.getSystemDataSettingServer()));
        }
        
        // 解密配置JSON
        if (setting.getSystemDataSettingConfig() != null && !setting.getSystemDataSettingConfig().isEmpty()) {
            setting.setSystemDataSettingConfig(decrypt(setting.getSystemDataSettingConfig()));
        }
    }


    /**
     * 重写getById方法，在查询后解密敏感数据
     */
    @Override
    public SystemDataSetting getById(java.io.Serializable id) {
        SystemDataSetting entity = super.getById(id);
        if (entity != null) {
            decryptSensitiveData(entity);
        }
        return entity;
    }

    /**
     * 重写list方法，在查询后解密敏感数据
     */
    @Override
    public java.util.List<SystemDataSetting> list() {
        java.util.List<SystemDataSetting> entities = super.list();
        if (entities != null) {
            entities.forEach(SystemDataSettingServiceImpl::decryptSensitiveData);
        }
        return entities;
    }

    /**
     * 重写list方法，在查询后解密敏感数据
     */
    @Override
    public java.util.List<SystemDataSetting> list(com.baomidou.mybatisplus.core.conditions.Wrapper<SystemDataSetting> queryWrapper) {
        java.util.List<SystemDataSetting> entities = super.list(queryWrapper);
        if (entities != null) {
            entities.forEach(SystemDataSettingServiceImpl::decryptSensitiveData);
        }
        return entities;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 根据协议和配置信息构建连接URL
     *
     * @param setting 数据源配置
     * @return 构建的连接URL，如果无法构建则返回null
     */
    private String buildConnectionUrl(SystemDataSetting setting) {
        String protocol = setting.getSystemDataSettingProtocol();
        String host = setting.getSystemDataSettingHost();
        Integer port = setting.getSystemDataSettingPort();
        String database = setting.getSystemDataSettingDatabase();
        String config = setting.getSystemDataSettingConfig();

        if (isBlank(protocol)) {
            return null;
        }

        String protocolLower = protocol.toLowerCase();
        StringBuilder urlBuilder = new StringBuilder();

        // 根据协议类型构建不同格式的URL
        switch (protocolLower) {
            case "mysql":
            case "mariadb":
                urlBuilder.append("jdbc:").append(protocolLower).append("://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 3306);
                urlBuilder.append("/").append(isBlank(database) ? "" : database);
                appendExtraParams(urlBuilder, config, "?", "&");
                break;

            case "postgresql":
                urlBuilder.append("jdbc:postgresql://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 5432);
                urlBuilder.append("/").append(isBlank(database) ? "" : database);
                appendExtraParams(urlBuilder, config, "?", "&");
                break;

            case "oracle":
                urlBuilder.append("jdbc:oracle:thin:@");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 1521);
                urlBuilder.append(":").append(isBlank(database) ? "orcl" : database);
                break;

            case "sqlserver":
                urlBuilder.append("jdbc:sqlserver://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 1433);
                if (!isBlank(database)) {
                    urlBuilder.append(";databaseName=").append(database);
                }
                appendExtraParams(urlBuilder, config, ";", ";");
                break;

            case "sqlite":
                urlBuilder.append("jdbc:sqlite:");
                urlBuilder.append(isBlank(database) ? ":memory:" : database);
                break;

            case "h2":
                urlBuilder.append("jdbc:h2:tcp://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 9092);
                urlBuilder.append("/").append(isBlank(database) ? "" : database);
                appendExtraParams(urlBuilder, config, ";", ";");
                break;

            case "dm":
                urlBuilder.append("jdbc:dm://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 5236);
                if (!isBlank(database)) {
                    urlBuilder.append("/").append(database);
                }
                break;

            case "kingbase8":
                urlBuilder.append("jdbc:kingbase8://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 54321);
                urlBuilder.append("/").append(isBlank(database) ? "" : database);
                break;

            case "clickhouse":
                urlBuilder.append("jdbc:clickhouse://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 8123);
                urlBuilder.append("/").append(isBlank(database) ? "" : database);
                appendExtraParams(urlBuilder, config, "?", "&");
                break;

            case "redis":
                urlBuilder.append("redis://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 6379);
                if (!isBlank(database)) {
                    urlBuilder.append("/").append(database);
                }
                break;

            case "mongodb":
                urlBuilder.append("mongodb://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 27017);
                if (!isBlank(database)) {
                    urlBuilder.append("/").append(database);
                }
                appendExtraParams(urlBuilder, config, "?", "&");
                break;

            case "zookeeper":
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 2181);
                break;

            case "influxdb":
                urlBuilder.append("http://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 8086);
                break;

            case "kafka":
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                urlBuilder.append(":").append(port != null ? port : 9092);
                break;

            default:
                // 默认格式: protocol://host:port/database
                urlBuilder.append(protocolLower).append("://");
                urlBuilder.append(isBlank(host) ? "localhost" : host);
                if (port != null && port > 0) {
                    urlBuilder.append(":").append(port);
                }
                if (!isBlank(database)) {
                    urlBuilder.append("/").append(database);
                }
                break;
        }

        return urlBuilder.toString();
    }

    /**
     * 追加额外参数到URL
     *
     * @param urlBuilder URL构建器
     * @param config     配置JSON字符串
     * @param firstSep   第一个参数的分隔符
     * @param otherSep   其他参数的分隔符
     */
    private void appendExtraParams(StringBuilder urlBuilder, String config, String firstSep, String otherSep) {
        if (isBlank(config)) {
            return;
        }
        try {
            // 解析JSON格式的额外参数
            com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(config);
            if (jsonObject == null || jsonObject.isEmpty()) {
                return;
            }
            boolean first = true;
            for (String key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (value != null) {
                    urlBuilder.append(first ? firstSep : otherSep);
                    urlBuilder.append(key).append("=").append(value);
                    first = false;
                }
            }
        } catch (Exception e) {
            log.warn("解析额外参数失败: {}", e.getMessage());
        }
    }
}


