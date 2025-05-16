package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 用户查询条件DTO
 */
@Data
public class UserQueryDTO extends PageDTO {

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 昵称（模糊查询）
     */
    private String nickname;

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 邮箱（模糊查询）
     */
    private String email;

    /**
     * 用户状态：0-禁用，1-正常，null-全部
     */
    private Integer status;

    /**
     * 用户角色：1-普通用户，2-管理员，null-全部
     */
    private Integer role;
} 