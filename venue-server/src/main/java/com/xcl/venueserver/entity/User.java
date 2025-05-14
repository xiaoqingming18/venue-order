package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 密码加密盐值
     */
    private String salt;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 性别
     */
    private String gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 地址
     */
    private String address;

    /**
     * 用户状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 用户角色：1-普通用户，2-管理员
     */
    private Integer role;

    /**
     * 连续登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 