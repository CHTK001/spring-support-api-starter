package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysAccount;

import java.util.List;

/**
 * 第三方账号服务
 *
 * <p>管理用户绑定的第三方账号(微信/支付宝/钉钉等),基于 {@code sysAccountOpenid}/{@code sysAccountUnionid}
 * 进行绑定/查找/解绑,与 {@code SysUser} 通过 {@code sysAccountUserId} 关联。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface SysAccountService extends IService<SysAccount> {

    /**
     * 根据 openid 查找第三方账号。
     *
     * @param accountType 账号类型(wechat/alipay/dingtalk...)
     * @param openid      第三方 openid
     * @return SysAccount 或 null
     */
    SysAccount getByOpenid(String accountType, String openid);

    /**
     * 根据 unionid 查找第三方账号(同一开放平台下多 App 共享)。
     *
     * @param accountType 账号类型
     * @param unionid     第三方 unionid
     * @return SysAccount 列表(可能多个应用)
     */
    List<SysAccount> listByUnionid(String accountType, String unionid);

    /**
     * 绑定第三方账号到本地用户。
     *
     * @param account 待绑定账号信息(不含 sysAccountId)
     * @return 绑定结果(成功返回保存后的账号)
     */
    ReturnResult<SysAccount> bindAccount(SysAccount account);

    /**
     * 解绑第三方账号。
     *
     * @param accountId 主键
     * @return 是否成功
     */
    ReturnResult<Boolean> unbindAccount(Integer accountId);

    /**
     * 解绑指定用户的所有第三方账号。
     *
     * @param userId 本地用户主键
     * @return 解绑条数
     */
    int unbindByUserId(Integer userId);

    /**
     * 列出用户绑定的所有第三方账号。
     *
     * @param userId 本地用户主键
     * @return 账号列表
     */
    List<SysAccount> listByUserId(Integer userId);

    /**
     * 更新登录信息(累计次数、最后 IP、最后登录时间)。
     *
     * @param accountId 主键
     * @param ip        本次登录 IP
     * @param ipAddress 本次登录归属地
     * @return 是否成功
     */
    boolean updateLoginInfo(Integer accountId, String ip, String ipAddress);

    /**
     * 禁用/启用账号。
     *
     * @param accountId 主键
     * @param forbidden true=禁用,false=启用
     * @return 是否成功
     */
    boolean setForbidden(Integer accountId, boolean forbidden);
}
