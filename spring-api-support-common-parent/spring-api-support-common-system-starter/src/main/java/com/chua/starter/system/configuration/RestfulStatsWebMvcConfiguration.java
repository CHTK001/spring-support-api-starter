package com.chua.starter.system.configuration;

import com.chua.starter.system.support.restful.RestfulStatsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * RESTful 实时统计拦截器注册（方案 A 旁路）。
 * <p>
 * 采集全部 HTTP 请求用于监控大盘 RESTful 趋势，排除静态资源与 Actuator 探针噪音。
 * </p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Configuration
public class RestfulStatsWebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RestfulStatsInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/error",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.svg",
                        "/**/*.ico",
                        "/**/*.map",
                        "/v2/server/info/stream",
                        "/v2/server/api/stats"
                );
    }
}
