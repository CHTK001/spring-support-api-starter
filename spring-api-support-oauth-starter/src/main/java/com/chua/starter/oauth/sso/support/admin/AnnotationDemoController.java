package com.chua.starter.oauth.sso.support.admin;

import com.chua.starter.common.support.api.annotations.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-test")
public class AnnotationDemoController {

    @GetMapping("/version")
    @ApiVersion("1.0.0")
    public String version() { return "v1"; }

    @GetMapping("/gray")
    @ApiGray("test-gray")
    public String gray() { return "gray"; }

    @GetMapping("/internal")
    @ApiInternal
    public String internal() { return "internal"; }

    @GetMapping("/mock")
    @ApiMock
    public String mock() { return "mock"; }

    @GetMapping("/profile")
    @ApiProfile("dev")
    public String profile() { return "profile"; }
}