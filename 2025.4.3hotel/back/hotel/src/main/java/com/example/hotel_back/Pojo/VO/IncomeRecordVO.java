package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IncomeRecordVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 订单ID
    private Long orderId;
    
    // 金额
    private BigDecimal amount;
    
    // 类型（0 订单收入，1 押金收入，2 其他收入）
    private Integer type;
    
    // 支付方式
    private String paymentMethod;
    
    // 备注
    private String remark;
    
    // 支付时间
    private LocalDateTime paymentTime;
    
    // 操作员ID
    private Long operatorId;
    
    // 操作员姓名
    private String operatorName;
    
    // 创建时间
    private LocalDateTime createTime;
}