package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;

import java.util.List;
import java.util.Set;

/**
 * 系统部门服务接口
 * 璇ユ帴鍙ｅ畾涔変簡涓庣郴缁熼儴闂ㄧ浉鍏崇殑涓氬姟操作锛屽寘鎷煡璇€佸鍒犳敼绛?
 * @since 2024/8/7
 * @author CH
 */
public interface SysDeptService extends IService<SysDept>{


    /**
     * 查询部门列表
     * 鏍规嵁部门查询鏉′欢鍜岃鑹查泦鍚堬紝返回绗﹀悎鏉′欢鐨勯儴闂ㄥ垪琛?
     * @param sysDept 部门查询鏉′欢
     * @param roles 角色闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨筛选?
     * @return 部门列表锛屽寘鍚瘡涓儴闂ㄧ殑详细信息
     */
    List<SysDeptVO> list(SysDept sysDept, Set<String> roles);

    /**
     * 删除部门
     * 鏍规嵁部门ID杩涜部门删除操作锛岃繑鍥炲垹闄ょ粨鏋?
     * @param deptId 部门ID锛屽敮涓€鏍囪瘑瑕佸垹闄ょ殑部门
     * @return 删除结果锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<Boolean> delete(Long deptId);

    /**
     * 更新部门信息
     * 鏍规嵁角色闆嗗悎鍜岄儴闂ㄤ俊鎭洿鏂伴儴闂紝返回更新结果
     * @param roles 角色闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨筛选?
     * @param sysDept 寰呮洿鏂扮殑部门信息
     * @return 更新结果锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept);

    /**
     * 鏂板部门
     * 鏍规嵁部门信息鍜岃鑹查泦鍚堬紝鏂板一个儴闂紝返回鏂板结果
     * @param sysDept 寰呮柊澧炵殑部门信息
     * @param roles 角色闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨筛选?
     * @return 鏂板结果锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles);
}

