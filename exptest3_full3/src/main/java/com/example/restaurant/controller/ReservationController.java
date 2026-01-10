package com.example.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.restaurant.common.Result;
import com.example.restaurant.entity.Reservation;
import com.example.restaurant.service.ReservationService; // 改用 Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * 预订控制器
 * 处理与预订相关的HTTP请求
 */
@RestController
@RequestMapping("/api")
public class ReservationController {

    @Autowired
    private ReservationService reservationService; // 使用 Service 而不是 Mapper

    // 提交预订
    @PostMapping("/reservations")
    public Result<?> createReservation(@RequestBody Reservation reservation) {
        // 设置默认状态，如果前端没传
        if (reservation.getStatus() == null) {
            reservation.setStatus("confirmed"); // 或者 "pending"
        }
        reservation.setCreateTime(LocalDateTime.now());

        // tableId 会由 MyBatis-Plus 自动根据前端传来的 JSON 映射到实体类中
        reservationService.save(reservation);

        return Result.success("预订申请已提交");
    }

    /**
     * 管理员获取预定列表
     * 兼容模式：
     * 1. 传 page, size -> 返回分页 Result<Page<Reservation>>
     * 2. 不传 -> 返回全量 Result<List<Reservation>> (原有逻辑)
     */
    @GetMapping("/reservations/admin")
    public Result<?> getAllReservations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        // 分支 1: 分页查询
        if (page != null && size != null) {
            Page<Reservation> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                // 搜索 姓名 或 电话
                queryWrapper.like(Reservation::getName, keyword)
                        .or()
                        .like(Reservation::getPhone, keyword);
            }

            // 按 ID 倒序 (新预订在前)
            queryWrapper.orderByDesc(Reservation::getId);

            reservationService.page(pageInfo, queryWrapper);
            return Result.success(pageInfo);
        }

        // 分支 2: 原有全量查询
        // 使用 Service 查询所有
        return Result.success(reservationService.list());
    }

    // 👇 新增：管理员更新预订状态 (如：点击完成释放餐桌)
    @PutMapping("/reservations/{id}/status")
    public Result<?> updateReservationStatus(@PathVariable Long id, @RequestParam String status) {
        reservationService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}