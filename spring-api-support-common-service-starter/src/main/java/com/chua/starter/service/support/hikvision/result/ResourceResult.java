package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CH
 */
@NoArgsConstructor
@Data
public class ResourceResult {


    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("required")
    private Integer required;
    @JsonProperty("fieldType")
    private String fieldType;
    @JsonProperty("desc")
    private String desc;
    @JsonProperty("minLength")
    private Integer minLength;
    @JsonProperty("maxLength")
    private Integer maxLength;
    @JsonProperty("valueType")
    private String valueType;
    @JsonProperty("optionKey")
    private Integer optionKey;
    @JsonProperty("key")
    private Integer key;
    @JsonProperty("regular")
    private String regular;
    @JsonProperty("unique")
    private Integer unique;
    @JsonProperty("ldapName")
    private String ldapName;
    @JsonProperty("objectClass")
    private Object objectClass;
}
