package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校字典实体类
 * 用于存储学校相关的字典数据，如学校类型、办学层次、主管部门等
 *
 * @author CH
 */
@Data
@TableName("school_dict")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校字典")
public class SchoolDict extends SysBase {

    /**
     * 字典ID，主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典ID")
    private Long schoolDictId;

    /**
     * 字典代码，用于标识字典项
     */
    @Schema(description = "字典代码")
    private String schoolDictCode;

    /**
     * 字典名称，字典项的描述
     */
    @Schema(description = "字典名称")
    private String schoolDictName;

    /**
     * 字典类型，用于分类字典项，如学校类型、办学层次等
     */
    @Schema(description = "字典类型")
    private String schoolDictType;
}