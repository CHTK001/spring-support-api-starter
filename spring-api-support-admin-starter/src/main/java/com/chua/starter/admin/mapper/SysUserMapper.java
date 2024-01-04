package com.chua.starter.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.query.UserPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户持久层
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 获取用户分页列表
     *
     * @param page        分页
     * @param queryParams 查询参数
     * @param isAdmin     是否是admin
     * @return 用户分页列表
     */
    Page<SysUser> getUserPage(Page<SysUser> page, @Param("queryParams") UserPageQuery queryParams, @Param("isAdmin") boolean isAdmin);

    /**
     * 是否是管理员
     *
     * @param userId 用户ID
     * @return 是否是管理员
     */
    List<String> isAdmin(String userId);
}
