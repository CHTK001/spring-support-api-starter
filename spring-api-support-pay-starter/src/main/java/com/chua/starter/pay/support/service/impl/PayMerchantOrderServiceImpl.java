package com.chua.starter.pay.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.mapper.PayMerchantOrderMapper;
import com.chua.starter.pay.support.pojo.PayMerchantOrderQueryRequest;
import com.chua.starter.pay.support.pojo.PayOrderQueryRequest;
import com.chua.starter.pay.support.service.PayMerchantOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * @author CH
 * @since 2024/12/30
 */
@Service
@RequiredArgsConstructor
public class PayMerchantOrderServiceImpl extends ServiceImpl<PayMerchantOrderMapper, PayMerchantOrder> implements PayMerchantOrderService {

    @Override
    public ReturnPageResult<PayMerchantOrder> page(Query<PayMerchantOrder> query, Set<String> roles, String deptId, String userId, PayOrderQueryRequest request) {
        if (AuthConstant.hasAdmin(roles)) {
            return ReturnPageResultUtils.ok(baseMapper.pageForOrder(query.createPage(), request));
        }

        if (AuthConstant.isDept(roles)) {
            return ReturnPageResultUtils.ok(null == request.getPayMerchantDeptId() ? new Page<>() : baseMapper.pageForOrderDept(query.createPage(), request));
        }

        if (AuthConstant.isDeptOrganizer(roles)) {
            return ReturnPageResultUtils.ok(null == request.getPayMerchantDeptId() ? new Page<>() : baseMapper.pageForOrderDeptOrganizer(query.createPage(), request));
        }

        return ReturnPageResultUtils.ok(baseMapper.pageForOwner(query.createPage(), request));
    }


    @Override
    public ReturnPageResult<PayMerchantOrder> page(Query<PayMerchantOrder> page, PayMerchantOrderQueryRequest request) {
        return page(page, Collections.emptySet(), null, null, BeanUtils.copyProperties(request, PayOrderQueryRequest.class));
    }


}
