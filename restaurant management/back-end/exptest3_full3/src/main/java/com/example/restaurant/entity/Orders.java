package com.example.restaurant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Orders {
    @TableId
    private String id;
    private String tableId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<OrderItem> orderItems;
}