package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RolePageQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 角色名称
    private String roleName;
    
    // 状态
    private Integer status;
    
    // 页码
    private int page = 1;
    
    // 每页显示记录数
    private int pageSize = 100;
}