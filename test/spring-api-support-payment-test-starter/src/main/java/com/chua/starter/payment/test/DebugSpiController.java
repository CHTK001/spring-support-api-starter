package com.chua.starter.payment.test;

import com.chua.payment.support.channel.impl.AlipayWebPaymentChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugSpiController {

    private final AlipayWebPaymentChannel alipayWebPaymentChannel;

    @GetMapping("/gateway-check")
    public Map<String, Object> checkGateway() {
        Map<String, Object> result = new HashMap<>();

        // Check AlipayWebPaymentChannel class source
        Class<?> channelClass = alipayWebPaymentChannel.getClass();
        result.put("channelClass", channelClass.getName());
        result.put("channelClassLoader", channelClass.getClassLoader().toString());
        result.put("channelCodeSource", channelClass.getProtectionDomain().getCodeSource().getLocation().toString());

        // Check buildPayRequest method via reflection to see if debug exists
        for (Method m : channelClass.getDeclaredMethods()) {
            if (m.getName().equals("buildPayRequest")) {
                result.put("buildPayRequest_exists", true);
                result.put("buildPayRequest_params", m.getParameterCount());
                // Can't see bytecode via reflection, but we can check annotations
            }
        }

        // Check superclass code source
        Class<?> superClass = channelClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            result.put("superClass", superClass.getName());
            try {
                result.put("superCodeSource", superClass.getProtectionDomain().getCodeSource().getLocation().toString());
            } catch (Exception e) {
                result.put("superCodeSource", "error: " + e.getMessage());
            }
        }

        return result;
    }
}
