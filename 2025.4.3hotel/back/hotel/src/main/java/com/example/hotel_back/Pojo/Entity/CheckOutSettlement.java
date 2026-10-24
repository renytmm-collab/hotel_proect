package com.example.hotel_back.Pojo.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckOutSettlement implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 结算ID
    private Long id;
    
    // 入住记录ID
    private Long checkInId;
    
    // 订单ID
    private Long orderId;
    
    // 房间ID
    private Long roomId;
    
    // 用户ID
    private Long userId;
    
    // 额外费用
    private BigDecimal extraFee;
    
    // 损坏赔偿费
    private BigDecimal damageFee;
    
    // 总费用
    private BigDecimal totalFee;
    
    // 押金退还
    private BigDecimal depositRefund;
    
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