package com.example.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.restaurant.common.Result;
import com.example.restaurant.dto.OrderDTO;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * 集成 Redis 缓存
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 提交订单
    @PostMapping("/orders")
    public Result<?> createOrder(@RequestBody OrderDTO orderDTO) {
        orderService.createOrder(orderDTO);
        return Result.success("下单成功");
    }

    /**
     * 获取订单列表
     * 策略：
     * 1. 分页/搜索 -> 走数据库 (因为条件多变，不适合缓存)
     * 2. 全量列表 -> 走 Redis 缓存 (调用 getCachedOrderList)
     */
    @GetMapping("/orders")
    public Result<?> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1：如果有分页参数，走分页查询
        if (page != null && size != null) {
            Page<Orders> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.eq(Orders::getId, keyword)
                        .or()
                        .like(Orders::getTotalAmount, keyword);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(Orders::getCreateTime);

            orderService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2：如果没有分页参数，获取全量数据 (🔥 核心修改：走 Redis 缓存)
        // 原来是: orderService.getAllOrders()
        // 现在改用:
        return Result.success(orderService.getCachedOrderList());
    }

    // 修改订单状态
    @PutMapping("/orders/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable String id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}