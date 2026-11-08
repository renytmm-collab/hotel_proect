package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPasswordDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 旧密码
    private String oldPassword;
    
    // 新密码
    private String newPassword;
}