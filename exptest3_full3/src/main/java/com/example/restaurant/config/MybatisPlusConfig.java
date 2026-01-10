package com.example.restaurant.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置类
 * 用于配置MybatisPlus的相关插件和设置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     * @return MybatisPlusInterceptor 分页拦截器
     * 该方法配置并返回一个MybatisPlus拦截器，用于实现分页功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型为 MySQL
        // 添加MySQL数据库的分页插件，确保分页功能在MySQL数据库上正常工作
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}