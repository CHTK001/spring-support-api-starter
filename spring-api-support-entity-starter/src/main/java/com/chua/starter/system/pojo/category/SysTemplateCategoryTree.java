package com.chua.starter.system.pojo.category;

import com.chua.starter.system.entity.SysTemplateCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 模板类别树
 * @author CH
 * @since 2024/9/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysTemplateCategoryTree extends SysTemplateCategory {


    /**
     * 子集
     */
    private List<SysTemplateCategoryTree> children;
}
