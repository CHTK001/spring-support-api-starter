package com.chua.starter.video.setting;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * 基于通用配置表 {@code sys_setting} 的视频配置存储。
 *
 * <p>复用仓库既有的 {@link SysSettingService}，因此自动获得配置分组、变更历史、
 * 导入导出与 {@code /v2/setting} 管理端能力；通过 {@link ObjectProvider} 注入，
 * 宿主未提供该服务时 {@link #available()} 返回 false，视频模块回退内置 JSON。</p>
 *
 * <p>{@code sys_setting_value} 已放宽为 {@code TEXT}，可承载直播目录整段 JSON；
 * 逐行键值仍用于标量配置。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class SysSettingStore implements VideoSettingStore {

    /**
     * 配置状态：启用。
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 系统内置配置标记。
     */
    private static final int IN_SYSTEM = 1;

    /**
     * 配置服务（可能为空）。
     */
    private final ObjectProvider<SysSettingService> settingServiceProvider;

    @Override
    public boolean available() {
        return settingServiceProvider != null && settingServiceProvider.getIfAvailable() != null;
    }

    @Override
    public List<Row> list(String group) {
        SysSettingService service = settingServiceProvider == null ? null : settingServiceProvider.getIfAvailable();
        if (service == null) {
            return List.of();
        }
        return service.list(Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, group)
                        .eq(SysSetting::getSysSettingStatus, STATUS_ENABLED))
                .stream()
                .map(it -> new Row(it.getSysSettingName(), it.getSysSettingValue(),
                        it.getSysSettingSort() == null ? 0 : it.getSysSettingSort()))
                .toList();
    }

    @Override
    public void upsert(String group, String name, String value, String valueType, String remark, int sort) {
        SysSettingService service = settingServiceProvider == null ? null : settingServiceProvider.getIfAvailable();
        if (service == null) {
            return;
        }
        SysSetting exist = service.getOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, group)
                .eq(SysSetting::getSysSettingName, name), false);
        if (exist == null) {
            SysSetting setting = new SysSetting();
            setting.setSysSettingGroup(group);
            setting.setSysSettingName(name);
            setting.setSysSettingValue(value);
            setting.setSysSettingValueType(valueType);
            setting.setSysSettingRemark(remark);
            setting.setSysSettingSort(sort);
            setting.setSysSettingStatus(STATUS_ENABLED);
            setting.setSysSettingInSystem(IN_SYSTEM);
            setting.setSysSettingCollect(0);
            service.save(setting);
            return;
        }
        if (!java.util.Objects.equals(exist.getSysSettingValue(), value)) {
            exist.setSysSettingValue(value);
            service.updateById(exist);
        }
    }

    @Override
    public int deleteByPrefix(String group, String namePrefix) {
        SysSettingService service = settingServiceProvider == null ? null : settingServiceProvider.getIfAvailable();
        if (service == null) {
            return 0;
        }
        return service.remove(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, group)
                .likeRight(SysSetting::getSysSettingName, namePrefix)) ? 1 : 0;
    }
}
