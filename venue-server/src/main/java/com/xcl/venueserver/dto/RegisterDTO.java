package com.xcl.venueserver.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 电子邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {ValidationGroups.NotEmptyValidation.class})
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {ValidationGroups.NotEmptyValidation.class})
    private String phone;

    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 是否为管理员
     */
    private Boolean isAdmin;
    
    /**
     * 分组校验接口
     */
    public interface ValidationGroups {
        // 非空校验分组
        interface NotEmptyValidation {}
    }
} 