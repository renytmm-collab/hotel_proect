package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Attendance implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 员工ID
    private Long employeeId;
    
    // 日期
    private LocalDate date;
    
    // 签到时间
    private LocalDateTime checkInTime;
    
    // 签退时间
    private LocalDateTime checkOutTime;
    
    // 状态（0正常，1迟到，2早退，3旷工，4请假5已签到未签退）
    private Integer status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}