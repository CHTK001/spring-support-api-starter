package com.chua.starter.service.support.hikvision.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 人员信息
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class PeopleInfo {
    /**
     * 人员ID，可以指定人员personId，不允许与其他人员personId重复，包括已删除的人员。 为空时平台自动生成人员ID
     */
    @JsonProperty("personId")
    private String personId;
    /**
     * 人员名称，1~32个字符；不能包含 ’ / \ : * ? " < >(必填)
     */
    @JsonProperty("personName")
    private String personName;
    /**
     * 性别，1：男；2：女；0：未知(必填)
     */
    @JsonProperty("gender")
    private String gender;
    /**
     * 所属组织标识，必须是已存在组织，从查询组织列表v2接口获取返回参数orgIndexCode(必填)
     */
    @JsonProperty("orgIndexCode")
    private String orgIndexCode;
    /**
     * 出生日期，举例：1992-09-12
     */
    @JsonProperty("birthday")
    private String birthday;
    /**
     * 手机号，1-20位数字,平台上人员信息实名标识选择为手机号码时必填
     */
    @JsonProperty("phoneNo")
    private String phoneNo;
    /**
     * 邮箱，举例：hic@163.com
     */
    @JsonProperty("email")
    private String email;
    /**
     * 证件类型，参考附录A 数据字典，平台上人员信息实名标识选择为身份证件时必填
     */
    @JsonProperty("certificateType")
    private String certificateType;
    /**
     * 证件号码，1-20位数字字母，平台上人员信息实名标识选择为身份证件时必填
     */
    @JsonProperty("certificateNo")
    private String certificateNo;
    /**
     * 工号，1-32个字符
     */
    @JsonProperty("jobNo")
    private String jobNo;
    /**
     * 人员类型（1：教工；2：学员；3：流动人员；4：其他人员）
     */
    @JsonProperty("personType")
    private String personType;
    @JsonProperty("faces")
    private List<FacesDTO> faces;

    @NoArgsConstructor
    @Data
    public static class FacesDTO {
        /**
         * 人脸图片base64编码后的字符
         */
        @JsonProperty("faceData")
        private String faceData;
    }
}
