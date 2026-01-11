package com.example.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.restaurant.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    // 关键点：这里必须继承 BaseMapper<Orders>
    // 之前报错是因为这里没写泛型或者没继承 BaseMapper
}