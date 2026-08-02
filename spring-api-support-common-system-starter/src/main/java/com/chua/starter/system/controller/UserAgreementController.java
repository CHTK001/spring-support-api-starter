package com.chua.starter.system.controller;

import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import com.chua.starter.system.pojo.user.UserAgreementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户协议
 */
@Tag(name = "用户协议")
@RestController
@RequestMapping("/v2/user")
public class UserAgreementController {
    private static final String USER_AGREEMENT_GROUP = "userAgreement";
    private static final String SECTION_PREFIX = "section.";
    private final SysSettingService sysSettingService;

    public UserAgreementController(SysSettingService sysSettingService) {
        this.sysSettingService = sysSettingService;
    }

    @Operation(summary = "获取用户协议")
    @GetMapping("/agreement")
    public ReturnResult<UserAgreementVO> agreement() {
        List<SysSetting> settings = loadAgreementSettings();
        Map<String, SysSetting> settingMap = settings.stream()
                .filter(item -> StringUtils.isNotBlank(item.getSysSettingName()))
                .collect(Collectors.toMap(SysSetting::getSysSettingName, item -> item, (left, right) -> left));

        List<UserAgreementVO.Section> sections = settings.stream()
                .filter(item -> defaultString(item.getSysSettingName()).startsWith(SECTION_PREFIX))
                .filter(item -> StringUtils.isNotBlank(item.getSysSettingValue()))
                .map(item -> new UserAgreementVO.Section(
                        firstNonBlank(item.getSysSettingRemark(), resolveSectionTitle(item.getSysSettingName())),
                        item.getSysSettingValue()
                ))
                .toList();

        UserAgreementVO payload = new UserAgreementVO(
                resolveSettingValue(settingMap, "title", "用户协议"),
                resolveSettingValue(settingMap, "label", "System Console Agreement"),
                resolveSettingValue(settingMap, "headline", "平台访问与数据使用说明"),
                resolveSettingValue(settingMap, "description", "账户中心、个人信息下拉与登录安全能力共用同一份协议定义，支持前端静态兜底与后端统一下发。"),
                resolveSettingValue(settingMap, "version", "2026.04"),
                resolveSettingValue(settingMap, "updatedAt", "2026-04-09"),
                sections.isEmpty() ? defaultSections() : sections
        );
        return ReturnResult.ok(payload);
    }

    private List<SysSetting> loadAgreementSettings() {
        SysSettingUpdate query = new SysSettingUpdate();
        query.setSysSettingGroup(USER_AGREEMENT_GROUP);
        return sysSettingService.list(query, Collections.emptySet()).stream()
                .filter(Objects::nonNull)
                .filter(item -> !Objects.equals(item.getSysSettingStatus(), 0))
                .sorted(Comparator
                        .comparing((SysSetting item) -> item.getSysSettingSort() == null ? Integer.MAX_VALUE : item.getSysSettingSort())
                        .thenComparing(item -> defaultString(item.getSysSettingName())))
                .toList();
    }

    private String resolveSettingValue(Map<String, SysSetting> settingMap, String settingName, String fallbackValue) {
        SysSetting setting = settingMap.get(settingName);
        if (setting == null || StringUtils.isBlank(setting.getSysSettingValue())) {
            return fallbackValue;
        }
        return setting.getSysSettingValue();
    }

    private String resolveSectionTitle(String settingName) {
        String rawName = StringUtils.removeStart(defaultString(settingName), SECTION_PREFIX);
        if (StringUtils.isBlank(rawName)) {
            return "协议条款";
        }
        return rawName.replace('-', ' ').replace('_', ' ').trim().toUpperCase(Locale.ROOT);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String value, String fallbackValue) {
        return StringUtils.isNotBlank(value) ? value : fallbackValue;
    }

    private List<UserAgreementVO.Section> defaultSections() {
        return List.of(
                new UserAgreementVO.Section("使用范围", "本系统仅面向已授权账号使用，账号、消息、日志、菜单与配置数据均纳入平台审计范围。"),
                new UserAgreementVO.Section("账号责任", "用户应妥善保管账号、密码与 OTP 动态口令，不得共享账号或绕过权限边界访问未授权数据。"),
                new UserAgreementVO.Section("数据合规", "涉及个人信息、部门信息、登录日志与系统消息时，应在业务授权范围内查看、导出和处理。"),
                new UserAgreementVO.Section("异常反馈", "发现异常登录、越权访问、消息异常或配置错误时，应立即停止高风险操作并通过帮助与反馈入口上报。")
        );
    }
}
