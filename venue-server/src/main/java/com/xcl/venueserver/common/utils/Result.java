package com.xcl.venueserver.common.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功结果（无数据）
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 失败结果
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败结果
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 未授权结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> unauthorized() {
        return fail(401, "未授权");
    }

    /**
     * 无权限结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> forbidden() {
        return fail(403, "权限不足");
    }

    /**
     * 资源不存在结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> notFound() {
        return fail(404, "资源不存在");
    }
    
    /**
     * 成功 (兼容旧代码)
     */
    public static <T> Result<T> success() {
        return ok(null);
    }

    /**
     * 成功 (兼容旧代码)
     */
    public static <T> Result<T> success(T data) {
        return ok(data);
    }

    /**
     * 成功 (兼容旧代码)
     */
    public static <T> Result<T> success(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 成功结果带布尔值和消息 (兼容旧代码)
     */
    public static Result<Boolean> success(boolean success, String message) {
        Result<Boolean> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(success);
        return result;
    }
    
    /**
     * 成功结果带ID和消息 (兼容旧代码)
     */
    public static Result<Long> success(Long id, String message) {
        Result<Long> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(id);
        return result;
    }

    /**
     * 失败 (兼容旧代码)
     */
    public static <T> Result<T> error() {
        return fail("操作失败");
    }

    /**
     * 失败 (兼容旧代码)
     */
    public static <T> Result<T> error(String message) {
        return fail(message);
    }

    /**
     * 失败 (兼容旧代码)
     */
    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }

    /**
     * 失败 (兼容旧代码)
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
} 