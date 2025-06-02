/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.model;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.chua.starter.monitor.jpom.common.ServerConst;
import com.chua.starter.monitor.jpom.db.TableName;
import com.chua.starter.monitor.jpom.model.data.WorkspaceModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作空间 数据
 *
 * @author bwcx_jzy
 * @since 2021/12/04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BaseWorkspaceModel extends BaseUserModifyDbModel {

    /**
     * 工作空间ID
     *
     * @see WorkspaceModel
     * @see Const#WORKSPACE_ID_REQ_HEADER
     */
    private String workspaceId;

    /**
     * 所有实现过的 class
     *
     * @return set
     */
    public static Set<Class<?>> allClass() {
        return ClassUtil.scanPackageBySuper("com.chua.starter.monitor.jpom", BaseWorkspaceModel.class);
    }

    /**
     * 所有实现过的 class
     *
     * @return set
     */
    public static Set<Class<?>> allTableClass() {
        Set<Class<?>> classes1 = allClass();
        return classes1.stream()
                .filter(aClass -> aClass.isAnnotationPresent(TableName.class))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public boolean global() {
        return StrUtil.equals(this.workspaceId, ServerConst.WORKSPACE_GLOBAL);
    }

}
