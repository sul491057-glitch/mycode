package com.example.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.restaurant.common.Result;
import com.example.restaurant.entity.Reservation;
import com.example.restaurant.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * 预订控制器
 * 集成 Redis 缓存
 */
@RestController
@RequestMapping("/api")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // 提交预订
    @PostMapping("/reservations")
    public Result<?> createReservation(@RequestBody Reservation reservation) {
        // 设置默认状态
        if (reservation.getStatus() == null) {
            reservation.setStatus("confirmed");
        }
        reservation.setCreateTime(LocalDateTime.now());

        reservationService.save(reservation);
        return Result.success("预订申请已提交");
    }

    /**
     * 管理员获取预定列表 (带缓存)
     */
    @GetMapping("/reservations/admin")
    public Result<?> getAllReservations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1: 分页查询 (走数据库)
        if (page != null && size != null) {
            Page<Reservation> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                // 搜索 姓名 或 电话
                queryWrapper.like(Reservation::getName, keyword)
                        .or()
                        .like(Reservation::getPhone, keyword);
            }

            // 按 ID 倒序
            queryWrapper.orderByDesc(Reservation::getId);

            reservationService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2: 全量查询 (🔥 核心修改：走 Redis 缓存)
        // 原来是: reservationService.list()
        // 现在改用:
        return Result.success(reservationService.getCachedReservationList());
    }

    /**
     * 更新预订状态
     */
    @PutMapping("/reservations/{id}/status")
    public Result<?> updateReservationStatus(@PathVariable Long id, @RequestParam String status) {
        // 🔥 技巧：直接利用 updateById，因为我们在 ServiceImpl 里重写了 updateById 来删除缓存
        // 这样就不需要在 Service 接口里专门定义一个 updateStatus 方法了
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setStatus(status);

        reservationService.updateById(reservation);

        return Result.success("状态更新成功");
    }
}