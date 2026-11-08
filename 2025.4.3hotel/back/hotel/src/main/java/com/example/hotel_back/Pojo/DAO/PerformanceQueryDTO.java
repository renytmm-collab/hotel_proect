package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class PerformanceQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 员工ID
    private Long employeeId;

    //员工号
    private String empNo;

    // 员工姓名
    private String employeeName;
    
    // 月份
    private String month;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}