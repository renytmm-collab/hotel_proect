package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

@Data
public class UserPageQueryDTO {
    
    private String realName;
    
    private Integer status;
    
    private String phone;
    
    private Integer pageNum=1;
    
    private Integer pageSize=10;
}