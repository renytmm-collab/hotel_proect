package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class AttendanceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 日期
    private LocalDate attendanceDate;
    
    // 状态
    private Integer status=3;
}