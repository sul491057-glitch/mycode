package com.example.restaurant.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.common.Result;
import com.example.restaurant.entity.User;
import com.example.restaurant.service.UserService;
import com.example.restaurant.utils.JwtUtils;
import com.example.restaurant.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 (已集成 Redis 会话管理)
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // 注入 Redis 工具类，用于存取 Token
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 1. 使用 MyBatis Plus 的包装器查找用户 (解决 getOne 报错问题)
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        wrapper.eq(User::getPassword, user.getPassword());

        // 查询数据库
        User dbUser = userService.getOne(wrapper);

        if (dbUser != null) {
            // 2. 生成 JWT 令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", dbUser.getId());
            claims.put("username", dbUser.getUsername());
            String token = JwtUtils.generateToken(claims);

            // 3. 🔥 核心升级：将用户信息存入 Redis (有效期 24 小时)
            // Key 格式: "login:token:你的Token字符串"
            String redisKey = "login:token:" + token;

            // 存入 Redis，这样拦截器就能验证 Token 是否有效了
            redisUtils.set(redisKey, JSON.toJSONString(dbUser), 86400);

            // 4. 返回 Token 给前端
            return Result.success(token);
        }

        return Result.error("用户名或密码错误");
    }

    /**
     * 获取当前用户信息 (从 Redis 缓存取，速度更快)
     */
    @GetMapping("/user/info")
    public Result getUserInfo(@RequestHeader("token") String token) {
        // 直接从 Redis 拿，不查数据库
        String redisKey = "login:token:" + token;
        String userJson = redisUtils.get(redisKey);

        if (userJson != null) {
            return Result.success(JSON.parseObject(userJson, User.class));
        }
        return Result.error("登录已过期");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("token") String token) {
        // 删除 Redis 里的 Token，这就相当于“注销”了
        redisUtils.delete("login:token:" + token);
        return Result.success("退出成功");
    }
}