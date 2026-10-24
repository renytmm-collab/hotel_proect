package com.example.hotel_back.Pojo.VO.uservo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 用户ID
    private Long id;
    
    // 用户名（昵称）
    private String username;
    
    // 真实姓名
    private String realName;
    
    // 头像
    private String avatar;
    
    // 手机号
    private String phone;
    
    // 状态（1正常，0禁用）
    private Integer status;

    // 性别
    private String sex;

    //身份证
    private String idCard;
    
    // 创建时间
    private LocalDateTime createTime;
}