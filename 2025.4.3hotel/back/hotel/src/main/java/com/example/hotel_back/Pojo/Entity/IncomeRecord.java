package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IncomeRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 订单ID
    private Long orderId;
    
    // 金额
    private BigDecimal amount;
    
    // 类型（0 房费，1 押金，2 其他）
    private Integer type;
    
    // 支付方式
    private String paymentMethod;
    
    // 支付时间
    private LocalDateTime paymentTime;
    
    // 操作员工ID
    private Long operatorId;
    
    // 备注
    private String remark;
    
    // 创建时间
    private LocalDateTime createTime;
}