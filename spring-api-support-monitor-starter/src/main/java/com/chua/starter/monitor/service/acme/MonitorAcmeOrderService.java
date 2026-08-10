package com.chua.starter.monitor.service.acme;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;

import java.util.List;

/**
 * ACME 订单服务
 *
 * <p>记录 ACME 订单(pending/ready/processing/valid/invalid)及其关联 challenge。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface MonitorAcmeOrderService extends IService<MonitorAcmeOrder> {

    /**
     * 查询账户的所有订单。
     *
     * @param accountId 账户主键
     * @return 订单列表
     */
    List<MonitorAcmeOrder> listByAccountId(Integer accountId);

    /**
     * 查询关联到某证书的所有订单。
     *
     * @param certId 证书主键
     * @return 订单列表
     */
    List<MonitorAcmeOrder> listByCertId(Integer certId);

    /**
     * 按状态查询订单。
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<MonitorAcmeOrder> listByStatus(String status);

    /**
     * 按 ACME Order URL 查找订单(去重/幂等)。
     *
     * @param orderUrl ACME 服务下发的 order URL
     * @return 订单或 null
     */
    MonitorAcmeOrder getByOrderUrl(String orderUrl);

    /**
     * 保存订单。
     *
     * @param order 订单
     * @return 保存后的对象
     */
    MonitorAcmeOrder saveOrUpdateOrder(MonitorAcmeOrder order);

    /**
     * 更新订单状态。
     *
     * @param orderId   订单主键
     * @param newStatus 新状态
     * @return 是否成功
     */
    boolean updateStatus(Integer orderId, String newStatus);

    /**
     * 绑定订单到已签发的证书。
     *
     * @param orderId 订单主键
     * @param certId  证书主键
     * @return 是否成功
     */
    boolean bindCert(Integer orderId, Integer certId);

    /**
     * 删除订单。
     *
     * @param orderId 订单主键
     * @return 是否成功
     */
    boolean removeOrder(Integer orderId);

    /**
     * 统计订单总数。
     *
     * @return 数量
     */
    long countOrders();
}
