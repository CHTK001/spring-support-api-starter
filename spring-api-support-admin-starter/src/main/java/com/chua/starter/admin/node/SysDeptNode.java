package com.chua.starter.admin.node;

import com.chua.starter.admin.entity.SysDept;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 机构树
 * @author CH
 */
@Data
@Schema(description = "机构树")
public class SysDeptNode extends SysDept {
    /**
     * 机构所有父ID
     */
    @Schema(description = "机构所有父ID")
    private List<SysDeptNode> children;
}
