package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeUpdateDTO implements Serializable {
    private String realName;
    private String phone;
    private String avatar;
    private String username;
}