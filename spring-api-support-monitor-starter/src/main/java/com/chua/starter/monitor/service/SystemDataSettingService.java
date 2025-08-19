package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.SystemDataSettingPageItem;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据源配置服务接口
 * <p>
 * 提供对数据源配置信息的增删改查、分页查询以及驱动上传等操作。
 *
 * @author CH
 * @since 2025-04-05
 */
public interface SystemDataSettingService extends IService<SystemDataSetting> {

    /**
     * 保存或更新数据源配置
     *
     * @param setting 数据源配置实体对象，不能为空。
     *                示例：new SystemDataSetting() {{ setSystemDataSettingName("MySQL数据库"); setSystemDataSettingType("JDBC"); }}
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    ReturnResult<Boolean> saveOrUpdateSetting(SystemDataSetting setting);

    /**
     * 根据主键ID删除数据源配置
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    ReturnResult<Boolean> deleteById(Integer id);

    /**
     * 读取指定数据源的控制台权限配置(JSON字符串)
     *
     * @param id 数据源配置的主键ID，不能为空。
     *           示例：1
     * @return 控制台权限配置的JSON字符串。
     */
    ReturnResult<String> getConsoleConfig(Integer id);

    /**
     * 保存指定数据源的控制台权限配置(JSON字符串)
     *
     * @param id         数据源配置的主键ID，不能为空。
     *                   示例：1
     * @param jsonConfig 控制台权限配置的JSON字符串，不能为空。
     *                   示例："{\"tableAccess\": true, \"graphAccess\": false}"
     * @return 操作结果，成功返回 true，失败返回 false。
     */
    ReturnResult<Boolean> saveConsoleConfig(Integer id, String jsonConfig);

    /**
     * 分页查询数据源配置并附带能力信息
     *
     * @param current 当前页码（从1开始），不能为空。
     *                示例：1
     * @param size    每页显示记录数，不能为空。
     *                示例：10
     * @param name    数据源名称模糊查询条件（可选）。
     *                示例："MySQL"
     * @param type    数据源类型精确查询条件（可选）。
     *                示例："JDBC"
     * @return 分页结果，包含数据源配置及其能力信息。
     */
    ReturnResult<Page<SystemDataSettingPageItem>> pageWithCapabilities(long current, long size, String name, String type);

    /**
     * 上传 JDBC 驱动文件，并返回其在服务器上的存储路径
     *
     * @param id   数据源配置的主键ID，不能为空。
     *             示例：1
     * @param file 要上传的 JDBC 驱动文件，不能为空。
     *             示例：new MockMultipartFile("driver.jar", "driver.jar", "application/java-archive", new byte[0])
     * @return 上传成功后返回文件存储路径，失败则返回错误信息。
     */
    ReturnResult<String> uploadJdbcDriver(Integer id, MultipartFile file);
}


