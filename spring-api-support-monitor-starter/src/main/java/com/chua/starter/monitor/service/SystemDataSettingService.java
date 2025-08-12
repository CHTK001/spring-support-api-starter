package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.SystemDataSettingPageItem;
import org.springframework.web.multipart.MultipartFile;

public interface SystemDataSettingService extends IService<SystemDataSetting> {
    ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting);
    ReturnResult<Boolean> deleteById(Integer id);

    /** 读取控制台权限配置(JSON字符串) */
    ReturnResult<String> getConsoleConfig(Integer id);

    /** 保存控制台权限配置(JSON字符串) */
    ReturnResult<Boolean> saveConsoleConfig(Integer id, String jsonConfig);

    /** 分页查询并附带能力 */
    ReturnResult<Page<SystemDataSettingPageItem>> pageWithCapabilities(long current, long size, String name, String type);

    /** 上传 JDBC 驱动，返回落盘路径 */
    ReturnResult<String> uploadJdbcDriver(Integer id, MultipartFile file);
}


