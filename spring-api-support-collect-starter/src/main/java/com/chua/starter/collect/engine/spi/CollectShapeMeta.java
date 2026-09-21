package com.chua.starter.collect.engine.spi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点类型元数据。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "节点类型元数据")
public class CollectShapeMeta {

    /**
     * 节点类型标识，与执行器 supportShape 一致。
     */
    @Schema(description = "节点类型标识")
    private String shape;

    /**
     * 界面显示名称。
     */
    @Schema(description = "界面显示名称")
    private String label;

    /**
     * 分组：来源、解析、控制、输出。
     */
    @Schema(description = "节点分组")
    private String group;

    /**
     * 图标标识，前端按分组着色。
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 功能说明。
     */
    @Schema(description = "功能说明")
    private String description;

    /**
     * 出参变量名说明，例如 resp 表示结果写入 ${resp}。
     */
    @Schema(description = "出参变量")
    private String outputVariable;

    /**
     * 是否展示抽取字段编辑表格。
     */
    @Schema(description = "是否展示抽取字段表格")
    private boolean fieldEditor;

    /**
     * 参数元数据列表。
     */
    @Schema(description = "参数元数据列表")
    private List<CollectParamMeta> params = new ArrayList<>();
}
