package com.example.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_user") // <--- 关键：告诉 MP 这个类对应数据库的 sys_user 表
public class User {

    @TableId(type = IdType.AUTO) // <--- 关键：声明这是主键，且是自增的
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String role;
    private String avatar;
    private Date createTime;
}