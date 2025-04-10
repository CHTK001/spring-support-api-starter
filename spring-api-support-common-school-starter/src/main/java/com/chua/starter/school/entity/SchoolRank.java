package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校排名实体类
 *
 * @author CH
 */
@Data
@TableName("school_rank")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校排名")
public class SchoolRank extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "排名ID")
    private Long schoolRankId;

    @Schema(description = "学校代码")
    private String schoolCode;

    @Schema(description = "排名值")
    private Integer schoolRankValue;

    @Schema(description = "排名类型")
    private String schoolRankType;

    @Schema(description = "排名名称")
    private String schoolRankName;
}