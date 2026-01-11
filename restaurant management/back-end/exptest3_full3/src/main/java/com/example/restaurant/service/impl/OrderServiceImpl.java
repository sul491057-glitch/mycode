package com.example.restaurant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.dto.OrderDTO;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.service.OrderService;
import com.example.restaurant.utils.RedisUtils;
import com.example.restaurant.server.WebSocketServer; // 👈 引入 WebSocket
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String CACHE_KEY = "order:list";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderDTO dto) {
        // 1. 生成订单号
        String orderId = UUID.randomUUID().toString().replace("-", "");

        // 2. 写入主表
        Orders order = new Orders();
        order.setId(orderId);
        order.setTableId(dto.getTableId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(order);

        // 3. 写入子表
        if (dto.getItems() != null) {
            for (OrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(orderId);
                item.setProductId(itemDTO.getId());
                item.setProductName(itemDTO.getName());
                item.setPrice(itemDTO.getPrice());
                item.setQuantity(itemDTO.getQuantity());
                orderItemMapper.insert(item);
            }
        }

        // 4. 清除 Redis 缓存
        redisUtils.delete(CACHE_KEY);

        // 5. 🔥 WebSocket 推送：通知管理员有新订单
        try {
            WebSocketServer.sendInfo("NEW_ORDER");
        } catch (Exception e) {
            System.err.println("WebSocket 推送失败: " + e.getMessage());
        }
    }

    @Override
    public List<Orders> getCachedOrderList() {
        String json = redisUtils.get(CACHE_KEY);
        if (json != null && !json.isEmpty()) {
            return JSON.parseArray(json, Orders.class);
        }

        List<Orders> list = this.getAllOrders();
        if (list != null && !list.isEmpty()) {
            redisUtils.set(CACHE_KEY, JSON.toJSONString(list), 600);
        }
        return list;
    }

    @Override
    public List<Orders> getAllOrders() {
        List<Orders> ordersList = this.list();
        if (ordersList != null) {
            for (Orders order : ordersList) {
                LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(OrderItem::getOrderId, order.getId());
                order.setOrderItems(orderItemMapper.selectList(queryWrapper));
            }
        }
        return ordersList;
    }

    @Override
    public void updateStatus(String id, String status) {
        this.update(new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, id)
                .set(Orders::getStatus, status));

        // 删除缓存
        redisUtils.delete(CACHE_KEY);

        // 🔥 WebSocket 推送：通知所有客户端订单状态已变更
        try {
            WebSocketServer.sendInfo("ORDER_UPDATE");
        } catch (Exception e) {
            // 忽略推送错误
        }
    }
}