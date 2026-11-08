package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String roleName;
    private String description;
    private Integer status;
}