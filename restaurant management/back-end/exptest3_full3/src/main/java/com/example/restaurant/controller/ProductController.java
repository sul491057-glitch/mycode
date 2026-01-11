package com.example.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.restaurant.common.Result;
import com.example.restaurant.entity.Product;
import com.example.restaurant.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 * 已集成 Redis 缓存功能
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     * 策略：
     * 1. 如果是分页或搜索 -> 走数据库查询 (因为条件多变，不适合缓存全量 List)
     * 2. 如果是获取全部 -> 走 Redis 缓存 (调用 getCachedProductList)
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1: 分页查询 或 带关键词搜索 -> 直接查数据库
        if (page != null && size != null) {
            Page<Product> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                // 搜索 菜名 或 分类
                queryWrapper.like(Product::getName, keyword)
                        .or()
                        .like(Product::getCategory, keyword);
            }

            // 按 ID 倒序
            queryWrapper.orderByDesc(Product::getId);

            productService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2: 获取全量列表 -> 🚀 改动点：这里改为调用带缓存的方法
        // 原来是: productService.list()
        // 现在是:
        List<Product> list = productService.getCachedProductList();
        return Result.success(list);
    }

    /**
     * 添加商品
     * ServiceImpl 内部会自动清除缓存
     */
    @PostMapping
    public Result<?> add(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    /**
     * 更新商品信息
     * ServiceImpl 内部会自动清除缓存
     */
    @PutMapping
    public Result<?> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    /**
     * 删除商品
     * ServiceImpl 内部会自动清除缓存
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 推荐/取消推荐
     * ServiceImpl 内部会自动清除缓存
     */
    @PostMapping("/recommend")
    public Result<?> toggleRecommend(@RequestBody Map<String, Object> params) {
        try {
            Object idObj = params.get("id");
            if (idObj == null) return Result.error("ID不能为空");
            Long id = Long.valueOf(idObj.toString());

            // 兼容前端参数
            Object recObj = params.get("isRecommended");
            if (recObj == null) recObj = params.get("isRecommend");

            if (recObj == null) return Result.error("状态参数不能为空");

            // 处理 Boolean 类型转换
            Boolean isRec;
            if (recObj instanceof Boolean) {
                isRec = (Boolean) recObj;
            } else if (recObj instanceof Integer) {
                isRec = ((Integer) recObj) == 1;
            } else {
                isRec = Boolean.valueOf(recObj.toString());
            }

            productService.updateRecommend(id, isRec);
            return Result.success("操作成功");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("错误：" + e.getMessage());
        }
    }
}