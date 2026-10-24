package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PerformanceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;

    // 员工工号
    private String empNo;
    
    // 员工ID
    private Long employeeId;
    
    // 员工姓名
    private String employeeName;
    
    // 月份
    private String month;
    
    // 绩效分数
    private BigDecimal score;
    
    // 评价内容
    private String evaluation;
    
    // 评价人ID
    private Long evaluatorId;
    
    // 评价人姓名
    private String evaluatorName;
}