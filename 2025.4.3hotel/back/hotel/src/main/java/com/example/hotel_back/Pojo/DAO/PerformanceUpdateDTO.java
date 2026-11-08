package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PerformanceUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 绩效记录ID
    private Long id;
    
    // 绩效分数
    private BigDecimal score;
    
    // 评价内容
    private String evaluation;
}