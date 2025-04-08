package com.chua.starter.pay.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.pay.support.entity.PayMerchantOrderWater;
import com.chua.starter.pay.support.pojo.WaterQueryV1Request;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Mapper
public interface PayMerchantOrderWaterMapper extends BaseMapper<PayMerchantOrderWater> {

    /**
     * 根据分页信息、查询请求、用户ID集合、开始日期和结束日期查询支付商户订单流水信息。
     *
     * @param page      分页对象，包含分页信息
     * @param request   查询请求对象，包含查询条件
     * @param userIds   用户ID集合，用于过滤特定用户的订单流水
     * @param startDate 查询的开始日期
     * @param endDate   查询的结束日期
     * @return 返回分页后的支付商户订单流水信息
     */
    IPage<PayMerchantOrderWater> water(
            @Param("page") Page<PayMerchantOrderWater> page,
            @Param("request") WaterQueryV1Request request,
            @Param("userIds") Set<String> userIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据支付商户订单编号查询管理员相关的支付商户订单流水信息。
     *
     * @param payMerchantOrderCode 支付商户订单编号
     * @return 返回符合条件的支付商户订单流水信息列表
     */
    List<PayMerchantOrderWater> waterForAdmin(@Param("payMerchantOrderCode") String payMerchantOrderCode);

    /**
     * 根据支付商户订单编号和部门ID查询部门相关的支付商户订单流水信息。
     *
     * @param payMerchantOrderCode 支付商户订单编号
     * @param deptId               部门ID
     * @return 返回符合条件的支付商户订单流水信息列表
     */
    List<PayMerchantOrderWater> waterForDept(@Param("payMerchantOrderCode") String payMerchantOrderCode, @Param("deptId") String deptId);

    /**
     * 根据支付商户订单编号、部门ID和用户ID查询部门组织者相关的支付商户订单流水信息。
     *
     * @param payMerchantOrderCode 支付商户订单编号
     * @param deptId               部门ID
     * @param userId               用户ID
     * @return 返回符合条件的支付商户订单流水信息列表
     */
    List<PayMerchantOrderWater> waterForDeptOrganizer(@Param("payMerchantOrderCode") String payMerchantOrderCode, @Param("deptId") String deptId, @Param("userId") String userId);

    /**
     * 根据支付商户订单编号和用户ID查询所有者相关的支付商户订单流水信息。
     *
     * @param payMerchantOrderCode 支付商户订单编号
     * @param userId               用户ID
     * @return 返回符合条件的支付商户订单流水信息列表
     */
    List<PayMerchantOrderWater> waterForOwner(@Param("payMerchantOrderCode") String payMerchantOrderCode, @Param("userId") String userId);
}
