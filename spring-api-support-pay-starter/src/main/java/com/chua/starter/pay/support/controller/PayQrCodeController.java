package com.chua.starter.pay.support.controller;

import com.chua.starter.pay.support.pojo.PayQrCodeRequest;
import com.chua.starter.pay.support.service.PayQrCodeService;
import com.chua.starter.pay.support.service.PayService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 生成二维码
     *
     * @return ResponseEntity
     */
    @PostMapping("/test/qrcode")
    public ResponseEntity<byte[]> qrcode(@ParameterObject @RequestBody PayQrCodeRequest request) {
        return payQrCodeService.qrcode(request);
    }
}
