package com.chua.starter.service.support.hikvision.adaptor.sub;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import com.chua.starter.service.support.hikvision.HikvisionClient;
import com.chua.starter.service.support.hikvision.query.YktQxTjQuery;
import com.chua.starter.service.support.hikvision.result.YktQxTjResult;

import java.util.stream.Collectors;

import static com.chua.starter.service.support.hikvision.HikvisionEnum.AUTH_CONFIG_ADD;


/**
 * 一卡通权限
 *
 * @author CH
 */
public class YktQxFactory {

    private final HikvisionClient hikvisionClient;

    public YktQxFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 添加权限配置
     * 权限配置支持按组织、人员和设备通道配置权限，适用综合大楼、学校、医院等场景。
     * 说明：权限配置数据采用异步分批入库方式，接口调用成功后返回权限配置单编号，在配置的过程中分批插入数据，只有当配置单结束时才能查询到完整的权限配置信息。相同的配置数据重复配置时，第一次配置生效后，后面相同的配置将自动过滤丢弃。
     * 合作方配置的tagId用于让多个应用共用出入控制权限服务时，用以区分各自的配置信息。
     * 注意点：不同业务组件设备通道隔离，应该根据业务场景使用不同的设备通道配置权限；如对相同的设备通道都有业务应用，那么人员隔离，应该根据场景使用不同的人员，否则会造成权限条目归属相互竞争的情况，在权限条目综合查询时，数据归属以最后一次入库配置为准。
     * 单次接口最多支持1000个设备资源和1000个人员数据。
     *
     * @return 权限配置
     */
    public YktQxTjResult authConfigAdd(YktQxTjQuery query) {
        JSONObject json = new JSONObject();
        json.put("personDatas", query.getPersonDatas().stream().map(it -> new JSONObject()
                .fluentPut("indexCodes", it.getIndexCodes().toArray(new String[0]))
                .fluentPut("personDataType", it.getPersonDataType())).collect(Collectors.toList())
        );
        if (!CollUtil.isEmpty(query.getResourceInfos())) {
            json.put("resourceInfos", query.getResourceInfos().stream().map(it -> new JSONObject()
                    .fluentPut("resourceIndexCode", it.getResourceIndexCode())
                    .fluentPut("resourceType", it.getResourceType())
                    .fluentPut("channelNos", it.getChannelNos()))
            );
        }
        try {
            return hikvisionClient.executePost(AUTH_CONFIG_ADD, json, YktQxTjResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new YktQxTjResult();
        }
    }
}
