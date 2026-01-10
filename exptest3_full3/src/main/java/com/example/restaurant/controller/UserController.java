package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import java.util.Map; // 如果 Result 类在其他包，保留这个；如果没有用到 Map 可以删掉，但你下面用到了 Map，所以必须保留 java.util.Map
import java.util.Map;

/**
 * 用户控制器类
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api") // 这里的路径 /api + 下面的 /login = /api/login
public class UserController {

    /**
     * 处理用户登录请求
     * @param loginData 包含用户名和密码的Map对象
     * @return 返回登录结果，成功或失败
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginData) {
        // 从请求中获取用户名和密码
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 🔴 这是一个硬编码测试，前端输入 admin / 123456 即可登录
        if ("admin".equals(username) && "123456".equals(password)) {
            return Result.success("登录成功");
        }

        // 登录失败返回错误信息
        return Result.error("账号或密码错误");
    }
}