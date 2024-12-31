package com.chua.starter.pay.support.controller;

import com.chua.common.support.annotations.Ignore;
import com.chua.starter.pay.support.pojo.WechatOrderCallbackRequest;
import com.chua.starter.pay.support.pojo.WechatOrderCallbackResponse;
import com.chua.starter.pay.support.service.PayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付回调
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "支付回调")
@Tag(name ="支付回调")
@RestController
@RequestMapping("/v2/pay/callback/wechat/order")
@Slf4j
@Ignore
@RequiredArgsConstructor
public class WechatPayOrderCallbackController {


    final PayOrderService payOrderService;

    @PostMapping(value = "/notify", produces = MediaType.APPLICATION_XML_VALUE)
    @ApiOperation("订单结果通知")
    @Operation(summary = "订单结果通知")
    @Ignore
    public WechatOrderCallbackResponse notifyOrder(@RequestBody WechatOrderCallbackRequest wechatOrderCallbackRequest) {
        return payOrderService.notifyOrder(wechatOrderCallbackRequest, "wechat_" + wechatOrderCallbackRequest.getTradeType());
    }

}
