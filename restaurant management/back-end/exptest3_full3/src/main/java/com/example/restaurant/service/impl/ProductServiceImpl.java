package com.example.restaurant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.ProductService;
import com.example.restaurant.utils.RedisUtils;
import com.example.restaurant.server.WebSocketServer; // 👈 引入 WebSocket
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private RedisUtils redisUtils;

    private static final String CACHE_KEY = "product:list";

    @Override
    public List<Product> getCachedProductList() {
        String cacheConfig = redisUtils.get(CACHE_KEY);
        if (cacheConfig != null && !cacheConfig.isEmpty()) {
            return JSON.parseArray(cacheConfig, Product.class);
        }

        List<Product> list = this.list();
        if (list != null && !list.isEmpty()) {
            redisUtils.set(CACHE_KEY, JSON.toJSONString(list), 1800);
        }
        return list;
    }

    @Override
    @Transactional
    public void updateRecommend(Long id, Boolean isRecommend) {
        Product product = new Product();
        product.setId(id);
        product.setIsRecommend(isRecommend);
        this.updateById(product);

        redisUtils.delete(CACHE_KEY);

        // 🔥 WebSocket 推送：菜品信息更新（比如推荐状态改变）
        try { WebSocketServer.sendInfo("PRODUCT_UPDATE"); } catch (Exception e) {}
    }

    // --- 重写增删改，加入 WebSocket 通知 ---

    @Override
    public boolean save(Product entity) {
        boolean result = super.save(entity);
        if (result) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 推送
            try { WebSocketServer.sendInfo("PRODUCT_UPDATE"); } catch (Exception e) {}
        }
        return result;
    }

    @Override
    public boolean updateById(Product entity) {
        boolean result = super.updateById(entity);
        if (result) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 推送
            try { WebSocketServer.sendInfo("PRODUCT_UPDATE"); } catch (Exception e) {}
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 推送
            try { WebSocketServer.sendInfo("PRODUCT_UPDATE"); } catch (Exception e) {}
        }
        return result;
    }
}