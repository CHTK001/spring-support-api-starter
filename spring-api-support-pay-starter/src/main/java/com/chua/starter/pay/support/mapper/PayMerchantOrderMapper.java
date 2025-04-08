package com.chua.starter.pay.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.pay.support.dto.PayOrderStatisticDTO;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.pojo.PayOrderQueryRequest;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayOrderV1Request;
import com.chua.starter.pay.support.pojo.PayReOrderQueryV1Request;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 商户订单Mapper
 *
 * @author CH
 */
@Mapper
public interface PayMerchantOrderMapper extends BaseMapper<PayMerchantOrder> {
    /**
     * 分页查询
     *
     * @param page    分页
     * @param request 请求
     * @return
     */
    IPage<PayMerchantOrder> pageForOrder(@Param("page") Page<PayMerchantOrder> page,
                                         @Param("request") PayOrderQueryRequest request);

    /**
     * 分页查询机构订单
     *
     * @param page    分页
     * @param request 查询条件
     * @return
     */
    IPage<PayMerchantOrder> pageForOrderDept(@Param("page") Page<PayMerchantOrder> page,
                                             @Param("request") PayOrderQueryRequest request);

    /**
     * 分页查询机构订单
     *
     * @param page    分页
     * @param request 查询条件
     * @return
     */
    IPage<PayMerchantOrder> pageForOrderDeptOrganizer(@Param("page") Page<PayMerchantOrder> page,
                                                      @Param("request") PayOrderQueryRequest request);

    /**
     * 分页查询
     *
     * @param page       分页
     * @param request    查询条件
     * @param sysUserIds 账单所属用户
     * @return
     */
    IPage<PayMerchantOrder> order(@Param("page") Page<PayMerchantOrder> page,
                                  @Param("request") PayOrderV1Request request,
                                  @Param("sysUserIds") Set<String> sysUserIds);

    /**
     * 分页查询
     *
     * @param page    分页
     * @param request 查询条件
     * @return
     */
    IPage<PayMerchantOrder> reOrder(@Param("page") Page<PayMerchantOrder> page, @Param("request") PayReOrderQueryV1Request request);


    /**
     * 分页用户自身查询
     *
     * @param page    分页
     * @param request 查询条件
     * @return
     */
    IPage<PayMerchantOrder> pageForOwner(@Param("page") Page<PayMerchantOrder> page, @Param("request") PayOrderQueryRequest request);

    /**
     * 查询是否存在未支付订单
     *
     * @param request    查询条件
     * @param newUserId  用户ID
     * @param checkScope 查询范围
     * @return true/false
     */
    boolean existsNoPayOrder(@Param("request") PayOrderRequest request, @Param("newUserId") String newUserId, @Param("scope") String checkScope);

    /**
     * 统计订单数据
     *
     * @return 订单统计数据
     */
    PayOrderStatisticDTO statOrderInfo();
}