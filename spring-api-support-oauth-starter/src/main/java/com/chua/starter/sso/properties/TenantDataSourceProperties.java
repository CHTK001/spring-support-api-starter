package com.chua.starter.sso.properties;


import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.chua.starter.sso.properties.TenantDataSourceProperties.PRE;

@Data
@ConfigurationProperties(prefix = PRE, ignoreInvalidFields = true)
public class TenantDataSourceProperties {

    public static final String PRE = "spring.tenant";
    public List<DataSourceProperties> properties;
    private boolean enable;
}
