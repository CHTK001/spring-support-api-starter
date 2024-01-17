//package com.chua.starter.learning.reg.entity;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import groovy.transform.Field;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.Transient;
//
//import javax.validation.constraints.NotNull;
//import java.util.Date;
//
///**
// * 人脸特征值
// *
// * @author CH
// */
//@Data
//@Document(indexName = "face",createIndex = false)
//public class SysFace {
//
//    /**
//     * 身份证/编号
//     */
//    @Id
//    @Field(type = FieldType.Text)
//    @ApiModelProperty("身份证/编号")
//    @NotNull(message = "编号不能为空")
//    private String code;
//    /**
//     * 姓名
//     */
//    @ApiModelProperty("姓名")
//    @NotNull(message = "姓名不能为空")
//    @Field(type = FieldType.Keyword, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
//    private String name;
//    /**
//     * 图片地址
//     */
//    @ApiModelProperty("图片地址")
//    @Field(type = FieldType.Text)
//    private String url;
//    /**
//     * 人脸512特征
//     */
//    @ApiModelProperty("人脸512特征")
//    @Field(type = FieldType.Float_Range)
//    private float[] feature;
//    /**
//     * 人脸相似度
//     */
//    @ApiModelProperty("人脸相似度")
//    @Field(type = FieldType.Float)
//    private Float score;
//    /**
//     * 人脸位置
//     */
//    @ApiModelProperty("人脸位置")
//    @Field(type = FieldType.Text)
//    private String box;
//    /**
//     * 关键词
//     */
//    @ApiModelProperty("关键词")
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
//    private String keyword;
//    /**
//     * 相似度
//     */
//    @ApiModelProperty("相似度")
//    @Transient
//    private float similarities;
//
//    @ApiModelProperty("创建人")
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @Field(type = FieldType.Text)
//    private String createName;
//    @ApiModelProperty("创建人")
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @Field(type = FieldType.Text)
//    private String createBy;
//
//    @ApiModelProperty("创建时间")
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
//    private Date createTime;
//
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @ApiModelProperty("更新时间")
//    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date updateTime;
//
//    @Field(type = FieldType.Text)
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @ApiModelProperty("更新人")
//    private String updateName;
//
//    @Field(type = FieldType.Text)
//    @JsonInclude(value = JsonInclude.Include.NON_NULL)
//    @ApiModelProperty("更新人")
//    private String updateBy;
//}
