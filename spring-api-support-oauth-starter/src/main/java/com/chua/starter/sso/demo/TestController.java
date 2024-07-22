package com.chua.starter.sso.demo;


import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.json.Json;
import com.chua.starter.common.support.annotations.Job;
import com.chua.starter.monitor.annotation.ConfigValue;
import com.chua.starter.sso.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestController {

    @ConfigValue(value = "${test:}", autoRefreshed = true)
    private String test;

    final SysUserMapper sysUserMapper;


    @Job("test")
    public void testJob(String job) {
        log.info("test job");
    }
    @GetMapping("/test")
    @Ignore
    public String test() {
        return this.test + " => \r\n" + Json.toPrettyFormat(sysUserMapper.test());
    }
}
