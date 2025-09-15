package com.chua.starter.video.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author CH
 * @since 2025/9/13 21:29
 */
@ComponentScan("com.chua.starter.video")
@MapperScan("com.chua.starter.video.mapper")
public class VideoConfiguration {
}
