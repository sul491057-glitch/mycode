package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.dto.OrderDTO;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.service.OrderService;
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

/**
 * 创建订单的方法
 * 使用@Transactional注解确保在出现异常时进行事务回滚
 * @param dto 订单数据传输对象，包含订单基本信息和订单项信息
 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderDTO dto) {
        // 1. 生成订单号 (使用 UUID 并去掉横杠)
        String orderId = UUID.randomUUID().toString().replace("-", "");

        // 2. 组装主订单信息
        Orders order = new Orders();
        order.setId(orderId);  // 设置订单ID
        order.setTableId(dto.getTableId()); // 如果你的 DTO 里没有 tableId，这行可以删掉
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus("pending"); // 默认状态
        order.setCreateTime(LocalDateTime.now());

        this.baseMapper.insert(order);

        // 3. 组装子订单
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
    }

    @Override
    public List<Orders> getAllOrders() {
        // 1. 先查出所有订单
        List<Orders> ordersList = this.list();

        // 2. 遍历每个订单，查出对应的商品详情 (为了让前端点击"详情"时有数据)
        // 注意：这需要在 Orders 实体类中添加 @TableField(exist = false) private List<OrderItem> orderItems;
        if (ordersList != null) {
            for (Orders order : ordersList) {
                LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(queryWrapper);
                order.setOrderItems(items); // 这里会报红如果你的 Orders 实体类没加这个字段
            }
        }
        return ordersList;
    }

    // 👇 新增：实现更新状态
    @Override
    public void updateStatus(String id, String status) {
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, id)
                .set(Orders::getStatus, status);
        this.update(updateWrapper);
    }
}