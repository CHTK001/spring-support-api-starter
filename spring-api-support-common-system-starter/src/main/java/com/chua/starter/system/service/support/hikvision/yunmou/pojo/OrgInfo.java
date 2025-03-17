package com.chua.starter.system.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机构信息
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class OrgInfo {

    @SerializedName("groupId")
    private String groupId;
    @SerializedName("groupName")
    private String groupName;
    @SerializedName("groupNo")
    private String groupNo;
    @SerializedName("parentId")
    private String parentId;
}
