package com.example.restaurant.interceptor;

import com.alibaba.fastjson2.JSON;
import com.example.restaurant.entity.User;
import com.example.restaurant.utils.JwtUtils;
import com.example.restaurant.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 升级版登录拦截器
 * 集成 Redis 验证，支持“强退”和“在线状态检测”
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 预检请求 (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取令牌
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 3. 🔥 核心升级：先去 Redis 查 Token 是否有效
        // Key 的格式约定为 "login:token:{token字符串}"
        String redisKey = "login:token:" + token;

        // 如果 Redis 里找不到这个 Token，说明：
        // a. Token 过期了
        // b. 用户点击了注销
        // c. 管理员把用户踢下线了
        if (!redisUtils.hasKey(redisKey)) {
            response.setStatus(401);
            return false;
        }

        // 4. (可选) 如果需要用户信息，可以从 Redis 取出来放到 request 里供 Controller 使用
        // String userInfo = redisUtils.get(redisKey);
        // request.setAttribute("currentUser", JSON.parseObject(userInfo, User.class));

        // 5. 依然可以保留 JWT 格式校验作为双重保险
        try {
            JwtUtils.parseToken(token);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}