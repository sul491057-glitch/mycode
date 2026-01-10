package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.entity.Product;

public interface ProductService extends IService<Product> {
    // IService 已经自动包含了 list(), save(), updateById() 等方法
    void updateRecommend(Long id, Boolean isRecommend);
}  // 所以这里暂时不需要写代码，除非有特殊业务逻辑
