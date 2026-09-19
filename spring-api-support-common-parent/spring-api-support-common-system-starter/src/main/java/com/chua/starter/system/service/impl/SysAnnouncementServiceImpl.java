package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.entity.system.SysAnnouncement;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysAnnouncementMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.service.SysAnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统公告服务实现
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement>
        implements SysAnnouncementService {

    /**
     * sysrolemapper
    */
    private final SysRoleMapper sysRoleMapper;

    @Override
    /**
     * 分页formanage
     * @param query 查询
     * @param sysAnnouncement sysannouncement
     * @param roles 角色
     */
    public IPage<SysAnnouncement> pageForManage(Query<SysAnnouncement> query, SysAnnouncement sysAnnouncement,
                                                 Set<String> roles) {
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (sysAnnouncement != null) {
            if (sysAnnouncement.getSysAnnouncementTitle() != null) {
                wrapper.like(SysAnnouncement::getSysAnnouncementTitle, sysAnnouncement.getSysAnnouncementTitle());
            }
            if (sysAnnouncement.getSysAnnouncementStatus() != null) {
                wrapper.eq(SysAnnouncement::getSysAnnouncementStatus, sysAnnouncement.getSysAnnouncementStatus());
            }
            if (sysAnnouncement.getSysAnnouncementLevel() != null) {
                wrapper.eq(SysAnnouncement::getSysAnnouncementLevel, sysAnnouncement.getSysAnnouncementLevel());
            }
        }
        wrapper.orderByDesc(SysAnnouncement::getSysAnnouncementSort);
        wrapper.orderByDesc(SysAnnouncement::getCreateTime);
        return baseMapper.selectPage(query.createPage(), wrapper);
    }

    @CacheEvict(value = "announcement:active", allEntries = true)
    @Override
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    /**
     * 保存Announcement
     * @param sysAnnouncement sysannouncement
     * @param roles 角色
     * @param userId 用户id
     */
    public ReturnResult<SysAnnouncement> saveAnnouncement(SysAnnouncement sysAnnouncement, Set<String> roles,
                                                           Integer userId) {
        if (sysAnnouncement.getSysAnnouncementSort() == null) {
            sysAnnouncement.setSysAnnouncementSort(0);
        }
        sysAnnouncement.setSysAnnouncementStatus(SysAnnouncement.STATUS_DRAFT);
        baseMapper.insert(sysAnnouncement);
        log.info("新增公告: title={}, id={}", sysAnnouncement.getSysAnnouncementTitle(),
                sysAnnouncement.getSysAnnouncementId());
        return ReturnResult.ok(sysAnnouncement);
    }

    @CacheEvict(value = "announcement:active", allEntries = true)
    @Override
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新Announcement
     *
     * @param sysAnnouncement sysAnnouncement
     * @param roles 角色
     * @return 更新announcement的结果
     */
    public ReturnResult<Boolean> updateAnnouncement(SysAnnouncement sysAnnouncement, Set<String> roles) {
        if (sysAnnouncement.getSysAnnouncementId() == null) {
            return ReturnResult.illegal("公告ID不能为空");
        }
        SysAnnouncement existing = baseMapper.selectById(sysAnnouncement.getSysAnnouncementId());
        if (existing == null) {
            return ReturnResult.illegal("公告不存在");
        }
        baseMapper.updateById(sysAnnouncement);
        log.info("更新公告: id={}", sysAnnouncement.getSysAnnouncementId());
        return ReturnResult.ok(true);
    }

    @CacheEvict(value = "announcement:active", allEntries = true)
    @Override
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除Announcement
     *
     * @param announcementId announcementId
     * @param roles 角色
     * @return 删除announcement的结果
     */
    public ReturnResult<Boolean> deleteAnnouncement(Integer announcementId, Set<String> roles) {
        if (announcementId == null) {
            return ReturnResult.illegal("公告ID不能为空");
        }
        baseMapper.deleteById(announcementId);
        log.info("删除公告: id={}", announcementId);
        return ReturnResult.ok(true);
    }

    /**
     * 发布公告并清除缓存
     * @param announcementId announcementId
     * @param roles 角色
     * @param userId 用户id
     * @return 发布announcement的结果
     */
    @CacheEvict(value = "announcement:active", allEntries = true)
    @Override
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> publishAnnouncement(Integer announcementId, Set<String> roles, Integer userId) {
        if (announcementId == null) {
            return ReturnResult.illegal("公告ID不能为空");
        }
        SysAnnouncement entity = baseMapper.selectById(announcementId);
        if (entity == null) {
            return ReturnResult.illegal("公告不存在");
        }
        entity.setSysAnnouncementStatus(SysAnnouncement.STATUS_PUBLISHED);
        entity.setSysAnnouncementPublishTime(LocalDateTime.now());
        baseMapper.updateById(entity);
        log.info("发布公告: id={}", announcementId);
        return ReturnResult.ok(true);
    }

    /**
     * 下架公告并清除缓存
     * @param announcementId announcementId
     * @param roles 角色
     * @return unpublishAnnouncement的结果
     */
    @CacheEvict(value = "announcement:active", allEntries = true)
    @Override
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> unpublishAnnouncement(Integer announcementId, Set<String> roles) {
        if (announcementId == null) {
            return ReturnResult.illegal("公告ID不能为空");
        }
        SysAnnouncement entity = baseMapper.selectById(announcementId);
        if (entity == null) {
            return ReturnResult.illegal("公告不存在");
        }
        entity.setSysAnnouncementStatus(SysAnnouncement.STATUS_UNPUBLISHED);
        baseMapper.updateById(entity);
        log.info("下架公告: id={}", announcementId);
        return ReturnResult.ok(true);
    }

    /**
     * 获取当前用户可见的活跃公告列表（缓存30秒）
     */
    @Cacheable(value = "announcement:active", key = "#userId")
    @Override
    public List<SysAnnouncement> getActiveAnnouncements(Integer userId, Set<String> roles) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAnnouncement::getSysAnnouncementStatus, SysAnnouncement.STATUS_PUBLISHED);
        wrapper.and(w -> w.isNull(SysAnnouncement::getSysAnnouncementExpireTime)
                .or(ee -> ee.ge(SysAnnouncement::getSysAnnouncementExpireTime, now)));
        wrapper.orderByDesc(SysAnnouncement::getSysAnnouncementSort);
        wrapper.orderByDesc(SysAnnouncement::getSysAnnouncementPublishTime);

        List<SysAnnouncement> all = baseMapper.selectList(wrapper);
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

 // 查询当前用户角色标识列表，用于角色过滤
        Set<Integer> userRoleIds = sysRoleMapper.listByUserId(Collections.singleton(userId))
                .stream()
                .map(SysRole::getSysRoleId)
                .collect(Collectors.toSet());

        return all.stream().filter(a -> {
            if (SysAnnouncement.TYPE_GLOBAL.equals(a.getSysAnnouncementType())) {
                return true;
            }
            if (a.getSysAnnouncementTargetRoles() == null || a.getSysAnnouncementTargetRoles().isBlank()) {
                return false;
            }
            try {
                @SuppressWarnings("unchecked")
                List<Integer> targetRoles = Json.fromJsonToList(a.getSysAnnouncementTargetRoles(), Integer.class);
                if (targetRoles == null || targetRoles.isEmpty()) {
                    return false;
                }
                return targetRoles.stream().anyMatch(userRoleIds::contains);
            } catch (Exception e) {
                log.warn("解析公告目标角色列表失败: id={}, roles={}", a.getSysAnnouncementId(),
                        a.getSysAnnouncementTargetRoles(), e);
                return false;
            }
        }).collect(Collectors.toList());
    }
}