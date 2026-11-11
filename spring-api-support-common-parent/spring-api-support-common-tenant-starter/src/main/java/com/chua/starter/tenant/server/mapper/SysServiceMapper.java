package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务 映射器 接口
 *
 * @since 2024/12/07
 * @author CH
 */
@Mapper
public interface SysServiceMapper extends BaseMapper<SysService> {

    /**
     * 按租户标识删除服务记录
     *
     * @param tenantId 租户标识
     * @param tenantIdColumn 租户标识列名
     * @return 删除行数
     */
    int deleteByTenantId(@Param("tenantId") String tenantId, @Param("tenantIdColumn") String tenantIdColumn);

    /**
     * 读取平台全部服务 ID（含逻辑删除）。
     *
     * <p>服务目录同步需要知道「哪些服务由平台维护」：只有这些服务在取消绑定后
     * 才能从租户库删除，租户自建的服务不能动。逻辑删除的服务也要算在内，
     * 否则平台上删掉的服务会永远留在租户库里。</p>
     *
     * @return 服务 ID 列表
     */
    @Select("SELECT sys_service_id FROM sys_service")
    List<Integer> selectAllServiceIdsIncludingDeleted();
}
