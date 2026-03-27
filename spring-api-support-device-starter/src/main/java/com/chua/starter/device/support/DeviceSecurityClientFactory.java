package com.chua.starter.device.support;

import com.chua.cloud.support.security.SecurityClient;
import com.chua.cloud.support.security.SecurityConfig;
import com.chua.cloud.support.security.SecurityClientFactory;
import com.chua.cloud.support.security.enums.PlatformType;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.entity.service.SysDictItem;
import com.chua.starter.project.service.SysProjectService;
import com.chua.starter.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 将设备商户配置转换为统一安防客户端。
 *
 * @author CH
 * @since 2026/3/26
 */
@Component
@RequiredArgsConstructor
public class DeviceSecurityClientFactory {

    private static final int PROJECT_FUNCTION_DICT_ID = 6;
    private static final String DEVICE_FUNCTION_CODE = "SHE_BEI";
    private static final String DEFAULT_HIKVISION_YUNMOU_API = "https://api2.hik-cloud.com";
    private static final String DEFAULT_HIKVISION_YUNYAO_API = "https://open.hikyun.com";

    private final SysProjectService sysProjectService;
    private final SysDictItemService sysDictItemService;

    public SecurityClient create(Integer sysProjectId) {
        return create(getDeviceMerchant(sysProjectId));
    }

    public SecurityClient create(SysProject sysProject) {
        return SecurityClientFactory.createClient(toSecurityConfig(sysProject));
    }

    public SysProject getDeviceMerchant(Integer sysProjectId) {
        SysProject sysProject = sysProjectService.getOne(sysProjectId);
        if (sysProject == null) {
            throw new IllegalArgumentException("设备商户不存在");
        }
        if (!hasDeviceFunction(sysProject)) {
            throw new IllegalArgumentException("当前商户未开通设备能力");
        }
        return sysProject;
    }

    public Integer getDeviceFunctionId() {
        SysDictItem sysDictItem = sysDictItemService.getTypeIdByName(DEVICE_FUNCTION_CODE, PROJECT_FUNCTION_DICT_ID);
        if (sysDictItem == null || sysDictItem.getSysDictItemId() == null) {
            throw new IllegalStateException("未找到设备功能字典项: " + DEVICE_FUNCTION_CODE);
        }
        return sysDictItem.getSysDictItemId();
    }

    public String appendDeviceFunction(String sysProjectFunction) {
        Set<String> functionIds = splitCsv(sysProjectFunction);
        functionIds.add(String.valueOf(getDeviceFunctionId()));
        return String.join(",", functionIds);
    }

    private boolean hasDeviceFunction(SysProject sysProject) {
        return splitCsv(sysProject.getSysProjectFunction()).contains(String.valueOf(getDeviceFunctionId()));
    }

    private SecurityConfig toSecurityConfig(SysProject sysProject) {
        PlatformType platformType = resolvePlatformType(sysProject);
        String accessKey = firstNonBlank(sysProject.getSysProjectAppId(), sysProject.getSysProjectAppKey());
        String secretKey = firstNonBlank(sysProject.getSysProjectAppSecret(), sysProject.getSysProjectSignCode());
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("商户缺少设备平台凭证，请检查 AppId/AppSecret 配置");
        }

        return SecurityConfig.builder()
                .platformType(platformType)
                .apiUrl(resolveApiUrl(sysProject, platformType))
                .accessKey(accessKey)
                .secretKey(secretKey)
                .projectId(firstNonBlank(sysProject.getSysProjectSign(), sysProject.getSysProjectCode()))
                .extraConfig(buildExtraConfig(sysProject))
                .build();
    }

    private Map<String, Object> buildExtraConfig(SysProject sysProject) {
        if (StringUtils.isAllBlank(sysProject.getSysProjectSign(), sysProject.getSysProjectCode())) {
            return null;
        }
        return Map.of(
                "projectSign", StringUtils.defaultString(sysProject.getSysProjectSign()),
                "projectCode", StringUtils.defaultString(sysProject.getSysProjectCode())
        );
    }

    private PlatformType resolvePlatformType(SysProject sysProject) {
        String code = StringUtils.upperCase(StringUtils.defaultString(sysProject.getSysProjectDictItemCode()), Locale.ROOT);
        String label = StringUtils.defaultString(sysProject.getSysProjectDictItemLabel());

        PlatformType exactType = PlatformType.fromCode(StringUtils.lowerCase(code, Locale.ROOT));
        if (exactType != null) {
            return exactType;
        }
        if (code.contains("YUN_MOU") || label.contains("云眸")) {
            return PlatformType.HIKVISION_YUNMOU;
        }
        if (code.contains("YUN_YAO") || label.contains("云耀") || label.contains("云曜") || label.contains("云耀")) {
            return PlatformType.HIKVISION_YUNYAO;
        }
        if (code.contains("HAI_KANG") || code.contains("HIKVISION") || label.contains("海康")) {
            return PlatformType.HIKVISION_ISECURE;
        }
        if (code.contains("DA_HUA") || code.contains("DAHUA") || label.contains("大华")) {
            return PlatformType.DAHUA_DSS;
        }
        if (code.contains("YU_SHI") || code.contains("UNIVIEW") || label.contains("宇视")) {
            return PlatformType.UNIVIEW;
        }
        if (code.contains("HUA_WEI") || code.contains("HUAWEI") || label.contains("华为")) {
            return PlatformType.HUAWEI;
        }
        throw new IllegalArgumentException("暂不支持的设备厂家: " + firstNonBlank(label, code));
    }

    private String resolveApiUrl(SysProject sysProject, PlatformType platformType) {
        String apiUrl = firstNonBlank(sysProject.getSysProjectApiAddress(), sysProject.getSysProjectEndpoint());
        if (StringUtils.isBlank(apiUrl)) {
            apiUrl = switch (platformType) {
                case HIKVISION_YUNMOU -> DEFAULT_HIKVISION_YUNMOU_API;
                case HIKVISION_YUNYAO -> DEFAULT_HIKVISION_YUNYAO_API;
                default -> null;
            };
        }
        if (StringUtils.isBlank(apiUrl)) {
            throw new IllegalArgumentException("商户缺少 API 地址配置");
        }

        String normalized = apiUrl.trim();
        if (!StringUtils.startsWithIgnoreCase(normalized, "http://")
                && !StringUtils.startsWithIgnoreCase(normalized, "https://")) {
            normalized = "https://" + normalized;
        }
        return StringUtils.removeEnd(normalized, "/");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private Set<String> splitCsv(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (StringUtils.isBlank(value)) {
            return result;
        }
        String[] values = value.split(",");
        for (String item : values) {
            if (StringUtils.isNotBlank(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }
}
