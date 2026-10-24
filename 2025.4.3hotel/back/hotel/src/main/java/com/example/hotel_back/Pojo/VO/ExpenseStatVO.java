package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExpenseStatVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 日期
    private String date;
    
    // 退款支出
    private BigDecimal refundFee;
    
    // 采购支出
    private BigDecimal purchaseFee;
    
    // 工资支出
    private BigDecimal salaryFee;
    
    // 水电费支出
    private BigDecimal utilityFee;
    
    // 其他支出
    private BigDecimal otherFee;
    
    // 总支出
    private BigDecimal totalAmount;
}