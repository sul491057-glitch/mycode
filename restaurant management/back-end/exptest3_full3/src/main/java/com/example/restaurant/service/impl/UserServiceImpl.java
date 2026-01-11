package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // 👈 必须导入这个
import com.example.restaurant.entity.User;
import com.example.restaurant.mapper.UserMapper;
import com.example.restaurant.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 修复核心：
 * 必须 extends ServiceImpl<UserMapper, User>
 * 这样你就自动拥有了 MyBatis Plus 的所有能力 (getOne, save, list...)
 * 同时也满足了 UserService 接口的要求
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 这里不需要再手动写 login 方法了
    // 因为我们会在 Controller 里直接用 MyBatis Plus 提供的 getOne 方法
    // 同时也自动解决了 "必须实现抽象方法 saveBatch" 的报错
}