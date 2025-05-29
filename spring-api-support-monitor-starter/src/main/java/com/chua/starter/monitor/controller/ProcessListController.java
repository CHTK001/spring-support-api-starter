package com.chua.starter.monitor.controller;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 进程服务
 *
 * @author CH
 * @since 2025/5/29 16:31
 */
@RestController
@Slf4j
@Api(tags = "进程服务")
@Tag(name = "进程服务")
@RequestMapping("/v2/process")
@RequiredArgsConstructor
public class ProcessListController {
}
