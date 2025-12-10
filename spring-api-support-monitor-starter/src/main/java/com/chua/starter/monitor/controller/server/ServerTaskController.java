package com.chua.starter.monitor.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器任务管理控制器
 * 提供任务管理相关的API接口
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/server/task")
@Tag(name = "服务器任务管理")
@RequiredArgsConstructor
public class ServerTaskController {


}
