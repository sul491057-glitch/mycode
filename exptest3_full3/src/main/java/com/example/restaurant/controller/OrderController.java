package com.example.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.restaurant.common.Result;
import com.example.restaurant.dto.OrderDTO;
import com.example.restaurant.entity.Orders; // 🔴 请确认您的实体类名是 Orders 还是 Order
import com.example.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * 提供订单相关的RESTful API接口
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    /**
     * 自动注入订单服务
     * 用于处理订单相关的业务逻辑
     */
    @Autowired
    private OrderService orderService;

    // 提交订单
    @PostMapping("/orders")
    public Result<?> createOrder(@RequestBody OrderDTO orderDTO) {
        orderService.createOrder(orderDTO);
        return Result.success("下单成功");
    }

    /**
     * 获取订单列表 (后台查看)
     * 兼容模式：
     * 1. 如果传了 page 和 size -> 返回 Page<Orders> (分页数据)
     * 2. 如果没传 -> 返回 List<Orders> (原有逻辑，所有数据)
     */
    @GetMapping("/orders")
    public Result<?> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1：如果有分页参数，走分页查询 (新功能)
        if (page != null && size != null) {
            Page<Orders> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                // 尝试搜 ID 或 总金额
                // 注意：如果您的 ID 是 String 类型，直接 eq；如果是 Long，需 try-catch 解析
                // 这里假设 ID 是 String (根据您 updateOrderStatus 的 id 类型推断)
                queryWrapper.eq(Orders::getId, keyword)
                        .or()
                        .like(Orders::getTotalAmount, keyword);
            }

            // 假设按创建时间倒序 (如果实体没这个字段，请改为 getId)
            // queryWrapper.orderByDesc(Orders::getCreateTime);
            queryWrapper.orderByDesc(Orders::getId);

            orderService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2：如果没有分页参数，执行原有逻辑 (旧功能)
        return Result.success(orderService.getAllOrders());
    }

    // 新增：修改订单状态 (如：从 pending 改为 已完成)
    @PutMapping("/orders/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable String id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}