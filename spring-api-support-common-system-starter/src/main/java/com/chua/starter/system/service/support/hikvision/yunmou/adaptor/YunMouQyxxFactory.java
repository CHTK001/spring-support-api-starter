package com.chua.starter.system.service.support.hikvision.yunmou.adaptor;

import com.chua.common.support.utils.PageUtils;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.HikvisionEnum;
import com.chua.starter.system.service.support.hikvision.adaptor.sub.QyxxFactory;
import com.chua.starter.system.service.support.hikvision.result.HikRegion;
import com.chua.starter.system.service.support.hikvision.result.HikRegionCascade;
import com.chua.starter.system.service.support.hikvision.yunmou.pojo.OrgInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouQyxxFactory extends QyxxFactory {
    private final SysProject sysProject;

    public YunMouQyxxFactory(HikvisionClient hikvisionClient, SysProject sysProject) {
        super(hikvisionClient);
        this.sysProject = sysProject;
    }

    @Override
    public HikRegion regions(int page, int pageSize) {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + hikvisionClient.getToken());
        List<OrgInfo> orgInfo = (List<OrgInfo>) hikvisionClient.executeGet(HikvisionEnum.YUN_MOU_GROUPS_LIST_ALL.getUrl(), headers, OrgInfo.class);
        HikRegion hikRegion = new HikRegion();
        hikRegion.setPageNo(page);
        hikRegion.setPageSize(pageSize);
        hikRegion.setTotal(orgInfo.size());
        hikRegion.setList(PageUtils.toPage(page, pageSize).page(orgInfo.stream().map(data -> {
            HikRegionCascade hikRegionCascade = new HikRegionCascade();
            hikRegionCascade.setIndexCode(data.getGroupId());
            hikRegionCascade.setName(data.getGroupName());
            hikRegionCascade.setRegionPath(data.getGroupNo());
            hikRegionCascade.setParentIndexCode(data.getParentId());
            return hikRegionCascade;
        }).collect(Collectors.toList())));

        return hikRegion;
    }
}
