package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public void updateRecommend(Long id, Boolean isRecommend) {
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, id)
                // 🔴 修正：这里用 getIsRecommend (没ed)
                .set(Product::getIsRecommend, isRecommend);

        this.update(updateWrapper);
    }
}