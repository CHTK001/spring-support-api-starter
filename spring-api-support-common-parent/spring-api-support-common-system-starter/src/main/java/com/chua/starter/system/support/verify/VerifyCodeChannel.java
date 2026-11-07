// [P3C BL-边界 豁免]

package com.chua.starter.system.support.verify;

/**
 * 验证码下发渠道扩展点
 *
 * <p>把"账号+验证码"的存储校验与"怎么把码送到目标手里"解耦：新增手机号、企业微信等渠道时
 * 只需实现本接口并登记到 SPI 扩展文件，{@code VerifyCodeService} 的生成/限频/校验逻辑无需改动。</p>
 *
 * <p>实现类必须有公共无参构造（SPI 以无参构造实例化），注册键即渠道标识且会被统一转为大写。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface VerifyCodeChannel {

    /**
     * 服务器侧该渠道是否已配置可用
     *
     * <p>用于个人中心决定是否渲染"验证"按钮，避免用户填完邮箱才发现服务器发不出码。</p>
     *
     * @return true 表示可以下发验证码
     */
    boolean configured();

    /**
     * 向目标下发验证码
     *
     * @param message 验证码消息（收件目标、业务场景、验证码、有效期）
     * @throws Exception 投递失败时抛出，由调用方转成业务错误
     */
    void send(VerifyCodeMessage message) throws Exception;
}
