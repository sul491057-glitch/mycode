package com.example.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * OrderDTO 类，用于封装订单相关的数据
 * 使用 Lombok 的 @Data 注解自动生成 getter、setter、toString 等方法
 */
@Data
public class OrderDTO {
    private String tableId;      // 餐桌ID，用于标识订单所属的餐桌
    private BigDecimal totalAmount; // 订单总金额，使用 BigDecimal 保证金额计算的精确性
    // 这里接收前端的 items 数组
    private List<OrderItemDTO> items; // 订单项列表，包含订单中所有菜品的信息

    /**
     * OrderItemDTO 内部类，用于封装订单项（菜品）的相关数据
     * 同样使用 Lombok 的 @Data 注解自动生成常用方法
     */
    @Data
    public static class OrderItemDTO {
        private Long id; // 前端传过来的菜品ID，用于标识具体菜品
        private String name; // 菜品名称
        private BigDecimal price; // 菜品单价，使用 BigDecimal 保证金额精确性
        private Integer quantity; // 数量，表示该菜品的点单数量
    }
}

