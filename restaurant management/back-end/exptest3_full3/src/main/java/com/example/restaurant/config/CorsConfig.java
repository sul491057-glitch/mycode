package com.example.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS跨域配置类
 * 用于配置Web应用的跨域资源共享(CORS)策略
 * 实现WebMvcConfigurer接口，重写addCorsMappings方法
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * 配置跨域映射规则
     * @param registry CORS注册对象，用于添加跨域映射配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")      // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源 (Vue项目地址)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowCredentials(true)     // 允许携带 Cookie (用于Session登录)
                .maxAge(3600);              // 预检请求缓存时间
    }
}