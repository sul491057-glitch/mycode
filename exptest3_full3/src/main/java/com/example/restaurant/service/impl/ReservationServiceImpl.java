package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.entity.Reservation;
import com.example.restaurant.mapper.ReservationMapper;
import com.example.restaurant.service.ReservationService;
import org.springframework.stereotype.Service;

/**
 * 预约服务实现类
 * 继承了ServiceImpl，实现了ReservationService接口
 * 提供预约相关的业务逻辑实现
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    /**
     * 更新预约状态
     * @param id 预约ID
     * @param status 新的状态值
     */
    @Override
    public void updateStatus(Long id, String status) {
        // 根据ID获取预约信息
        Reservation reservation = this.getById(id);
        // 检查预约是否存在
        if (reservation != null) {
            reservation.setStatus(status);
            this.updateById(reservation);
        }
    }
}