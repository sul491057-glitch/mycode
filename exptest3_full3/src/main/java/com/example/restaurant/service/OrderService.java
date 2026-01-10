package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.dto.OrderDTO;
import com.example.restaurant.entity.Orders;
import java.util.List;

public interface OrderService extends IService<Orders> {
    // 定义下单接口
    void createOrder(OrderDTO orderDTO);

    // 定义获取列表接口
    List<Orders> getAllOrders();

    // 👇 新增：定义更新状态接口
    void updateStatus(String id, String status);
}