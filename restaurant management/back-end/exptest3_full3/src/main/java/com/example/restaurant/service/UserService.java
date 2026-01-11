package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.entity.User;

/**
 * 继承 IService<User> 之后，
 * 你就自动拥有了 getOne, save, update, list 等几十个方法
 */
public interface UserService extends IService<User> {
    // 这里暂时留空即可，不需要自己写 login 方法
}