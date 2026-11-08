package com.example.hotel_back.Pojo.VO.employeevo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EAttendanceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 考勤记录ID
    private Long id;
    
    // 员工ID
    private Long employeeId;
    
    // 员工姓名
    private String employeeName;
    
    // 员工头像
    private String avatar;
    
    // 日期
    private LocalDate date;
    
    // 状态
    private Integer status;
}