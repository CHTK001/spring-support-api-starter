package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.entity.SysSfc;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcService extends IService<SysSfc> {


    /**
     * 分页列表
     *
     * @param query   查询条件
     * @param sysSfc 查询条件
     * @param roles   角色
     * @param userId  用户id
     * @return 分页列表
     */
    IPage<SysSfc> pageForUserId(Query<SysSfc> query, SysSfc sysSfc, Set<String> roles, String userId);


    /**
     * 上传组件
     *
     * @param file    文件
     * @param sysSfc 组件信息
     * @return 上传结果
     */
    ReturnResult<Boolean> uploadSfc(MultipartFile file, SysSfc sysSfc);

    /**
     * 保存组件
     *
     * @param sysSfc 组件信息
     * @param roles  角色
     * @return 保存结果
     */
    ReturnResult<SysSfc> save(SysSfc sysSfc, Set<String> roles);

    /**
     * 更新组件
     * @param sysSfc 组件信息
     * @param roles 角色
     * @return 更新结果
     */
    ReturnResult<Boolean> updateById(SysSfc sysSfc, Set<String> roles);

    /**
     * 删除组件
     * @param sfcId 组件id
     * @param roles 角色
     * @return 删除结果
     */
    ReturnResult<Boolean> delete(Long sfcId, Set<String> roles);

    /**
     * 安装组件
     * @param sysSfc 组件信息
     * @param userId 用户id
     * @return 安装结果
     */
    ReturnResult<Boolean> install(SysSfc sysSfc, Integer userId);

    /**
     * 卸载组件
     * @param sysSfc 组件信息
     * @param userId 用户id
     * @return 卸载结果
     */
    ReturnResult<Boolean> uninstall(SysSfc sysSfc, Integer userId);
}
