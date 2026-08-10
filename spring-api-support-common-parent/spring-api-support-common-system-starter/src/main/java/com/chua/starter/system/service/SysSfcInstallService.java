package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.entity.system.SysSfcInstall;

import java.util.List;

/**
 * SFC 安装记录服务
 *
 * <p>管理用户安装 SFC (Software Function Component) 的授权记录。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface SysSfcInstallService extends IService<SysSfcInstall> {

    /**
     * 查询用户的安装记录。
     *
     * @param userId 用户主键
     * @return 安装记录列表
     */
    List<SysSfcInstall> listByUserId(Integer userId);

    /**
     * 查询安装某 SFC 的所有用户。
     *
     * @param sfcId SFC 主键
     * @return 安装记录列表
     */
    List<SysSfcInstall> listBySfcId(Integer sfcId);

    /**
     * 查询用户是否已安装指定 SFC。
     *
     * @param userId 用户主键
     * @param sfcId  SFC 主键
     * @return SysSfcInstall 或 null(未安装)
     */
    SysSfcInstall getInstall(Integer userId, Integer sfcId);

    /**
     * 记录一次安装。
     *
     * @param install 安装记录
     * @return 保存后的对象
     */
    SysSfcInstall recordInstall(SysSfcInstall install);

    /**
     * 卸载(记录删除)。
     *
     * @param userId 用户主键
     * @param sfcId  SFC 主键
     * @return 是否删除成功
     */
    boolean uninstall(Integer userId, Integer sfcId);
}
