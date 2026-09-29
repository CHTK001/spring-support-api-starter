package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysAnnouncement;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;
import java.util.Set;

/**
 * 系统公告服务接口
 *
 * @since 4.0.0.42
 * @author CH
 */
public interface SysAnnouncementService extends IService<SysAnnouncement> {

    /**
     * 分页查询公告
     *
     * @param query         分页参数
     * @param sysAnnouncement 查询条件
     * @param roles         当前用户角色
     * @return 公告分页结果
     */
    IPage<SysAnnouncement> pageForManage(Query<SysAnnouncement> query, SysAnnouncement sysAnnouncement, Set<String> roles);

    /**
     * 新增公告
     *
     * @param sysAnnouncement 公告数据
     * @param roles          当前用户角色
     * @param userId         当前用户标识
     * @return 操作结果
     */
    ReturnResult<SysAnnouncement> saveAnnouncement(SysAnnouncement sysAnnouncement, Set<String> roles, Integer userId);

    /**
     * 更新公告
     *
     * @param sysAnnouncement 公告数据
     * @param roles          当前用户角色
     * @return 操作结果
     */
    ReturnResult<Boolean> updateAnnouncement(SysAnnouncement sysAnnouncement, Set<String> roles);

    /**
     * 删除公告
     *
     * @param announcementId 公告标识
     * @param roles          当前用户角色
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteAnnouncement(Integer announcementId, Set<String> roles);

    /**
     * 发布公告
     *
     * @param announcementId 公告标识
     * @param roles          当前用户角色
     * @param userId         当前用户标识
     * @return 操作结果
     */
    ReturnResult<Boolean> publishAnnouncement(Integer announcementId, Set<String> roles, Integer userId);

    /**
     * 下架公告
     *
     * @param announcementId 公告标识
     * @param roles          当前用户角色
     * @return 操作结果
     */
    ReturnResult<Boolean> unpublishAnnouncement(Integer announcementId, Set<String> roles);

    /**
     * 获取当前用户可见的活跃公告列表
     *
     * @param userId 当前用户标识
     * @param roles  当前用户角色
     * @return 活跃公告列表
     */
    List<SysAnnouncement> getActiveAnnouncements(Integer userId, Set<String> roles);
}