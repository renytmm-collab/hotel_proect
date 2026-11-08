package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PerformanceAddDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 员工ID
    private Long employeeId;
    
    // 月份
    private String month;
    
    // 绩效分数
    private BigDecimal score;
    
    // 评价内容
    private String evaluation;
}