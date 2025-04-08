package com.chua.starter.pay.support.controller;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.common.support.annotations.ApiIpLimit;
import com.chua.starter.pay.support.pojo.PayOrderCreateRequest;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.service.PayQrCodeService;
import com.chua.starter.pay.support.service.PayService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 支付测试
 *
 * @author CH
 */
@Api(tags = "支付二维码接口")
@Tag(name = "支付二维码接口")
@RestController
@RequestMapping("/v3/pay/merchant")
@Slf4j
@RequiredArgsConstructor
public class PayQrCodeController {

    final PayQrCodeService payQrCodeService;
    final PayService payService;

    /**
     * 创建订单
     */
    @PutMapping("test/createOrder")
    @Operation(summary = "创建订单")
    @ApiIpLimit
    public ReturnResult<PayOrderResponse> createOrder(@Validated(AddGroup.class) @RequestBody PayOrderCreateRequest request,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        PayOrderRequest payOrderRequest = BeanUtils.copyProperties(request, PayOrderRequest.class);
        payOrderRequest.setUserId(StringUtils.defaultString(request.getOpenId(), null));
        return payService.createOrder(payOrderRequest);
    }

    /**
     * 生成二维码
     *
     * @return ResponseEntity
     */
    @PostMapping("/test/qrcode")
    public ResponseEntity<byte[]> qrcode(@ParameterObject @RequestBody PayQrCodeRequest request) {
        return payQrCodeService.qrcode(request);
    }
}
