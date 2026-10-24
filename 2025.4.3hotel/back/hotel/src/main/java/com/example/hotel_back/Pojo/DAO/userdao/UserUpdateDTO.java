package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // 性别
    private String sex;
    
    // 手机号
    private String phone;
    
    // 头像
    private String avatar;
    
    // 用户名（昵称）
    private String username;
}