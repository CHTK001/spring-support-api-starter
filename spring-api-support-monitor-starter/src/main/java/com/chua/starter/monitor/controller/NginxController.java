package com.chua.starter.monitor.controller;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * nginx接口
 * @author CH
 * @since 2024/12/28
 */
@RestController
@Slf4j
@Api(tags = "nginx接口")
@Tag(name = "nginx接口")
@RequestMapping("/v2/nginx")
@RequiredArgsConstructor
public class NginxController {
}
