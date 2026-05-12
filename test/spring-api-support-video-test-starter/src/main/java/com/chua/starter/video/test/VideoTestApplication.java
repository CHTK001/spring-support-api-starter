package com.chua.starter.video.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 视频模块联调测试应用
 *
 * @author CH
 */
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.video.test"
}, excludeName = {
        "com.chua.starter.video.config.VideoConfiguration"
})
public class VideoTestApplication {

    public static void main(String[] args) {
        System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        System.setProperty("mybatis-plus.encryptor.password", "video-test");
        SpringApplication.run(VideoTestApplication.class, args);
    }
}
