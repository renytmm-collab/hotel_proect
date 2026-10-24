package com.example.hotel_back.Pojo.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRecordVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 记录ID
    private Long id;
    
    // 金额
    private BigDecimal amount;
    
    // 类型（0 退款，1 采购，2 工资，3 维修，4 其他）
    private Integer type;
    
    // 支付方式
    private String paymentMethod;
    

    
    // 操作员ID
    private Long operatorId;
    
    // 操作员姓名
    private String operatorName;
    
    // 备注
    private String remark;
    
    // 创建时间
    private LocalDateTime createTime;
}