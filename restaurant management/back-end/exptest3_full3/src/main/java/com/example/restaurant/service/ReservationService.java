package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.entity.Reservation;
import java.util.List;

public interface ReservationService extends IService<Reservation> {
    // 获取带缓存的预约列表
    List<Reservation> getCachedReservationList();
}