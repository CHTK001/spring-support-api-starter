package com.chua.starter.monitor.service.impl;

import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.starter.monitor.entity.SystemDataSetting;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDataConsoleServiceImplTest {

    @Test
    void buildOptionsDecryptsSensitiveFieldsWithoutMutatingSourceSetting() {
        String originalServer = "jdbc:mysql://127.0.0.1:3306/app?user=root&password=secret123";
        String originalPassword = "db-pass";
        SystemDataSetting setting = new SystemDataSetting();
        setting.setSystemDataSettingName("primary");
        setting.setSystemDataSettingServer(originalServer);
        setting.setSystemDataSettingDatabase("app");
        setting.setSystemDataSettingPolicy("default");
        setting.setSystemDataSettingUsername("root");
        setting.setSystemDataSettingPassword(originalPassword);
        setting.setSystemDataSettingDriverClass("com.mysql.cj.jdbc.Driver");
        setting.setSystemDataSettingDriverPath("C:/drivers/mysql.jar");

        SystemDataSettingServiceImpl.encryptSensitiveData(setting);
        String encryptedServer = setting.getSystemDataSettingServer();
        String encryptedPassword = setting.getSystemDataSettingPassword();

        DataSourceOptions options = SystemDataConsoleServiceImpl.buildOptions(setting);

        assertThat(options.getName()).isEqualTo("primary");
        assertThat(options.getUrl()).isEqualTo(originalServer);
        assertThat(options.getDatabaseName()).isEqualTo("app");
        assertThat(options.getRetentionPolicy()).isEqualTo("default");
        assertThat(options.getUsername()).isEqualTo("root");
        assertThat(options.getPassword()).isEqualTo(originalPassword);
        assertThat(options.getDriverClass()).isEqualTo("com.mysql.cj.jdbc.Driver");
        assertThat(options.getDriverPath()).isEqualTo("C:/drivers/mysql.jar");

        assertThat(encryptedServer).isNotEqualTo(originalServer);
        assertThat(encryptedPassword).isNotEqualTo(originalPassword);
        assertThat(setting.getSystemDataSettingServer()).isEqualTo(encryptedServer);
        assertThat(setting.getSystemDataSettingPassword()).isEqualTo(encryptedPassword);
    }

    @Test
    void buildOptionsFallsBackToHostAndPortWhenServerIsBlank() {
        SystemDataSetting setting = new SystemDataSetting();
        setting.setSystemDataSettingName("fallback");
        setting.setSystemDataSettingHost("10.0.0.8");
        setting.setSystemDataSettingPort(5432);
        setting.setSystemDataSettingDatabase("analytics");

        DataSourceOptions options = SystemDataConsoleServiceImpl.buildOptions(setting);

        assertThat(options.getName()).isEqualTo("fallback");
        assertThat(options.getUrl()).isEqualTo("10.0.0.8:5432");
        assertThat(options.getDatabaseName()).isEqualTo("analytics");
        assertThat(setting.getSystemDataSettingServer()).isNull();
    }
}
