package com.xcl.venueserver.vo;

import java.io.Serializable;

/**
 * 统一响应结果VO类
 */
public class ResultVO<T> implements Serializable {

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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 成功
     */
    public static <T> ResultVO<T> success() {
        return success(null);
    }

    /**
     * 成功
     */
    public static <T> ResultVO<T> success(T data) {
        return success(200, "操作成功", data);
    }

    /**
     * 成功带消息
     */
    public static <T> ResultVO<T> success(String message) {
        return success(200, message, null);
    }

    /**
     * 成功带数据和消息
     */
    public static <T> ResultVO<T> success(T data, String message) {
        return success(200, message, data);
    }

    /**
     * 成功
     */
    public static <T> ResultVO<T> success(Integer code, String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败
     */
    public static <T> ResultVO<T> error() {
        return error(500, "操作失败");
    }

    /**
     * 失败
     */
    public static <T> ResultVO<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败
     */
    public static <T> ResultVO<T> error(Integer code, String message) {
        return error(code, message, null);
    }

    /**
     * 失败
     */
    public static <T> ResultVO<T> error(Integer code, String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
} 