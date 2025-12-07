package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysSfc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcService extends IService<SysSfc> {


    /**
     * 分页列表
     *
     * @param query   查询鏉′欢
     * @param sysSfc 查询鏉′欢
     * @param roles   角色
     * @param userId  用户id
     * @return 分页列表
     */
    IPage<SysSfc> pageForUserId(Query<SysSfc> query, SysSfc sysSfc, Set<String> roles, String userId);


    /**
     * 上传缁勪欢
     *
     * @param file    文件
     * @param sysSfc 缁勪欢信息
     * @return 上传结果
     */
    ReturnResult<Boolean> uploadSfc(MultipartFile file, SysSfc sysSfc);

    /**
     * 保存缁勪欢
     *
     * @param sysSfc 缁勪欢信息
     * @param roles  角色
     * @return 保存结果
     */
    ReturnResult<SysSfc> save(SysSfc sysSfc, Set<String> roles);

    /**
     * 更新缁勪欢
     * @param sysSfc 缁勪欢信息
     * @param roles 角色
     * @return 更新结果
     */
    ReturnResult<Boolean> updateById(SysSfc sysSfc, Set<String> roles);

    /**
     * 删除缁勪欢
     * @param sfcId 缁勪欢id
     * @param roles 角色
     * @return 删除结果
     */
    ReturnResult<Boolean> delete(Long sfcId, Set<String> roles);

    /**
     * 瀹夎缁勪欢
     * @param sysSfc 缁勪欢信息
     * @param userId 用户id
     * @return 瀹夎结果
     */
    ReturnResult<Boolean> install(SysSfc sysSfc, Integer userId);

    /**
     * 鍗歌浇缁勪欢
     * @param sysSfc 缁勪欢信息
     * @param userId 用户id
     * @return 鍗歌浇结果
     */
    ReturnResult<Boolean> uninstall(SysSfc sysSfc, Integer userId);

    /**
     * 获取缁勪欢
     * @param sysSfcId 缁勪欢id
     * @param roles 角色
     * @param userId 用户id
     * @return 缁勪欢信息
     */
    ReturnResult<String> get(Long sysSfcId, Set<String> roles, String userId);

    /**
     * 鎴戠殑缁勪欢
     * @param sysSfcCategory 缁勪欢鍒嗙被
     * @param userId 用户id
     * @return 组件列表
     */
    ReturnResult<List<SysSfc>> mine(String sysSfcCategory, String userId);
}
