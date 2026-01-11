package com.example.restaurant.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.restaurant.entity.Product;
import com.example.restaurant.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {}