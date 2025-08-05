package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 节点API分组
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Schema(description = "节点API分组")
public class NodeApiGroup {

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "API列表")
    private List<NodeApiInfo> apis;

    public NodeApiGroup() {}

    public NodeApiGroup(String name, List<NodeApiInfo> apis) {
        this.name = name;
        this.apis = apis;
    }
}
