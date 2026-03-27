package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.base.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.network.net.NetAddress;
import com.chua.common.support.core.utils.ObjectUtils;
import com.chua.starter.monitor.engine.SystemDataCapability;
import com.chua.starter.monitor.engine.SystemDataEngine;
import com.chua.starter.monitor.engine.SystemDataEngineFactory;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.pojo.data.SystemDataSettingPageItem;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.service.data.SystemDataSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.GeneralSecurityException;
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
    final SystemDataEngineFactory systemDataEngineFactory;

    @Override
    public ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting) {
        try {
            SystemDataSetting persistence = prepareForPersistence(setting);
            if (persistence.getSystemDataSettingId() == null) {
                persistence.setSystemDataSettingTimeoutMs(ObjectUtils.defaultIfNull(persistence.getSystemDataSettingTimeoutMs(), 10_000));
                save(persistence);
            } else {
                Boolean systemDataSettingIdle = persistence.getSystemDataSettingIdle();
                Boolean systemDataSettingEnabled = persistence.getSystemDataSettingEnabled();
                if (updateById(persistence) && null != systemDataSettingEnabled && systemDataSettingEnabled && null != systemDataSettingIdle && systemDataSettingIdle) {
                    systemDataConsoleService.getRoot(persistence.getSystemDataSettingId(), new MetadataQuery());
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
    public ReturnResult<Page<SystemDataSettingPageItem>> pageWithCapabilities(long current, long size, String name, String type, String sort, String order) {
        Page<SystemDataSetting> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemDataSetting> qw = new LambdaQueryWrapper<>();
        // 名称模糊查询
        if (name != null && !name.isEmpty()) {
            qw.like(SystemDataSetting::getSystemDataSettingName, name);
        }
        // 类型精确查询
        if (type != null && !type.isEmpty()) {
            qw.eq(SystemDataSetting::getSystemDataSettingType, type);
        }
        // 排序处理
        boolean isAsc = !"desc".equalsIgnoreCase(order);
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "systemDataSettingName":
                    qw.orderBy(true, isAsc, SystemDataSetting::getSystemDataSettingName);
                    break;
                case "systemDataSettingType":
                    qw.orderBy(true, isAsc, SystemDataSetting::getSystemDataSettingType);
                    break;
                default:
                    // 默认按名称排序
                    qw.orderBy(true, isAsc, SystemDataSetting::getSystemDataSettingName);
                    break;
            }
        } else {
            // 默认按名称升序
            qw.orderByAsc(SystemDataSetting::getSystemDataSettingName);
        }
        this.page(page, qw);

        Page<SystemDataSettingPageItem> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(setting -> {
            // 在返回前解密敏感数据
            decryptSensitiveData(setting);
            SystemDataSettingPageItem item = new SystemDataSettingPageItem();
            BeanUtils.copyProperties(setting, item);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                java.util.Map<String, Boolean> capabilityMap = engine.capabilities();
                item.setCapabilityLevels(engine.capabilityLevels());
                applyCapabilities(item, capabilityMap);
            } catch (Exception e) {
                log.warn("分析数据源能力失败: {}", setting.getSystemDataSettingId(), e);
            }
            return item;
        }).toList());
        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<String> uploadJdbcDriver(Integer id, MultipartFile file) {
        try {
            SystemDataSetting setting = baseMapper.selectById(id);
            if (setting == null) { return ReturnResult.error("配置不存在"); }
            String baseDir = System.getProperty("login.home", System.getProperty("user.dir", ".")) + File.separator + "data-drivers";
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
            return decryptInternal(encryptedText);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return encryptedText;
        }
    }

    private static String decryptInternal(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("解密失败", e);
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

        setting.setSystemDataSettingPassword(
                encodeSensitiveValue(setting.getSystemDataSettingPassword(), true)
        );
        setting.setSystemDataSettingServer(
                encodeSensitiveValue(setting.getSystemDataSettingServer(), shouldEncryptServer(setting.getSystemDataSettingServer()))
        );
        setting.setSystemDataSettingConfig(
                encodeSensitiveValue(setting.getSystemDataSettingConfig(), shouldEncryptConfig(setting.getSystemDataSettingConfig()))
        );
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

    public static SystemDataSetting copySetting(SystemDataSetting source) {
        if (source == null) {
            return null;
        }
        SystemDataSetting target = new SystemDataSetting();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private SystemDataSetting prepareForPersistence(SystemDataSetting setting) {
        SystemDataSetting persistence = copySetting(setting);
        normalizeConnection(persistence);
        mergeRawSensitiveFields(persistence);
        encryptSensitiveData(persistence);
        return persistence;
    }

    private void normalizeConnection(SystemDataSetting setting) {
        String systemDataSettingServer = decrypt(setting.getSystemDataSettingServer());
        setting.setSystemDataSettingServer(systemDataSettingServer);
        if (systemDataSettingServer != null && null == setting.getSystemDataSettingProtocol()) {
            NetAddress netAddress = NetAddress.of(systemDataSettingServer);
            setting.setSystemDataSettingProtocol(netAddress.getProtocol());
            setting.setSystemDataSettingHost(netAddress.getHost());
            setting.setSystemDataSettingPort(netAddress.getPort(8080));
        } else if (isBlank(systemDataSettingServer) && setting.getSystemDataSettingProtocol() != null) {
            String generatedUrl = buildConnectionUrl(setting);
            if (generatedUrl != null) {
                setting.setSystemDataSettingServer(generatedUrl);
            }
        }
    }

    private void mergeRawSensitiveFields(SystemDataSetting setting) {
        Integer id = setting.getSystemDataSettingId();
        if (id == null) {
            return;
        }
        SystemDataSetting storedSetting = baseMapper.selectById(id);
        if (storedSetting == null) {
            return;
        }
        if (setting.getSystemDataSettingPassword() == null) {
            setting.setSystemDataSettingPassword(storedSetting.getSystemDataSettingPassword());
        }
        if (setting.getSystemDataSettingServer() == null) {
            setting.setSystemDataSettingServer(storedSetting.getSystemDataSettingServer());
        }
        if (setting.getSystemDataSettingConfig() == null) {
            setting.setSystemDataSettingConfig(storedSetting.getSystemDataSettingConfig());
        }
    }

    private static String encodeSensitiveValue(String value, boolean encryptRequired) {
        if (value == null || value.isEmpty() || !encryptRequired || isEncryptedText(value)) {
            return value;
        }
        return encrypt(value);
    }

    private static boolean shouldEncryptServer(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("password=") || lowerValue.contains("pwd=");
    }

    private static boolean shouldEncryptConfig(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("password")
                || lowerValue.contains("secret")
                || lowerValue.contains("token");
    }

    private static boolean isEncryptedText(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            String decryptedText = decryptInternal(value);
            return value.equals(encrypt(decryptedText));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return false;
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

    private void applyCapabilities(SystemDataSettingPageItem item, java.util.Map<String, Boolean> capabilityMap) {
        item.setCapabilitiesBackup(capabilityMap.getOrDefault(SystemDataCapability.BACKUP.getKey(), false));
        item.setCapabilitiesBackupLifecycle(capabilityMap.getOrDefault(SystemDataCapability.BACKUP_LIFECYCLE.getKey(), false));
        item.setCapabilitiesTableBackup(capabilityMap.getOrDefault(SystemDataCapability.TABLE_BACKUP.getKey(), false));
        item.setCapabilitiesDocument(capabilityMap.getOrDefault(SystemDataCapability.DOCUMENT.getKey(), false));
        item.setCapabilitiesFile(capabilityMap.getOrDefault(SystemDataCapability.FILE.getKey(), false));
        item.setCapabilitiesHierarchy(capabilityMap.getOrDefault(SystemDataCapability.HIERARCHY.getKey(), false));
        item.setCapabilitiesCommand(capabilityMap.getOrDefault(SystemDataCapability.COMMAND.getKey(), false));
        item.setCapabilitiesOrmSearch(capabilityMap.getOrDefault(SystemDataCapability.ORM_SEARCH.getKey(), false));
        item.setCapabilitiesOrmUpdate(capabilityMap.getOrDefault(SystemDataCapability.ORM_UPDATE.getKey(), false));
        item.setCapabilitiesSqlQuery(capabilityMap.getOrDefault(SystemDataCapability.SQL_QUERY.getKey(), false));
        item.setCapabilitiesSqlUpdate(capabilityMap.getOrDefault(SystemDataCapability.SQL_UPDATE.getKey(), false));
        item.setCapabilitiesStructure(capabilityMap.getOrDefault(SystemDataCapability.STRUCTURE.getKey(), false));
        item.setCapabilitiesIndexes(capabilityMap.getOrDefault(SystemDataCapability.INDEXES.getKey(), false));
        item.setCapabilitiesPartitions(capabilityMap.getOrDefault(SystemDataCapability.PARTITIONS.getKey(), false));
        item.setCapabilitiesRowMutation(capabilityMap.getOrDefault(SystemDataCapability.ROW_MUTATION.getKey(), false));
        item.setCapabilitiesKv(capabilityMap.getOrDefault(SystemDataCapability.KV.getKey(), false));
        item.setCapabilitiesTimeSeries(capabilityMap.getOrDefault(SystemDataCapability.TIME_SERIES.getKey(), false));
        item.setCapabilitiesTopic(capabilityMap.getOrDefault(SystemDataCapability.TOPIC.getKey(), false));
        item.setCapabilitiesTopicAdmin(capabilityMap.getOrDefault(SystemDataCapability.TOPIC_ADMIN.getKey(), false));
        item.setCapabilitiesCluster(capabilityMap.getOrDefault(SystemDataCapability.CLUSTER.getKey(), false));
        item.setCapabilitiesNode(capabilityMap.getOrDefault(SystemDataCapability.NODE.getKey(), false));
    }
}


