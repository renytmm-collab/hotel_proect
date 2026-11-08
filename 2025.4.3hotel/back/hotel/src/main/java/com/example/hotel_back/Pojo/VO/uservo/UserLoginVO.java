package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
@Builder
@Data
public class UserLoginVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // JWT令牌
    private String token;
    
    // 用户ID
    private Long id;
    
    // 用户状态
    private Integer status;
    
    // 用户名
    private String username;
    
    // 头像
    private String avatar;
    
    // 手机号
    private String phone;

    private String sex;
}