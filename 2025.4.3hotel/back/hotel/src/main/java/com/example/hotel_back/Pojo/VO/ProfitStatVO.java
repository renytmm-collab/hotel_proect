package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProfitStatVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 日期
    private String date;
    
    // 收入
    private BigDecimal income;
    
    // 支出
    private BigDecimal expense;
    
    // 利润
    private BigDecimal profit;
    
    // 利润率
    private BigDecimal profitRate;
}