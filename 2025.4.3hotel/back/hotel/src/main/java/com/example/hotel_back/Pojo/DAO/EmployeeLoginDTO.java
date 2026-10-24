package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data

public class EmployeeLoginDTO implements Serializable {


    private String empNo;


    private String password;
}
