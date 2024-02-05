package com.chua.starter.sso;


import com.chua.common.support.annotations.Ignore;
import com.chua.starter.monitor.annotation.ConfigValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @ConfigValue(value = "test", autoRefreshed = true)
    private String test;



    @GetMapping("/test")
    @Ignore
    public String test() {
        return this.test;
    }
}
