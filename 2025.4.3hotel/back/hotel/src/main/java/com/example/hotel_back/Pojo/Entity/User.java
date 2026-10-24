package com.example.hotel_back.Pojo.Entity;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 用户ID
    private Long id;
    
    // 用户名（昵称）
    private String username;
    
    // 手机号
    private String phone;
    
    // 密码
    private String password;
    
    // 真实姓名
    private String realName;
    
    // 身份证号
    private String idCard;
    
    // 状态（1正常，0禁用）
    private Integer status;

    private String avatar;
    private String sex;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}