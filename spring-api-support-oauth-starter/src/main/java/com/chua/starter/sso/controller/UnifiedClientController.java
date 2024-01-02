package com.chua.starter.sso.controller;

import com.alibaba.fastjson2.JSON;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.sso.impl.SysUserMapper;
import com.chua.starter.unified.client.support.annotation.ConfigValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * @author CH
 */
@RestController
public class UnifiedClientController {


    @ConfigValue(value = "${test:}", autoRefreshed = true)
    private String test;

    @Resource
    private SysUserMapper sysUserMapper;

    private FileInputStream fileInputStream;

    private String name = "233455";
    @GetMapping("test")
    public String getTest() {
        return test;
    }

    @GetMapping("test5")
    public String getTest3() {
        return test;
    }
    @GetMapping("openHandler")
    public void openHandler() {
        if(null == fileInputStream) {
            try {
                fileInputStream = new FileInputStream("Z://1.txt");
            } catch (IOException ignored) {
            }
        }
    }
    @GetMapping("closeHandler")
    public void closeHandler() {
        if(null != fileInputStream) {
            IoUtils.closeQuietly(fileInputStream);
            fileInputStream = null;
        }
    }

    @GetMapping("mybatis")
    public String mybatis() {
        return JSON.toJSONString(sysUserMapper.test());
    }
    @GetMapping("except")
    public String except() {
        if(new SecureRandom().nextBoolean()) {
            throw new RuntimeException("测试错误");
        }

        return mybatis();
    }
}
