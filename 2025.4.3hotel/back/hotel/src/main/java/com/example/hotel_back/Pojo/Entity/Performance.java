package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Performance implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 员工ID
    private Long employeeId;
    
    // 月份
    private String month;
    
    // 绩效分数
    private BigDecimal score;
    
    // 评价内容
    private String evaluation;
    
    // 评价人ID
    private Long evaluatorId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}