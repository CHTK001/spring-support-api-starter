package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemGroup;
import com.chua.starter.service.SysFileSystemGroupService;
import com.chua.starter.service.mapper.SysFileSystemGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件系统分组服务实现类
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileSystemGroupServiceImpl extends ServiceImpl<SysFileSystemGroupMapper, SysFileSystemGroup> implements SysFileSystemGroupService {

    @Override
    public List<SysFileSystemGroup> getTreeForFileSystem() {
        List<SysFileSystemGroup> allGroups = list(Wrappers.<SysFileSystemGroup>lambdaQuery()
                .eq(SysFileSystemGroup::getSysFileSystemGroupStatus, SysFileSystemGroup.Status.ENABLED)
                .orderByAsc(SysFileSystemGroup::getSysFileSystemGroupSort));

        return buildTree(allGroups);
    }

    @Override
    public SysFileSystemGroup getDefaultGroupForFileSystem() {
        return getOne(Wrappers.<SysFileSystemGroup>lambdaQuery()
                .eq(SysFileSystemGroup::getSysFileSystemGroupIsDefault, true)
                .eq(SysFileSystemGroup::getSysFileSystemGroupStatus, SysFileSystemGroup.Status.ENABLED)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultGroupsForFileSystem() {
        log.info("初始化默认分组");

        // 检查是否已有分组
        long count = count();
        if (count > 0) {
            log.info("已存在分组，跳过初始化");
            return ReturnResult.ok(true);
        }

        // 创建默认分组
        List<SysFileSystemGroup> defaultGroups = new ArrayList<>();

        // 默认分组
        SysFileSystemGroup defaultGroup = createGroup(SysFileSystemGroup.DefaultGroupType.DEFAULT, "默认分组", 0, 1, true);
        defaultGroups.add(defaultGroup);

        // 图片分组
        SysFileSystemGroup imageGroup = createGroup(SysFileSystemGroup.DefaultGroupType.IMAGE, "图片", 0, 2, false);
        defaultGroups.add(imageGroup);

        // 视频分组
        SysFileSystemGroup videoGroup = createGroup(SysFileSystemGroup.DefaultGroupType.VIDEO, "视频", 0, 3, false);
        defaultGroups.add(videoGroup);

        // 文档分组
        SysFileSystemGroup documentGroup = createGroup(SysFileSystemGroup.DefaultGroupType.DOCUMENT, "文档", 0, 4, false);
        defaultGroups.add(documentGroup);

        // 音频分组
        SysFileSystemGroup audioGroup = createGroup(SysFileSystemGroup.DefaultGroupType.AUDIO, "音频", 0, 5, false);
        defaultGroups.add(audioGroup);

        boolean result = saveBatch(defaultGroups);
        return result ? ReturnResult.ok(true) : ReturnResult.error("初始化默认分组失败");
    }

    /**
     * 创建分组
     *
     * @param path       路径
     * @param name       名称
     * @param parentId   父ID
     * @param sort       排序
     * @param isDefault  是否默认
     * @return 分组对象
     */
    private SysFileSystemGroup createGroup(String path, String name, Integer parentId, Integer sort, boolean isDefault) {
        SysFileSystemGroup group = new SysFileSystemGroup();
        group.setSysFileSystemGroupName(name);
        group.setSysFileSystemGroupPath(path);
        group.setSysFileSystemGroupFullPath("/" + path);
        group.setSysFileSystemGroupParentId(parentId);
        group.setSysFileSystemGroupLevel(1);
        group.setSysFileSystemGroupTreePath(path);
        group.setSysFileSystemGroupSort(sort);
        group.setSysFileSystemGroupStatus(SysFileSystemGroup.Status.ENABLED);
        group.setSysFileSystemGroupIsDefault(isDefault);
        return group;
    }

    /**
     * 构建树形结构
     *
     * @param allGroups 所有分组列表
     * @return 树形结构列表
     */
    private List<SysFileSystemGroup> buildTree(List<SysFileSystemGroup> allGroups) {
        List<SysFileSystemGroup> roots = new ArrayList<>();
        Map<Integer, SysFileSystemGroup> groupMap = new HashMap<>();

        // 构建ID -> 分组的映射
        for (SysFileSystemGroup group : allGroups) {
            groupMap.put(group.getSysFileSystemGroupId(), group);
        }

        // 构建树形结构
        for (SysFileSystemGroup group : allGroups) {
            Integer parentId = group.getSysFileSystemGroupParentId();
            if (parentId == null || parentId == 0) {
                roots.add(group);
            } else {
                SysFileSystemGroup parent = groupMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(group);
                }
            }
        }

        return roots;
    }
}
