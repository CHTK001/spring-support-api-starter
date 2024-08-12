package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;

import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
public interface SysDeptService extends IService<SysDept>{


    List<SysDeptVO> list(SysDept sysDept, Set<String> roles);

    ReturnResult<Boolean> delete(Long deptId);

    ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept);

    ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles);
}
