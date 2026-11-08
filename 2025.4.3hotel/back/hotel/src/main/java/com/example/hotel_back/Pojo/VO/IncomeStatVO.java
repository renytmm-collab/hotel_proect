package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class IncomeStatVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 日期
    private String date;
    
    // 房费收入
    private BigDecimal roomFee;
    
    // 押金收入
    private BigDecimal depositFee;
    
    // 其他收入
    private BigDecimal otherFee;
    
    // 总收入
    private BigDecimal totalAmount;
}