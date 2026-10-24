package com.example.hotel_back.Pojo.DAO.userdao;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRealAuthDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 真实姓名
    private String realName;
    
    // 身份证号
    private String idCard;
}