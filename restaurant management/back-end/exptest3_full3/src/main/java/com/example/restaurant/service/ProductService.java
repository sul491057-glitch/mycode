package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.entity.Product;
import java.util.List;

public interface ProductService extends IService<Product> {

    // 1. 自定义一个带缓存的查询列表方法
    List<Product> getCachedProductList();

    // 2. 原有的推荐修改方法
    void updateRecommend(Long id, Boolean isRecommend);
}