package com.example.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig 类
 * 用于配置Spring MVC的资源处理器
 * 实现WebMvcConfigurer接口，自定义MVC的配置
 */
@Configuration  // 标识这是一个配置类
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 重写addResourceHandlers方法
     * 用于添加资源处理器，配置静态资源的访问路径
     * @param registry 资源处理器注册表，用于注册资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目根目录 (和 CommonController 里保持一致)
        String projectPath = System.getProperty("user.dir");

        // 构建本地绝对路径 file:///f:/uodate/src/main/resources/static/images/
        // 注意：Windows 下 file: 协议需要正确处理路径
        String uploadDir = "file:" + projectPath + "/src/main/resources/static/images/";

        // 映射规则：当访问 /images/** 时，去 uploadDir 找文件
        registry.addResourceHandler("/images/**")  // 设置访问URL路径
                .addResourceLocations(uploadDir);  // 设置资源实际存储位置
    }
}