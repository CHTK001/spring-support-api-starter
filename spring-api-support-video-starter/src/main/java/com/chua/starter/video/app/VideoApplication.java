package com.chua.starter.video.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import com.chua.starter.job.support.JobAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 视频平台启动类。
 *
 * <p>装配 {@code spring-api-support-common-video-starter}（视频检索 + 直播 + 配置数据库）、
 * 通用配置服务与 job-starter（配置同步任务），默认 H2 自包含运行；
 * 面向前端 {@code vue-support-video-starter}。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(JobAutoConfiguration.MAPPER_PACKAGE)
@OpenAPIDefinition(info = @Info(title = "Video Platform API", version = "1.0.0",
        description = "视频资源检索与电视直播平台"))
public class VideoApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VideoApplication.class, args);
    }
}
