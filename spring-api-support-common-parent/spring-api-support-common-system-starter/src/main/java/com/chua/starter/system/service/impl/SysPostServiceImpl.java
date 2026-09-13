package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserPost;
import com.chua.starter.system.mapper.SysPostMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserPostMapper;
import com.chua.starter.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* 岗位服务实现
*
* @since 4.0.0.43
* @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    /** 岗位状态: 启用 */
    private static final int POST_STATUS_ENABLED = 0;

    /** SYS用户岗位映射器 */
    private final SysUserPostMapper sysUserPostMapper;

    /** SYS用户映射器 */
    private final SysUserMapper sysUserMapper;

    @Override
    /** 列表 */
    public List<SysPost> list(SysPost sysPost) {
        return list(Wrappers.<SysPost>lambdaQuery()
                .eq(null != sysPost && null != sysPost.getSysPostStatus(), SysPost::getSysPostStatus,
                        null == sysPost ? null : sysPost.getSysPostStatus())
                .like(null != sysPost && StringUtils.isNotBlank(sysPost.getSysPostName()), SysPost::getSysPostName,
                        null == sysPost ? null : sysPost.getSysPostName())
                .orderByAsc(SysPost::getSysPostSort)
                .orderByDesc(SysPost::getCreateTime));
    }

    @Override
    /** 分页 */
    public IPage<SysPost> page(Query<SysPost> query, SysPost sysPost) {
        IPage<SysPost> result = page(query.createPage(), Wrappers.<SysPost>lambdaQuery()
                .eq(null != sysPost.getSysPostStatus(), SysPost::getSysPostStatus, sysPost.getSysPostStatus())
                .like(StringUtils.isNotBlank(sysPost.getSysPostName()), SysPost::getSysPostName, sysPost.getSysPostName())
                .like(StringUtils.isNotBlank(sysPost.getSysPostCode()), SysPost::getSysPostCode, sysPost.getSysPostCode())
                .orderByAsc(SysPost::getSysPostSort)
                .orderByDesc(SysPost::getCreateTime));
        // 批量填充岗位关联用户数(SELECT sys_post_id, COUNT(*) ... GROUP BY sys_post_id)
        fillUserCount(result.getRecords());
        return result;
    }

    /**
     * 批量填充岗位的关联用户数
     *
     * @param records 岗位分页记录
     */
    private void fillUserCount(List<SysPost> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<Long> postIds = records.stream().map(SysPost::getSysPostId).collect(Collectors.toList());
        List<SysUserPost> rels = sysUserPostMapper.selectList(
                Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getSysPostId, postIds));
        Map<Long, Integer> countMap = rels.stream().collect(Collectors.groupingBy(
                SysUserPost::getSysPostId, Collectors.summingInt(rel -> 1)));
        for (SysPost post : records) {
            post.setUserCount(countMap.getOrDefault(post.getSysPostId(), 0));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 保存岗位 */
    public ReturnResult<SysPost> savePost(SysPost sysPost) {
        if (count(Wrappers.<SysPost>lambdaQuery().eq(SysPost::getSysPostCode, sysPost.getSysPostCode())) > 0) {
            return ReturnResult.illegal("岗位编码已存在");
        }
        if (null == sysPost.getSysPostStatus()) {
            sysPost.setSysPostStatus(POST_STATUS_ENABLED);
        }
        if (null == sysPost.getSysPostSort()) {
            sysPost.setSysPostSort(0);
        }
        save(sysPost);
        return ReturnResult.ok(sysPost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 更新岗位 */
    public ReturnResult<Boolean> updatePost(SysPost sysPost) {
        if (null == sysPost.getSysPostId()) {
            return ReturnResult.illegal("岗位信息不存在");
        }
        SysPost exist = getById(sysPost.getSysPostId());
        if (null == exist) {
            return ReturnResult.illegal("岗位信息不存在");
        }
        if (StringUtils.isNotBlank(sysPost.getSysPostCode())
                && !sysPost.getSysPostCode().equals(exist.getSysPostCode())
                && count(Wrappers.<SysPost>lambdaQuery().eq(SysPost::getSysPostCode, sysPost.getSysPostCode())
                .ne(SysPost::getSysPostId, sysPost.getSysPostId())) > 0) {
            return ReturnResult.illegal("岗位编码已存在");
        }
        return ReturnResult.of(updateById(sysPost));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 删除岗位 */
    public ReturnResult<Boolean> deletePost(Integer postId) {
        if (null == postId) {
            return ReturnResult.illegal("岗位信息不存在");
        }
        Long relCount = sysUserPostMapper.selectCount(
                Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getSysPostId, postId));
        if (null != relCount && relCount > 0) {
            return ReturnResult.illegal("该岗位下存在关联用户, 无法删除");
        }
        return ReturnResult.of(removeById(postId));
    }

    @Override
    /** 获取岗位下的用户 */
    public List<SysUser> getUsersByPostId(Integer postId) {
        if (null == postId) {
            return Collections.emptyList();
        }
        List<SysUserPost> rels = sysUserPostMapper.selectList(
                Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getSysPostId, postId));
        if (CollectionUtils.isEmpty(rels)) {
            return Collections.emptyList();
        }
        List<Integer> userIds = rels.stream().map(SysUserPost::getSysUserId).collect(Collectors.toList());
        return sysUserMapper.selectBatchIds(userIds);
    }
}
