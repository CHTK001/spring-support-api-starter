package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ACME 证书 Mapper，提供 monitor_acme_certificate 表的基础增删改查能力。
 *
 * @since 2025-10-17
 * @author CH
 */
@Mapper
public interface MonitorAcmeCertificateMapper extends BaseMapper<MonitorAcmeCertificate> {

    /**
     * 根据主域名查询证书
     *
     * @param primaryDomain 主域名
     * @return 证书列表
     */
    List<MonitorAcmeCertificate> selectByPrimaryDomain(@Param("primaryDomain") String primaryDomain);
}
