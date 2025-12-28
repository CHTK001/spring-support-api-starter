package com.chua.starter.monitor.controller.acme;

import com.chua.starter.monitor.service.acme.AcmeChallengeService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * ACME HTTP-01 验证响应控制器
 * 用于本机部署模式，响应 ACME 服务器的验证请求
 *
 * @author CH
 * @since 2025-12-28
 */
@Slf4j
@RestController
@Api(tags = "ACME验证响应")
@Tag(name = "ACME验证响应")
@RequiredArgsConstructor
public class AcmeChallengeController {

    private final AcmeChallengeService challengeService;

    /**
     * 响应 ACME HTTP-01 验证请求
     * ACME 服务器会访问 http://域名/.well-known/acme-challenge/{token}
     *
     * @param token 验证 token
     * @return keyAuthorization 内容
     */
    @GetMapping(value = "/.well-known/acme-challenge/{token}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "响应ACME HTTP-01验证请求")
    public ResponseEntity<String> challenge(@PathVariable String token) {
        log.info("[ACME-CHALLENGE] 收到验证请求: token={}", token);
        String keyAuthorization = challengeService.getKeyAuthorization(token);
        if (keyAuthorization == null) {
            log.warn("[ACME-CHALLENGE] 验证token不存在: {}", token);
            return ResponseEntity.notFound().build();
        }
        log.info("[ACME-CHALLENGE] 返回验证内容: token={}", token);
        return ResponseEntity.ok(keyAuthorization);
    }
}
