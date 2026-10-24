package com.example.hotel_back.Pojo.DAO.employeedao;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EAttendanceQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 员工ID
    private Long employeeId;
    
    // 员工姓名
    private String employeeName;
    
    // 日期
    private LocalDate date;
    
    // 状态
    private Integer status;
    
    // 开始日期
    private LocalDate startDate;
    
    // 结束日期
    private LocalDate endDate;
    
    // 页码
    private int pageNum = 1;
    
    // 每页显示记录数
    private int pageSize = 10;
}