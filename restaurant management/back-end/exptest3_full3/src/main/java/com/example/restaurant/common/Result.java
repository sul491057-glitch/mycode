package com.example.restaurant.common;

import lombok.Data;

/**
 * 泛型结果类，用于封装API返回结果
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {
    private Integer code; // 状态码: 200成功, 500失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    /**
     * 返回操作成功的结果
     * @param <T> 返回数据的类型
     * @return 操作成功的Result对象
     */
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "操作成功";
        return r;
    }

    /**
     * 返回操作成功的结果，并携带数据
     * @param data 返回的数据
     * @param <T> 返回数据的类型
     * @return 操作成功的Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    /**
     * 返回操作失败的结果
     * @param msg 失败提示信息
     * @param <T> 返回数据的类型
     * @return 操作失败的Result对象
     */
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
}