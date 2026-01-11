package com.example.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("products") // 对应数据库表名
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private BigDecimal price;
    private String description;
    private String category;

    private String imageUrl;


    @TableField("is_recommended")
    private Boolean isRecommend;

}