package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 密码
    private String password;
    
    // 手机号
    private String phone;
    
    // 用户名（昵称）
    private String username;
}