package com.chua.starter.collect.dto;

import com.chua.starter.collect.entity.CollectField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 结果字段整体保存入参。
 *
 * <p>字段定义决定结果表格列、去重键与导出列，保存时按流程节点整体覆盖。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "采集结果字段保存入参")
public class CollectFieldSaveDTO {

    /**
     * 所属流程ID。
     */
    @Schema(description = "所属流程ID")
    private Integer collectFlowId;

    /**
     * 产出字段的节点ID，空表示流程级。
     */
    @Schema(description = "产出字段的节点ID")
    private String collectNodeId;

    /**
     * 字段定义，顺序即列顺序。
     */
    @Schema(description = "字段定义列表")
    private List<CollectField> fields = new ArrayList<>();
}
