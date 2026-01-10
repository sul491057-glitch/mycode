package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.entity.Reservation;

public interface ReservationService extends IService<Reservation> {
    // 新增：更新预订状态
    void updateStatus(Long id, String status);
}