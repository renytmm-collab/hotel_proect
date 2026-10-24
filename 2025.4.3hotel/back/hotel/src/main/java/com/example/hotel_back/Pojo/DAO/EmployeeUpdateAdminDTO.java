package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeUpdateAdminDTO implements Serializable {
    private Long id;
    private String realName;
    private String phone;
    private Long roleId;
    private Integer status;
    private String password;
}