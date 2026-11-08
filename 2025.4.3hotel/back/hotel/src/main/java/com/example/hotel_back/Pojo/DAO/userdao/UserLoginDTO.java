package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 用户名/手机号
    private String phone;
    
    // 密码
    private String password;
}