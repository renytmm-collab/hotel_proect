package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttendanceStatusUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 考勤记录ID
    private Long id;
    
    // 考勤状态
    private Integer status;
}