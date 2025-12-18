package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 配置管理Mapper
 * 
 * @author CH
 * @since 2024/12/16
 */
@Mapper
public interface MonitorSysGenConfigMapper extends BaseMapper<MonitorSysGenConfig> {

    /**
     * 根据配置键和环境查找配置
     *
     * @param configKey 配置键
     * @param env       环境
     * @return 配置信息
     */
    @Select("SELECT * FROM monitor_sys_gen_config WHERE monitor_sys_gen_config_key = #{configKey} AND monitor_sys_gen_config_env = #{env} LIMIT 1")
    MonitorSysGenConfig findByKeyAndEnv(@Param("configKey") String configKey, @Param("env") String env);

    /**
     * 获取所有环境列表
     *
     * @return 环境列表
     */
    @Select("SELECT DISTINCT monitor_sys_gen_config_env FROM monitor_sys_gen_config WHERE monitor_sys_gen_config_env IS NOT NULL AND monitor_sys_gen_config_env != ''")
    List<String> findAllEnvs();

    /**
     * 获取所有应用列表
     *
     * @return 应用列表
     */
    @Select("SELECT DISTINCT monitor_sys_gen_config_app FROM monitor_sys_gen_config WHERE monitor_sys_gen_config_app IS NOT NULL AND monitor_sys_gen_config_app != ''")
    List<String> findAllApps();

    /**
     * 获取配置统计信息
     *
     * @return 配置统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN monitor_sys_gen_config_status = 1 THEN 1 ELSE 0 END) as enabled, " +
            "SUM(CASE WHEN monitor_sys_gen_config_status = 0 THEN 1 ELSE 0 END) as disabled " +
            "FROM monitor_sys_gen_config")
    ConfigStatistics getConfigStatistics();

    /**
     * 获取各环境配置数量统计
     *
     * @return 环境配置统计列表
     */
    @Select("SELECT monitor_sys_gen_config_env as env, COUNT(*) as count FROM monitor_sys_gen_config " +
            "WHERE monitor_sys_gen_config_env IS NOT NULL AND monitor_sys_gen_config_env != '' " +
            "GROUP BY monitor_sys_gen_config_env")
    List<EnvConfigCount> getEnvConfigStatistics();

    /**
     * 根据环境查找配置列表
     *
     * @param env 环境
     * @return 配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_config WHERE monitor_sys_gen_config_env = #{env}")
    List<MonitorSysGenConfig> findByEnv(@Param("env") String env);

    /**
     * 根据应用查找配置列表
     *
     * @param app 应用名
     * @return 配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_config WHERE monitor_sys_gen_config_app = #{app}")
    List<MonitorSysGenConfig> findByApp(@Param("app") String app);

    /**
     * 配置统计信息
     */
    interface ConfigStatistics {
        Long getTotal();
        Long getEnabled();
        Long getDisabled();
    }

    /**
     * 环境配置统计
     */
    interface EnvConfigCount {
        String getEnv();
        Long getCount();
    }
}
