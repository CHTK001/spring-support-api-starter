package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.channel.PaymentResult;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.dto.OrderCreateDTO;
import com.chua.payment.support.dto.OrderOperateDTO;
import com.chua.payment.support.dto.OrderPayDTO;
import com.chua.payment.support.dto.RefundApplyDTO;
import com.chua.payment.support.service.PaymentOrderService;
import com.chua.payment.support.vo.OrderStateLogVO;
import com.chua.payment.support.vo.OrderVO;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单管理控制器
 */
@Tag(name = "订单管理", description = "支付订单相关接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final PaymentOrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    @Permission(value = "payment:order:create", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<OrderVO> createOrder(@RequestBody OrderCreateDTO dto) {
        return ReturnResult.success(orderService.createOrder(dto));
    }

    @Operation(summary = "查询订单")
    @GetMapping("/{id}")
    @Permission(value = "payment:order:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<OrderVO> getOrder(@PathVariable Long id) {
        return ReturnResult.success(orderService.getOrder(id));
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/list")
    @Permission(value = "payment:order:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<OrderVO>> listOrders(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Long merchantId,
                                                  @RequestParam(required = false) String orderNo,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String channelType,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate payTimeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate payTimeEnd,
                                                  @RequestParam(required = false) String productId,
                                                  @RequestParam(required = false) String productName) {
        Page<OrderVO> pageResult = orderService.listOrders(page, size, merchantId, orderNo, status, userId, channelType, payTimeStart, payTimeEnd, productId, productName);
        return ReturnResult.success(PageResult.of(pageResult));
    }

    @Operation(summary = "查询商户订单")
    @GetMapping("/merchant/{merchantId}")
    @Permission(value = "payment:order:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<OrderVO>> listMerchantOrders(@PathVariable Long merchantId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String status) {
        return ReturnResult.success(PageResult.of(orderService.listMerchantOrders(merchantId, page, size, status)));
    }

    @Operation(summary = "查询订单状态流转日志")
    @GetMapping("/{id}/logs")
    @Permission(value = "payment:order:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<List<OrderStateLogVO>> listOrderLogs(@PathVariable Long id) {
        return ReturnResult.success(orderService.listOrderLogs(id));
    }

    @Operation(summary = "真实发起支付")
    @PostMapping("/{id}/pay")
    @Permission(value = "payment:order:pay", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<PaymentResult> payOrder(@PathVariable Long id,
                                          @RequestBody(required = false) OrderPayDTO dto) {
        return ReturnResult.success(orderService.payOrder(id, dto));
    }

    @Operation(summary = "同步第三方支付状态")
    @PostMapping("/{id}/sync")
    @Permission(value = "payment:order:sync", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<OrderVO> syncOrder(@PathVariable Long id) {
        return ReturnResult.success(orderService.syncOrder(id));
    }

    @Operation(summary = "发起支付")
    @PutMapping("/{id}/pay-start")
    @Permission(value = "payment:order:pay", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> startPay(@PathVariable Long id,
                                    @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.startPay(id, dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "标记支付成功")
    @PutMapping("/{id}/pay-success")
    @Permission(value = "payment:order:pay", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> paySuccess(@PathVariable Long id,
                                      @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.paySuccess(
                id,
                dto != null ? dto.getPaidAmount() : null,
                dto != null ? dto.getThirdPartyOrderNo() : null,
                dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "标记支付失败")
    @PutMapping("/{id}/pay-fail")
    @Permission(value = "payment:order:pay", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> payFail(@PathVariable Long id,
                                   @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.payFail(id, dto != null ? dto.getRemark() : null, dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    @Permission(value = "payment:order:cancel", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> cancelOrder(@PathVariable Long id,
                                       @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.cancelOrder(id, dto != null ? dto.getOperator() : null, dto != null ? dto.getRemark() : null));
    }

    @Operation(summary = "完成订单")
    @PutMapping("/{id}/complete")
    @Permission(value = "payment:order:complete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> completeOrder(@PathVariable Long id,
                                         @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.completeOrder(id, dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{id}/refund")
    @Permission(value = "payment:order:refund", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> refundOrder(@PathVariable Long id,
                                       @RequestBody RefundApplyDTO dto) {
        return ReturnResult.success(orderService.refundOrder(id, dto));
    }

    @Operation(summary = "标记退款成功")
    @PutMapping("/{id}/refund-success")
    @Permission(value = "payment:order:refund", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> refundSuccess(@PathVariable Long id,
                                         @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.refundSuccess(id, dto != null ? dto.getRefundAmount() : null, dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "标记退款失败")
    @PutMapping("/{id}/refund-fail")
    @Permission(value = "payment:order:refund", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> refundFail(@PathVariable Long id,
                                      @RequestBody(required = false) OrderOperateDTO dto) {
        return ReturnResult.success(orderService.refundFail(id, dto != null ? dto.getRemark() : null, dto != null ? dto.getOperator() : null));
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    @Permission(value = "payment:order:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<OrderVO> updateOrder(@PathVariable Long id,
                                              @RequestBody OrderCreateDTO dto) {
        return ReturnResult.success(orderService.updateOrder(id, dto));
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    @Permission(value = "payment:order:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteOrder(@PathVariable Long id) {
        return ReturnResult.success(orderService.deleteOrder(id));
    }
}
