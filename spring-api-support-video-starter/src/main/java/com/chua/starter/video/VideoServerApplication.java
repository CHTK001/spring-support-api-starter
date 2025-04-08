package com.chua.starter.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 视频服务
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {"com.chua.starter.video", "com.chua.starter.service"})
public class VideoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoServerApplication.class);
    }
}