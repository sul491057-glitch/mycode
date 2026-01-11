package com.example.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reservations")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String phone;
    private LocalDateTime reserveTime;
    private Integer peopleCount;
    private String note;
    private LocalDateTime createTime;

    // --- 新增字段 ---
    private String tableId; // 餐桌号，如 A01
    private String status;  // 状态：pending, confirmed, completed
}