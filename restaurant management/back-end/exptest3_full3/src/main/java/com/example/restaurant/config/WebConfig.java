package com.example.restaurant.config;

import com.example.restaurant.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired; // 👈 必须导入这个
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 🔥 核心修复：请求 Spring 注入已经管理好的拦截器实例
    @Autowired
    private LoginInterceptor loginInterceptor;



//关于顾客和管理员关于token处理
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                    // 1. 登录接口 (必须改成 /api 开头！)
                    "/api/login",           // 对应 UserController 的 @PostMapping("/login")
                    "/api/user/guestLogin", // 如果你有免密登录，且路径也是 /api 下的

                    // 2. 静态资源
                    "/images/**",
                    "/static/**",

                    // 3. 菜品浏览 (之前修好的)
                    "/api/products",
                    "/api/products/**",

                    // 4. 下单与预定 (如果有)
                    "/api/orders/**",
                    "/api/reservations/**"
            );
}

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}