package com.example.restaurant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.entity.Reservation;
import com.example.restaurant.mapper.ReservationMapper;
import com.example.restaurant.service.ReservationService;
import com.example.restaurant.utils.RedisUtils;
import com.example.restaurant.server.WebSocketServer; // 👈 引入 WebSocket
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private RedisUtils redisUtils;

    private static final String CACHE_KEY = "reservation:list";

    @Override
    public List<Reservation> getCachedReservationList() {
        String json = redisUtils.get(CACHE_KEY);
        if (json != null && !json.isEmpty()) {
            return JSON.parseArray(json, Reservation.class);
        }

        List<Reservation> list = this.list();
        if (list != null && !list.isEmpty()) {
            redisUtils.set(CACHE_KEY, JSON.toJSONString(list), 600);
        }
        return list;
    }

    // --- 重写增删改，加入 WebSocket 通知 ---

    @Override
    public boolean save(Reservation entity) {
        boolean r = super.save(entity);
        if (r) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 WebSocket 推送：有新预约
            try { WebSocketServer.sendInfo("RESERVATION_UPDATE"); } catch (Exception e) {}
        }
        return r;
    }

    @Override
    public boolean updateById(Reservation entity) {
        boolean r = super.updateById(entity);
        if (r) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 WebSocket 推送：预约状态变更
            try { WebSocketServer.sendInfo("RESERVATION_UPDATE"); } catch (Exception e) {}
        }
        return r;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean r = super.removeById(id);
        if (r) {
            redisUtils.delete(CACHE_KEY);
            // 🔥 WebSocket 推送
            try { WebSocketServer.sendInfo("RESERVATION_UPDATE"); } catch (Exception e) {}
        }
        return r;
    }
}