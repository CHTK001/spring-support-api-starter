package com.chua.starter.monitor.service.acme;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * ACME 订单服务实现
 *
 * @author CH
 * @since 4.0.0.42
 */
@Service
public class MonitorAcmeOrderServiceImpl
        extends ServiceImpl<MonitorAcmeOrderMapper, MonitorAcmeOrder>
        implements MonitorAcmeOrderService {

    @Override
    public List<MonitorAcmeOrder> listByAccountId(Integer accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeOrder>lambdaQuery()
                .eq(MonitorAcmeOrder::getAcmeAccountId, accountId)
                .orderByDesc(MonitorAcmeOrder::getCreateTime));
    }

    @Override
    public List<MonitorAcmeOrder> listByCertId(Integer certId) {
        if (certId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeOrder>lambdaQuery()
                .eq(MonitorAcmeOrder::getAcmeCertId, certId));
    }

    @Override
    public List<MonitorAcmeOrder> listByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeOrder>lambdaQuery()
                .eq(MonitorAcmeOrder::getAcmeOrderStatus, status));
    }

    @Override
    public MonitorAcmeOrder getByOrderUrl(String orderUrl) {
        if (orderUrl == null || orderUrl.isEmpty()) {
            return null;
        }
        return getOne(Wrappers.<MonitorAcmeOrder>lambdaQuery()
                .eq(MonitorAcmeOrder::getAcmeOrderUrl, orderUrl)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorAcmeOrder saveOrUpdateOrder(MonitorAcmeOrder order) {
        if (order == null) {
            return null;
        }
        if (order.getAcmeOrderId() == null) {
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            save(order);
            return order;
        }
        order.setUpdateTime(LocalDateTime.now());
        boolean ok = updateById(order);
        return ok ? order : null;
    }

    @Override
    public boolean updateStatus(Integer orderId, String newStatus) {
        if (orderId == null || newStatus == null) {
            return false;
        }
        MonitorAcmeOrder update = new MonitorAcmeOrder();
        update.setAcmeOrderId(orderId);
        update.setAcmeOrderStatus(newStatus);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    public boolean bindCert(Integer orderId, Integer certId) {
        if (orderId == null || certId == null) {
            return false;
        }
        MonitorAcmeOrder update = new MonitorAcmeOrder();
        update.setAcmeOrderId(orderId);
        update.setAcmeCertId(certId);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeOrder(Integer orderId) {
        if (orderId == null) {
            return false;
        }
        return removeById(orderId);
    }

    @Override
    public long countOrders() {
        return count();
    }
}
