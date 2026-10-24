package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExpenseUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 支出记录ID
    private Long id;
    
    // 金额
    private BigDecimal amount;
    
    // 类型（0 退款，1 采购，2 工资，3 维修，4 其他）
    private Integer type;
    
    // 支付方式
    private String paymentMethod;
    
    // 备注
    private String remark;
}