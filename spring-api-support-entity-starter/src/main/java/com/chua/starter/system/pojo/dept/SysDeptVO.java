package com.chua.starter.system.pojo.dept;

import com.chua.starter.system.entity.SysDept;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

/**
 * 组织部门
 * @author CH
 * @since 2024/8/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "组织部门")
public class SysDeptVO extends SysDept {

    /**
     * 子部门
     */
    private List<SysDeptVO> children = Collections.emptyList();
}
