package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校字典实体类
 *
 * @author CH
 */
@Data
@TableName("school_dict")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校字典")
public class SchoolDict extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "字典ID")
    private Long schoolDictId;

    @Schema(description = "字典代码")
    private String schoolDictCode;

    @Schema(description = "字典名称")
    private String schoolDictName;

    @Schema(description = "字典类型")
    private String schoolDictType;
}