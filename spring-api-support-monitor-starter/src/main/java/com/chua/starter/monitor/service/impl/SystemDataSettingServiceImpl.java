package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.NetAddress;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.service.SystemDataSettingService;
import com.chua.starter.monitor.pojo.SystemCapabilities;
import com.chua.starter.monitor.pojo.SystemDataSettingPageItem;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.document.Document;
import com.chua.common.support.spi.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 数据管理配置服务实现
 *
 * 负责数据源配置的持久化与控制台权限配置的读写。
 *
 * @author CH
 */
@Slf4j
@Service
public class SystemDataSettingServiceImpl extends ServiceImpl<SystemDataSettingMapper, SystemDataSetting>
        implements SystemDataSettingService {

    @Override
    /** 保存或更新数据源配置 */
    public ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting) {
        try {
            String systemDataSettingServer = setting.getSystemDataSettingServer();
            if(systemDataSettingServer != null) {
                NetAddress netAddress = NetAddress.of(systemDataSettingServer);
                setting.setSystemDataSettingProtocol(netAddress.getProtocol());
                setting.setSystemDataSettingHost(netAddress.getHost());
                setting.setSystemDataSettingPort(netAddress.getPort(8080));
            }
            if (setting.getSystemDataSettingId() == null) {
                save(setting);
            } else {
                updateById(setting);
            }
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("保存数据配置失败", e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    /** 删除数据源配置 */
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
    /** 读取控制台权限配置 */
    public ReturnResult<String> getConsoleConfig(Integer id) {
        SystemDataSetting setting = getById(id);
        if (setting == null) {
            return ReturnResult.error("配置不存在");
        }
        return ReturnResult.ok(setting.getSystemDataSettingConfig());
    }

    @Override
    /** 保存控制台权限配置 */
    public ReturnResult<Boolean> saveConsoleConfig(Integer id, String jsonConfig) {
        try {
            SystemDataSetting setting = getById(id);
            if (setting == null) { return ReturnResult.error("配置不存在"); }
            setting.setSystemDataSettingConfig(jsonConfig);
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
            SystemDataSettingPageItem item = new SystemDataSettingPageItem();
            // 拷贝基础字段
            item.setSystemDataSettingId(setting.getSystemDataSettingId());
            item.setSystemDataSettingName(setting.getSystemDataSettingName());
            item.setSystemDataSettingType(setting.getSystemDataSettingType());
            item.setSystemDataSettingServer(setting.getSystemDataSettingServer());
            item.setSystemDataSettingHost(setting.getSystemDataSettingHost());
            item.setSystemDataSettingPort(setting.getSystemDataSettingPort());
            item.setSystemDataSettingUsername(setting.getSystemDataSettingUsername());
            item.setSystemDataSettingPassword(setting.getSystemDataSettingPassword());
            item.setSystemDataSettingAuthType(setting.getSystemDataSettingAuthType());
            item.setSystemDataSettingIcon(setting.getSystemDataSettingIcon());
            item.setSystemDataSettingConsoleType(setting.getSystemDataSettingConsoleType());
            item.setSystemDataSettingChartType(setting.getSystemDataSettingChartType());
            item.setSystemDataSettingTimeoutMs(setting.getSystemDataSettingTimeoutMs());
            item.setSystemDataSettingMode(setting.getSystemDataSettingMode());
            item.setSystemDataSettingEnabled(setting.getSystemDataSettingEnabled());
            item.setSystemDataSettingConfig(setting.getSystemDataSettingConfig());
            item.setSystemDataSettingImageUrl(setting.getSystemDataSettingImageUrl());

            // 能力判断（SPI）
            boolean backup = ServiceProvider.of(Backup.class).isSupport(setting.getSystemDataSettingType());
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
            String baseDir = System.getProperty("user.home") + File.separator + "data-drivers";
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
}


