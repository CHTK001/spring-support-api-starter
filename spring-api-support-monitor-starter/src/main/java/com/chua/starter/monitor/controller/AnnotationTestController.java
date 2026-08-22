package com.chua.starter.monitor.controller;

import com.chua.starter.common.support.api.ApiVersion;
import com.chua.starter.common.support.api.ApiGray;
import com.chua.starter.common.support.api.ApiInternal;
import com.chua.starter.common.support.api.ApiFieldCrypto;
import com.chua.starter.common.support.api.ApiFieldIgnore;
import com.chua.starter.common.support.api.ApiFieldPrivacyEncrypt;
import com.chua.starter.common.support.api.ApiMock;
import com.chua.starter.common.support.api.ApiProfile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotation-test")
public class AnnotationTestController {

    @GetMapping("/version")
    @ApiVersion("1.0.0")
    public String version() {
        return "v1";
    }

    @GetMapping("/gray")
    @ApiGray("test-gray")
    public String gray() {
        return "gray";
    }

    @GetMapping("/internal")
    @ApiInternal
    public String internal() {
        return "internal";
    }

    @GetMapping("/mock")
    @ApiMock
    public String mock() {
        return "mock";
    }

    @GetMapping("/profile")
    @ApiProfile("dev")
    public String profile() {
        return "profile";
    }

    @PostMapping("/crypto")
    @ApiFieldCrypto
    public String crypto(@RequestBody String body) {
        return body;
    }
}