package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {


    private String realName;

    private String phone;
    private String sex;
    private String empNo;
    private Long roleId;

}
