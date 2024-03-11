package com.chua.starter.sso;


import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.json.Json;
import com.chua.starter.common.support.annotations.Job;
import com.chua.starter.monitor.annotation.ConfigValue;
import com.chua.starter.sso.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {

    @ConfigValue(value = "test", autoRefreshed = true)
    private String test;

    @Resource
    private SysUserMapper sysUserMapper;


    @Job("test")
    public void testJob(String job) {
        System.out.println("test job");
    }
    @GetMapping("/test")
    @Ignore
    public String test() {
        return this.test + " => \r\n" + Json.toPrettyFormat(sysUserMapper.test());
    }
}
