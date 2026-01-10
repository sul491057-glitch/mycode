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
 * 提供商品的增删改查、推荐等功能接口
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * 商品服务接口
     * 用于处理商品相关的业务逻辑
     */
    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     * 兼容模式：
     * 1. 传 page, size -> 返回分页 Result<Page<Product>>
     * 2. 不传 -> 返回全量 Result<List<Product>> (原有逻辑)
     * @param page 页码（可选）
     * @param size 每页大小（可选）
     * @param keyword 搜索关键词（可选）
     * @return 返回商品列表结果
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1: 分页查询
        if (page != null && size != null) {
            Page<Product> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                // 搜索 菜名 或 分类
                queryWrapper.like(Product::getName, keyword)
                        .or()
                        .like(Product::getCategory, keyword);
            }

            // 假设按 ID 倒序
            queryWrapper.orderByDesc(Product::getId);

            productService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2: 原有全量查询
        return Result.success(productService.list());
    }

    /**
     * 添加商品
     * @param product 商品信息
     * @return 返回操作结果
     */
    @PostMapping
    public Result<?> add(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 返回操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }


    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }


    @PostMapping("/recommend")
    public Result<?> toggleRecommend(@RequestBody Map<String, Object> params) {
        try {
            Object idObj = params.get("id");
            if (idObj == null) return Result.error("ID不能为空");
            Long id = Long.valueOf(idObj.toString());

            // 兼容前端可能传 isRecommend 或 isRecommended
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