package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 金额
    private BigDecimal amount;
    
    // 类型（0 退款，1 采购，2 工资，3 水电，4 其他）
    private Integer type;
    
    // 支付方式
    private String paymentMethod;
    
    // 操作员工ID
    private Long operatorId;
    
    // 备注
    private String remark;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}